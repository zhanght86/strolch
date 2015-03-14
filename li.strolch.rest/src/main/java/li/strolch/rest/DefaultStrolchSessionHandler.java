/*
 * Copyright 2013 Robert von Burg <eitch@eitchnet.ch>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package li.strolch.rest;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import li.strolch.agent.api.ComponentContainer;
import li.strolch.agent.api.StrolchComponent;
import li.strolch.exception.StrolchException;
import li.strolch.rest.model.UserSession;
import li.strolch.runtime.configuration.ComponentConfiguration;
import li.strolch.runtime.privilege.PrivilegeHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eitchnet.privilege.base.AccessDeniedException;
import ch.eitchnet.privilege.model.Certificate;
import ch.eitchnet.privilege.model.PrivilegeContext;
import ch.eitchnet.privilege.model.SimpleRestrictable;
import ch.eitchnet.utils.dbc.DBC;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class DefaultStrolchSessionHandler extends StrolchComponent implements StrolchSessionHandler {

	private static final Logger logger = LoggerFactory.getLogger(DefaultStrolchSessionHandler.class);
	private static final String PARAM_SESSION_TTL_MINUTES = "session.ttl.minutes"; //$NON-NLS-1$
	private PrivilegeHandler privilegeHandler;
	private Map<String, Certificate> certificateMap;
	private long sessionTtl;
	private Timer sessionTimeoutTimer;

	/**
	 * @param container
	 * @param componentName
	 */
	public DefaultStrolchSessionHandler(ComponentContainer container, String componentName) {
		super(container, componentName);
	}

	@Override
	public void initialize(ComponentConfiguration configuration) {
		this.sessionTtl = TimeUnit.MINUTES.toMillis(configuration.getInt(PARAM_SESSION_TTL_MINUTES, 30));
		super.initialize(configuration);
	}

	@Override
	public void start() {
		this.privilegeHandler = getContainer().getComponent(PrivilegeHandler.class);
		this.certificateMap = new HashMap<>();

		this.sessionTimeoutTimer = new Timer("SessionTimeoutTimer", true); //$NON-NLS-1$
		long checkInterval = TimeUnit.MINUTES.toMillis(1);
		this.sessionTimeoutTimer.schedule(new SessionTimeoutTask(), checkInterval, checkInterval);

		super.start();
	}

	@Override
	public void stop() {
		if (this.certificateMap != null) {
			synchronized (this.certificateMap) {
				for (Certificate certificate : this.certificateMap.values()) {
					this.privilegeHandler.invalidateSession(certificate);
				}
				this.certificateMap.clear();
			}
		}

		if (this.sessionTimeoutTimer != null) {
			this.sessionTimeoutTimer.cancel();
		}

		this.sessionTimeoutTimer = null;
		this.privilegeHandler = null;
		super.stop();
	}

	@Override
	public void destroy() {
		this.certificateMap = null;
		super.destroy();
	}

	@Override
	public Certificate authenticate(String username, byte[] password) {
		DBC.PRE.assertNotEmpty("Origin must be set!", username); //$NON-NLS-1$
		DBC.PRE.assertNotEmpty("Username must be set!", username); //$NON-NLS-1$
		DBC.PRE.assertNotNull("Passwort must be set", password); //$NON-NLS-1$

		synchronized (this.certificateMap) {
			Certificate certificate = this.privilegeHandler.authenticate(username, password);
			certificate.setLastAccess(new Date());
			this.certificateMap.put(certificate.getAuthToken(), certificate);

			logger.info(MessageFormat.format("{0} sessions currently active.", this.certificateMap.size())); //$NON-NLS-1$
			return certificate;
		}
	}

	@Override
	public Certificate validate(String authToken) {
		DBC.PRE.assertNotEmpty("SessionId must be set!", authToken); //$NON-NLS-1$

		Certificate certificate;
		synchronized (this.certificateMap) {
			certificate = this.certificateMap.get(authToken);
		}

		if (certificate == null)
			throw new StrolchException(MessageFormat.format("No certificate exists for sessionId {0}", authToken)); //$NON-NLS-1$

		return validate(certificate);
	}

	@Override
	public Certificate validate(Certificate certificate) {
		this.privilegeHandler.isCertificateValid(certificate);
		certificate.setLastAccess(new Date());
		return certificate;
	}

	@Override
	public void invalidate(Certificate certificate) {
		DBC.PRE.assertNotNull("Certificate must bet given!", certificate); //$NON-NLS-1$

		Certificate removedCert;
		synchronized (this.certificateMap) {
			removedCert = this.certificateMap.remove(certificate.getAuthToken());
		}
		if (removedCert == null)
			logger.error(MessageFormat.format("No session was registered with token {0}", certificate.getAuthToken())); //$NON-NLS-1$

		this.privilegeHandler.invalidateSession(certificate);
	}

	/**
	 * @return the certificateMap
	 */
	protected Map<String, Certificate> getCertificateMap() {
		return this.certificateMap;
	}

	/**
	 * Simpler {@link TimerTask} to check for sessions which haven't been active for
	 * {@link DefaultStrolchSessionHandler#PARAM_SESSION_TTL_MINUTES} minutes.
	 * 
	 * @author Robert von Burg <eitch@eitchnet.ch>
	 */
	private class SessionTimeoutTask extends TimerTask {

		@Override
		public void run() {

			Map<String, Certificate> map = getCertificateMap();
			Map<String, Certificate> certificateMap;
			synchronized (map) {
				certificateMap = new HashMap<>(map);
			}

			LocalDateTime timeOutTime = LocalDateTime.now().minus(sessionTtl, ChronoUnit.MILLIS);
			ZoneId systemDefault = ZoneId.systemDefault();

			for (Certificate certificate : certificateMap.values()) {
				Instant lastAccess = certificate.getLastAccess().toInstant();
				if (timeOutTime.isAfter(LocalDateTime.ofInstant(lastAccess, systemDefault))) {
					String msg = "Session {0} for user {1} has expired, invalidating session..."; //$NON-NLS-1$
					logger.info(MessageFormat.format(msg, certificate.getAuthToken(), certificate.getUsername()));
					invalidate(certificate);
				}
			}
		}
	}

	@Override
	public UserSession getSession(Certificate certificate, String sessionId) {
		PrivilegeContext ctx = this.privilegeHandler.getPrivilegeContext(certificate);
		ctx.assertHasPrivilege("GetSession");
		for (Certificate cert : certificateMap.values()) {
			if (cert.getSessionId().equals(sessionId)) {
				ctx.validateAction(new SimpleRestrictable("GetSession", cert));
				return new UserSession(cert);
			}
		}

		return null;
	}

	@Override
	public List<UserSession> getSessions(Certificate certificate) {
		PrivilegeContext ctx = this.privilegeHandler.getPrivilegeContext(certificate);
		ctx.assertHasPrivilege("GetSession");
		List<UserSession> sessions = new ArrayList<>(this.certificateMap.size());
		for (Certificate cert : certificateMap.values()) {
			try {
				ctx.validateAction(new SimpleRestrictable("GetSession", cert));
				sessions.add(new UserSession(cert));
			} catch (AccessDeniedException e) {
				// so no, user may not get this session
			}
		}

		return sessions;
	}

	@Override
	public void invalidateSession(Certificate certificate, String sessionId) {
		PrivilegeContext ctx = this.privilegeHandler.getPrivilegeContext(certificate);
		ctx.assertHasPrivilege("InvalidateSession");
		for (Certificate cert : certificateMap.values()) {
			if (cert.getSessionId().equals(sessionId)) {
				ctx.validateAction(new SimpleRestrictable("InvalidateSession", cert));
				invalidate(cert);
			}
		}
	}

	@Override
	public void setSessionLocale(Certificate certificate, String sessionId, Locale locale) {
		if (!certificate.getSessionId().equals(sessionId)) {
			String msg = "User''s can only change their own session locale: {0} may not change locale of session {1}";
			throw new AccessDeniedException(MessageFormat.format(msg, certificate.getUsername(), sessionId));
		}

		for (Certificate cert : certificateMap.values()) {
			if (cert.getSessionId().equals(sessionId)) {
				cert.setLocale(locale);
			}
		}
	}
}
