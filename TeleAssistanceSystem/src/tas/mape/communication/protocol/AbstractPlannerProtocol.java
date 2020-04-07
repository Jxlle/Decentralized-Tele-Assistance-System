package tas.mape.communication.protocol;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.planner.Planner;

/**
 * Abstract class representing a planner protocol
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public abstract class AbstractPlannerProtocol extends AbstractProtocol<PlannerMessage, Planner> {

	/**
	 * Create a new planner protocol
	 */
	protected AbstractPlannerProtocol(int neededAmountOfComponents) {
		super(neededAmountOfComponents);
	}
}
