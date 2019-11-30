package service.adaptation.probes;

import service.adaptation.probes.interfaces.ServiceRegistryProbeInterface;
import service.auxiliary.ServiceDescription;

/**
 * @author Jelle van de sijpe
 *
 * Monitor changes in the service registry
 */
public class ServiceRegistryProbe extends AbstractProbe<ServiceRegistryProbeInterface>{
    
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
     * Notify subscribed probed that a service has failed.
     * @param description the description of the failed service
     */
    public void notifyServiceFailed(ServiceDescription description) {
    	for (ServiceRegistryProbeInterface serviceRegistryInterface : subscribers) {
    		serviceRegistryInterface.serviceFailed(description);
    	}
    }
}
