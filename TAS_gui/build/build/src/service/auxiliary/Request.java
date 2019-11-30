package service.auxiliary;

/**
 * Definition of formated request message
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Request extends AbstractMessage{
	private String serviceName;
	private String opName;
	private Param [] params;
	
	/**
	 * Constructor
	 * @param messageID the message id
	 * @param endpoint the source endpoint
	 * @param serviceName the service name
	 * @param opName the operation name
	 * @param params the parameters for the operation
	 */
	public Request(int messageID,String endpoint, String serviceName, String opName, Object[] params){
		super(messageID,endpoint,"request");
		this.serviceName=serviceName;
		this.opName=opName;
		this.params = new Param[params.length];
		for (int i = 0; i < params.length; i++) {
		    this.params[i] = new Param(params[i]);
		}
	}
	
	/**
	 * Constructor
	 * @param messageID the message id
	 * @param endpoint the source endpoint
	 * @param serviceName the service name
	 * @param opName the operation name
	 */
	public Request(int messageID,String endpoint,String serviceName, String opName){
		super(messageID,endpoint,"request");
		this.serviceName=serviceName;
		this.opName=opName;
	}
	
	/**
	 * Set service name
	 * @param serviceName the new service name
	 */
	public void setServiceName(String serviceName) {
	    this.serviceName = serviceName;
	}
	
	/**
	 * Return service name
	 * @return the service name
	 */
	public String getServiceName() {
	    return serviceName;
	}
	
	/**
	 * Set operation name
	 * @param opName the operation name
	 */
	public void setOpName(String opName) {
	    this.opName = opName;
	}
	
	/**
	 * Return operation name
	 * @return the operation name
	 */
	public String getOpName() {
	    return opName;
	}
	
	/**
	 * Set parameters for the operation
	 * @param params the parameters
	 */
	public void setParams(Param[] params) {
	    this.params = params;
	}
	
	/**
	 * Return parameters
	 * @return the parameters for the operation
	 */
	public Param[] getParams() {
	    return params;
	}
}
