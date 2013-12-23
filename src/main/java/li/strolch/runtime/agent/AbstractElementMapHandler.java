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
package li.strolch.runtime.agent;

import java.text.MessageFormat;
import java.util.Map;

import li.strolch.exception.StrolchException;
import li.strolch.model.query.StrolchQuery;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public abstract class AbstractElementMapHandler extends StrolchComponent implements ElementMapHandler {

	protected Map<String, StrolchRealm> realms;

	/**
	 * @param container
	 * @param componentName
	 */
	public AbstractElementMapHandler(ComponentContainerImpl container, String componentName) {
		super(container, componentName);
	}

	@Override
	public ResourceMap getResourceMap() {
		return getResourceMap(StrolchQuery.DEFAULT_REALM);
	}

	@Override
	public ResourceMap getResourceMap(String realm) {
		StrolchRealm strolchRealm = getRealm(realm);
		return strolchRealm.getResourceMap();
	}

	@Override
	public OrderMap getOrderMap() {
		return getOrderMap(StrolchQuery.DEFAULT_REALM);
	}

	@Override
	public OrderMap getOrderMap(String realm) {
		StrolchRealm strolchRealm = getRealm(realm);
		return strolchRealm.getOrderMap();
	}

	private StrolchRealm getRealm(String realm) {
		assertContainerStarted();
		StrolchRealm strolchRealm = this.realms.get(realm);
		if (strolchRealm == null) {
			String msg = "No realm is configured with the name {0}"; //$NON-NLS-1$
			msg = MessageFormat.format(msg, realm);
			throw new StrolchException(msg);
		}
		return strolchRealm;
	}
}