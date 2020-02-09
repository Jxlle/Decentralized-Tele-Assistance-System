package tas.mape.communication.protocol;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.planner.Planner;

/**
 * Class representing a planner protocol that does nothing. 
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 *
 * @note This class is only used to compare results with other protocols.
 */
public class PlannerProtocolDoNothing extends PlannerTwoComponentProtocol {

	@Override
	protected void InitializeAndSendFirstMessage(int startIndex, int... receiverIndices) {
		// Do nothing		
	}

	@Override
	public void receiveAndHandleMessage(PlannerMessage message, Planner receiver) {
		// Messages are never handled
	}
}
