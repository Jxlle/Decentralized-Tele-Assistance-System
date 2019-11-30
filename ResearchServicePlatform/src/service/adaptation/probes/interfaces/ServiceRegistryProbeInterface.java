package service.adaptation.probes.interfaces;

import service.auxiliary.ServiceDescription;

/**
 * @author Jelle van de sijpe
 *
 * Interface with list of function definitions for a service registry probe
 */
public interface ServiceRegistryProbeInterface {
    
    /**
     * Generates an event when a service is invoked by a workflow and doesn't fail.
     * @param description the invoked service description
     */
    public void serviceSucceeded(ServiceDescription description);

    /**
     * Generates an event when a service invoked by a workflow fails, 
     * that is, the service has not responded within a predefined timeout.
     * @param description the description of the failed service
     */
    public void serviceFailed(ServiceDescription description);
    
    /**
     * Generates an event when a service has been removed from the registry.
     * @param description the removed service description
     */
    public void serviceRemovedFromRegistry(ServiceDescription description);

    /**
     * Generates an event when a service has been added to the registry.
     * @param description the added service description
     */
    public void serviceAddedToRegistry(ServiceDescription description);
}
