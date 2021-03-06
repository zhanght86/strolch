package li.strolch.execution.command;

import java.text.MessageFormat;

import li.strolch.agent.api.ComponentContainer;
import li.strolch.exception.StrolchException;
import li.strolch.model.State;
import li.strolch.model.activity.Action;
import li.strolch.model.activity.Activity;
import li.strolch.persistence.api.StrolchTransaction;
import li.strolch.utils.dbc.DBC;

public class ExecuteStoppedActionCommand extends ExecutionCommand {

	private Action action;

	public ExecuteStoppedActionCommand(ComponentContainer container, StrolchTransaction tx) {
		super(container, tx);
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public void validate() {
		DBC.PRE.assertNotNull("action can not be null", this.action);

		if (this.action.getState() != State.STOPPED) {
			String msg = "Action {0} is not in state " + State.STOPPED + " and can thus not be put into execution!";
			msg = MessageFormat.format(msg, this.action.getState(), State.ERROR, this.action.getLocator());
			throw new StrolchException(msg);
		}
	}

	@Override
	public void doCommand() {
		Activity rootElement = this.action.getRootElement();
		tx().lock(rootElement);

		State currentState = rootElement.getState();
		rootElement.accept(this);

		updateOrderState(rootElement, currentState, rootElement.getState());
	}

	@Override
	public void undo() {
		// can't undo execution
	}
}
