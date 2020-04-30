package tas.communication.message;
import service.auxiliary.AbstractMessage;

/**
 * Abstract class representing the message of a component
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 * @param <T> the message type
 */
public abstract class ComponentMessage<T extends ComponentMessageContent> extends AbstractMessage {
	
	// The sender endpoint
	protected String senderEndpoint;
	
	// The message content
	protected T content;
	
	/**
	 * Create a new abstract component message with a given id, receiver endpoint, sender endpoint, message type and message content.
	 * @param id the id of the message (identifier)
	 * @param receiverEndpoint the given receiver endpoint
	 * @param senderEndpoint the given sender endpoint
	 * @param msgType the given message type
	 * @param content the given message content
	 */
	protected ComponentMessage(int id, String receiverEndpoint, String senderEndpoint, String msgType, T content) {
		super(id, receiverEndpoint, msgType);
		this.senderEndpoint = senderEndpoint;
		this.content = content;
	}
	
	/**
	 * Return the sender endpoint
	 * @return the sender endpoint
	 */
	public String getSenderEndpoint() {
		return senderEndpoint;
	}
	
	/**
	 * Return the message content
	 * @return the message content
	 */
	public T getContent() {
		return content;
	}
}
