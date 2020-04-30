package tas.communication.protocol;

import tas.communication.message.PlannerMessage;
import tas.communication.message.ProtocolMessageInformation;
import tas.mape.planner.Planner;

/**
 * Class representing a planner protocol that does nothing with the communication features.
 * The protocol just sends a confirmation to the other entity and the other entity ends the protocol as if
 * both entities never communicated. This protocol uses exactly 2 messages during execution.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class TwoPlannerProtocolDoNothing extends AbstractTwoPlannerProtocol {

	@Override
	public void receiveAndHandleMessage(PlannerMessage message, Planner receiver) {
	
		observer.protocolMessageSent(new ProtocolMessageInformation(message.getSenderEndpoint(), message.getReceiverEndpoint(), message.getType()));
		String messageType = message.getType();
		messageID++;
		
		switch(messageType) {
		
			// First offer has been received
			case "FIRST_OFFER":
				PlannerMessage response = new PlannerMessage(messageID, message.getSenderEndpoint(), receiver.getEndpoint(), "AGREED_OFFER", null);
				receiver.setCurrentServiceCombination(receiver.getBestRandomServiceCombination());
				
				// Increase message ID
				receiver.sendMessage(response);	
				receiver.finishedProtocol(messageID);
				break;
				
			// Offer has been accepted, stop protocol
			case "AGREED_OFFER":
				receiver.setCurrentServiceCombination(receiver.getBestRandomServiceCombination());
				receiver.finishedProtocol(messageID);
				resetProtocol();
				break;
				
			// Exception state
			default:
				throw new IllegalStateException("The received message has a type that cannot be processed! Type: " + messageType);
		}
	}
}
