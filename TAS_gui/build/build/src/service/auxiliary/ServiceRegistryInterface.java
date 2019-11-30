package service.auxiliary;

import java.util.List;


/**
 * 
 * Interface with a list of function definitions for service registry
 */

public interface ServiceRegistryInterface {
	/**
	 * The name
	 */
    public static final String NAME = "ServiceRegistry";
    
    /**
     * The endpoint
     */
    public static final String ADDRESS = "se.lnu.service.registry";
    
    /**
     * Register a service with its description
     * @param serviceDescription the service description
     * @return the register id
     */
    public int register(ServiceDescription serviceDescription);
    
    /**
     * Unregister a service with its register id
     * @param registerId the unique register id
     */
    public void unRegister(int registerId);
    
    /**
     * Look up list of services with service type and operation
     * @param serviceType the service type
     * @param opName the operation name
     * @return list of found service descriptions
     */
    public List<ServiceDescription> lookup(String serviceType, String opName);
}
