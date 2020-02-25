package tas.mape.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * A class for choosing service combinations in the analyzer step of the MAPE-K loop 
 * based on the reliability QoS requirement.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class ReliabilityReq extends AbstractWorkflowQoSRequirement {
	
	// Used property for all methods
	static final String usedProperty = "FailureRate";
	
	/**
	 * Choose the service combinations for the cost an reliability requirements with a given combination limit, 
	 * rating type, knowledge and service combinations without rating or type
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type
	 * @param knowledge the given knowledge
	 * @param allServiceCombinations the given generated service combinations without rating or type
	 * @return a list of the chosen service combinations
	 * @throws IllegalArgumentException throw when the given rating type has no implementation for the requirement
	 */
	@Override
	public List<ServiceCombination> getServiceCombinations(int combinationLimit, RatingType ratingType, Knowledge knowledge, 
			List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations)
			throws IllegalArgumentException {
			
		List<Comparable<?>> scoreList = new ArrayList<>();
		
		switch (ratingType) {
		
		case SCORE:	
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreList.add(GetNumberRatingDouble(getTotalApproximatedFailureRateValue(allServiceCombinations.get(i), knowledge)));	
			}
			
			break;
			
		case CLASS:	
			for (int i = 0; i < allServiceCombinations.size(); i++) {
				scoreList.add(getClassRating(knowledge.getGoals(), 
						getTotalApproximatedFailureRateValue(allServiceCombinations.get(i), knowledge), usedProperty));	
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
	 * Re-rank the given service combinations with a given map of service failure rates and given knowledge
	 * @param serviceCombinations the given service combinations
	 * @param serviceFailureRates the given map of service failure rates
	 * @param knowledge the given knowledge
	 * @return the new service combinations
	 * @throws IllegalArgumentException the given service combination rating type has no implementation 
	 *         for the requirement
	 */
	@Override
	public List<ServiceCombination> getNewServiceCombinations(List<ServiceCombination> serviceCombinations, 
			Map<String, Double> serviceFailureRates, Knowledge knowledge) {
		
		List<Comparable<?>> scoreList = new ArrayList<>();
		
		switch (serviceCombinations.get(0).getRatingType()) {
		
		case SCORE:	
			for (int i = 0; i < serviceCombinations.size(); i++) {
				scoreList.add(GetNumberRatingDouble(getTotalApproximatedFailureRateValue(serviceCombinations.get(i), serviceFailureRates, knowledge)));
			}
			
			break;
			
		case CLASS:	
			for (int i = 0; i < serviceCombinations.size(); i++) {
				scoreList.add(getClassRating(knowledge.getGoals(), 
						getTotalApproximatedFailureRateValue(serviceCombinations.get(i), serviceFailureRates, knowledge), usedProperty));	
			}
			
			break;
			
		default:
			throw new IllegalArgumentException("The given service combination rating type " + serviceCombinations.get(0).getRatingType() + " has no implementation for the requirement!");
		
		}
		
		// Return sorted service combinations based on given values
		return getSortedServiceCombinations(serviceCombinations, scoreList);
	}
}
