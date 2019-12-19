package service.auxiliary;

/**
 * Definition of formated message
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public abstract class AbstractMessage {
	
	private final int id;
	private final String receiverEndpoint;
	private final String msgType;
	
	/**
	 * Constructor
	 * @param id the id of the message (identifier)
	 * @param receiverEndpoint the given receiver endpoint
	 * @param msgType the given message type
	 */
	protected AbstractMessage(int id, String receiverEndpoint, String msgType){
		this.id = id;
		this.receiverEndpoint = receiverEndpoint;
		this.msgType = msgType;
	}
	
	/**
	 * Return the message id
	 * @return the message id
	 */
	public int getId() {
	    return id;
	}
	
	/**
	 * Return the source endpoint
	 * @return the source endpoint
	 */
	public String getReceiverEndpoint() {
	    return receiverEndpoint;
	}
	
	/**
	 * Return message type
	 * @return the message type, request or response
	 */
	public String getType() {
	    return msgType;
	}
}
