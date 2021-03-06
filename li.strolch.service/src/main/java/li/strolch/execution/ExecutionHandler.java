package li.strolch.execution;

import li.strolch.agent.api.ComponentContainer;
import li.strolch.agent.api.StrolchComponent;
import li.strolch.execution.policy.DurationExecution;
import li.strolch.execution.policy.ExecutionPolicy;
import li.strolch.model.Locator;
import li.strolch.model.State;
import li.strolch.model.activity.Action;
import li.strolch.model.activity.Activity;
import li.strolch.model.activity.TimeOrdering;

/**
 * <p>
 * The ExecutionHandler enables the automated execution of {@link Activity} and {@link Action} elements.
 * </p>
 * 
 * <p>
 * To start the execution of an {@link Activity} add it to the {@link ExecutionHandler} by calling
 * {@link #addForExecution(String, Locator)}. Actual execution is asynchronously performed and the
 * {@link ExecutionPolicy} of the resources of the {@link Action Actions} will perform the actual execution.
 * </p>
 * 
 * <p>
 * Execution of Actions is done either in series or in parallel, depending on the {@link TimeOrdering} on the relevant
 * {@link Activity}
 * </p>
 * 
 * @author Robert von Burg <eitch@eitchnet.ch>
 */
public abstract class ExecutionHandler extends StrolchComponent {

	public ExecutionHandler(ComponentContainer container, String componentName) {
		super(container, componentName);
	}

	/**
	 * Registers the given {@link Locator} of an {@link Activity} for execution, and submits it for execution
	 * immediately in an asynchronous manner
	 * 
	 * @param realm
	 *            the realm where the {@link Activity} resides
	 * @param activityLoc
	 *            the {@link Locator} of the {@link Activity}
	 */
	public abstract void addForExecution(String realm, Locator activityLoc);

	/**
	 * Removes the given {@link Locator} for an {@link Activity} from execution, so it is not executed further
	 * 
	 * @param realm
	 *            the realm where the {@link Activity} resides
	 * @param activityLoc
	 *            the {@link Locator} of the {@link Activity}
	 */
	public abstract void removeFromExecution(String realm, Locator activityLoc);

	/**
	 * Triggers a to execution for all registered activities in the given realm
	 * 
	 * @param realm
	 *            the realm to trigger execution for
	 */
	public abstract void triggerExecution(String realm);

	/**
	 * <p>
	 * Returns the {@link DelayedExecutionTimer}
	 * </p>
	 * 
	 * <p>
	 * The {@link DelayedExecutionTimer} allows to delay the {@link #toExecuted(String, Locator)} call by a given time.
	 * See the {@link DurationExecution} policy
	 * </p>
	 * 
	 * @return the {@link DelayedExecutionTimer}
	 */
	public abstract DelayedExecutionTimer getDelayedExecutionTimer();

	/**
	 * Starts the execution of the given {@link Activity} with the given {@link Locator}
	 * 
	 * @param realm
	 *            the realm where the {@link Activity} resides
	 * @param activityLoc
	 *            the {@link Locator} of the {@link Activity}
	 */
	public abstract void toExecution(String realm, Locator activityLoc);

	/**
	 * Completes the execution of the given {@link Action} with the given {@link Locator}
	 * 
	 * @param realm
	 *            the realm where the {@link Action} resides
	 * @param actionLoc
	 *            the {@link Locator} of the {@link Action}
	 */
	public abstract void toExecuted(String realm, Locator actionLoc);

	/**
	 * Sets the state of the {@link Action} with the given {@link Locator} to {@link State#STOPPED}
	 * 
	 * @param realm
	 *            the realm where the {@link Action} resides
	 * @param actionLoc
	 *            the {@link Locator} of the {@link Action}
	 */
	public abstract void toStopped(String realm, Locator actionLoc);

	/**
	 * Sets the state of the {@link Action} with the given {@link Locator} to {@link State#WARNING}
	 * 
	 * @param realm
	 *            the realm where the {@link Action} resides
	 * @param actionLoc
	 *            the {@link Locator} of the {@link Action}
	 */
	public abstract void toWarning(String realm, Locator actionLoc);

	/**
	 * Sets the state of the {@link Action} with the given {@link Locator} to {@link State#ERROR}
	 * 
	 * @param realm
	 *            the realm where the {@link Action} resides
	 * @param actionLoc
	 *            the {@link Locator} of the {@link Action}
	 */
	public abstract void toError(String realm, Locator actionLoc);

}
