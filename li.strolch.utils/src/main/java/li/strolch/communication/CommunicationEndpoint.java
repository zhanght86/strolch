/*
 * Copyright 2014 Robert von Burg <eitch@eitchnet.ch>
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
package li.strolch.communication;

/**
 * @author Robert von Burg &lt;eitch@eitchnet.ch&gt;
 */
public interface CommunicationEndpoint {

	public void configure(CommunicationConnection connection, IoMessageVisitor converter);

	public void start();

	public void stop();

	public void reset();

	public String getLocalUri();

	public String getRemoteUri();

	public void send(IoMessage message) throws Exception;

	public void simulate(IoMessage message) throws Exception;
}
