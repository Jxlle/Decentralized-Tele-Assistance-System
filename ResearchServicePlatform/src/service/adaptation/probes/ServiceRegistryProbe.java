package service.adaptation.probes;

import service.adaptation.probes.interfaces.ServiceRegistryProbeInterface;
import service.auxiliary.ServiceDescription;

/**
 * @author Jelle van de sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Monitor changes in the service registry
 */
public class ServiceRegistryProbe extends AbstractProbe<ServiceRegistryProbeInterface> {
    
    /**
     * Notify subscribed probes that a service was invoked and succeeded.
     * @param description the invoked service description
     */
    public void notifyServiceSucceeded(ServiceDescription description) {
    	for (ServiceRegistryProbeInterface serviceRegistryInterface : subscribers) {
    		serviceRegistryInterface.serviceSucceeded(description);
    	}
    }
    
    /**
     * Notify subscribed probes that a service has failed.
     * @param description the description of the failed service
     */
    public void notifyServiceFailed(ServiceDescription description) {
    	for (ServiceRegistryProbeInterface serviceRegistryInterface : subscribers) {
    		serviceRegistryInterface.serviceFailed(description);
    	}
    }
    
    /**
     * Notify subscribed probes that a service has been removed from the registry.
     * @param description the description of the removed service
     */
    public void notifyServiceRemovedFromRegistry(ServiceDescription description) {
    	for (ServiceRegistryProbeInterface serviceRegistryInterface : subscribers) {
    		serviceRegistryInterface.serviceRemovedFromRegistry(description);
    	}
    }
    
    /**
     * Notify subscribed probes that a service has added to the registry.
     * @param description the description of the added service
     */
    public void notifyServiceAddedToRegistry(ServiceDescription description) {
    	for (ServiceRegistryProbeInterface serviceRegistryInterface : subscribers) {
    		serviceRegistryInterface.serviceAddedToRegistry(description);
    	}
    }
}
