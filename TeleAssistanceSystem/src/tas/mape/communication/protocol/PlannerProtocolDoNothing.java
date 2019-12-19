package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.planner.Planner;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a planner protocol that does nothing. 
 * @note This class is only used to compare results with other protocols.
 */
public class PlannerProtocolDoNothing extends PlannerTwoComponentProtocol {

	@Override
	protected void InitializeAndSendFirstMessage(List<Planner> components, int startIndex, int... receiverIndices) {
		// Do nothing		
	}

	@Override
	public void receiveAndHandleMessage(PlannerMessage message, Planner receiver) {
		// Messages are never handled
	}
}
