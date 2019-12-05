package tas.mape;

import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.adaptation.probes.ServiceRegistryProbe;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;
import tas.mape.knowledge.Knowledge;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *  
 * Class that represents the monitor component in a MAPE-K component
 */
public class Monitor {
	
	private Knowledge knowledge;
	private Analyzer analyzer;
	private MonitorWorkflowProbe workflowProbe;
	private Boolean analyzerRequired, executed;
	private double minFailureDelta, failureChange;
	
	public Monitor(CompositeService compositeService, Knowledge knowledge, Analyzer analyzer, double minFailureDelta, double failureChange) {
		
		this.knowledge = knowledge;
		this.analyzer = analyzer;
		
		workflowProbe = new MonitorWorkflowProbe();
		
		// Subscribe to workflow probe
		compositeService.getWorkflowProbe().register(workflowProbe);
		
		// Subscribe to workflow cache probe
		compositeService.getCache().getCacheProbe().register(workflowProbe);
		
		// Subscribe to service registry probes
		for (ServiceRegistryProbe serviceRegistryProbe : compositeService.getServiceRegistryProbes()) {
			serviceRegistryProbe.register(workflowProbe);
		}
	}
	
	public void setMinFailureDelta(double minFailureDelta) {
		this.minFailureDelta = minFailureDelta;
	}
	
	public double getMinFailureDelta() {
		return minFailureDelta;
	}
	
	public void setFailureChange(double failureChange) {
		this.failureChange = failureChange;
	}
	
	public double getFailureChange() {
		return failureChange;
	}
	
	public void execute() {
		
		// Update knowledge with new service failure rate information
		CollectAndProcessFailureData();
		
		// Update knowledge with new cache information
		collectAndProcessCacheData();
		
		// Extra: Update cache when registry changes
		collectAndProcessRegistryData();
		
		workflowProbe.reset();
		executed = true;
	}
	
	private void CollectAndProcessFailureData() {
		
		Map<String, Integer> serviceInvocations = workflowProbe.getServiceInvocations();
		Map<String, Integer> serviceFailures = workflowProbe.getServiceFailures();
		
		for (String serviceEndpoint : serviceInvocations.keySet()) {
			
			double approximatedServiceFailureRate = serviceFailures.get(serviceEndpoint) / (double) serviceInvocations.get(serviceEndpoint);
			double currentServiceFailureRate = knowledge.getApproximatedServiceFailureRate(serviceEndpoint, serviceInvocations.get(serviceEndpoint));
			double newServiceFailureRate = getNewFailureRate(approximatedServiceFailureRate, currentServiceFailureRate);
			
			if (newServiceFailureRate != currentServiceFailureRate) {
				
				analyzerRequired = true;
				
				// TODO function that checks whether a failure rate needs to be changed. Give service invocations or previous load? 
				knowledge.setApproximatedServiceFailureRate(serviceEndpoint, serviceInvocations.get(serviceEndpoint), newServiceFailureRate);
			}
		}
	}
	
	private void collectAndProcessRegistryData() {
		
		List<Pair<ServiceDescription, String>> servicesChangedInRegistry = workflowProbe.getServicesChangedInRegistry();
		
		for (Pair<ServiceDescription, String> serviceChange : servicesChangedInRegistry) {
			
			String type = serviceChange.getValue();
			PlanComponent planComponent = null;
			
			if (type == "added") {
				
				planComponent = new PlanComponent(PlanComponentType.ADD_TO_CACHE, serviceChange.getKey());

			} else if (type == "removed") {
				
				planComponent = new PlanComponent(PlanComponentType.ADD_TO_CACHE, serviceChange.getKey());

			} else {
				throw new RuntimeException("Cache service change type is illegal!");
			}	
			
			knowledge.addregistryChangePlanComponents(planComponent);
		}	
	}
	
	private void collectAndProcessCacheData() throws RuntimeException {
		
		List<Pair<Pair<Description, ServiceDescription>, String>> servicesChangedInCache = workflowProbe.getServicesChangedInCache();
		
		for (Pair<Pair<Description, ServiceDescription>, String> serviceChange : servicesChangedInCache) {
			
			String type = serviceChange.getValue();
			
			if (type == "added") {
				knowledge.addUsableService(serviceChange.getKey().getKey(), serviceChange.getKey().getValue());
			} else if (type == "removed") {
				knowledge.removeUsableService(serviceChange.getKey().getKey(), serviceChange.getKey().getValue());
			} else {
				throw new RuntimeException("Cache service change type is illegal!");
			}	
		}
	}
	
	private double getNewFailureRate(double approximatedFailureRate, double currentFailureRate) {
		
		double failureDelta = approximatedFailureRate - currentFailureRate;
		double newFailureRate = currentFailureRate;
		
		if (Math.abs(failureDelta) >= minFailureDelta) {
			
			int deltaCount = (int) (failureDelta / minFailureDelta);
			newFailureRate += deltaCount * failureChange;
		}
		
		return newFailureRate;
	}
	
	public void triggerAnalyzer() {	
		if (analyzerRequired && executed) {
			analyzer.execute();
			executed = false;
		}
	}
	
}
