package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.CommunicationComponent;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing the structure of a two-component protocol.
 */
public abstract class AbstractTwoComponentProtocol extends AbstractProtocol {
	
	/**
	 * Create a new two-component protocol
	 */
	protected AbstractTwoComponentProtocol() {
		neededAmountOfComponents = 2;
	}
	
	/**
	 * Start the protocol, choose starting and receiving components to begin communication
	 * @param components the given list of communication components
	 */
	@Override
	protected void startProtocol(List<CommunicationComponent> components) {
		
		int senderIndex = getRandomComponentIndex();	
		int receiverIndex = Math.abs(senderIndex - 1);
		
		sendFirstMessage(components, senderIndex, receiverIndex);
	}
}
