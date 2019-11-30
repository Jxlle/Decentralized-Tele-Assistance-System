package service.adaptation.probes.interfaces;

import service.auxiliary.ServiceDescription;


/**
 * 
 * Interface with list of function definitions for workflow probe
 */
public interface WorkflowProbeInterface {

	/**
	 * This event is triggered when a request has been made to composite service to start the execution of the workflow.
	 * @param qosRequirement the QoS requirements for executing the current workflow
	 * @param params initial parameters for the current workflow
	 */
    public void workflowStarted(String qosRequirement, Object[] params);

    /**
     * Generates an event when a service operation is invoked by the workflow.
     * @param service the invoked service description
     * @param opName the invoked operation name
     * @param params the parameters of the invoked operation
     */
    public void serviceOperationInvoked(ServiceDescription service, String opName, Object[] params);

    /**
     * Generates an event when a service operation invoked by the workflow returns successfully.
     * @param service the invoked service description
     * @param result the result after the operation invoked
     * @param opName the invoked operation name
     * @param params the parameters of the invoked operation
     */
    public void serviceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params);

    /**
     * Generates an event when a service operation invoked by the workflow fails, 
     * that is, the service has not responded within a predefined timeout.
     * @param service the invoked service description
     * @param opName the invoked operation name
     * @param params the parameters of the invoked operation
     */
    public void serviceOperationTimeout(ServiceDescription service, String opName, Object[] params);

    /**
     * Generates an event when the workflow cannot find a concrete service for the given service type and operation.
     * @param serviceType the not found service type
     * @param opName the not found service operation name
     */
    public void serviceNotFound(String serviceType, String opName);

    /**
     * When a workflow finish execution, this event is triggered.
     * @param result  the result after executing the current workflow
     * @param qosRequirement the QoS requirements for executing the current workflow
     * @param params initial parameters for the current workflow
     */
    public void workflowEnded(Object result, String qosRequirement, Object[] params);

}
