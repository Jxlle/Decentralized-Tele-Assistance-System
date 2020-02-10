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
 * A class for choosing service combinations in the analyzer step of the MAPE-K loop 
 * based on the cost and reliability QoS requirements.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
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
	public List<ServiceCombination> getServiceCombinations(int combinationLimit, 
			RatingType ratingType, List<Goal> goals, List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations)
			throws IllegalArgumentException {
			
		List<Comparable<?>> scoreListCost = new ArrayList<>();
		List<Comparable<?>> scoreListFailureRate = new ArrayList<>();
		List<Comparable<?>> scoreList = new ArrayList<>();
		
		switch (ratingType) {
		
		case SCORE:
			
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
			
			// Final score can be calculated as follows:
			// The above code sorts the service combinations from lowest score to highest for cost and failure rate.
			// That means that the indices sum for cost and requirement together is a valid score.
			// A higher score would mean that you have a high index count for cost + requirement, but because the lists are sorted from lowest to highest,
			// a higher score is a better combination.
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				System.err.print(indexListCost.get(i).doubleValue() + " + " + indexListFailureRate.get(i).doubleValue() + " =  " + (indexListCost.get(i).doubleValue() + indexListFailureRate.get(i).doubleValue()) + " \n");
				scoreList.add(indexListCost.get(i).doubleValue() + indexListFailureRate.get(i).doubleValue());
			}	
			
			break;
			
		case CLASS:	
			
			// Calculate requirement scores
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreListCost.add(getClassRating(goals, getTotalValue(allServiceCombinations.get(i), "Cost"), "Cost"));
				scoreListFailureRate.add(getClassRating(goals, getTotalValue(allServiceCombinations.get(i), "FailureRate"), "FailureRate"));
			}
			
			// Calculate total class for each service combination
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreList.add((int) scoreListCost.get(i) + (int) scoreListFailureRate.get(i));
			}
			
			break;
			
		default:
			throw new IllegalArgumentException("The given rating type " + ratingType + " has no implementation for the requirement!");
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
	 * @throws IllegalArgumentException the given service combination rating type has no implementation 
	 *         for the requirement
	 */
	@Override
	public List<ServiceCombination> getNewServiceCombinations(List<ServiceCombination> serviceCombinations, 
			Map<String, Double> serviceFailureRates, List<Goal> goals) throws IllegalArgumentException {
		
		List<Comparable<?>> scoreListCost = new ArrayList<>();
		List<Comparable<?>> scoreListFailureRate = new ArrayList<>();
		List<Comparable<?>> scoreList = new ArrayList<>();
		
		switch (serviceCombinations.get(0).getRatingType()) {
		
		case SCORE:
			
			// Calculate requirement scores
			for (int i = 0; i < serviceCombinations.size(); i++) {
				scoreListCost.add((double) GetNumberRatingDouble(getTotalValue(serviceCombinations.get(i), "Cost")));
				scoreListFailureRate.add((double) GetNumberRatingDouble(getTotalValue(serviceCombinations.get(i), serviceFailureRates, "FailureRate")));
			}
			
			// Initialize lists
			List<Integer> indexListCost = new ArrayList<>();
			List<Double> sortedScoreListCost = new ArrayList<>();
			List<Integer> indexListFailureRate = new ArrayList<>();
			List<Double> sortedScoreListFailureRate = new ArrayList<>();
			
			// Sort on cost and failure rate from lowest to highest score
			for (int i = 0; i < serviceCombinations.size(); i++) {
				
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
			
			// Final score can be calculated as follows:
			// The above code sorts the service combinations from lowest score to highest for cost and failure rate.
			// That means that the indices sum for cost and requirement together is a valid score.
			// A higher score would mean that you have a high index count for cost + requirement, but because the lists are sorted from lowest to highest,
			// a higher score is a better combination.
			for (int i = 0; i < serviceCombinations.size(); i++) {
				scoreList.add(indexListCost.get(i).doubleValue() + indexListFailureRate.get(i).doubleValue());
			}	
			
			break;
			
		case CLASS:	
			
			// Calculate requirement scores
			for (int i = 0; i < serviceCombinations.size(); i++) {
				scoreListCost.add(getClassRating(goals, getTotalValue(serviceCombinations.get(i), "Cost"), "Cost"));
				scoreListFailureRate.add(getClassRating(goals, getTotalValue(serviceCombinations.get(i), serviceFailureRates, "FailureRate"), "FailureRate"));
			}
			
			// Calculate total class for each service combination
			for (int i = 0; i < serviceCombinations.size(); i++) {
				scoreList.add((int) scoreListCost.get(i) + (int) scoreListFailureRate.get(i));
			}
			
			break;
			
		default:
			throw new IllegalArgumentException("The given service combination rating type " + serviceCombinations.get(0).getRatingType() + " has no implementation for the requirement!");
		}
		
		// Return sorted service combinations based on given values
		return getSortedServiceCombinations(serviceCombinations, scoreList);
	}
	
}
