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
 * A class for choosing service combinations in the analyzer step of the MAPE-K loop based on the cost QoS requirement.
 */
public class MinCostReq extends AbstractWorkflowQoSRequirement {
	
	/**
	 * Applies a strategy for choosing the service combinations for the "Cost" requirement with a given combination limit, rating type and service combinations without rating or type
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type
	 * @param goals the given system goals
	 * @param allServiceCombinations the given generated service combinations without rating or type
	 * @return a list of the chosen service combinations
	 */
	public List<ServiceCombination> applyStrategy1 (int combinationLimit, RatingType ratingType, List<Goal> goals, List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations) {
		
		// TODO add goal support
		List<ServiceCombination> chosenServicesList = new ArrayList<>();
		
		List<Integer> indexList = new ArrayList<>();
		List<Double> scoreList = new ArrayList<>();
		
		for (int i = 0; i < allServiceCombinations.size(); i++) {
			
			double combinationScore = (double) getServiceCombinationRating(allServiceCombinations.get(i), RatingType.NUMBER, "Cost", goals);	
			
			int index = Collections.binarySearch(scoreList, combinationScore);
			
			if (index < 0) {
				index = -1 * (Collections.binarySearch(scoreList, combinationScore) + 1);
			}
			
			scoreList.add(index, combinationScore);
			indexList.add(i, index);
			
			for (int i2 = 0; i2 < i; i2++) {
				if (indexList.get(i2) >= index) {
					indexList.set(i2, indexList.get(i2) + 1);
				}
			}
			
			if (i < combinationLimit) {
				chosenServicesList.add(null);
			}	
		}
		
		ServiceCombination chosenServicesEntry;
		Map<Description, WeightedCollection<ServiceDescription>> chosenServicesCombination;
		
		for (int i = 0; i < Math.min(combinationLimit, allServiceCombinations.size()); i++) {
			
			chosenServicesCombination = allServiceCombinations.get(i);
			chosenServicesEntry = new ServiceCombination(chosenServicesCombination, RatingType.NUMBER, scoreList.get(indexList.get(i)));
			chosenServicesList.set(chosenServicesList.size() - indexList.get(i) - 1, chosenServicesEntry);
		}
		
		return chosenServicesList;
	}
}
