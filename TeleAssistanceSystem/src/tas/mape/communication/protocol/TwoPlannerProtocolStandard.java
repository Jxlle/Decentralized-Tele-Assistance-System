package tas.mape.communication.protocol;

import java.util.ArrayList;
import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.message.PlannerMessageContent;
import tas.mape.communication.message.ProtocolMessageInformation;
import tas.mape.planner.Planner;
import tas.mape.planner.ServiceCombination;

/**
 * Class representing a standard two-component planner protocol.
 * This protocol works as follows:
 * 
 * 1) A planner gets chosen to send the first message, it sends its message to the other planner with the message type "FIRST_OFFER".
 *    This first message holds data about the public service failure rates from the current best service combination of this planner.
 * 
 * 2) The other planner responds by adjusting his service combinations list that it got from the analyzer. 
 *    It doesn't matter if this list is overwritten because the service combinations will still hold the same service data,
 *    only the score and the order of each service combination may have changed. It then responds with a message with type "NEW_OFFER"
 *    and with data from his new best service combination.
 *    
 * 3) The other planner gets "NEW_OFFER". It adjusts its service combination list based on the response it got. 
 *    It does a check depending on the rating type of the service combinations:
 * 
 * 	  SCORE:
 *    It checks if his best service combination is still the same service combination as before. 
 *    
 *    CLASS:
 *    It checks if his best service combination is still in the same class as before. 
 *    
 *    If this is true, the planner will end the protocol and send its response with type "CONFIRMED_OFFER".
 *    If this is not true, the planner will send his response message as above, with type "NEW_OFFER". The other planner will reason the 
 *    same way. It's possible that the planners keep communicating and don't converge to a solution. An iteration limit will force a
 *    planner to respond with the "CONFIRMED_OFFER" type if the time is up.
 *    
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class TwoPlannerProtocolStandard extends AbstractTwoPlannerProtocol {

	/**
	 * Handle a given message that was received by the given communication component
	 * @param message the given message
	 * @param receiver the given communication component (receiver)
	 * @throws IllegalStateException throw when the protocol doesn't support a service combination rating type
	 * @throws IllegalStateException throw when the received message has a type that cannot be processed
	 */
	@Override
	public void receiveAndHandleMessage(PlannerMessage message, Planner receiver) throws IllegalStateException {
		
		observer.protocolMessageSent(new ProtocolMessageInformation(message.getSenderEndpoint(), message.getReceiverEndpoint(), message.getType()));
		String messageType = message.getType();
		PlannerMessageContent content = null;
		PlannerMessage response = null;
		
		//System.out.println("\t> " + messageType + " , receiver: " + receiver.getEndpoint() + " sender: " + message.getSenderEndpoint());
		switch(messageType) {
		
		// First offer has been received
		case "FIRST_OFFER":
			receiver.setAvailableServiceCombinations(receiver.calculateNewServiceCombinations(message.getContent()));
			content = receiver.generateMessageContent(receiver.getAvailableServiceCombinations().get(0), sharedRegistryEndpoints, messageContentPercentage);
			response = new PlannerMessage(messageID, message.getSenderEndpoint(), receiver.getEndpoint(), "NEW_OFFER", content);
			receiver.setCurrentServiceCombination(receiver.getAvailableServiceCombinations().get(0));
			
			// Increase message ID
			messageID++;
			receiver.sendMessage(response);	
			
			break;
		
		// New offer has been received
		case "NEW_OFFER":
			List<ServiceCombination> newServiceCombinations = receiver.calculateNewServiceCombinations(message.getContent());
			ServiceCombination chosenCombination = null;
			String responseType = null;
			
			switch (newServiceCombinations.get(0).getRatingType()) {
			
			case SCORE:
				
				chosenCombination = newServiceCombinations.get(0);
				
				if (chosenCombination.hasSameCollection(receiver.getAvailableServiceCombinations().get(0)) || messageID == maxIterations) {				
					responseType = "ACCEPTED_OFFER";
				}
				else {
					responseType = "NEW_OFFER";
				}
				break;
				
			case CLASS:
				
				if (messageID != maxIterations) {
					for (ServiceCombination s : newServiceCombinations) {
						if (s.hasSameCollection(receiver.getCurrentServiceCombination())) {
							if (s.getRating().equals(receiver.getCurrentServiceCombination().getRating())) {
								responseType = "ACCEPTED_OFFER";
								chosenCombination = s;
							}
							else {
								responseType = "NEW_OFFER";
								List<ServiceCombination> bestCombinations = new ArrayList<>();
								
								for (ServiceCombination s2 : newServiceCombinations) {
									if (s2.getRating().equals(newServiceCombinations.get(0).getRating())) {
										bestCombinations.add(s2);
									}
								}
								
								chosenCombination = bestCombinations.get(AbstractProtocol.random.nextInt(bestCombinations.size()));
							}
							
							break;
						}
					}
				}
				else {
					responseType = "ACCEPTED_OFFER";
				}	
				
				break;
				
			default:
				throw new IllegalStateException("The protocol doesn't support this rating type. Type: " + newServiceCombinations.get(0).getRatingType());	
			}
			
			//System.err.print("OLD offer \n" + receiver.getAvailableServiceCombinations().get(0).toString() + ", class: " + receiver.getAvailableServiceCombinations().get(0).getRating() + "score: " + receiver.getAvailableServiceCombinations().get(0).getProperty("FailureRate") + "\n");
			//System.err.print("NEW offer \n" + chosenCombination.toString() + ", class: " + chosenCombination.getRating() + "score: " +  chosenCombination.getProperty("FailureRate") + "\n");
			
			receiver.setAvailableServiceCombinations(newServiceCombinations);	
			content = receiver.generateMessageContent(chosenCombination, sharedRegistryEndpoints, messageContentPercentage);
			response = new PlannerMessage(messageID, message.getSenderEndpoint(), receiver.getEndpoint(), responseType, content);
			receiver.setCurrentServiceCombination(chosenCombination);
			
			if (responseType == "ACCEPTED_OFFER") {
				receiver.finishedProtocol(messageID + 1);
			}
			
			// Increase message ID
			messageID++;
			receiver.sendMessage(response);	
			
			break;
		
		// Offer has been accepted, stop protocol
		case "ACCEPTED_OFFER":
			receiver.finishedProtocol(messageID);
			resetProtocol();
			break;
			
		// Exception state
		default:
			throw new IllegalStateException("The received message has a type that cannot be processed! Type: " + messageType);
		}
	}
}