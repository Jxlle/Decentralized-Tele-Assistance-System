package tas.mape.communication.protocol;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.planner.Planner;

/**
 * Class representing a planner protocol that does nothing with the communication features.
 * The protocol just sends a confirmation to the other entity and the other entity ends the protocol as if
 * both entities never communicated. This protocol uses exactly 2 messages during execution.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 *
 * @note This class is only used to compare results with other protocols.
 */
public class PlannerProtocolDoNothing extends PlannerTwoComponentProtocol {

	@Override
	public void receiveAndHandleMessage(PlannerMessage message, Planner receiver) {
	
		String messageType = message.getType();
		
		switch(messageType) {
		
			// First offer has been received
			case "FIRST_OFFER":
				System.err.println("FIRST_OFFER , receiver: " + receiver.getEndpoint() + " sender: " + message.getSenderEndpoint());
				PlannerMessage response = new PlannerMessage(messageID, message.getSenderEndpoint(), receiver.getEndpoint(), "ACCEPTED_OFFER", null);
				receiver.setCurrentServiceCombination(receiver.getAvailableServiceCombinations().get(0));
				receiver.finishedProtocol(messageID + 1);
				
				// Increase message ID
				messageID++;
				receiver.sendMessage(response);	
				break;
				
			// Offer has been accepted, stop protocol
			case "ACCEPTED_OFFER":
				System.err.println("ACCEPTED , receiver: " + receiver.getEndpoint() + " sender: " + message.getSenderEndpoint());
				receiver.setCurrentServiceCombination(receiver.getAvailableServiceCombinations().get(0));
				receiver.finishedProtocol(messageID);
				resetProtocol();
				break;
				
			// Exception state
			default:
				throw new IllegalStateException("The received message has a type that cannot be processed! Type: " + messageType);
		}
	}
}
