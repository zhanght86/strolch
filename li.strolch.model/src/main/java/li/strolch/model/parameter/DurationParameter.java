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
package li.strolch.model.parameter;

import li.strolch.model.StrolchValueType;
import li.strolch.model.visitor.ParameterVisitor;
import li.strolch.utils.dbc.DBC;
import li.strolch.utils.iso8601.ISO8601FormatFactory;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class DurationParameter extends AbstractParameter<Long> {

	private static final long serialVersionUID = 0L;

	private Long value;

	/**
	 * Empty constructor
	 */
	public DurationParameter() {
		//
	}

	/**
	 * Default Constructor
	 *
	 * @param id
	 * @param name
	 * @param value
	 */
	public DurationParameter(String id, String name, Long value) {
		super(id, name);
		setValue(value);
	}

	@Override
	public String getValueAsString() {
		return ISO8601FormatFactory.getInstance().formatDuration(this.value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Long getValue() {
		return this.value;
	}

	@Override
	public void setValue(Long value) {
		validateValue(value);
		this.value = value;
	}

	/**
	 * Sets the value to 0
	 * 
	 * @see Parameter#clear()
	 */
	@Override
	public void clear() {
		this.value = 0L;
	}

	@Override
	public boolean isEmpty() {
		return this.value == 0L;
	}

	@Override
	public void setValueFromString(String valueAsString) {
		setValue(parseFromString(valueAsString));
	}

	@Override
	public String getType() {
		return StrolchValueType.DURATION.getType();
	}

	@Override
	public DurationParameter getClone() {
		DurationParameter clone = new DurationParameter();

		super.fillClone(clone);

		clone.setValue(this.value);

		return clone;
	}

	@Override
	public <U> U accept(ParameterVisitor visitor) {
		return visitor.visitDurationParam(this);
	}

	public static Long parseFromString(String valueS) {
		return ISO8601FormatFactory.getInstance().getDurationFormat().parse(valueS);
	}

	@Override
	public int compareTo(Parameter<?> o) {
		DBC.PRE.assertEquals("Not same Parameter types!", this.getType(), o.getType());
		return this.getValue().compareTo(((DurationParameter) o).getValue());
	}
}
