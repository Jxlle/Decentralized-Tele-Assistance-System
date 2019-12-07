package tas.mape.communication;

import java.util.HashMap;
import java.util.Map;

import service.auxiliary.AbstractMessage;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class used for handling message sending and receiving between components
 * @note does not support concurrent message sending and receiving, not multithreaded
 */
public class ComponentMessageHost {
	
	// All communication components connected to this message host
	private Map<String, CommunicationComponent> listeners;
	
	/**
	 * Create a new message host
	 */
	public ComponentMessageHost() {
		listeners = new HashMap<>();
	}
	
	/**
	 * Return the list of listeners
	 * @return the list of listeners
	 */
	public Map<String, CommunicationComponent> getListeners() {
		return listeners;
	}
	
	/**
	 * Register a given communication component to this host
	 * @param component the communication component to be registered
	 */
	public void register(CommunicationComponent component) {
		listeners.put(component.getEndpoint(), component);
		component.setMessageHost(this);
	}
	
	/**
	 * Unregister a given communication component from this host
	 * @param component the communication component to be unregistered
	 * @throws IllegalArgumentException throw when the given component is not registered to this host
	 */
	public void unRegister(CommunicationComponent component) throws IllegalArgumentException {
		
		if (listeners.get(component.getEndpoint()) == null) {
			throw new IllegalArgumentException("Given component is not registered!");
		}
		
		listeners.remove(component.getEndpoint());
		component.setMessageHost(null);
	}
	
	/**
	 * Handles message sending between communication components connected to this host
	 * @param message the message containing the sender, receiver, type and content
	 */
	public void sendMessage(AbstractMessage message) {
		String messageEndpoint = message.getReceiverEndpoint();
		listeners.get(messageEndpoint).receiveMessage(message);
	}
}
