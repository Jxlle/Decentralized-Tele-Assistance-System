package tas.mape.probes;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.ServiceCombination;

/**
 * Class used to calculate service combination data used to generate graphs
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class ServiceCombinationQoSAnalyzer {
	
	public double getRealServiceCombinationCost(ServiceCombination combination) {
		return AbstractWorkflowQoSRequirement.getTotalValue(combination, "Cost");
	}
	
	public double getRealServiceCombinationFailureRate(ServiceCombination combination, Knowledge knowledge) {
		double totalValue = 0;
		
		for (Description description : combination.getDescriptions()) {
			
			for (ServiceDescription service : combination.getAllServices(description).getItems()) {
				
				if (service.getCustomProperties().containsKey("FailureRate")) {
					
					double useChance = combination.getAllServices(description).getChance(service);	
					
					// TODO
					//totalValue += useChance * knowledge.getServiceUsageChance(description) *   e(service.getServiceEndpoint(), description, useChance);
				}
			}
		}
		
		return totalValue;
	}

}
