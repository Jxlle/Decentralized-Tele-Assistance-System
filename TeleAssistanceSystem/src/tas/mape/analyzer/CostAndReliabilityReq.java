package tas.mape.analyzer;

import java.util.ArrayList;
import java.util.Collections;
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
 * based on the cost and reliability QoS requirements.
 */
public class CostAndReliabilityReq extends AbstractWorkflowQoSRequirement {
	
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
	public List<ServiceCombination> getServiceCombinations(int combinationLimit, RatingType ratingType, List<Goal> goals, List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations) throws IllegalArgumentException {
			
		List<Object> scoreListCost = new ArrayList<>();
		List<Object> scoreListFailureRate = new ArrayList<>();
		List<ServiceCombination> sortedServiceCombinations = new ArrayList<>();
		
		switch (ratingType) {
		
		case NUMBER:
			
			// Calculate requirement scores
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreListCost.add((double) GetNumberRatingDouble(getTotalValue(allServiceCombinations.get(i), "Cost")));
				scoreListFailureRate.add((double) GetNumberRatingDouble(getTotalValue(allServiceCombinations.get(i), "FailureRate")));
			}
			
			// Initialize lists
			List<Integer> indexListCost = new ArrayList<>();
			List<Double> sortedScoreListCost = new ArrayList<>();
			List<Integer> indexListFailureRate = new ArrayList<>();
			List<Double> sortedScoreListFailureRate = new ArrayList<>();
			
			// Sort on cost and failure rate from lowest to highest score
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				
				double combinationScoreCost = (double) scoreListCost.get(i);
				double combinationScoreFailureRate = (double) scoreListFailureRate.get(i);
				
				int indexCost = Collections.binarySearch(sortedScoreListCost, combinationScoreCost);
				int indexFailureRate = Collections.binarySearch(sortedScoreListFailureRate, combinationScoreFailureRate);
				
				if (indexCost < 0) {
					indexCost = -1 * (Collections.binarySearch(sortedScoreListCost, combinationScoreCost) + 1);
				}
				
				if (indexFailureRate < 0) {
					indexFailureRate = -1 * (Collections.binarySearch(sortedScoreListFailureRate, combinationScoreFailureRate) + 1);
				}
				
				sortedScoreListCost.add(indexCost, combinationScoreCost);
				indexListCost.add(i, indexCost);
				sortedScoreListFailureRate.add(indexFailureRate, combinationScoreFailureRate);
				indexListFailureRate.add(i, indexFailureRate);
				
				for (int i2 = 0; i2 < i; i2++) {
					if (indexListCost.get(i2) >= indexCost) {
						indexListCost.set(i2, indexListCost.get(i2) + 1);
					}
					
					if (indexListFailureRate.get(i2) >= indexFailureRate) {
						indexListFailureRate.set(i2, indexListFailureRate.get(i2) + 1);
					}
				}
			}
			
			List<Object> unsortedFinalScoreList = new ArrayList<>();
			
			// Final score can be calculated as follows:
			// The above code sorts the service combinations from lowest score to highest for cost and failure rate.
			// That means that the indices sum for cost and requirement together is a valid score.
			// A higher score would mean that you have a high index count for cost + requirement, but because the lists are sorted from lowest to highest,
			// a higher score is a better combination.
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				unsortedFinalScoreList.add(indexListCost.get(i).doubleValue() + indexListFailureRate.get(i).doubleValue());
			}	
			
			// Get sorted service combinations
			sortedServiceCombinations = getSortedServiceCombinations(combinationLimit, ratingType, unsortedFinalScoreList, allServiceCombinations);
			
			break;
			
		case CLASS:	
			
			List<Object> unsortedClassList = new ArrayList<>();
			
			// Calculate requirement scores
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreListCost.add(getClassRating(goals, getTotalValue(allServiceCombinations.get(i), "Cost"), "Cost"));
				scoreListFailureRate.add(getClassRating(goals, getTotalValue(allServiceCombinations.get(i), "FailureRate"), "FailureRate"));
			}
			
			// Calculate total class for each service combination
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				unsortedClassList.add((int) scoreListCost.get(i) + (int) scoreListFailureRate.get(i));
			}
			
			// Get sorted service combinations
			sortedServiceCombinations = getSortedServiceCombinations(combinationLimit, ratingType, unsortedClassList, allServiceCombinations);
			
			break;
			
		default:
			throw new IllegalArgumentException("The given rating type " + ratingType + " has no implementation for the requirement!");
		
		}
		
		return sortedServiceCombinations;
	}

	/**
	 * Re-rank the given service combinations with a given map of service failure rates
	 * @param serviceCombinations the given service combinations
	 * @param serviceFailureRates the given map of service failure rates
	 * @return the new service combinations
	 */
	@Override
	public List<ServiceCombination> getNewServiceCombinations(List<ServiceCombination> serviceCombinations, 
			Map<String, Double> serviceFailureRates) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
