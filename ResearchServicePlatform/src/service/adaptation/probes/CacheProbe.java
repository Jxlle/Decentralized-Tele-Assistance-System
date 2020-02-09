package service.adaptation.probes;

import service.adaptation.probes.interfaces.CacheProbeInterface;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;

/**
 * Class that monitors changes in the cache
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class CacheProbe extends AbstractProbe<CacheProbeInterface> {

    /**
     * Notify subscribed probes that a service has added to the registry.
	 * @param ServiceTypeOperation the added service description
	 * @param description the service type and operation combination for the added service
     */
    public void notifyServiceAddedToCache(Description ServiceTypeOperation, ServiceDescription description) {
    	for (CacheProbeInterface cacheProbeInterface : subscribers) {
    		cacheProbeInterface.serviceAddedToCache(ServiceTypeOperation, description);
    	}
    }
	
    /**
     * Notify subscribed probes that a service has been removed from the registry.
	 * @param ServiceTypeOperation the removed service description
	 * @param description the service type and operation combination for the removed service
     */
    public void notifyServiceRemovedFromCache(Description ServiceTypeOperation, ServiceDescription description) {
    	for (CacheProbeInterface cacheProbeInterface : subscribers) {
    		cacheProbeInterface.serviceRemovedFromCache(ServiceTypeOperation, description);
    	}
    }
	
}
