package service.atomic;

/**
 * 
 * Responsible for emulating the non-functional characteristics 
 * of the behavior of services. 
 */
public abstract class ServiceProfile {

	/**
	 * This method is called before invoking service operations. Argument operationName have the value of the operation which is going to invoke. 
	 * If this method returns true then that service operation will be invoked. If this method
	 * returns false then no operation will be invoked and a null value will be returned.
	 * 
	 * @param operationName the operation name
	 * @param args   the parameters for this operation
	 * @return if true continue to execute the operation, otherwise stop
	 */
	public boolean preInvokeOperation(String operationName, Object...args){
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

}
