package tas.mape;

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
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class MonitorWorkflowProbe implements WorkflowProbeInterface, ServiceRegistryProbeInterface, CacheProbeInterface {

	private Map<String, Integer> serviceInvocations, serviceFailures;
	private List<Pair<Pair<Description, ServiceDescription>, String>> servicesChangedInCache;
	private List<Pair<ServiceDescription, String>> servicesChangedInRegistry;
	
	public MonitorWorkflowProbe() {	
		serviceInvocations = new HashMap<String, Integer>();
		serviceFailures = new HashMap<String, Integer>();
	}
	
	public void reset() {
		serviceInvocations.clear();
		serviceFailures.clear();
		servicesChangedInCache.clear();
		servicesChangedInRegistry.clear();
	}
	
	public Map<String, Integer> getServiceInvocations() {
		return serviceInvocations;
	}
	
	public Map<String, Integer> getServiceFailures() {
		return serviceFailures;
	}
	
	public List<Pair<ServiceDescription, String>> getServicesChangedInRegistry() {
		return servicesChangedInRegistry;
	}
	
	public List<Pair<Pair<Description, ServiceDescription>, String>> getServicesChangedInCache() {
		return servicesChangedInCache;
	}

	@Override
	public void workflowEnded(Object result, String qosRequirement, Object[] params) {
	}

	@Override
	public void serviceSucceeded(ServiceDescription description) {
		String serviceEndpoint = description.getServiceEndpoint();
		serviceInvocations.compute(serviceEndpoint, (k, v) -> (v == null) ? 0 : v + 1);
	}

	@Override
	public void serviceFailed(ServiceDescription description) {
		String serviceEndpoint = description.getServiceEndpoint();
		serviceFailures.compute(serviceEndpoint, (k, v) -> (v == null) ? 0 : v + 1);		
		serviceInvocations.compute(serviceEndpoint, (k, v) -> (v == null) ? 0 : v + 1);
	}

	@Override
	public void serviceRemovedFromRegistry(ServiceDescription description) {
		servicesChangedInRegistry.add(new Pair<ServiceDescription, String>(description, "removed"));
	}

	@Override
	public void serviceAddedToRegistry(ServiceDescription description) {	
		servicesChangedInRegistry.add(new Pair<ServiceDescription, String>(description, "added"));
	}
	
	@Override
	public void serviceAddedToCache(Description ServiceTypeOperation, ServiceDescription description) {
		servicesChangedInCache.add(new Pair<Pair<Description, ServiceDescription>, String>(new Pair<Description, ServiceDescription>(ServiceTypeOperation, description), "added"));
	}

	@Override
	public void serviceRemovedFromCache(Description ServiceTypeOperation, ServiceDescription description) {
		servicesChangedInCache.add(new Pair<Pair<Description, ServiceDescription>, String>(new Pair<Description, ServiceDescription>(ServiceTypeOperation, description), "removed"));
	}
	
	@Override
	public void workflowStarted(String qosRequirement, Object[] params) {	
	}
	
	@Override
	public void serviceNotFound(String serviceType, String opName) {
	}
	
	@Override
	public void serviceOperationInvoked(ServiceDescription service, String opName, Object[] params) {
	}

	@Override
	public void serviceOperationTimeout(ServiceDescription service, String opName, Object[] params) {
	}
	
	@Override
	public void serviceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params) {
	}
}
