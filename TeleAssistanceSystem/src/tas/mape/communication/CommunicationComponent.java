package tas.mape.communication;

import tas.mape.communication.message.ComponentMessage;
import tas.mape.communication.message.ComponentMessageHost;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing a component that has the ability to communicate with other components of this type.
 * @param <T> the message type
  @note Connection between multiple hosts currently not supported
 */
public abstract class CommunicationComponent<T extends ComponentMessage<?>> {
	
	// Unique endpoint for identification
	private String endpoint;
	
	// Message host of this communication component
	private ComponentMessageHost<T> messageHost;
	
	/**
	 * Create a new communication component with a given endpoint
	 * @param endpoint the given endpoint (identifier)
	 */
	protected CommunicationComponent(String endpoint) {
		this.endpoint = endpoint;
	}
	
	/**
	 * Return the endpoint of this component
	 * @return the endpoint
	 */
	public String getEndpoint() {
		return endpoint;
	}
	
	/**
	 * Set the component endpoint to a new given endpoint
	 * @param endpoint the new endpoint
	 * @throws IllegalArgumentException throw when the endpoint is changed while still being connected to a host
	 */
	public void setEndpoint(String endpoint) throws IllegalArgumentException {
		
		if (messageHost != null) {
			throw new IllegalArgumentException("Can't change endpoint when the component is still connected to a message host!");
		}
		
		this.endpoint = endpoint;
	}
	
	/**
	 * Return the message host
	 * @return the message host
	 */
	public ComponentMessageHost<T> getMessageHost() {
		return messageHost;
	}
	
	/**
	 * Set the message host to a new given message host
	 * @param messageHost the new component message host
	 * @throws IllegalArgumentException throw when calling this method directly. This method should only be called from the message host
	 */
	public void setMessageHost(ComponentMessageHost<T> messageHost) throws IllegalArgumentException {
		
		if (this.messageHost != null && this.messageHost.getListeners().get(endpoint) != null) {
			throw new IllegalArgumentException("Can't change message host directly!");
		}
		
		this.messageHost = messageHost;
	}
	
	/**
	 * Send a given message to another communication component
	 * @param message the message to be sent
	 * @throws IllegalStateException throw when the message host has not been set
	 */
	public void sendMessage(T message) throws IllegalStateException {
		
		if (messageHost == null) {
			throw new IllegalStateException("Message host has not been set!");
		}
		
		messageHost.sendMessage(message);
	}
	
	/**
	 * This method is called from the message host when receiving a message
	 * @param message the received message
	 */
	public abstract void receiveMessage(T message);
}
