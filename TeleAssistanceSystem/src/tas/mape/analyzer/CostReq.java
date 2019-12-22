package tas.mape.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.knowledge.Goal;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * A class for choosing service combinations in the analyzer step of the MAPE-K loop 
 * based on the cost QoS requirement.
 */
public class CostReq extends AbstractWorkflowQoSRequirement {
	
	// Used property for all methods
	static final String usedProperty = "Cost";
	
	/**
	 * Chooses the service combinations for the cost an reliability requirements with a given combination limit, 
	 * rating type and service combinations without rating or type
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type
	 * @param goals the given system goals
	 * @param allServiceCombinations the given generated service combinations without rating or type
	 * @return a list of the chosen service combinations
	 * @throws IllegalArgumentException throw when the given rating type has no implementation for the requirement
	 */
	@Override
	public List<ServiceCombination> getServiceCombinations(int combinationLimit, 
			RatingType ratingType, List<Goal> goals, List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations)
			throws IllegalArgumentException {
			
		List<Comparable<?>> scoreList = new ArrayList<>();
		
		switch (ratingType) {
		
		case NUMBER:	
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreList.add(GetNumberRatingDouble(getTotalValue(allServiceCombinations.get(i), usedProperty)));	
			}
			
			break;
			
		case CLASS:	
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreList.add(getClassRating(goals, getTotalValue(allServiceCombinations.get(i), usedProperty), usedProperty));	
			}
			
			break;
			
		default:
			throw new IllegalArgumentException("The given rating type " + ratingType + 
					" has no implementation for the requirement!");
		
		}
		
		// Return sorted service combinations
		return getSortedServiceCombinations(combinationLimit, ratingType, scoreList, allServiceCombinations);
	}

	/**
	 * Re-rank the given service combinations with a given map of service failure rates and given system goals
	 * @param serviceCombinations the given service combinations
	 * @param serviceFailureRates the given map of service failure rates
	 * @param goals the given system goals
	 * @return the new service combinations
	 */
	@Override
	public List<ServiceCombination> getNewServiceCombinations(List<ServiceCombination> serviceCombinations, 
			Map<String, Double> serviceFailureRates, List<Goal> goals) {
		
		return serviceCombinations;
	}
}
