package service.adaptation.probes.interfaces;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;

/**
 * Interface with list of function definitions for a cache probe
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public interface CacheProbeInterface {
	
	/**
	 * Generates an event when a service has been removed from the cache.
	 * @param ServiceTypeOperation the added service description
	 * @param description the service type and operation combination for the added service
	 */
	public void serviceAddedToCache(Description ServiceTypeOperation, ServiceDescription description);
	
	/**
	 * Generates an event when a service has been added to the cache.
	 * @param ServiceTypeOperation the removed service description
	 * @param description the service type and operation combination for the removed service
	 */
	public void serviceRemovedFromCache(Description ServiceTypeOperation, ServiceDescription description);
}
