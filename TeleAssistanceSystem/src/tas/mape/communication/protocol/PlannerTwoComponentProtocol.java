package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.message.PlannerMessageContent;
import tas.mape.planner.Planner;

/**
 * Abstract class representing the structure of a two-component planner protocol.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public abstract class PlannerTwoComponentProtocol extends AbstractTwoComponentProtocol<PlannerMessage, Planner> {

	// Shared registry endpoints between both planners
	protected List<String> sharedRegistryEndpoints;
	
	/**
	 * Initialize local protocol properties and let a starting communication component send 
	 * its first message to given receiver(s) to start the protocol.
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given index of the receiver of the first message
	 */
	@Override
	protected void InitializeAndSendFirstMessage(int startIndex, int... receiverIndices) {
	
		sharedRegistryEndpoints = components.get(0).getRegistryEndpoints();
		List<String> registryEndpointsOther = components.get(1).getRegistryEndpoints();
		Planner sender = components.get(startIndex);
		Planner receiver = components.get(receiverIndices[0]);
		
		// Calculate shared registry endpoints
		sharedRegistryEndpoints.retainAll(registryEndpointsOther);
		
		// Make message
		PlannerMessageContent content = sender.generateMessageContent(sender.getAvailableServiceCombinations().get(0), sharedRegistryEndpoints);
		PlannerMessage message = new PlannerMessage(messageID, receiver.getEndpoint(), sender.getEndpoint(), "FIRST_OFFER", content);
		
		// Send message
		sender.sendMessage(message);
		
		// Increase message ID
		messageID++;
	}
}
