package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.PlannerMessage;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a planner protocol that does nothing. 
 * @note This class is purely used to compare results with other protocols.
 */
public class PlannerProtocolDoNothing extends PlannerTwoComponentProtocol {

	@Override
	protected void sendFirstMessage(List<CommunicationComponent> components, int startIndex, int... receiverIndices) {
		// Do nothing		
	}

	@Override
	public void receiveAndHandleMessage(PlannerMessage message, CommunicationComponent receiver) {
		// Messages are never handled
	}
}
