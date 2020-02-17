package service.adaptation.probes;

import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.ServiceDescription;

/**
 * 
 * Monitor the start and end of a workflow execution
 * 
 */
public class WorkflowProbe extends AbstractProbe<WorkflowProbeInterface> {
    
	/**
	 * 
	 * @param params
	 */
    public void notifyWorkflowStarted(Object[] params){
    	for(WorkflowProbeInterface workflowInterface: subscribers) {
    		workflowInterface.workflowStarted(params);
    	}
    }

    /**
     * 
     * @param result
     * @param params
     */
    public void notifyWorkflowEnded(Object result, Object[] params){
    	for(WorkflowProbeInterface workflowInterface: subscribers) {
    		workflowInterface.workflowEnded(result, params);
    	}
    }
    
    /**
     * Notify the event that a service operation has returned
     * 
     * @param description description of the service 
     * @param result the result after the operation invoked
     * @param opName the invoked operation name
     * @param params the parameters of the invoked operation
     */
    public void notifyServiceOperationReturned(ServiceDescription description, Object result, String opName, Object[] params){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceOperationReturned(description, result, opName, params);
    	}
    }
    
    /**
     * 
     * @param serviceType
     * @param opName
     */
    public void notifyServiceNotFound(String serviceType, String opName){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceNotFound(serviceType, opName);
    	}
    }
    
    /**
     * 
     * @param description
     * @param opName
     * @param params
     */
    public void notifyServiceOperationInvoked(ServiceDescription description, String opName, Object[] params){
    	for (WorkflowProbeInterface serviceRegistryInterface : subscribers) {
    		serviceRegistryInterface.serviceOperationInvoked(description, opName, params);
    	}
    }
    
    /**
     * 
     * @param description
     * @param opName
     * @param params
     */
    public void notifyServiceOperationTimeout(ServiceDescription description, String opName, Object[] params){
    	for (WorkflowProbeInterface serviceRegistryInterface : subscribers) {
    		serviceRegistryInterface.serviceOperationTimeout(description, opName, params);
    	}
    }
}
