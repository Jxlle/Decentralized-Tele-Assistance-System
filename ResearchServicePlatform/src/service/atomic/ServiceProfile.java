package service.atomic;

import javafx.util.Pair;
import service.auxiliary.ServiceDescription;

/**
 * 
 * Responsible for emulating the non-functional characteristics 
 * of the behavior of services. 
 */
public abstract class ServiceProfile {
	
	// If this is true, the service profile will be enabled at by default
	protected boolean defaultEnabled;
	
	// Type of the service profile
	protected String type;
	
	// Text representation of the TreeMap data, if present
	protected Pair<String, String> treeMapText;
	
	/**
	 * This method is called before invoking service operations. Argument operationName have the value of the operation which is going to invoke. 
	 * If this method returns true then that service operation will be invoked. If this method
	 * returns false then no operation will be invoked and a null value will be returned.
	 * 
	 * @param description the description of the service using this profile
	 * @param operationName the operation name
	 * @param args   the parameters for this operation
	 * @return if true continue to execute the operation, otherwise stop
	 */
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object...args){
		return true;
	}
	
	/**
	 * This method will be called after invoking a service operation. Argument operationName has the value of the operation which was invoked.
	 * and argument result has the value which is returned by invoking the operation.  
	 * 
	 * @param operationName the operation name
	 * @param result the result after executing the operation
	 * @param args the parameters for this operation
	 * @return  object to be returned to requester
	 */
	public Object postInvokeOperation(String operationName, Object result, Object...args){
		return result;
	}
	
	/**
	 * return if the service profile is enabled by default
	 * @return if the service profile is enabled by default
	 */
	public boolean getDefaultEnabled() {
		return defaultEnabled;
	}

	/**
	 * Return the service type
	 * @return the service type
	 */
	public String getServiceType() {
		return type;
	}
	
	/**
	 * Return the text representation of the TreeMap data
	 * @return the text representation of the TreeMap data
	 */
	public Pair<String, String> getServiceTreeMapText() {
		return treeMapText;
	}
	
}
