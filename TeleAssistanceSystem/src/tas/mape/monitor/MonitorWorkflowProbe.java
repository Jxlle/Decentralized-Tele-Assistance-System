package tas.mape.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.adaptation.probes.interfaces.CacheProbeInterface;
import service.adaptation.probes.interfaces.ServiceRegistryProbeInterface;
import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;

/**
 * Class representing the monitor workflow probe for the monitor component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class MonitorWorkflowProbe implements WorkflowProbeInterface, ServiceRegistryProbeInterface, CacheProbeInterface {

	// Fields
	private Map<String, Integer> serviceInvocations, serviceFailures;
	private List<Pair<Pair<Description, ServiceDescription>, String>> servicesChangedInCache;
	private List<Pair<ServiceDescription, String>> servicesChangedInRegistries;
	
	/**
	 * Create a new monitor workflow probe
	 */
	public MonitorWorkflowProbe() {	
		serviceInvocations = new HashMap<String, Integer>();
		serviceFailures = new HashMap<String, Integer>();
	}
	
	/**
	 * Reset the probe
	 */
	public void reset() {
		serviceInvocations.clear();
		serviceFailures.clear();
		servicesChangedInCache.clear();
		servicesChangedInRegistries.clear();
	}
	
	/**
	 * Return the service invocations map
	 * @return the service invocations map
	 */
	public Map<String, Integer> getServiceInvocations() {
		return serviceInvocations;
	}
	
	/**
	 * Return the service failures map
	 * @return the service failures map
	 */
	public Map<String, Integer> getServiceFailures() {
		return serviceFailures;
	}
	
	/**
	 * Return the list with information about service change in the registries
	 * @return the list with information about service change in the registries
	 */
	public List<Pair<ServiceDescription, String>> getServicesChangedInRegistries() {
		return servicesChangedInRegistries;
	}
	
	/**
	 * Return the list with information about service change in the cache
	 * @return the list with information about service change in the cache
	 */
	public List<Pair<Pair<Description, ServiceDescription>, String>> getServicesChangedInCache() {
		return servicesChangedInCache;
	}

	/**
	 * Gets called when a service has succeeded execution
	 */
	@Override
	public void serviceSucceeded(ServiceDescription description) {
		String serviceEndpoint = description.getServiceEndpoint();
		serviceInvocations.compute(serviceEndpoint, (k, v) -> (v == null) ? 0 : v + 1);
	}

	/**
	 * Gets called when a service has failed
	 */
	@Override
	public void serviceFailed(ServiceDescription description) {
		String serviceEndpoint = description.getServiceEndpoint();
		serviceFailures.compute(serviceEndpoint, (k, v) -> (v == null) ? 0 : v + 1);		
		serviceInvocations.compute(serviceEndpoint, (k, v) -> (v == null) ? 0 : v + 1);
	}

	/**
	 * Gets called when a service has been removed from a linked registry
	 */
	@Override
	public void serviceRemovedFromRegistry(ServiceDescription description) {
		servicesChangedInRegistries.add(new Pair<ServiceDescription, String>(description, "removed"));
	}

	/**
	 * Gets called when a service has been added to a linked registry
	 */
	@Override
	public void serviceAddedToRegistry(ServiceDescription description) {	
		servicesChangedInRegistries.add(new Pair<ServiceDescription, String>(description, "added"));
	}
	
	/**
	 * Gets called when a service has been added to the cache
	 */
	@Override
	public void serviceAddedToCache(Description ServiceTypeOperation, ServiceDescription description) {
		servicesChangedInCache.add(new Pair<Pair<Description, ServiceDescription>, String>(new Pair<Description, ServiceDescription>(ServiceTypeOperation, description), "added"));
	}

	/**
	 * Gets called when a service has been removed from the cache
	 */
	@Override
	public void serviceRemovedFromCache(Description ServiceTypeOperation, ServiceDescription description) {
		servicesChangedInCache.add(new Pair<Pair<Description, ServiceDescription>, String>(new Pair<Description, ServiceDescription>(ServiceTypeOperation, description), "removed"));
	}
	
	/**
	 * Not used
	 */
	@Override
	public void workflowStarted(String qosRequirement, Object[] params) {	
	}
	
	/**
	 * Not used
	 */
	@Override
	public void serviceNotFound(String serviceType, String opName) {
	}
	
	/**
	 * Not used
	 */
	@Override
	public void serviceOperationInvoked(ServiceDescription service, String opName, Object[] params) {
	}

	/**
	 * Not used
	 */
	@Override
	public void serviceOperationTimeout(ServiceDescription service, String opName, Object[] params) {
	}
	
	/**
	 * Not used
	 */
	@Override
	public void serviceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params) {
	}
	
	/**
	 * Not used
	 */
	@Override
	public void workflowEnded(Object result, String qosRequirement, Object[] params) {
	}
}
