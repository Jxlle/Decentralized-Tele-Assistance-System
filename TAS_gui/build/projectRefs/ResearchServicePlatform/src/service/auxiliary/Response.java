package service.auxiliary;

/**
 * Definition of formated response message
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Response extends AbstractMessage{

	private int requestID;
	private Class<?> returnType;
	private Object returnValue;
	
	/**
	 * Constructor
	 * @param messageID the message id
	 * @param requestID the response for which request
	 * @param endpoint  the source endpoint
	 * @param returnValue the result 
	 */
	public Response(int messageID, int requestID,String endpoint, Object returnValue){
		super(messageID,endpoint,"response");
		this.setRequestID(requestID);
		if(returnValue!=null){
			this.setReturnType(returnValue.getClass());
			this.setReturnValue(returnValue);
		}
		else{
			this.setReturnType(null);
			this.setReturnValue(null);
		}
	}

	/**
	 * Return request id
	 * @return the request id
	 */
	public int getRequestID() {
	    return requestID;
	}

	/**
	 * Set request id
	 * @param requestID the new request id
	 */
	public void setRequestID(int requestID) {
	    this.requestID = requestID;
	}

	/**
	 * Return result type
	 * @return the result type
	 */
	public Class<?> getReturnType() {
	    return returnType;
	}

	/**
	 * Set result type
	 * @param returnType the result type
	 */
	public void setReturnType(Class<?> returnType) {
	    this.returnType = returnType;
	}

	/**
	 * Return result value
	 * @return the result value
	 */
	public Object getReturnValue() {
	    return returnValue;
	}

	/**
	 * Set result value
	 * @param returnValue the new result value
	 */
	public void setReturnValue(Object returnValue) {
	    this.returnValue = returnValue;
	}
	
}
