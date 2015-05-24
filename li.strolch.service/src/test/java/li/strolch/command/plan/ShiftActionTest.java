/*
 * Copyright 2015 Martin Smock <martin.smock@bluewin.ch>
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
package li.strolch.command.plan;

import static li.strolch.model.ModelGenerator.STATE_INTEGER_ID;
import static li.strolch.model.ModelGenerator.STATE_INTEGER_NAME;
import static li.strolch.model.ModelGenerator.STATE_INTEGER_TIME_0;
import static li.strolch.model.ModelGenerator.STATE_TIME_0;
import static li.strolch.model.ModelGenerator.STATE_TIME_10;
import static li.strolch.model.ModelGenerator.STATE_TIME_20;
import static li.strolch.model.ModelGenerator.STATE_TIME_30;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.SortedSet;

import li.strolch.model.Locator;
import li.strolch.model.ModelGenerator;
import li.strolch.model.ParameterBag;
import li.strolch.model.Resource;
import li.strolch.model.State;
import li.strolch.model.Tags;
import li.strolch.model.activity.Action;
import li.strolch.model.parameter.IntegerParameter;
import li.strolch.model.parameter.Parameter;
import li.strolch.model.timedstate.IntegerTimedState;
import li.strolch.model.timedstate.StrolchTimedState;
import li.strolch.model.timevalue.ITimeValue;
import li.strolch.model.timevalue.ITimeVariable;
import li.strolch.model.timevalue.IValue;
import li.strolch.model.timevalue.IValueChange;
import li.strolch.model.timevalue.impl.IntegerValue;
import li.strolch.model.timevalue.impl.ValueChange;
import li.strolch.persistence.api.StrolchTransaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Smock <martin.smock@bluewin.ch>
 */
public class ShiftActionTest {

	Resource resource;
	Action action;
	IntegerTimedState timedState;
	StrolchTransaction tx;

	@Before
	public void init() {

		// add a resource with integer state variable
		resource = ModelGenerator.createResource("@1", "Test With States", "Stated");
		timedState = new IntegerTimedState(STATE_INTEGER_ID, STATE_INTEGER_NAME);
		timedState.applyChange(new ValueChange<>(STATE_TIME_0, new IntegerValue(STATE_INTEGER_TIME_0)));
		resource.addTimedState(timedState);

		action = new Action("action", "Action", "Use");

		Assert.assertEquals(State.CREATED, action.getState());

		final IntegerParameter iP = new IntegerParameter("quantity", "Occupation", 1);
		action.addParameterBag(new ParameterBag("objective", "Objective", "Don't know"));
		action.addParameter("objective", iP);

		createChanges(action);

		action.setResourceId(resource.getId());
		action.setResourceType(resource.getType());

		tx = mock(StrolchTransaction.class);

		final Locator locator = Locator.newBuilder(Tags.RESOURCE, "Stated", "@1").build();
		when(tx.findElement(eq(locator))).thenReturn(resource);

		final PlanActionCommand cmd = new PlanActionCommand(null, tx);
		cmd.setAction(action);
		cmd.doCommand();

	}

	@Test
	public void test() {

		final ShiftActionCommand cmd = new ShiftActionCommand(null, tx);
		cmd.setAction(action);
		cmd.setShift(10L);
		cmd.doCommand();

		// check the state
		Assert.assertEquals(State.PLANNED, action.getState());

		// check the resource Id
		Assert.assertEquals(resource.getId(), action.getResourceId());

		// check if we get the expected result
		final StrolchTimedState<IValue<Integer>> timedState = resource.getTimedState(STATE_INTEGER_ID);
		final ITimeVariable<IValue<Integer>> timeEvolution = timedState.getTimeEvolution();
		final SortedSet<ITimeValue<IValue<Integer>>> values = timeEvolution.getValues();

		Assert.assertEquals(3, values.size());

		ITimeValue<IValue<Integer>> valueAt = timeEvolution.getValueAt(STATE_TIME_0);
		Assert.assertEquals(true, valueAt.getValue().equals(new IntegerValue(0)));

		valueAt = timeEvolution.getValueAt(STATE_TIME_10);
		Assert.assertEquals(true, valueAt.getValue().equals(new IntegerValue(0)));

		valueAt = timeEvolution.getValueAt(STATE_TIME_20);
		Assert.assertEquals(true, valueAt.getValue().equals(new IntegerValue(1)));

		valueAt = timeEvolution.getValueAt(STATE_TIME_30);
		Assert.assertEquals(true, valueAt.getValue().equals(new IntegerValue(0)));

		// check the undo functionality
		cmd.undo();

		valueAt = timeEvolution.getValueAt(STATE_TIME_0);
		Assert.assertEquals(true, valueAt.getValue().equals(new IntegerValue(0)));

		valueAt = timeEvolution.getValueAt(STATE_TIME_10);
		Assert.assertEquals(true, valueAt.getValue().equals(new IntegerValue(1)));

		valueAt = timeEvolution.getValueAt(STATE_TIME_20);
		Assert.assertEquals(true, valueAt.getValue().equals(new IntegerValue(0)));

	}

	/**
	 * <p>
	 * add changes to action start and end time with a value defined in the
	 * action objective and set the stateId of the state variable to apply the
	 * change to
	 * </p>
	 * 
	 * @param action
	 */
	protected static void createChanges(final Action action) {

		final Parameter<Integer> parameter = action.getParameter("objective", "quantity");
		final Integer quantity = parameter.getValue();

		final IValueChange<IntegerValue> startChange = new ValueChange<>(STATE_TIME_10, new IntegerValue(quantity));
		startChange.setStateId(STATE_INTEGER_ID);
		action.addChange(startChange);

		final IValueChange<IntegerValue> endChange = new ValueChange<>(STATE_TIME_20, new IntegerValue(-quantity));
		endChange.setStateId(STATE_INTEGER_ID);
		action.addChange(endChange);
	}

}
