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
package ch.eitchnet.xmlpers.test.impl;

import ch.eitchnet.xmlpers.api.DomParser;
import ch.eitchnet.xmlpers.api.ParserFactory;
import ch.eitchnet.xmlpers.api.SaxParser;
import ch.eitchnet.xmlpers.test.model.MyModel;

public class MyModelParserFactory implements ParserFactory<MyModel> {

	@Override
	public DomParser<MyModel> getDomParser() {
		return new MyModelDomParser();
	}

	@Override
	public SaxParser<MyModel> getSaxParser() {
		return new MyModelSaxParser();
	}
}