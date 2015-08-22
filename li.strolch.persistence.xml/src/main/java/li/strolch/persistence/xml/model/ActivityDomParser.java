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
package li.strolch.persistence.xml.model;

import li.strolch.model.activity.Activity;
import li.strolch.model.xml.ActivityFromDomVisitor;
import li.strolch.model.xml.ActivityToDomVisitor;

import org.w3c.dom.Document;

import ch.eitchnet.xmlpers.api.DomParser;

public class ActivityDomParser implements DomParser<Activity> {

	private Activity activity;

	@Override
	public Activity getObject() {
		return this.activity;
	}

	@Override
	public void setObject(Activity activity) {
		this.activity = activity;
	}

	@Override
	public Document toDom() {
		ActivityToDomVisitor activityDomVisitor = new ActivityToDomVisitor();
		activityDomVisitor.visit(this.activity);
		return activityDomVisitor.getDocument();
	}

	@Override
	public void fromDom(Document document) {
		Activity activity = new ActivityFromDomVisitor().visit(document);
		this.activity = activity;
	}
}