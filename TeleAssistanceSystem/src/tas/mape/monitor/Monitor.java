package tas.mape.monitor;

import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.adaptation.probes.ServiceRegistryProbe;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;
import tas.mape.analyzer.Analyzer;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.PlanComponent;
import tas.mape.planner.PlanComponentType;

/**
 * Class that represents the monitor component in a MAPE-K component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class Monitor {
	
	// Fields
	private Knowledge knowledge;
	private Analyzer analyzer;
	private MonitorWorkflowProbe workflowProbe;
	private boolean executed;
	private double minFailureDelta, failureChange;
	
	/**
	 * Create a new monitor with a given knowledge, analyzer, minimum failure delta and failure change
	 * @param knowledge the given knowledge
	 * @param analyzer the given analyzer
	 * @param minFailureDelta the minimum amount of difference needed in failure rate between the current approximated failure rate and
	 * 						  the real failure rate before the approximated failure rate is updated
	 * @param failureChange the value increment used for updating approximated failure rates
	 */
	public Monitor(Knowledge knowledge, Analyzer analyzer, double minFailureDelta, double failureChange) {		
		this.knowledge = knowledge;
		this.analyzer = analyzer;	
		this.minFailureDelta = minFailureDelta;
		this.failureChange = failureChange;
		workflowProbe = new MonitorWorkflowProbe();
	}
	
	/**
	 * Connect monitor probes to the given composite service
	 * @param compositeService the given composite service
	 */
	public void connectProbes(CompositeService compositeService) {
		
		// Subscribe to workflow probe
		compositeService.getWorkflowProbe().register(workflowProbe);
		
		// Subscribe to workflow cache probe
		compositeService.getCache().getCacheProbe().register(workflowProbe);
		
		// Subscribe to service registry probes
		for (ServiceRegistryProbe serviceRegistryProbe : compositeService.getServiceRegistryProbes()) {
			serviceRegistryProbe.register(workflowProbe);
		}
	}
	
	/**
	 * Reset the monitor probes
	 */
	public void resetProbes() {
		workflowProbe.reset();
	}
	
	/**
	 * Set minimum failure delta to the given value
	 * @param minFailureDelta the new minimum failure delta
	 */
	public void setMinFailureDelta(double minFailureDelta) {
		this.minFailureDelta = minFailureDelta;
	}
	
	/**
	 * Return the minimum failure delta
	 * @return the minimum failure delta
	 */
	public double getMinFailureDelta() {
		return minFailureDelta;
	}
	
	/**
	 * Set failure change to the given value
	 * @param failureChange the new failure change
	 */
	public void setFailureChange(double failureChange) {
		this.failureChange = failureChange;
	}
	
	/**
	 * Return the failure change
	 * @return the failure change
	 */
	public double getFailureChange() {
		return failureChange;
	}
	
	/**
	 * Execute the monitor
	 */
	public void execute() {
		
		// Update knowledge with new service failure rate information
		CollectAndProcessFailureData();
		
		// Update knowledge with new cache information
		collectAndProcessCacheData();
		
		// Extra: Update cache when registry changes
		collectAndProcessRegistryData();
		
		knowledge.increaseSystemCycle();
		workflowProbe.reset();
		executed = true;
	}
	
	/**
	 * Trigger the analyzer when the monitor has been executed and when the analyzer is needed.
	 */
	public void triggerAnalyzer() {	
		if (executed) {
			//System.err.print("Executing analyzer...\n");
			analyzer.execute();
			executed = false;
		}
	}
	
	/**
	 * Collect and process service failure rate data and update the knowledge with the new data
	 */
	private void CollectAndProcessFailureData() {
		
		Map<String, Integer> serviceInvocations = workflowProbe.getServiceInvocations();
		Map<String, Integer> serviceFailures = workflowProbe.getServiceFailures();
		
		// Loop over each service endpoint in the service invocations map
		for (String serviceEndpoint : serviceInvocations.keySet()) {
			
			//System.err.print("System invocations: " + serviceInvocations.get(serviceEndpoint) + ", service failures: " + serviceFailures.get(serviceEndpoint));
			
			double approximatedServiceFailureRate = 0;
			
			if (serviceInvocations.get(serviceEndpoint) != 0) {
				approximatedServiceFailureRate = serviceFailures.get(serviceEndpoint) / (double) serviceInvocations.get(serviceEndpoint);
			}			
			
			double currentServiceFailureRate = knowledge.getApproximatedServiceFailureRate(serviceEndpoint, serviceInvocations.get(serviceEndpoint));
			double newServiceFailureRate = getNewFailureRate(approximatedServiceFailureRate, currentServiceFailureRate);
			
			System.err.print("MONITOR UPDATE " + knowledge.getParentEntityName() + " " + serviceEndpoint + " " + approximatedServiceFailureRate + " " + newServiceFailureRate + " " + currentServiceFailureRate + "\n");
			
			if (newServiceFailureRate != currentServiceFailureRate) {			
				knowledge.setApproximatedServiceFailureRate(serviceEndpoint, serviceInvocations.get(serviceEndpoint), newServiceFailureRate);
			}
		}
	}
	
	/**
	 * Collect and process registry change data and update the knowledge with the new data
	 */
	private void collectAndProcessRegistryData() {
		
		List<Pair<ServiceDescription, String>> servicesChangedInRegistry = workflowProbe.getServicesChangedInRegistries();
		
		// Loop over each pair in the probe data
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
			
			// Add special plan component to the knowledge to execute later
			knowledge.addCachePlanComponents(planComponent);
		}	
	}
	
	/**
	 * Collect and process cache change data and update the knowledge with the new data
	 */
	private void collectAndProcessCacheData() throws RuntimeException {
		
		List<Pair<Pair<Description, ServiceDescription>, String>> servicesChangedInCache = workflowProbe.getServicesChangedInCache();
		
		// Loop over each pair in the probe data
		for (Pair<Pair<Description, ServiceDescription>, String> serviceChange : servicesChangedInCache) {
			
			String type = serviceChange.getValue();
			
			if (type == "added") {
				knowledge.addUsedService(serviceChange.getKey().getKey(), serviceChange.getKey().getValue());
			} else if (type == "removed") {
				knowledge.removeUsedService(serviceChange.getKey().getKey(), serviceChange.getKey().getValue());
			} else {
				throw new RuntimeException("Cache service change type is illegal!");
			}	
		}
	}
	
	/**
	 * Calculate the new failure rate with a given approximated failure rate and current failure rate
	 * @param approximatedFailureRate the given approximated failure rate
	 * @param currentFailureRate the given current failure rate
	 * @return the new failure rate
	 */
	private double getNewFailureRate(double approximatedFailureRate, double currentFailureRate) {
		
		double failureDelta = approximatedFailureRate - currentFailureRate;
		double newFailureRate = currentFailureRate;
		
		//System.err.print("FAILURE DELTA " + failureDelta + " " + minFailureDelta + "\n");
		
		if (Math.abs(failureDelta) >= minFailureDelta) {
			
			int deltaCount = (int) Math.round(failureDelta / (double) minFailureDelta);
			newFailureRate += deltaCount * failureChange;
		}
		
		return newFailureRate;
	}	
}
