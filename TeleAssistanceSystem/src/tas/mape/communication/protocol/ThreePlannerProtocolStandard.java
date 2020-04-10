package tas.mape.communication.protocol;

import java.util.ArrayList;
import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.message.PlannerMessageContent;
import tas.mape.communication.message.ProtocolMessageInformation;
import tas.mape.planner.Planner;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * Class representing a standard three-component planner protocol.
 *    
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class ThreePlannerProtocolStandard extends AbstractThreePlannerProtocol {

	/**
	 * Let a starting communication component send 
	 * its first message to a given receiver to start the protocol.
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given list of receiver indices
	 */
	@Override
	protected void sendFirstMessage(int startIndex, List<Integer> receiverIndices) {
		
		ServiceCombination chosenCombination;
		Planner sender = components.get(startIndex);
		Planner receiver = components.get(receiverIndices.get(AbstractProtocol.random.nextInt(receiverIndices.size())));
		
		// Ask data from third planner
		PlannerMessage message = new PlannerMessage(messageID, getThirdEntityEndpoint(sender.getEndpoint(), receiver.getEndpoint()), sender.getEndpoint(), "ASK_DATA", null);
		sender.sendMessage(message);
		
		// Choose first service combination
		if (sender.getAvailableServiceCombinations().get(0).getRatingType() == RatingType.CLASS) {
			// Choose random best combination
			List<ServiceCombination> bestCombinations = new ArrayList<>();
			
			for (ServiceCombination s : sender.getAvailableServiceCombinations()) {
				if (s.getRating().equals(sender.getAvailableServiceCombinations().get(0).getRating())) {
					bestCombinations.add(s);
				}
			}
			
			chosenCombination = bestCombinations.get(AbstractProtocol.random.nextInt(bestCombinations.size()));
		}
		else {
			chosenCombination = sender.getAvailableServiceCombinations().get(0);
		}
		
		// Set chosen combination
		sender.setCurrentServiceCombination(chosenCombination);
		
		// Update message ID
		messageID++;
		
		// Make message
		PlannerMessageContent content = sender.generateMessageContent(chosenCombination, findSharedRegistryEndpoints(sender.getEndpoint(), receiver.getEndpoint()), messageContentPercentage);
		message = new PlannerMessage(messageID, receiver.getEndpoint(), sender.getEndpoint(), "FIRST_OFFER", content);
		
		//System.out.println("-----------------------------------------------------------\nPROTOCOL STARTED");
		
		// Update message ID
		messageID++;
		
		//System.err.print(" rating: " + chosenCombination.getRating() + "\n " + chosenCombination.toString() + " \n");
		
		// Send message
		sender.sendMessage(message);
	}
	
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
			
			messageID++;
			
		    response = new PlannerMessage(messageID, getThirdEntityEndpoint(message.getSenderEndpoint(), receiver.getEndpoint()), receiver.getEndpoint(), "ASK_DATA", null);
			receiver.sendMessage(response);	
			
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
			
			receiver.setAvailableServiceCombinations(receiver.calculateNewServiceCombinations());
			receiver.setCurrentServiceCombination(receiver.getAvailableServiceCombinations().get(0));
			content = receiver.generateMessageContent(receiver.getCurrentServiceCombination(), findSharedRegistryEndpoints(sender, receiver.getEndpoint()), messageContentPercentage);
			response = new PlannerMessage(messageID, message.getSenderEndpoint(), receiver.getEndpoint(), "NEW_OFFER", content);
			receiver.sendMessage(response);	
			
			break;
		
		// New offer has been received
		case "NEW_OFFER2":
		case "NEW_OFFER":
			
			receiver.addToLoadBuffer(sender, message.getContent());
			newServiceCombinations = receiver.calculateNewServiceCombinations();
			chosenCombination = null;
			String responseType = null;
			
			switch (newServiceCombinations.get(0).getRatingType()) {
			
			case SCORE:
				
				chosenCombination = newServiceCombinations.get(0);
				
				if (chosenCombination.hasSameCollection(receiver.getAvailableServiceCombinations().get(0)) || messageID == (maxIterations - 2)) {						
					responseType = "ACCEPTED_OFFER";
				}
				else {	
					responseType = "NEW_OFFER";
				}
				break;
				
			case CLASS:
				
				if (messageID != (maxIterations - 2)) {
					
					bestCombinations = new ArrayList<>();
					boolean found =  false;
					
					for (ServiceCombination s2 : newServiceCombinations) {
						if (s2.getRating().equals(newServiceCombinations.get(0).getRating())) {
							bestCombinations.add(s2);
						}
					}
					
					System.out.println("best combination count : " + bestCombinations.size() + " , rating: " + bestCombinations.get(0).getRating());
					
					for (ServiceCombination s : bestCombinations) {
						if (s.hasSameCollection(receiver.getCurrentServiceCombination())) {
							//s.getRating().equals(receiver.getCurrentServiceCombination().getRating())
							//System.out.println("least offending value: " + receiver.getLeastOffendingCombinations(bestCombinations).getValue());
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
						chosenCombination = leastOffendingCombinations.get(AbstractProtocol.random.nextInt(leastOffendingCombinations.size()));//bestCombinations.get(AbstractProtocol.random.nextInt(bestCombinations.size()));
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
			
			System.err.print("OLD offer \n" + receiver.getAvailableServiceCombinations().get(0).toString() + ", class: " + receiver.getAvailableServiceCombinations().get(0).getRating() + "score: " + receiver.getAvailableServiceCombinations().get(0).getProperty("FailureRate") + "\n");
			System.err.print("NEW offer \n" + chosenCombination.toString() + ", class: " + chosenCombination.getRating() + "score: " +  chosenCombination.getProperty("FailureRate") + "\n");
			
			receiver.setAvailableServiceCombinations(newServiceCombinations);	
			receiver.setCurrentServiceCombination(chosenCombination);
			content = receiver.generateMessageContent(chosenCombination, findSharedRegistryEndpoints(sender, receiver.getEndpoint()), messageContentPercentage);
			
			messageID++;
			
			if (responseType == "ACCEPTED_OFFER") {
				if (messageType == "NEW_OFFER2") {				
					response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "ACCEPTED_OFFER2", content);				
					receiver.sendMessage(response);	
					
					response = new PlannerMessage(messageID, getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint(), "ACCEPTED_OFFER2", content);
					receiver.sendMessage(response);
					
					receiver.finishedProtocol(messageID);
				}
				else {
					response = new PlannerMessage(messageID, getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint(), "GIVE_DATA", content);
					receiver.sendMessage(response);
					
					response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "ACCEPTED_OFFER", content);				
					receiver.sendMessage(response);	
				}
			}
			else {
				response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), responseType, content);				
				receiver.sendMessage(response);	
			}
			
			break;
			
		case "ASK_DATA":
			
			ServiceCombination combo = receiver.getCurrentServiceCombination();
			
			if (combo == null) {
				receiver.setCurrentServiceCombination(receiver.getAvailableServiceCombinations().get(0));
				combo = receiver.getAvailableServiceCombinations().get(0);
			}
			
			content = receiver.generateMessageContent(combo, findSharedRegistryEndpoints(sender, receiver.getEndpoint()), messageContentPercentage);
			response = new PlannerMessage(messageID, message.getSenderEndpoint(), receiver.getEndpoint(), "GIVE_DATA", content);
			messageID++;	
			receiver.sendMessage(response);	
			break;
			
		case "GIVE_DATA":		
			receiver.addToLoadBuffer(sender, message.getContent());	
			newServiceCombinations = receiver.calculateNewServiceCombinations();
			receiver.setAvailableServiceCombinations(newServiceCombinations);
			messageID++;		
			break;
			
		// Offer has been accepted
		case "ACCEPTED_OFFER":		
			content = receiver.generateMessageContent(receiver.getCurrentServiceCombination(), findSharedRegistryEndpoints(sender, receiver.getEndpoint()), messageContentPercentage);
			response = new PlannerMessage(messageID, getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint(), "NEW_OFFER2", content);
			messageID++;
			receiver.sendMessage(response);	
			break;
			
		// Offer has been accepted, stop protocol
		case "ACCEPTED_OFFER2":
			receiver.finishedProtocol(messageID);
			resetProtocol();
			break;
			
		// Exception state
		default:
			throw new IllegalStateException("The received message has a type that cannot be processed! Type: " + messageType);
		}
	}
	
	/**
	 * Return the third participating entity name in the protocol with the given 2 actively communicating component names
	 * @param e1 the first component name
	 * @param e2 the second component name
	 * @return the third component name
	 * @throws IllegalStateException throw when no third entity was found.
	 */
	private String getThirdEntityEndpoint(String e1, String e2) throws IllegalStateException {
		for (Planner planner : components) {
			if (planner.getEndpoint() != e1 && planner.getEndpoint() != e2) {
				return planner.getEndpoint();
			}
		}
		
		throw new IllegalStateException("No third entity found!");
	}
}
