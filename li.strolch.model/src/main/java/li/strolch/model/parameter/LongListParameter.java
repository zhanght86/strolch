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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import li.strolch.model.StrolchValueType;
import li.strolch.model.visitor.ParameterVisitor;
import li.strolch.utils.dbc.DBC;
import li.strolch.utils.helper.StringHelper;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public class LongListParameter extends AbstractParameter<List<Long>> implements ListParameter<Long> {

	private static final long serialVersionUID = 1L;

	protected List<Long> value;

	/**
	 * Empty constructor
	 */
	public LongListParameter() {
		//
	}

	/**
	 * Default constructor
	 *
	 * @param id
	 * @param name
	 * @param value
	 */
	public LongListParameter(String id, String name, List<Long> value) {
		super(id, name);

		setValue(value);
	}

	@Override
	public String getValueAsString() {
		if (this.value.isEmpty()) {
			return StringHelper.EMPTY;
		}

		StringBuilder sb = new StringBuilder();
		Iterator<Long> iter = this.value.iterator();
		while (iter.hasNext()) {

			sb.append(iter.next());

			if (iter.hasNext()) {
				sb.append(VALUE_SEPARATOR2);
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getValue() {
		return new ArrayList<>(this.value);
	}

	@Override
	public void setValue(List<Long> value) {
		validateValue(value);
		if (this.value == null) {
			this.value = new ArrayList<>(value.size());
		}
		this.value.clear();
		this.value.addAll(value);
	}

	@Override
	public void setValueFromString(String valueAsString) {
		setValue(parseFromString(valueAsString));
	}

	@Override
	public void addValue(Long value) {
		this.value.add(value);
	}

	@Override
	public boolean removeValue(Long value) {
		return this.value.remove(value);
	}

	@Override
	public void clear() {
		this.value.clear();
	}

	@Override
	public boolean isEmpty() {
		return this.value.isEmpty();
	}

	@Override
	public int size() {
		return this.value.size();
	}

	@Override
	public boolean contains(Long value) {
		return this.value.contains(value);
	}

	@Override
	public String getType() {
		return StrolchValueType.LONG_LIST.getType();
	}

	@Override
	public LongListParameter getClone() {
		LongListParameter clone = new LongListParameter();

		super.fillClone(clone);

		clone.setValue(this.value);

		return clone;
	}

	@Override
	public <U> U accept(ParameterVisitor visitor) {
		return visitor.visitLongListParam(this);
	}

	public static List<Long> parseFromString(String value) {
		if (value.isEmpty()) {
			return Collections.emptyList();
		}

		String[] valueArr;
		if (value.contains(VALUE_SEPARATOR1))
			valueArr = value.split(VALUE_SEPARATOR1);
		else
			valueArr = value.split(VALUE_SEPARATOR2);

		List<Long> values = new ArrayList<>();
		for (String val : valueArr) {
			values.add(Long.valueOf(val.trim()));
		}
		return values;
	}

	@Override
	public int compareTo(Parameter<?> o) {
		DBC.PRE.assertEquals("Not same Parameter types!", this.getType(), o.getType());
		return Integer.valueOf(this.getValue().size()).compareTo(((LongListParameter) o).getValue().size());
	}
}
