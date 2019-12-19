package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.PlannerMessage;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing the structure of a two-component planner protocol.
 */
public abstract class PlannerTwoComponentProtocol extends AbstractTwoComponentProtocol<PlannerMessage> {

	@Override
	protected void sendFirstMessage(List<CommunicationComponent> components, int startIndex, int... receiverIndices) {
		
		// TODO generate first planner message and send it to the receiver 
		//PlannerMessage firstMessage = new PlannerMessage(messageID, );
		//messageID++;
	}
}
