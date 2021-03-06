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
package li.strolch.agent.impl;

import static li.strolch.agent.impl.DefaultRealmHandler.PROP_ENABLED_DELAYED_OBSERVER_UPDATES;
import static li.strolch.agent.impl.DefaultRealmHandler.PROP_ENABLE_AUDIT_TRAIL;
import static li.strolch.agent.impl.DefaultRealmHandler.PROP_ENABLE_AUDIT_TRAIL_FOR_READ;
import static li.strolch.agent.impl.DefaultRealmHandler.PROP_ENABLE_OBSERVER_UPDATES;
import static li.strolch.agent.impl.DefaultRealmHandler.PROP_ENABLE_VERSIONING;
import static li.strolch.runtime.StrolchConstants.makeRealmKey;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.strolch.agent.api.ActivityMap;
import li.strolch.agent.api.AuditTrail;
import li.strolch.agent.api.ComponentContainer;
import li.strolch.agent.api.LockHandler;
import li.strolch.agent.api.ObserverHandler;
import li.strolch.agent.api.OrderMap;
import li.strolch.agent.api.ResourceMap;
import li.strolch.agent.api.StrolchRealm;
import li.strolch.model.Locator;
import li.strolch.privilege.model.PrivilegeContext;
import li.strolch.runtime.configuration.ComponentConfiguration;
import li.strolch.utils.dbc.DBC;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public abstract class InternalStrolchRealm implements StrolchRealm {

	public static final String PROP_TRY_LOCK_TIME_UNIT = "tryLockTimeUnit"; //$NON-NLS-1$
	public static final String PROP_TRY_LOCK_TIME = "tryLockTime"; //$NON-NLS-1$
	protected static final Logger logger = LoggerFactory.getLogger(StrolchRealm.class);
	private String realm;
	private LockHandler lockHandler;
	private boolean auditTrailEnabled;
	private boolean auditTrailEnabledForRead;
	private boolean versioningEnabled;
	private boolean updateObservers;
	private ObserverHandler observerHandler;

	public InternalStrolchRealm(String realm) {
		DBC.PRE.assertNotEmpty("RealmName may not be empty!", realm); //$NON-NLS-1$
		this.realm = realm;
	}

	@Override
	public String getRealm() {
		return this.realm;
	}

	@Override
	public void lock(Locator locator) {
		DBC.PRE.assertNotNull("Can not lock a null pointer =)", locator); //$NON-NLS-1$
		this.lockHandler.lock(locator);
	}

	@Override
	public void unlock(Locator locator) {
		this.lockHandler.unlock(locator);
	}

	@Override
	public void releaseLock(Locator locator) {
		this.lockHandler.releaseLock(locator);
	}

	public void initialize(ComponentContainer container, ComponentConfiguration configuration) {

		logger.info("Initializing Realm " + getRealm() + "...");

		// audits
		String enableAuditKey = makeRealmKey(getRealm(), PROP_ENABLE_AUDIT_TRAIL);
		this.auditTrailEnabled = configuration.getBoolean(enableAuditKey, Boolean.FALSE);

		// audits for read
		String enableAuditForReadKey = makeRealmKey(getRealm(), PROP_ENABLE_AUDIT_TRAIL_FOR_READ);
		this.auditTrailEnabledForRead = configuration.getBoolean(enableAuditForReadKey, Boolean.FALSE);

		// observer updates
		String updateObserversKey = makeRealmKey(getRealm(), PROP_ENABLE_OBSERVER_UPDATES);
		this.updateObservers = configuration.getBoolean(updateObserversKey, Boolean.FALSE);
		if (this.updateObservers) {
			String delayedObserversKey = makeRealmKey(getRealm(), PROP_ENABLED_DELAYED_OBSERVER_UPDATES);
			if (configuration.getBoolean(delayedObserversKey, Boolean.FALSE)) {
				this.observerHandler = new DefaultObserverHandler();
			} else {
				this.observerHandler = new EventCollectingObserverHandler();
				logger.info("Enabled Delayed Observer Updates.");
			}
		}

		// lock timeout
		String propTryLockTimeUnit = makeRealmKey(this.realm, PROP_TRY_LOCK_TIME_UNIT);
		String propTryLockTime = makeRealmKey(this.realm, PROP_TRY_LOCK_TIME);
		TimeUnit timeUnit = TimeUnit.valueOf(configuration.getString(propTryLockTimeUnit, TimeUnit.SECONDS.name()));
		long time = configuration.getLong(propTryLockTime, 10L);
		this.lockHandler = new DefaultLockHandler(this.realm, timeUnit, time);

		// versioning
		String enableVersioningKey = makeRealmKey(getRealm(), PROP_ENABLE_VERSIONING);
		this.versioningEnabled = configuration.getBoolean(enableVersioningKey, Boolean.FALSE);

		if (this.auditTrailEnabled)
			logger.info("Enabling AuditTrail for realm " + getRealm()); //$NON-NLS-1$
		else
			logger.info("AuditTrail not enabled for realm " + getRealm()); //$NON-NLS-1$
		if (this.auditTrailEnabledForRead)
			logger.info("Enabling AuditTrail for read for realm " + getRealm()); //$NON-NLS-1$
		else
			logger.info("AuditTrail not enabled for read for realm " + getRealm()); //$NON-NLS-1$
		if (this.updateObservers)
			logger.info("Enabling Observer Updates for realm " + getRealm()); //$NON-NLS-1$
		else
			logger.info("Observer Updates not enabled for realm " + getRealm()); //$NON-NLS-1$
		if (this.versioningEnabled)
			logger.info("Enabling Versioning for realm " + getRealm()); //$NON-NLS-1$
		else
			logger.info("Versioning not enabled for realm " + getRealm()); //$NON-NLS-1$

		logger.info(MessageFormat.format("Using a locking try timeout of {0}s", timeUnit.toSeconds(time))); //$NON-NLS-1$
	}

	@Override
	public boolean isAuditTrailEnabled() {
		return this.auditTrailEnabled;
	}

	@Override
	public boolean isAuditTrailEnabledForRead() {
		return this.auditTrailEnabledForRead;
	}

	@Override
	public boolean isUpdateObservers() {
		return this.updateObservers;
	}

	@Override
	public boolean isVersioningEnabled() {
		return this.versioningEnabled;
	}

	@Override
	public ObserverHandler getObserverHandler() throws IllegalArgumentException {
		if (!this.updateObservers)
			throw new IllegalArgumentException("ObserverUpdates are not enabled!"); //$NON-NLS-1$
		return this.observerHandler;
	}

	public void start(PrivilegeContext privilegeContext) {

		if (this.observerHandler != null) {
			this.observerHandler.start();
		}
	}

	public void stop() {

		if (this.observerHandler != null) {
			this.observerHandler.stop();
		}
	}

	public abstract void destroy();

	public abstract ResourceMap getResourceMap();

	public abstract OrderMap getOrderMap();

	public abstract ActivityMap getActivityMap();

	public abstract AuditTrail getAuditTrail();
}
