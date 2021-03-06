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
package li.strolch.model.query;

import li.strolch.model.query.ParameterSelection.AnyTypeParameterSelection;
import li.strolch.model.query.ParameterSelection.BooleanParameterSelection;
import li.strolch.model.query.ParameterSelection.DateParameterSelection;
import li.strolch.model.query.ParameterSelection.DateRangeParameterSelection;
import li.strolch.model.query.ParameterSelection.DurationParameterSelection;
import li.strolch.model.query.ParameterSelection.FloatListParameterSelection;
import li.strolch.model.query.ParameterSelection.FloatParameterSelection;
import li.strolch.model.query.ParameterSelection.IntegerListParameterSelection;
import li.strolch.model.query.ParameterSelection.IntegerParameterSelection;
import li.strolch.model.query.ParameterSelection.LongListParameterSelection;
import li.strolch.model.query.ParameterSelection.LongParameterSelection;
import li.strolch.model.query.ParameterSelection.NullParameterSelection;
import li.strolch.model.query.ParameterSelection.StringListParameterSelection;
import li.strolch.model.query.ParameterSelection.StringParameterSelection;

/**
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public interface ParameterSelectionVisitor extends QueryVisitor {

	public void visit(StringParameterSelection selection);

	public void visit(IntegerParameterSelection selection);

	public void visit(BooleanParameterSelection selection);

	public void visit(LongParameterSelection selection);

	public void visit(FloatParameterSelection selection);

	public void visit(DateParameterSelection selection);

	public void visit(DurationParameterSelection selection);

	public void visit(DateRangeParameterSelection selection);

	public void visit(StringListParameterSelection selection);

	public void visit(IntegerListParameterSelection selection);

	public void visit(FloatListParameterSelection selection);

	public void visit(LongListParameterSelection selection);

	public void visit(NullParameterSelection selection);

	public void visit(AnyTypeParameterSelection selection);
}
