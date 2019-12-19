package tas.mape.communication.message;
import service.auxiliary.AbstractMessage;

public abstract class ComponentMessage<T extends ComponentMessageContent> extends AbstractMessage {
	
	// The sender endpoint
	private String senderEndpoint;
	
	// The message content
	private T content;
	
	/**
	 * Create a new planner message with a given id, receiver endpoint, sender endpoint, message type and message content.
	 * @param id the id of the message (identifier)
	 * @param receiverEndpoint the given receiver endpoint
	 * @param senderEndpoint the given sender endpoint
	 * @param msgType the given message type
	 * @param content the given message content
	 */
	protected ComponentMessage(int id, String receiverEndpoint, String senderEndpoint, String msgType, T content) {
		super(id, receiverEndpoint, msgType);
		this.senderEndpoint = senderEndpoint;
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
