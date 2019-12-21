package tas.mape.analyzer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
 * An abstract class for choosing service combinations in the analyzer step of the MAPE-K loop 
 * based on a QoS requirement and strategy.
 */
public abstract class AbstractWorkflowQoSRequirement {
	
	/**
	 * Apply a QoS requirement strategy with a given strategy, combination limit, rating type, list of goals 
	 * and a map of usable services.
	 * @param strategy the given strategy number
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type 
	 * @param goals the given system goals
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return a list of the chosen service combinations
	 * @throws IllegalArgumentException throw when the combination limit is illegal
	 */
	@SuppressWarnings("unchecked")
	public List<ServiceCombination> getServiceCombinations(Integer strategy, int combinationLimit, 
			RatingType ratingType, List<Goal> goals, Map<Description, List<ServiceDescription>> usableServices) 
					throws IllegalArgumentException {
		
		// Check if given combination limit is legal
		if (combinationLimit < 1) {
			throw new IllegalArgumentException("Combination limit must be at least 1.");
		}
		
		Method method = null;
		List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations = null;
		
		// Search service combination generator 
		try {
			method = this.getClass().getMethod("getAllServiceCombinations" + strategy, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		// Invoke method
		try {
			allServiceCombinations = (List<Map<Description, WeightedCollection<ServiceDescription>>>) 
					method.invoke(this, usableServices);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}	

		return getServiceCombinations(combinationLimit, ratingType, goals, allServiceCombinations);
	}
	
	/**
	 * Re-rank the given service combinations with a given map of service failure rates and given system goals
	 * @param serviceCombinations the given service combinations
	 * @param serviceFailureRates the given map of service failure rates
	 * @param goals the given system goals
	 * @return the new service combinations
	 */
	public abstract List<ServiceCombination> getNewServiceCombinations(List<ServiceCombination> serviceCombinations, 
			Map<String, Double> serviceFailureRates, List<Goal> goals);
	
	// TODO OTHER STRATEGIES
	/**
	 * Generate and return all possible service combinations without rating or type
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return the generated service combinations without rating or type
	 */
	public List<Map<Description, WeightedCollection<ServiceDescription>>> getAllServiceCombinations1(Map<Description, 
			List<ServiceDescription>> usableServices) {
		
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
	 * Calculate the accumulated value of a given property for a given service combination.
	 * @param combination the given service combination without the rating and rating type
	 * @param property the given property
	 * @return the total value
	 */
	public double getTotalValue(Map<Description, WeightedCollection<ServiceDescription>> combination, String property) {
		
		double totalValue = 0;
		
		for (Description description : combination.keySet()) {
			
			for (ServiceDescription service : combination.get(description).getItems()) {
				
				if (service.getCustomProperties().containsKey(property)) {
					totalValue += (double) service.getCustomProperties().get(property);
				}
			}
		}
		
		return totalValue;
	}
	
	/**
	 * Calculate the accumulated value of a given property for a given service combination.
	 * @param combination the given service combination
	 * @param property the given property
	 * @return the total value
	 */
	public double getTotalValue(ServiceCombination combination, String property) {
		
		double totalValue = 0;
		
		for (Description description : combination.getDescriptions()) {
			
			for (ServiceDescription service : combination.getAllServices(description).getItems()) {
				
				if (service.getCustomProperties().containsKey(property)) {
					totalValue += (double) service.getCustomProperties().get(property);
				}
			}
		}
		
		return totalValue;
	}
	
	/**
	 * Calculate the accumulated value of a given property for a given service combination based on a given value map. 
	 * The service combination is searched when a certain value isn't present in the given map.
	 * @param combination the given service combination
	 * @param values the given values map
	 * @param property the given property
	 * @return the total value
	 */
	public double getTotalValue(ServiceCombination combination, Map<String, Double> values, String property) {
		
		double totalValue = 0;
		
		for (Description description : combination.getDescriptions()) {
			
			for (ServiceDescription service : combination.getAllServices(description).getItems()) {
				
				if (values.get(service.getServiceEndpoint()) != null) {
					totalValue += values.get(service.getServiceEndpoint()) * combination.getAllServices(description).getChance(service);
				}
				else if (service.getCustomProperties().containsKey(property)) {
					totalValue += (double) service.getCustomProperties().get(property) * combination.getAllServices(description).getChance(service);
				}
			}
		}
		
		return totalValue;
	}
	
	/**
	 * Chooses the service combinations for a requirement with a given combination limit, rating type 
	 * and service combinations without rating or type
	 * @param combinationLimit the given limit of returned service combinations
	 * @param ratingType the given rating type
	 * @param goals the given system goals
	 * @param allServiceCombinations the given generated service combinations without rating or type
	 * @return a list of the chosen service combinations
	 */
	protected abstract List<ServiceCombination> getServiceCombinations(int combinationLimit, RatingType ratingType, 
			List<Goal> goals, List<Map<Description, WeightedCollection<ServiceDescription>>> allServiceCombinations);
	
	/**
	 * Calculate the number rating of a service combination with a given value.
	 * The given value is an accumulation of property values where lower means better score.
	 * @param totalValue the given accumulated property value
	 * @return the service combination rating
	 */
	protected double GetNumberRatingDouble(double totalValue) {
		
		// Return score. +1 excludes special cases where the total value is between 0 and 1
		return 1 / (totalValue + 1) * 100;
	}
	
	/**
	 * Calculate the class rating of a service combination with a given list of goals, property and a value.
	 * The given value is an accumulation of property values where lower means better score.
	 * The rating is a number representing the class the service combination is in. The number represents the amount of met goals.
	 * This class rating is calculated by checking all goals, finding the relevant goal and checking that goal with the given value.
	 * @param totalValue the given accumulated property value
	 * @param goals the given system goals
	 * @param property the given name of the custom service property
	 * @return the service combination rating
	 * @throws IllegalStateException throw when list of goals doesn't include the right goal type
	 */
	protected int getClassRating(List<Goal> goals, double totalValue, String property) throws IllegalStateException {
		
		for (Goal goal : goals) {
			
			if (goal.getType() == GoalType.fromString(property)) {
				
				if (goal.validValue(totalValue)) {
					return 1;
				}
				else {
					return 0;
				}
			}
		}
		
		throw new IllegalStateException("List of goals doesn't include the right goal type! Called wrong method?");
	}
	
	/**
	 * Return a sorted service combination list with a maximum amount of service combinations and a given rating type 
	 * based on a list of combination scores and a list of all service combinations
	 * @param combinationLimit the given maximum amount of service combinations
	 * @param ratingType the given rating type
	 * @param combinationScores the given list of combination scores
	 * @param serviceCombinations the given list of all service combinations
	 * @return a list of sorted service combinations
	 */
	protected List<ServiceCombination> getSortedServiceCombinations(int combinationLimit, RatingType ratingType, 
			List<Comparable<?>> combinationScores, List<Map<Description, WeightedCollection<ServiceDescription>>> serviceCombinations) {
		
		List<ServiceCombination> chosenServicesList = new ArrayList<>();
		ServiceCombination chosenServicesEntry;
		Map<Description, WeightedCollection<ServiceDescription>> chosenServicesCombination;
		
		for (int i = 0; i < serviceCombinations.size(); i++) {
			chosenServicesCombination = serviceCombinations.get(i);
			chosenServicesEntry = new ServiceCombination(chosenServicesCombination, ratingType, combinationScores.get(i));
			chosenServicesList.add(chosenServicesEntry);
		}
		
		Collections.sort(chosenServicesList, Collections.reverseOrder());
		return chosenServicesList.subList(0, Math.min(combinationLimit, serviceCombinations.size()));
	}
	
	/**
	 * Return a sorted service combination list by generating a new list of service combinations and
	 * rating them using the given new combination scores. 
	 * @param serviceCombinations the given service combinations
	 * @param combinationScores the given list of combination scores
	 * @return a list of sorted service combinations
	 */
	protected List<ServiceCombination> getSortedServiceCombinations(List<ServiceCombination> serviceCombinations, 
			List<Comparable<?>> combinationScores) {
		
		List<ServiceCombination> chosenServicesList = new ArrayList<>();
		
		for (int i = 0; i < serviceCombinations.size(); i++) {
			chosenServicesList.add(serviceCombinations.get(i).GetCloneNewRating(combinationScores.get(i)));
		}
		
		Collections.sort(chosenServicesList, Collections.reverseOrder());
		return chosenServicesList;
	}
}
