package tas.mape.communication.protocol;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import service.auxiliary.AbstractMessage;
import tas.mape.communication.CommunicationComponent;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing the structure of a protocol.
 * @param <T> the message type
 * @param <E> the communication component type
 * @note Currently only supports communication between components of the same type, sending the same type of messages
 */
public abstract class AbstractProtocol<T extends AbstractMessage, E extends CommunicationComponent> {
	
	// Message ID of the last sent message
	protected int messageID;
	
	// Needed amount of components to start the protocol
	protected int neededAmountOfComponents;
	
	/**
	 * Execute the protocol with a given list of communication components that will communicate
	 * @param components the given list of communication components
	 * @throws IllegalArgumentException throw when the used protocol doesn't support the given amount of components
	 */
	public void executeProtocol(List<E> components) throws IllegalArgumentException {
		
		if (components.size() != getNeededAmountOfComponents()) {
			throw new IllegalArgumentException("The used protocol doesn't support the given amount of components: " + components.size());
		}
		
		startProtocol(components);
	}
	
	/**
	 * Handle a given message that was received by the given communication component
	 * @param message the given message
	 * @param receiver the given communication component (receiver)
	 */
	public abstract void receiveAndHandleMessage(T message, E receiver);
	
	/**
	 * Return the index of a randomly chosen component
	 * @return the index of the chosen component
	 */
	protected int getRandomComponentIndex() {
		return ThreadLocalRandom.current().nextInt(0, getNeededAmountOfComponents() + 1);
	}
	
	/**
	 * Reset protocol data
	 */
	protected void resetProtocol() {
		messageID = 0;
	}
	
	/**
	 * Return the exact amount of components needed before the protocol can start
	 * @return the exact amount of components needed
	 */
	public int getNeededAmountOfComponents() {
		return neededAmountOfComponents;
	}
	
	/**
	 * Start the protocol, choose starting and receiving components to begin communication
	 * @param components the given list of communication components
	 */
	protected abstract void startProtocol(List<E> components);
	
	/**
	 * Initialize local protocol properties and let a starting communication component send 
	 * its first message to given receiver(s) to start the protocol.
	 * @param components the given list of communication components
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given indices of the receivers of the first message
	 */
	protected abstract void InitializeAndSendFirstMessage(List<E> components, int startIndex, int... receiverIndices);
}
