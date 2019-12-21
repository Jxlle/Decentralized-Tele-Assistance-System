package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.ComponentMessage;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing the structure of a two-component protocol.
 * @param <T> the message type
 * @param <E> the communication component type
 */
public abstract class AbstractTwoComponentProtocol<T extends ComponentMessage<?>, E extends CommunicationComponent<T>> extends AbstractProtocol<T, E> {
	
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
	protected void startProtocol(List<E> components) {
		
		int senderIndex = getRandomComponentIndex();
		int receiverIndex = Math.abs(senderIndex - 1);
		
		InitializeAndSendFirstMessage(components, senderIndex, receiverIndex);
	}
}
