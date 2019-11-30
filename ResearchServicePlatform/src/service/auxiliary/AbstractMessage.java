package service.auxiliary;

/**
 * Definition of formated message
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class AbstractMessage {
	private int id;
	private String endpoint;
	private String msgType;
	
	/**
	 * Constructor
	 * @param id  the message id
	 * @param endpoint the source endpoint
	 * @param msgType the message type, request or response
	 */
	public AbstractMessage(int id, String endpoint, String msgType){
		this.id=id;
		this.endpoint=endpoint;
		this.msgType=msgType;
	}
	
	/**
	 * Set the message id
	 * @param id the new message id
	 */
	public void setId(int id) {
	    this.id = id;
	}
	
	/**
	 * Return the message id
	 * @return the message id
	 */
	public int getId() {
	    return id;
	}
	
	/**
	 * Set the source endpoint
	 * @param endpoint the new source endpoint
	 */
	public void setEndpoint(String endpoint) {
	    this.endpoint = endpoint;
	}
	
	/**
	 * Return the source endpoint
	 * @return the source endpoint
	 */
	public String getEndpoint() {
	    return endpoint;
	}
	
	/**
	 * Set the message type
	 * @param msgType the message type, request or response
	 */
	public void setType(String msgType) {
	    this.msgType = msgType;
	}
	
	/**
	 * Return message type
	 * @return the message type, request or response
	 */
	public String getType() {
	    return msgType;
	}
}
