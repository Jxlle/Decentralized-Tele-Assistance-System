package service.adaptation.effectors;

import java.util.List;

import service.auxiliary.Operation;
import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;
import service.composite.SDCache;

/**
 * 
 * Responsible for changing cache
 * 
 */
public class CacheEffector {

    private CompositeService compositeService;
  
    /**
     * Constructor
     * @param compositeService which composite service to be affected
     */
    public CacheEffector(CompositeService compositeService) {
    	this.compositeService = compositeService;
    }

    /**
     * Remove service from cache
     * @param description the service description
     */
    public void removeService(ServiceDescription description) {
	    compositeService.getCache().remove(description);
    }
    
    /**
     * Remove service from cache with register id
     * @param registerID the unique register id
     */
    public void removeService(int registerID) {
	    compositeService.getCache().remove(registerID);
    }

    /**
     * Remove service from cache with service description and operation name
     * @param description the service description
     * @param opName the operation name
     */
    public void removeService(ServiceDescription description, String opName) {
    	compositeService.getCache().remove(description, opName);
    }

    /**
     * Refresh the cache
     * all services will be removed.
     */
    public void refreshCache() {
    	compositeService.getCache().refresh();
    }
    
    /**
     * Return all services with same type and operation
     * @param serviceType the service type
     * @param opName the operation name
     */
    public void getAllServices(String serviceType, String opName){
    	compositeService.getCache().remove(serviceType, opName);
    	compositeService.lookupService(serviceType, opName);
    }

    /**
     * Remove all services with same service description and operation
     * @param description the service description
     * @param opName the operation name
     * @return a list of service descriptions after refreshing
     */
    public List<ServiceDescription> refreshCache(ServiceDescription description, String opName) {
    	removeService(description, opName);
    	return compositeService.lookupService(description.getServiceType(), opName);
    }

    /**
     * Update service description
     * @param oldDescription the old service description
     * @param newDescription the new service description
     */
    public void updateServiceDescription(ServiceDescription oldDescription, ServiceDescription newDescription) {
    	if (oldDescription.getRegisterID() == newDescription.getRegisterID()) {
    		for (Operation operation : oldDescription.getOperationList())
    			compositeService.getCache().update(oldDescription, newDescription, operation.getOpName());
    	}
    }

    /**
     * Returns a service description by its registration id
     * @param registerID registerId the service register id
     * @return the service description
     */
    public ServiceDescription getService(int registerID){
	return compositeService.getCache().getServiceDescription(registerID);
    }
}
