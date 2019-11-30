package tas.mape;

import java.util.HashMap;
import java.util.Map;

import service.adaptation.probes.interfaces.ServiceRegistryProbeInterface;
import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.ServiceDescription;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class MonitorWorkflowProbe implements WorkflowProbeInterface, ServiceRegistryProbeInterface {

	private Map<String, Integer> serviceInvocations;
	private Map<String, Integer> serviceFailures;
	
	public MonitorWorkflowProbe() {	
		serviceInvocations = new HashMap<String, Integer>();
		serviceFailures = new HashMap<String, Integer>();
	}
	
	public void reset() {
		serviceInvocations.clear();
		serviceFailures.clear();
	}
	
	public Map<String, Integer> getServiceInvocations() {
		return serviceInvocations;
	}
	
	public Map<String, Integer> getServiceFailures() {
		return serviceFailures;
	}
	
	@Override
	public void workflowStarted(String qosRequirement, Object[] params) {
		
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
		// TODO maybe? Mape loop can reconfigure cache if a registry change happens
	}

	@Override
	public void serviceAddedToRegistry(ServiceDescription description) {	
		// TODO maybe? Mape loop can reconfigure cache if a registry change happens
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
