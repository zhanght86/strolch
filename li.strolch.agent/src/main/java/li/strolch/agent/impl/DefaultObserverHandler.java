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

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.strolch.agent.api.Observer;
import li.strolch.agent.api.ObserverEvent;
import li.strolch.agent.api.ObserverHandler;
import li.strolch.model.StrolchRootElement;
import li.strolch.runtime.ThreadPoolFactory;
import li.strolch.utils.collections.MapOfLists;

/**
 * A simple {@link ObserverHandler} which keeps a reference to all registered {@link Observer Observers} and notifies
 * them when one of the notify methods are called
 * 
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class DefaultObserverHandler implements ObserverHandler {

	private static final Logger logger = LoggerFactory.getLogger(DefaultObserverHandler.class);

	private MapOfLists<String, Observer> observerMap;

	private ExecutorService executorService;

	public DefaultObserverHandler() {
		this.observerMap = new MapOfLists<>();
	}

	@Override
	public void start() {
		this.executorService = Executors.newSingleThreadExecutor(new ThreadPoolFactory("ObserverHandler"));
	}

	@Override
	public void stop() {
		if (this.executorService != null) {
			this.executorService.shutdown();
			while (!this.executorService.isTerminated()) {
				logger.info("Waiting for last update to complete...");
				try {
					Thread.sleep(50L);
				} catch (InterruptedException e) {
					logger.error("Interrupted!");
				}
			}

			this.executorService = null;
		}
	}

	@Override
	public void notify(ObserverEvent event) {

		if (event.added.isEmpty() && event.updated.isEmpty() && event.removed.isEmpty())
			return;

		this.executorService.execute(() -> {
			doUpdates(event);
		});
	}

	protected void doUpdates(ObserverEvent event) {
		synchronized (this.observerMap) {
			for (String key : event.added.keySet()) {
				add(key, event.added.getList(key));
			}
			for (String key : event.updated.keySet()) {
				update(key, event.updated.getList(key));
			}
			for (String key : event.removed.keySet()) {
				remove(key, event.removed.getList(key));
			}
		}
	}

	private void add(String key, List<StrolchRootElement> elements) {
		if (elements == null || elements.isEmpty())
			return;

		List<Observer> observerList = this.observerMap.getList(key);
		if (observerList != null && !observerList.isEmpty()) {
			for (Observer observer : observerList) {
				try {
					observer.add(key, elements);
				} catch (Exception e) {
					String msg = "Failed to update observer {0} with {1} due to {2}"; //$NON-NLS-1$
					msg = MessageFormat.format(msg, key, observer, e.getMessage());
					logger.error(msg, e);
				}
			}
		}
	}

	private void update(String key, List<StrolchRootElement> elements) {
		if (elements == null || elements.isEmpty())
			return;

		List<Observer> observerList = this.observerMap.getList(key);
		if (observerList != null && !observerList.isEmpty()) {
			for (Observer observer : observerList) {
				try {
					observer.update(key, elements);
				} catch (Exception e) {
					String msg = "Failed to update observer {0} with {1} due to {2}"; //$NON-NLS-1$
					msg = MessageFormat.format(msg, key, observer, e.getMessage());
					logger.error(msg, e);
				}
			}
		}
	}

	private void remove(String key, List<StrolchRootElement> elements) {
		if (elements == null || elements.isEmpty())
			return;

		List<Observer> observerList = this.observerMap.getList(key);
		if (observerList != null && !observerList.isEmpty()) {
			for (Observer observer : observerList) {
				try {
					observer.remove(key, elements);
				} catch (Exception e) {
					String msg = "Failed to update observer {0} with {1} due to {2}"; //$NON-NLS-1$
					msg = MessageFormat.format(msg, key, observer, e.getMessage());
					logger.error(msg, e);
				}
			}
		}
	}

	@Override
	public void registerObserver(String key, Observer observer) {
		synchronized (this.observerMap) {
			this.observerMap.addElement(key, observer);
			String msg = MessageFormat.format("Registered observer {0} with {1}", key, observer); //$NON-NLS-1$
			logger.info(msg);
		}
	}

	@Override
	public void unregisterObserver(String key, Observer observer) {
		synchronized (this.observerMap) {
			if (this.observerMap.removeElement(key, observer)) {
				String msg = MessageFormat.format("Unregistered observer {0} with {1}", key, observer); //$NON-NLS-1$
				logger.info(msg);
			}
		}
	}
}
