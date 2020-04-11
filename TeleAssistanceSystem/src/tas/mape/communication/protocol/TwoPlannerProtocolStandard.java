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
		String sender = message.getSenderEndpoint();
		PlannerMessageContent content = null;
		PlannerMessage response = null;
		
		ServiceCombination chosenCombination = null;
		List<ServiceCombination> newServiceCombinations = null;
		List<ServiceCombination> bestCombinations = null;
		List<ServiceCombination> leastOffendingCombinations = null;
		
		System.out.println("\t> " + messageType + " , receiver: " + receiver.getEndpoint() + " sender: " + message.getSenderEndpoint());
		switch(messageType) {
		
		// First offer has been received
		case "FIRST_OFFER":		
			receiver.addToLoadBuffer(sender, message.getContent());					
			newServiceCombinations = receiver.calculateNewServiceCombinations();
			
			switch (newServiceCombinations.get(0).getRatingType()) {
			
				case SCORE:
					
					chosenCombination = newServiceCombinations.get(0);
					break;
					
				case CLASS:
					
					bestCombinations = new ArrayList<>();
					
					for (ServiceCombination s2 : newServiceCombinations) {
						if (s2.getRating().equals(newServiceCombinations.get(0).getRating())) {
							bestCombinations.add(s2);
						}
					}
					
					leastOffendingCombinations = receiver.getLeastOffendingCombinations(bestCombinations).getKey();
					chosenCombination = leastOffendingCombinations.get(AbstractProtocol.random.nextInt(leastOffendingCombinations.size()));
					break;
					
				default:
					throw new IllegalStateException("The protocol doesn't support this rating type. Type: " + newServiceCombinations.get(0).getRatingType());	
					
			}
			
			receiver.setAvailableServiceCombinations(newServiceCombinations);
			//System.err.println("chosen combination rating: " + chosenCombination.getRating() + " combination: "+ chosenCombination);
			receiver.setCurrentServiceCombination(chosenCombination);
			
			content = receiver.generateMessageContent(receiver.getCurrentServiceCombination(), sharedRegistryEndpoints, messageContentPercentage);
			response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "NEW_OFFER", content);
			
			// Increase message ID
			messageID++;
			receiver.sendMessage(response);	
			
			break;
		
		// New offer has been received
		case "NEW_OFFER":
			receiver.addToLoadBuffer(sender, message.getContent());
			newServiceCombinations = receiver.calculateNewServiceCombinations();
			chosenCombination = null;
			String responseType = null;
			
			switch (newServiceCombinations.get(0).getRatingType()) {
			
			case SCORE:
				
				chosenCombination = newServiceCombinations.get(0);
				
				if (chosenCombination.hasSameCollection(receiver.getAvailableServiceCombinations().get(0)) || messageID == (maxIterations - 1)) {				
					responseType = "ACCEPTED_OFFER";
				}
				else {
					responseType = "NEW_OFFER";
				}
				break;
				
			case CLASS:
				
				if (messageID != (maxIterations - 1)) {
					
					bestCombinations = new ArrayList<>();
					boolean found =  false;
					
					for (ServiceCombination s2 : newServiceCombinations) {
						if (s2.getRating().equals(newServiceCombinations.get(0).getRating())) {
							bestCombinations.add(s2);
						}
					}
					
					//System.out.println("best combination count : " + bestCombinations.size() + " , rating: " + bestCombinations.get(0).getRating());
					
					for (ServiceCombination s : bestCombinations) {
						if (s.hasSameCollection(receiver.getCurrentServiceCombination())) {
							//s.getRating().equals(receiver.getCurrentServiceCombination().getRating())
							System.out.println("least offending value: " + receiver.getLeastOffendingCombinations(bestCombinations).getValue());
							if (s.getRating().equals(receiver.getCurrentServiceCombination().getRating()) || receiver.getLeastOffendingCombinations(bestCombinations).getValue().equals(receiver.getServiceCombinationOffences(receiver.getCurrentServiceCombination()))) {
								responseType = "ACCEPTED_OFFER";
								chosenCombination = s;
								found = true;
							}
							
							break;
						}
					}
					
					if (!found) {
						responseType = "NEW_OFFER";	
						leastOffendingCombinations = receiver.getLeastOffendingCombinations(bestCombinations).getKey();
						chosenCombination = leastOffendingCombinations.get(AbstractProtocol.random.nextInt(leastOffendingCombinations.size()));
					}		
				}
				else {
					responseType = "ACCEPTED_OFFER";
					chosenCombination = receiver.getCurrentServiceCombination();
				}	
				
				break;
				
			default:
				throw new IllegalStateException("The protocol doesn't support this rating type. Type: " + newServiceCombinations.get(0).getRatingType());	
			}
			
			//System.err.print("OLD offer \n" + receiver.getCurrentServiceCombination()+ ", class: " + receiver.getCurrentServiceCombination().getRating() + "score: " + receiver.getCurrentServiceCombination().getProperty("FailureRate") + "\n");
			//System.err.print("NEW offer \n" + chosenCombination.toString() + ", class: " + chosenCombination.getRating() + "score: " +  chosenCombination.getProperty("FailureRate") + "\n");
			
			receiver.setAvailableServiceCombinations(newServiceCombinations);	
			receiver.setCurrentServiceCombination(chosenCombination);
			content = receiver.generateMessageContent(chosenCombination, sharedRegistryEndpoints, messageContentPercentage);
			response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), responseType, content);
			
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
