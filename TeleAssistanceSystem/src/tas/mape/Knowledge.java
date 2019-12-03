package tas.mape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.util.Pair;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class Knowledge {
	
	private String currentQoSRequirement;
	private Map<Description, Double> servicesUsageChance;
	private Map<String, TreeMap<Integer, Double>> approximatedServiceFailureRates;
	private Map<String, AbstractWorkflowQoSRequirement> QoSRequirementClasses;
	private int workflowCyclesMax, loadFailureDelta;
	
	// TODO ServiceDescriptions must be copy
	private Map<Description, List<ServiceDescription>> usableServices;
	
	// TODO
	public Knowledge(int workflowCyclesMax, int loadFailureDelta, String currentQoSRequirement, Map<String, AbstractWorkflowQoSRequirement> QoSRequirementClasses, Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
		
		this.workflowCyclesMax = workflowCyclesMax;
		this.loadFailureDelta = loadFailureDelta;
		this.currentQoSRequirement = currentQoSRequirement;
		this.QoSRequirementClasses = QoSRequirementClasses;
		
		for (Description description : usableServicesAndChance.keySet()) {
			servicesUsageChance.put(description, usableServicesAndChance.get(description).getValue());
			usableServices.put(description, usableServicesAndChance.get(description).getKey());
		}
		
		initializeApproximatedServiceFailureRates();
	}
	
	public void setCurrentQoSRequirement(String currentQoSRequirement) {
		this.currentQoSRequirement = currentQoSRequirement;
	}
	
	public String getCurrentQoSRequirement() {
		return currentQoSRequirement;
	}
	
	public AbstractWorkflowQoSRequirement getQoSRequirementClass(String requirementName) {
		return QoSRequirementClasses.get(requirementName);
	}
	
	public void setQoSRequirementClass(String requirementName, AbstractWorkflowQoSRequirement requirementClass) {
		QoSRequirementClasses.put(requirementName, requirementClass);
	}
	
	public void setApproximatedServiceFailureRate(String serviceEndpoint, int load, double failureRate) {
		TreeMap<Integer, Double> approximatedFailureRate = approximatedServiceFailureRates.get(serviceEndpoint);
		approximatedFailureRate.put(load, failureRate);
		approximatedServiceFailureRates.put(serviceEndpoint, approximatedFailureRate);
	}
	
	public Double getApproximatedServiceFailureRate(String serviceEndpoint, int load) throws IllegalArgumentException {
		
		if (approximatedServiceFailureRates.get(serviceEndpoint) != null) {
			
			TreeMap<Integer, Double> serviceFailureTable = approximatedServiceFailureRates.get(serviceEndpoint);
			Map.Entry<Integer, Double> entry = serviceFailureTable.ceilingEntry(load);
			
			if (entry == null) {
				serviceFailureTable.floorEntry(load);
			}
						
			return entry.getValue();
		}
		
		throw new IllegalArgumentException("Something went wrong! Service endpoint " + serviceEndpoint + " was not found. \n");
	}
	
	public Map<Description, List<ServiceDescription>> getUsableServices() {
		return usableServices;
	}
	
	public int getServiceLoad(Description description, double usePercentage) {
		return (int) (servicesUsageChance.get(description) * usePercentage * workflowCyclesMax);
	}
	
	private void initializeApproximatedServiceFailureRates() {
		
		// New HashMap
		approximatedServiceFailureRates = new HashMap<>();
		
		// Loop over all service descriptions
		for (List<ServiceDescription> serviceDescriptions : usableServices.values()) {
			for (ServiceDescription serviceDescription : serviceDescriptions) {
				
				// If service description is new
				if (approximatedServiceFailureRates.get(serviceDescription.getServiceEndpoint()) != null) {
					
					// New TreeMap
					approximatedServiceFailureRates.put(serviceDescription.getServiceEndpoint(), new TreeMap<>());
					
					Map<Integer, Double> failureRates = new TreeMap<>();
					double serviceFailureRate = 0;
					
					if (serviceDescription.getCustomProperties().containsKey("FailureRate")) {
						serviceFailureRate = (double) serviceDescription.getCustomProperties().get("FailureRate");
					}		
					
					// Fill failure rate map with the default service failure rate
					for (int i = 0; i < workflowCyclesMax * loadFailureDelta; i++) {
						failureRates.put(i * loadFailureDelta, serviceFailureRate);
					}	
				}
			}
		}
	}
}
