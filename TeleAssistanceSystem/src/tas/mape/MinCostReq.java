package tas.mape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;

public class MinCostReq extends AbstractWorkflowQoSRequirement {
	
	
    /**
     * 
     * @param combinationLimit
     * @param usableServices
     * @return
     */
	public List<ServiceCombination> applyStrategy1 (int combinationLimit, Map<Description, List<ServiceDescription>> usableServices) {
		
		List<ServiceCombination> chosenServicesList = new ArrayList<>();
		List<Map<Description, ServiceDescription>> allServiceCombinations = getAllServiceCombinations(usableServices);
		
		List<Integer> indexList = new ArrayList<>();
		List<Double> scoreList = new ArrayList<>();
		
		for (int i = 0; i < allServiceCombinations.size(); i++) {
			
			double combinationScore = getServiceCombinationScore(allServiceCombinations.get(i));	
			
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
		
		for (int i = 0; i < Math.min(combinationLimit, allServiceCombinations.size()); i++) {
			
			Map<Description, WeightedCollection<String>> chosenServicesMap = new HashMap<>();
			Map<String, String> chosenServicesRegistryData = new HashMap<>();
			Map<Description, ServiceDescription> chosenServicesCombination = allServiceCombinations.get(i);
			ServiceCombination chosenServicesEntry;
			
			for (Description description : chosenServicesCombination.keySet()) {
				
				WeightedCollection<String> serviceCollection = new WeightedCollection<String>();
				serviceCollection.add(chosenServicesCombination.get(description).getServiceEndpoint(), 100);		
				chosenServicesMap.put(description, serviceCollection);
				
			}
			
			for (ServiceDescription serviceDescription : chosenServicesCombination.values()) {
				chosenServicesRegistryData.put(serviceDescription.getServiceEndpoint(), serviceDescription.getServiceRegistryEndpoint());
			}
			
			chosenServicesEntry = new ServiceCombination(chosenServicesMap, chosenServicesRegistryData, ratingType.NUMBER, scoreList.get(indexList.get(i)));
			chosenServicesList.set(indexList.get(i), chosenServicesEntry);
		}
		
		return chosenServicesList;
	}
	
	private double getServiceCombinationScore(Map<Description, ServiceDescription> combination) {
		
		double score = 0;
		
		for (Description description : combination.keySet()) {
			
			double cost = 0;
			
			if (combination.get(description).getCustomProperties().containsKey("Cost")) {
				cost = (double) combination.get(description).getCustomProperties().get("Cost");
			}

			score += cost;
		}
		
		return score;
	}
	
	public List<Map<Description, ServiceDescription>> getAllServiceCombinations(Map<Description, List<ServiceDescription>> usableServices) {
		
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
		List<Map<Description, ServiceDescription>> allServiceCombinations = new ArrayList<>();
		
		for (int i = 0; i < combinations.size(); i++) {
			
			Map<Description, ServiceDescription> serviceCombination = new HashMap<>();
			
			for (int i2 = 0; i2 < listSize; i2++) {
				serviceCombination.put(descriptionList.get(i2), combinations.get(i)[i2]);
			}
			
			allServiceCombinations.add(serviceCombination);
		}
		
		return allServiceCombinations;
	}
	
	
	// CODE BELOW COULD BE USED AS A BASIS TO IMPROVE 'getAllCombinations()' method IN THE FUTURE
	// The method currently generates all possible solutions and choses the N best, but this could be solved in a different
	// way without generating all solutions first.
	// (code originally from applyStrategy1)
	//--------------------------------------------------------------------------------------------
	
	/*
	 	Comparator<Pair<ServiceDescription, Integer>> pairComparator = new Comparator<Pair<ServiceDescription, Integer>>() {

		@Override
		public int compare(Pair<ServiceDescription, Integer> o1, Pair<ServiceDescription, Integer> o2) {			
			return o1.getValue().compareTo(o2.getValue());
		}
		
    };
	 
	 List<Pair<Description, List<Pair<ServiceDescription, Integer>>>> sortedServicesAll = new ArrayList<>();
	
	// Loop over each description
	for (Description description : usableServices.keySet()) {
		
		List<Pair<ServiceDescription, Integer>> sortedServices = new ArrayList<>();
		
		// Loop over each service description and sort them depending on cost
		for (ServiceDescription serviceDescription : usableServices.get(description)) {
			
			int cost = 0;
			
			// Get service cost
			if (serviceDescription.getCustomProperties().containsKey("Cost")) {
				cost = (int) serviceDescription.getCustomProperties().get("Cost");
			}
			
			// Use binary search to add the new service pair at the right index in the sorted list
			Pair<ServiceDescription, Integer> servicePair = new Pair<ServiceDescription, Integer>(serviceDescription, cost);		
			int index = -1 * (Collections.binarySearch(sortedServices, servicePair, pairComparator) + 1);
			sortedServices.add(index, servicePair);
		}
		
		// Add sorted service list to the total sorted services list with the current description
		sortedServicesAll.add(new Pair<Description, List<Pair<ServiceDescription, Integer>>>(description, sortedServices));
	}
	
	
	List<Integer> currentScore = new ArrayList<>();
	Map<Description, WeightedCollection<String>> currentChosenServiceMap = new HashMap<>();
	WeightedCollection<String> chosenServicesForDescription = new WeightedCollection<String>();
	
	for (int i = 0; i < sortedServicesAll.size(); i++) {
		currentScore.add(sortedServicesAll.get(i).getValue().get(0).getValue());
		chosenServicesForDescription.add(sortedServicesAll.get(i).getValue().get(0).getKey().getServiceEndpoint(), 100);
		currentChosenServiceMap.put(sortedServicesAll.get(i).getKey(), chosenServicesForDescription);
	}
	
	chosenServicesList.add(currentChosenServiceMap);
	
	
	for (int i = 0; i < combinationLimit - 1; i++) {
		chosenServicesForDescription = new WeightedCollection<String>();
		currentChosenServiceMap = new HashMap<>();
		
		// TODO find chosen service
	}*/
}
