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
 * @note Can easily be upgraded to more entities, can be made abstract for undefined amount of entities.
 *    
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class CoordinatingPlanners extends AbstractThreePlannerProtocol {

	// These are protocol specific data properties. In a real protocol, these would be inside the communicating component.
	// These are placed here to improve the flexibility of creating new protocols.
	Planner coordinator;
	int acceptedOffers = 0;
	boolean maxReached = false;	
	
	/**
	 * Let a starting communication component send 
	 * its first message to a given receiver to start the protocol.
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given list of receiver indices
	 */
	@Override
	protected void sendFirstMessage(int startIndex, List<Integer> receiverIndices) {
		
		maxReached = false;
		acceptedOffers = 0;
		
		ServiceCombination chosenCombination;
		coordinator = participatingComponents.get(startIndex);
		Planner receiver = participatingComponents.get(receiverIndices.get(AbstractProtocol.random.nextInt(receiverIndices.size())));
		
		System.out.println("-----------------------------------------------------------\nPROTOCOL STARTED");
		
		// Ask data from other planners
		PlannerMessage message = new PlannerMessage(messageID, receiver.getEndpoint(), coordinator.getEndpoint(), "EXCHANGE_DATA", null);
		coordinator.sendMessage(message);
		
		message = new PlannerMessage(messageID, getThirdEntityEndpoint(coordinator.getEndpoint(), receiver.getEndpoint()), coordinator.getEndpoint(), "EXCHANGE_DATA", null);
		coordinator.sendMessage(message);
		
		List<ServiceCombination> newServiceCombinations = coordinator.calculateNewServiceCombinations();
		coordinator.setAvailableServiceCombinations(newServiceCombinations);
		
		// Choose first service combination
		if (coordinator.getAvailableServiceCombinations().get(0).getRatingType() == RatingType.CLASS) {
			// Choose random best combination
			List<ServiceCombination> bestCombinations = new ArrayList<>();
			
			for (ServiceCombination s : coordinator.getAvailableServiceCombinations()) {
				if (s.getRating().equals(coordinator.getAvailableServiceCombinations().get(0).getRating())) {
					bestCombinations.add(s);
				}
			}
			
			chosenCombination = bestCombinations.get(AbstractProtocol.random.nextInt(bestCombinations.size()));
		}
		else {
			chosenCombination = coordinator.getAvailableServiceCombinations().get(0);
		}
		
		// Set chosen combination
		coordinator.setCurrentServiceCombination(chosenCombination);
		
		// Make message
		PlannerMessageContent content = coordinator.generateMessageContentEverything(chosenCombination, findSharedRegistryEndpoints(coordinator.getEndpoint(), receiver.getEndpoint()), receiver.getEndpoint(), usedMessageContentPercentage);
		message = new PlannerMessage(messageID, receiver.getEndpoint(), coordinator.getEndpoint(), "FIRST_OFFER", content);
		
		//System.err.print(" rating: " + chosenCombination.getRating() + "\n " + chosenCombination.toString() + " \n");
		
		// Send message
		coordinator.sendMessage(message);
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
		
		System.out.println("\t> id " + messageID + " " + messageType + " , receiver: " + receiver.getEndpoint() + " sender: " + message.getSenderEndpoint());
		messageID++;
		switch(messageType) {
		
		// New offer has been received
		case "FIRST_OFFER":
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
					
					if (messageID == (maxIterations - 1)) {
						maxReached = true;
					}
				}
				else {	
					responseType = "NEW_OFFER";
				}
				break;
				
			case CLASS:
				
				if (messageID < (maxIterations - 2)) {
					
					bestCombinations = new ArrayList<>();
					boolean found =  false;
					
					for (ServiceCombination s2 : newServiceCombinations) {
						if (s2.getRating().equals(newServiceCombinations.get(0).getRating())) {
							bestCombinations.add(s2);
						}
					}
					
					//System.out.println("best combination count : " + bestCombinations.size() + " , rating: " + bestCombinations.get(0).getRating());	
					//System.out.println("least offending " + receiver.getLeastOffendingCombinations(bestCombinations).getValue());
					for (ServiceCombination s : bestCombinations) {
						if (s.hasSameCollection(receiver.getCurrentServiceCombination())) {
							if (s.getRating().equals(receiver.getCurrentServiceCombination().getRating()) || receiver.getLeastOffendingCombinations(bestCombinations).getValue().equals(receiver.getServiceCombinationOffences(receiver.getCurrentServiceCombination()))) {
								//System.out.println("same as before");
								responseType = "ACCEPTED_OFFER";
								chosenCombination = s;
								found = true;
							}
							
							break;
						}
					}
					
					if (!found) {
						//System.out.println("not found");
						responseType = "NEW_OFFER";
						leastOffendingCombinations = receiver.getLeastOffendingCombinations(bestCombinations).getKey();
						chosenCombination = leastOffendingCombinations.get(AbstractProtocol.random.nextInt(leastOffendingCombinations.size()));//bestCombinations.get(AbstractProtocol.random.nextInt(bestCombinations.size()));
					}		
				}
				else {
					responseType = "ACCEPTED_OFFER";
					chosenCombination = receiver.getCurrentServiceCombination();
					maxReached = true;
				}		
				
				break;
			default:
				throw new IllegalStateException("The protocol doesn't support this rating type. Type: " + newServiceCombinations.get(0).getRatingType());	
			}
			
			//System.err.print("OLD offer " + receiver.getEndpoint() + "\n" + receiver.getCurrentServiceCombination() + ", class: " + receiver.getCurrentServiceCombination().getRating() + "score: " + receiver.getCurrentServiceCombination().getProperty("FailureRate") + "\n");
			//System.err.print("NEW offer " + receiver.getEndpoint() + "\n" + chosenCombination.toString() + ", class: " + chosenCombination.getRating() + "score: " +  chosenCombination.getProperty("FailureRate") + "\n");
			
			receiver.setAvailableServiceCombinations(newServiceCombinations);	
			receiver.setCurrentServiceCombination(chosenCombination);
			
			if (responseType == "ACCEPTED_OFFER") {
				
				acceptedOffers++;
				
				if (receiver == coordinator) {
					if (acceptedOffers == participatingComponents.size() - 1 || maxReached) {
						response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "AGREED_OFFER", null);
						receiver.sendMessage(response);	
						response = new PlannerMessage(messageID, getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint(), "AGREED_OFFER", null);
						receiver.sendMessage(response);	
						
						receiver.finishedProtocol(messageID);
						resetProtocol();
					}
					else {
						response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "ACCEPTED_OFFER", null);				
						receiver.sendMessage(response);
						
						content = receiver.generateMessageContentEverything(chosenCombination, findSharedRegistryEndpoints(getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint()), getThirdEntityEndpoint(sender, receiver.getEndpoint()), usedMessageContentPercentage);
						response = new PlannerMessage(messageID, getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint(), "FIRST_OFFER", content);				
						receiver.sendMessage(response);
					}
				}
				else {
					response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "ACCEPTED_OFFER", null);				
					receiver.sendMessage(response);
				}
			}
			else {
				acceptedOffers = 0;
				
				if (receiver == coordinator) {
					content = receiver.generateMessageContentEverything(chosenCombination, findSharedRegistryEndpoints(sender, receiver.getEndpoint()), receiver.getEndpoint(), usedMessageContentPercentage);
					response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), responseType, content);				
					receiver.sendMessage(response);
				}
				else {
					content = receiver.generateMessageContent(chosenCombination, receiver.getRegistryEndpoints(), usedMessageContentPercentage);
					response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), responseType, content);				
					receiver.sendMessage(response);
				}
			}
			
			break;
			
		case "EXCHANGE_DATA":
			
			ServiceCombination combo = receiver.getCurrentServiceCombination();
			
			if (combo == null) {
				receiver.setCurrentServiceCombination(receiver.getAvailableServiceCombinations().get(0));
				combo = receiver.getAvailableServiceCombinations().get(0);
			}
			
			content = receiver.generateMessageContent(combo, receiver.getRegistryEndpoints(), usedMessageContentPercentage);
			response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "GIVE_DATA", content);
			receiver.sendMessage(response);	
			break;
			
		case "GIVE_DATA":		
			receiver.addToLoadBuffer(sender, message.getContent());
			break;
			
		// Offer has been accepted
		case "ACCEPTED_OFFER":		
			if (receiver == coordinator) {
				if (acceptedOffers == participatingComponents.size() - 1 || maxReached) {
					response = new PlannerMessage(messageID, sender, receiver.getEndpoint(), "AGREED_OFFER", null);
					receiver.sendMessage(response);
					response = new PlannerMessage(messageID, getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint(), "AGREED_OFFER", null);
					receiver.sendMessage(response);	
					
					receiver.finishedProtocol(messageID);
					resetProtocol();
				}
				else {
					content = receiver.generateMessageContentEverything(receiver.getCurrentServiceCombination(), findSharedRegistryEndpoints(getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint()), getThirdEntityEndpoint(sender, receiver.getEndpoint()), usedMessageContentPercentage);
					response = new PlannerMessage(messageID, getThirdEntityEndpoint(sender, receiver.getEndpoint()), receiver.getEndpoint(), "FIRST_OFFER", content);				
					receiver.sendMessage(response);
				}
			}
			
			break;
			
		// Offer has been accepted, stop protocol
		case "AGREED_OFFER":
			receiver.finishedProtocol(messageID);
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
		for (Planner planner : participatingComponents) {
			if (planner.getEndpoint() != e1 && planner.getEndpoint() != e2) {
				return planner.getEndpoint();
			}
		}
		
		throw new IllegalStateException("No third entity found!");
	}
}
