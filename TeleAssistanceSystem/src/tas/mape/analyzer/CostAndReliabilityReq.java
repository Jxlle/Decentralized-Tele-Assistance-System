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
 * A class for choosing service combinations in the analyzer step of the MAPE-K loop based on the cost and reliability QoS requirements.
 */
public class CostAndReliabilityReq extends AbstractWorkflowQoSRequirement {
	
	/**
	 * Chooses the service combinations for the cost an reliability requirements with a given combination limit, rating type and service combinations without rating or type
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type
	 * @param goals the given system goals
	 * @param allServiceCombinations the given generated service combinations without rating or type
	 * @return a list of the chosen service combinations
	 * @throws IllegalArgumentException throw when the given rating type has no implementation for the cost requirement
	 */
	@Override
	public List<ServiceCombination> chooseServices(int combinationLimit, RatingType ratingType, List<Goal> goals, List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations) throws IllegalArgumentException {
			
		List<Object> scoreListCost = new ArrayList<>();
		List<Object> scoreListFailureRate = new ArrayList<>();
		List<ServiceCombination> sortedServiceCombinations = new ArrayList<>();
		List<ServiceCombination> sortedServiceCombinationsCost, sortedServiceCombinationsFailureRate;
		
		switch (ratingType) {
		
		case NUMBER:
			// Calculate requirement scores
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreListCost.add((double) calculateNumberRatingCost(allServiceCombinations.get(i)));
				scoreListFailureRate.add((double) calculateNumberRatingFailureRate(allServiceCombinations.get(i)));
			}
			
			// TODO
			sortedServiceCombinationsCost = getSortedServiceCombinations(combinationLimit, ratingType, scoreListCost, allServiceCombinations);
			sortedServiceCombinationsFailureRate = getSortedServiceCombinations(combinationLimit, ratingType, scoreListCost, allServiceCombinations);
			
			// Pre-fill final service combinations list
			for (int i = 0; i < sortedServiceCombinationsCost.size(); i++) {
				sortedServiceCombinations.add(null);
			}
			
			for (int i = 0; i < sortedServiceCombinationsCost.size(); i++) {		
				for (int j = 0; j < sortedServiceCombinationsFailureRate.size(); j++) {
					if (sortedServiceCombinationsCost.get(i).hasSameCollection(sortedServiceCombinationsFailureRate.get(j))) {
						
					}
				}
			}
			
			break;
			
		case CLASS:	
			
			List<Object> totalClassList = new ArrayList<>();
			
			// Calculate requirement scores
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreListCost.add(calculateClassRating(allServiceCombinations.get(i), goals, "Cost"));
				scoreListFailureRate.add(calculateClassRating(allServiceCombinations.get(i), goals, "FailureRate"));
			}
			
			// Calculate total class for each service combination
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				totalClassList.add((int) scoreListCost.get(i) + (int) scoreListFailureRate.get(i));
			}
			
			// Get sorted service combinations
			sortedServiceCombinations = getSortedServiceCombinations(combinationLimit, ratingType, totalClassList, allServiceCombinations);
			
			break;
			
		default:
			throw new IllegalArgumentException("The given rating type " + ratingType + " has no implementation for the cost requirement!");
		
		}
		
		return sortedServiceCombinations;
	}
	
}
