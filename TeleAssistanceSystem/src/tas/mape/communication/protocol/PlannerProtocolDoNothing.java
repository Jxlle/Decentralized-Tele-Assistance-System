package tas.mape.communication.protocol;

import java.util.List;

import service.auxiliary.AbstractMessage;
import tas.mape.communication.CommunicationComponent;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a planner protocol that does nothing. 
 * @note This class is purely used to compare results with other protocols.
 */
public class PlannerProtocolDoNothing extends AbstractTwoComponentProtocol {

	@Override
	protected void sendFirstMessage(List<CommunicationComponent> components, int startIndex, int... receiverIndices) {
		// Do nothing		
	}

	@Override
	protected void receiveAndHandleMessage(AbstractMessage message, CommunicationComponent receiver) {
		// Messages are never handled
	}
}
