package tas.mape;

import java.util.Map;

import service.adaptation.probes.ServiceRegistryProbe;
import service.composite.CompositeService;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class Monitor {
	
	private Knowledge knowledge;
	private Analyzer analyzer;
	private MonitorWorkflowProbe workflowProbe;
	
	public Monitor(CompositeService compositeService, Knowledge knowledge, Analyzer analyzer, int workflowCyclesMax) {
		
		this.knowledge = knowledge;
		this.analyzer = analyzer;
		
		workflowProbe = new MonitorWorkflowProbe(workflowCyclesMax);
		workflowProbe.connect(this);
		
		// Subscribe to workflow probe
		compositeService.getWorkflowProbe().register(workflowProbe);
		
		// Subscribe to service registry probes
		for (ServiceRegistryProbe serviceRegistryProbe : compositeService.getServiceRegistryProbes()) {
			serviceRegistryProbe.register(workflowProbe);
		}
		
	}
	
	
	public void execute() {
		
		Map<String, Integer> serviceInvocations = workflowProbe.getServiceInvocations();
		Map<String, Integer> serviceFailures = workflowProbe.getServiceFailures();
		
		for (String serviceEndpoint : serviceInvocations.keySet()) {
			double approximatedServiceFailureRate = serviceFailures.get(serviceEndpoint) / (double) serviceInvocations.get(serviceEndpoint);
			knowledge.setApproximatedServiceFailureRate(serviceEndpoint, serviceInvocations.get(serviceEndpoint), approximatedServiceFailureRate);
		}
		
		// TODO Check changes in cache and service registry
		
		workflowProbe.reset();
		
	}
	
	private Boolean analyzerRequired() {
		// TODO
		return true;
	}
	
	public void triggerAnalyzer() {	
		if (analyzerRequired()) {
			analyzer.execute();
		}
	}
	
}
