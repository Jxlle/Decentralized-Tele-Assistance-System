package service.adaptation.probes;

import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.ServiceDescription;

/**
 * 
 * Monitor the start and end of a workflow execution
 * 
 */
public class WorkflowProbe extends AbstractProbe<WorkflowProbeInterface>{
    
	/**
	 * 
	 * @param qosRequirement
	 * @param params
	 */
    public void notifyWorkflowStarted(String qosRequirement, Object[] params){
    	for(WorkflowProbeInterface workflowInterface: subscribers){
    		workflowInterface.workflowStarted(qosRequirement, params);
    	}
    }

    /**
     * 
     * @param result
     * @param qosRequirement
     * @param params
     */
    public void notifyWorkflowEnded(Object result, String qosRequirement, Object[] params){
    	for(WorkflowProbeInterface workflowInterface: subscribers){
    		workflowInterface.workflowEnded(result, qosRequirement, params);
    	}
    }
    
    /**
     * 
     * @param description
     * @param opName
     * @param params
     */
    public void notifyServiceOperationInvoked(ServiceDescription description, String opName, Object[] params){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceOperationInvoked(description, opName, params);
    	}
    }
    
    /**
     * 
     * @param description
     * @param result
     * @param opName
     * @param params
     */
    public void notifyServiceOperationReturned(ServiceDescription description, Object result, String opName, Object[] params){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceOperationReturned(description, result, opName, params);
    	}
    }
    
    /**
     * 
     * @param description
     * @param opName
     * @param params
     */
    public void notifyServiceOperationTimeout(ServiceDescription description, String opName, Object[] params){
    	for (WorkflowProbeInterface workflowInterface : subscribers) {
    		workflowInterface.serviceOperationTimeout(description, opName, params);
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
}
