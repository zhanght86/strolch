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
package li.strolch.testbase.runtime;

import java.io.File;
import java.text.MessageFormat;

import li.strolch.agent.api.ComponentContainer;
import li.strolch.agent.api.StrolchAgent;
import li.strolch.agent.impl.StrolchRealm;
import li.strolch.runtime.configuration.RuntimeConfiguration;
import li.strolch.runtime.privilege.StrolchPrivilegeHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eitchnet.utils.dbc.DBC;
import ch.eitchnet.utils.helper.FileHelper;

public final class RuntimeMock {

	private static final Logger logger = LoggerFactory.getLogger(RuntimeMock.class);
	private static final String TARGET = "target"; //$NON-NLS-1$

	private ComponentContainer container;
	private StrolchAgent agent;
	private File rootPath;
	private File rootSrc;

	public ComponentContainer getContainer() {
		return this.container;
	}

	public StrolchAgent getAgent() {
		return this.agent;
	}

	public StrolchPrivilegeHandler getPrivilegeHandler() {
		return this.container.getComponent(StrolchPrivilegeHandler.class);
	}

	public StrolchRealm getDefaultRealm() {
		return this.container.getDefaultRealm();
	}

	public StrolchRealm getRealm(String realm) {
		return this.container.getRealm(realm);
	}

	public void mockRuntime(File rootPath, File rootSrc) {

		this.rootPath = rootPath;
		this.rootSrc = rootSrc;

		if (!this.rootPath.getParentFile().getName().equals(TARGET)) {
			String msg = "Mocking path must be in a maven target: {0}"; //$NON-NLS-1$
			msg = MessageFormat.format(msg, this.rootPath.getAbsolutePath());
			throw new RuntimeException(msg);
		}

		File configSrc = new File(this.rootSrc, RuntimeConfiguration.PATH_CONFIG);

		if (!configSrc.isDirectory() || !configSrc.canRead()) {
			String msg = "Could not find config source in: {0}"; //$NON-NLS-1$
			msg = MessageFormat.format(msg, configSrc.getAbsolutePath());
			throw new RuntimeException(msg);
		}

		if (this.rootPath.exists()) {
			logger.info("Deleting all files in " + this.rootPath.getAbsolutePath()); //$NON-NLS-1$
			if (!FileHelper.deleteFile(this.rootPath, true)) {
				String msg = "Failed to delete {0}"; //$NON-NLS-1$
				msg = MessageFormat.format(msg, this.rootPath.getAbsolutePath());
				throw new RuntimeException(msg);
			}
		}

		if (!this.rootPath.mkdirs()) {
			String msg = "Failed to create {0}"; //$NON-NLS-1$
			msg = MessageFormat.format(msg, this.rootPath.getAbsolutePath());
			throw new RuntimeException(msg);
		}

		logger.info(MessageFormat.format("Mocking runtime from {0} to {1}", this.rootSrc.getAbsolutePath(), //$NON-NLS-1$
				this.rootPath.getAbsolutePath()));

		if (!FileHelper.copy(this.rootSrc.listFiles(), this.rootPath, false)) {
			String msg = "Failed to copy source files from {0} to {1}"; //$NON-NLS-1$
			msg = MessageFormat.format(msg, this.rootSrc.getAbsolutePath(), this.rootPath.getAbsolutePath());
			throw new RuntimeException(msg);
		}
	}

	/**
	 * @deprecated use parameterless {@link #startContainer()}
	 */
	@Deprecated
	public void startContainer(File rootPathF) {
		DBC.PRE.assertEquals("Starting a different runtime than was mocked!", this.rootPath, rootPathF); //$NON-NLS-1$
		startContainer();
	}

	public void startContainer() {

		try {
			StrolchAgent agent = new StrolchAgent();
			agent.setup(this.rootPath);
			agent.initialize();
			agent.start();

			this.agent = agent;
			this.container = agent.getContainer();

		} catch (Exception e) {
			logger.error("Failed to start mocked container due to: " + e.getMessage(), e); //$NON-NLS-1$
			destroyRuntime();
			throw e;
		}
	}

	public void destroyRuntime() {

		if (this.agent == null)
			return;

		try {
			this.agent.stop();
		} catch (Exception e) {
			logger.info("Failed to stop container: " + e.getMessage()); //$NON-NLS-1$
		}

		try {
			this.agent.destroy();
		} catch (Exception e) {
			logger.info("Failed to destroy container: " + e.getMessage()); //$NON-NLS-1$
		}
	}
}
