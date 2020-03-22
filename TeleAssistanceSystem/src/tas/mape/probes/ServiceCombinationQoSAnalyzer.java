package tas.mape.probes;

import java.util.HashMap;
import java.util.List;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.StaticTree;
import tas.data.serviceinfo.GlobalServiceInfo;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.ServiceCombination;
import tas.services.profiles.ServiceFailureLoadProfile;

/**
 * Class used to calculate service combination data used to generate graphs
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class ServiceCombinationQoSAnalyzer {
	
	// Local map used for storing failure rates
	private HashMap<Description, Double> failureRates;
	
	/**
	 * Calculate and return the total cost of the given service combination
	 * @param combination the given service combination
	 * @return the total cost of the given service combination
	 */
	public double getRealServiceCombinationCost(ServiceCombination combination) {
		return AbstractWorkflowQoSRequirement.getTotalValue(combination, "Cost");
	}
	
	/**
	 * Calculate and return the real failure rate of the given service combination based on the given knowledge
	 * @param combination the given service combination
	 * @param knowledge the given knowledge
	 * @return the real failure rate of the given service combination
	 * @throws IllegalStateException throws when the required profile 'ServiceFailureLoadProfile' was not found
	 */
	public double getRealServiceCombinationFailureRate(ServiceCombination combination, Knowledge knowledge) throws IllegalStateException {
		
		failureRates = new HashMap<>();
		double totalValue = 0;
		
		// Get workflow service tree from knowledge
		StaticTree<Description> tree = knowledge.getWorkflowServiceTree();
		
		// Initialize simple failure rates per description
		for (Description description : combination.getDescriptions()) {	
			setSimpleServiceFailureRate(combination, description);
		}
		
		// Calculate full failure rate
		for (Description description : combination.getDescriptions()) {	
			double subValue = failureRates.get(description) * knowledge.getServiceUsageChance(description);
			List<Description> workflowServiceFlow = tree.findNodePath(description);
			
			for (Description desc : workflowServiceFlow) {			
				if (!description.equals(desc)) {
					subValue *= 1 - failureRates.get(desc);
				}
			}
			
			totalValue += subValue;
		}
		
		return totalValue;
	}
	
	/**
	 * Calculate and set the simple service failure rate for a given description (= service type & operation name) with a given service combination
	 * and description. This failure rate does not take the service flow of the workflow into account. The full accurate failure rate is calculated above. 
	 * @param combination the given service combination
	 * @param description the given description
	 * @throws IllegalStateException throws when the failure rate map already contains description data
	 * @throws IllegalStateException throws when the required profile 'ServiceFailureLoadProfile' was not found
	 */
	private void setSimpleServiceFailureRate(ServiceCombination combination, Description description) throws IllegalStateException {
		
		if (failureRates.containsKey(description)) {
			throw new IllegalStateException("The failure rate map already contains description data!");
		}
		
		double totalValue = 0;
		
		for (ServiceDescription service : combination.getAllServices(description).getItems()) {
			if (service.getCustomProperties().containsKey("FailureRate")) {
				
				// Find service failure load profile to get the real failure rate
				ServiceFailureLoadProfile profile = 
						(ServiceFailureLoadProfile) GlobalServiceInfo.getService(service.getServiceName())
				.getServiceProfiles().stream().filter(x -> x.getClass().equals(ServiceFailureLoadProfile.class)).findAny().orElse(null);
				
				if (profile == null) {
					throw new IllegalStateException("The required profile 'ServiceFailureLoadProfile' was not found!");
				}
				
				// The failure rate (not taking services that activate this service into account) is calculated as follows:
				//
				// failure rate = chance that this service for this service type is used 
				//            * actual fail rate (= 1 - (success rate * profile success rate multiplier (depends on the service load)))
				double useChance = combination.getAllServices(description).getChance(service);	
				//System.err.println("FAIL CALC " + useChance + " " + (1 - (1 - (double) service.getCustomProperties().get("FailureRate")) * profile.getTableEntry(service).getValue()) + " " + profile.getTableEntry(service).getValue());
				totalValue += useChance * (1 - (1 - (double) service.getCustomProperties().get("FailureRate")) * profile.getTableEntry(service).getValue());
			}
		}
		
		failureRates.put(description, totalValue);
	}

}
