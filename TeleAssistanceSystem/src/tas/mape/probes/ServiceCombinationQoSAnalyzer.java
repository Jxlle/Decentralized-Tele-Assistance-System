package tas.mape.probes;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
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
		double totalValue = 0;
		
		for (Description description : combination.getDescriptions()) {
			
			for (ServiceDescription service : combination.getAllServices(description).getItems()) {
				
				if (service.getCustomProperties().containsKey("FailureRate")) {
					
					// Find service failure load profile to get the real failure rate
					ServiceFailureLoadProfile profile = 
							(ServiceFailureLoadProfile) GlobalServiceInfo.getService(service.getServiceName())
					.getServiceProfiles().stream().filter(x -> x.getClass().equals(ServiceFailureLoadProfile.class)).findAny().orElse(null);
					
					if (profile == null) {
						throw new IllegalStateException("The required profile 'ServiceFailureLoadProfile' was not found!");
					}
					
					// Calculate current service failure rate value and add to the current value
					double useChance = combination.getAllServices(description).getChance(service);	
					totalValue += useChance * knowledge.getServiceUsageChance(description) * profile.getTableEntry(service).getValue();
				}
			}
		}
		
		return totalValue;
	}

}
