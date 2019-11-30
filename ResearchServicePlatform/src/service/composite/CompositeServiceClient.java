package service.composite;

import java.util.List;

import service.client.AbstractServiceClient;

/**
 * 
 * Providing support for remotely accessing a composite service
 * 
 */
public class CompositeServiceClient {

    AbstractServiceClient client;
    
    /**
     * Automatically take care the client endpoint
     * @param serviceEndpoint the service endpoint
     */
    public CompositeServiceClient(String serviceEndpoint){
    	client = new AbstractServiceClient(serviceEndpoint);
    }
    
    /**
     * Note: client endpoint should be unique
     * @param serviceEndpoint the service endpoint
     * @param clientEndpoint the client endpoint
     */
    public CompositeServiceClient(String serviceEndpoint, String clientEndpoint){
    	client = new AbstractServiceClient(serviceEndpoint, clientEndpoint);
    }
    
    /**
     * Invoke related composite service to start a workflow with specific QoS requirements 
     * and initial parameters for the workflow
     * @param qosRequirement the QoS requirements
     * @param params  the initial parameters
     * @return the result 
     */
    public Object invokeCompositeService(String qosRequirement, Object...params){
    	return client.sendRequest("invokeCompositeService", qosRequirement, params);
    }
    
    /**
     * Get list of QoS requirements
     * @return the list of QoS requirement names
     */
    @SuppressWarnings("unchecked")
	public List<String> getQosRequirementNames(){
    	return (List<String>) client.sendRequest("getQosRequirementNames"); 
    }
}
