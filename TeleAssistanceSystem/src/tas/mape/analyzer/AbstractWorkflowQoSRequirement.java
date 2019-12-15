package tas.mape.analyzer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * An abstract class for choosing service combinations in the analyzer step of the MAPE-K loop based on a QoS requirement and strategy.
 */
public abstract class AbstractWorkflowQoSRequirement {
	
	/**
	 * Apply a QoS requirement strategy with a given strategy, combination limit, rating type, list of goals and a map of usable services.
	 * @param strategy a pair where the first element represents the service combination strategy and the second number the general requirement strategy
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type 
	 * @param goals the given system goals
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return a list of the chosen service combinations
	 * @throws IllegalArgumentException throw when the combination limit is illegal
	 */
	@SuppressWarnings("unchecked")
	public List<ServiceCombination> applyStrategy(Pair<Integer, Integer> strategy, int combinationLimit, RatingType ratingType, List<Goal> goals, Map<Description, List<ServiceDescription>> usableServices) throws IllegalArgumentException {
		
		// Check if given combination limit is legal
		if (combinationLimit < 1) {
			throw new IllegalArgumentException("Combination limit must be at least 1.");
		}
		
		Method method = null;
		List<Map<Description, WeightedCollection<ServiceDescription>>> generatorResult = null;
		
		// Search service combination generator 
		try {
			method = this.getClass().getMethod("getAllServiceCombinations" + strategy.getKey(), Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		// Invoke method
		try {
			generatorResult = (List<Map<Description, WeightedCollection<ServiceDescription>>>) method.invoke(this, usableServices);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}	

		List<ServiceCombination> endResult = null;
		
		// Search strategy method
		try {
			method = this.getClass().getMethod("applyStrategy" + strategy.getValue(), int.class, RatingType.class, List.class, List.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		// Invoke method
		try {
			endResult = (List<ServiceCombination>) method.invoke(this, combinationLimit, ratingType, goals, generatorResult);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		// return service combinations
		return endResult;
	}
	
	/**
	 * Generate and return all possible service combinations without rating or type
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return the generated service combinations without rating or type
	 */
	public List<Map<Description, WeightedCollection<ServiceDescription>>> getAllServiceCombinations1(Map<Description, List<ServiceDescription>> usableServices) {
		
		//--------------------------------------------
		// Create a List<List<.>> from the given Map
		//--------------------------------------------
		
		List<List<ServiceDescription>> usableServicesList = new ArrayList<>();
		List<Description> descriptionList = new ArrayList<>();
		
		for (Description description : usableServices.keySet()) {
			descriptionList.add(description);
			usableServicesList.add(usableServices.get(description));
		}
		
		
		//--------------------------------------------
		// Begin the combination process
		//--------------------------------------------
		
		List<ServiceDescription[]> combinations = new ArrayList<>();
		int listSize = usableServicesList.size();
		int[] indices = new int[listSize];
		
		while (true) {
			
	        // Add current combination 
			ServiceDescription[] currentCombination = new ServiceDescription[listSize];
			
	        for (int i = 0; i < listSize; i++) {
	        	currentCombination[i] = usableServicesList.get(i).get(indices[i]);
	        }
	        
	        combinations.add(currentCombination);
	  
	        // find the rightmost array that has more 
	        // elements left after the current element  
	        // in that array 
	        int next = listSize - 1; 
	        
	        while (next >= 0 && (indices[next] + 1 >= usableServicesList.get(next).size())) {
	            next--; 
	        }
	  
	        // No more combinations are left
	        if (next < 0) 
	            break; 
	  
	        // Move to next element in that array 
	        indices[next]++; 
	  
	        // Reset other arrays to the right of current array
	        for (int i = next + 1; i < listSize; i++) 
	            indices[i] = 0;
		}
		
		
		//--------------------------------------------
		// Get result
		//--------------------------------------------
		
		// Transform the combinations list to the suitable result type
		List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations = new ArrayList<>();
		Map<Description, WeightedCollection<ServiceDescription>> serviceCombination;
		WeightedCollection<ServiceDescription> serviceCollection;
		
		for (int i = 0; i < combinations.size(); i++) {
			
			serviceCombination = new HashMap<>();
			
			for (int i2 = 0; i2 < listSize; i2++) {
				serviceCollection = new WeightedCollection<ServiceDescription>();
				serviceCollection.add(combinations.get(i)[i2], 100);
				serviceCombination.put(descriptionList.get(i2), serviceCollection);
			}
			
			allServiceCombinations.add(serviceCombination);
		}
		
		return allServiceCombinations;
	}
	
	/**
	 * Calculate the service combination rating for a given service combination, rating type, requirement and multiplier
	 * @param combination the given service combination without rating or type
	 * @param ratingType the given rating type
	 * @param requirement the given requirement where the service combination rating is based off
	 * @param goals the given system goals
	 * @return the calculated service combination rating
	 * @throws IllegalArgumentException throw when rating generation for given rating type isn't implemented.
	 * @throws IllegalArgumentException throw when NUMBER rating generation for given requirement isn't implemented
	 * @throws IllegalArgumentException throw when CLASS rating generation for given requirement isn't implemented
	 */
	protected Object getServiceCombinationRating(Map<Description, WeightedCollection<ServiceDescription>> combination, RatingType ratingType, String requirement, List<Goal> goals) throws IllegalArgumentException {
		
		switch (ratingType) {
		
		case NUMBER:
			
			switch (requirement) {
				
			case "Cost":
				return calculateNumberRatingCost(combination);
			
			case "FailureRate":
				return calculateNumberRatingFailureRate(combination);
		
			default:
				throw new IllegalArgumentException("NUMBER rating generation for given requirement isn't implemented!");
			}
			
		case CLASS:	
			return calculateClassRating(combination, goals, requirement);
			
		default:
			throw new IllegalArgumentException("Rating generation for given rating type isn't implemented!");
		}
	}
	
	/**
	 * Calculate the rating of a given service combination for the NUMBER rating type and the "Cost" requirement
	 * @param combination the given service combination without rating or type
	 * @return the rating of the service combination
	 */
	protected double calculateNumberRatingCost(Map<Description, WeightedCollection<ServiceDescription>> combination) {
		
		// TODO change, make scoring better
		double score = 0;
		
		for (Description description : combination.keySet()) {
			
			double cost = 0;
			
			for (ServiceDescription service : combination.get(description).getItems()) {
				
				if (service.getCustomProperties().containsKey("Cost")) {
					cost = (double) service.getCustomProperties().get("Cost");
					score += (1 / (cost + 1)) * combination.get(description).getChance(service);
				}
				else {
					score += 1 * combination.get(description).getChance(service);
				}
			}
		}
		
		return score;
	}
	
	/**
	 * Calculate the rating of a given service combination for the NUMBER rating type and the "FailureRate" requirement
	 * @param combination the given service combination without rating or type
	 * @return the rating of the service combination
	 */
	protected double calculateNumberRatingFailureRate(Map<Description, WeightedCollection<ServiceDescription>> combination) {
		
		// TODO change, make scoring better
		double score = 0;
		
		for (Description description : combination.keySet()) {
			
			double failureRate = 0;
			
			for (ServiceDescription service : combination.get(description).getItems()) {
				
				if (service.getCustomProperties().containsKey("FailureRate")) {
					failureRate = (double) service.getCustomProperties().get("FailureRate");
					score += (1 / (failureRate + 1)) * combination.get(description).getChance(service);
				}
				else {
					score += 1 * combination.get(description).getChance(service);
				}
			}
		}
		
		return score;
	}
	
	/**
	 * Calculate the rating of a given service combination for the CLASS rating type and a given requirement
	 * @param combination the given service combination without rating or type
	 * @param requirement the given requirement where the service combination rating is based off
	 * @param goals the given system goals
	 * @return the rating of the service combination
	 * @throws IllegalStateException throw when class rating generation for given class relation isn't implemented
	 * @throws IllegalStateException throw when list of goals doesn't include the right goal type
	 */
	protected int calculateClassRating(Map<Description, WeightedCollection<ServiceDescription>> combination, List<Goal> goals, String requirement) throws IllegalStateException {
		
		double totalValue = 0;
		
		for (Description description : combination.keySet()) {
			
			for (ServiceDescription service : combination.get(description).getItems()) {
				
				if (service.getCustomProperties().containsKey(requirement)) {
					totalValue += (double) service.getCustomProperties().get(requirement);
				}
			}
		}
		
		for (Goal goal : goals) {
			
			if (goal.getType() == GoalType.valueOf(requirement)) {
				
				switch (goal.getRelation()) {
				
				case HIGHER_THAN:
					
						if (totalValue > goal.getValue()) {
							return 0;
						}
						else {
							return 1;
						}
						
				case HIGHER_OR_EQUAL_TO:
					
					if (totalValue >= goal.getValue()) {
						return 0;
					}
					else {
						return 1;
					}
					
				case LOWER_THAN:

					if (totalValue < goal.getValue()) {
						return 0;
					}
					else {
						return 1;
					}
					
				case LOWER_OR_EQUAL_TO:
					
					if (totalValue <= goal.getValue()) {
						return 0;
					}
					else {
						return 1;
					}
					
				default:
					
					throw new IllegalStateException("Class rating generation for given class relation isn't implemented!");
					
				}
			}
		}
		
		throw new IllegalStateException("List of goals doesn't include the right goal type! Called wrong method?");
	}
}
