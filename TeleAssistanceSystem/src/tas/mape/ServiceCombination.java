package tas.mape;


import java.util.Map;
import java.util.Set;

import service.auxiliary.Description;
import service.auxiliary.WeightedCollection;

public class ServiceCombination {
	
	 // @return a list of the N best service combinations where each element consists of a map that depicts which services (endpoint + weight) must be used for which description (method type + name)
	Map<Description, WeightedCollection<String>> allServices;
	Map<String, String> serviceRegistryData;
	ratingType combinationRateType;
	Object rating;
	
	public ServiceCombination(Map<Description, WeightedCollection<String>> allServices, Map<String, String> serviceRegistryData, ratingType combinationRateType, Object rate) {
		
		this.allServices = allServices;
		this.serviceRegistryData = serviceRegistryData;
		this.combinationRateType = combinationRateType;
		
		try {
			this.rating = combinationRateType.getRateClass().cast(rate);
		} catch(ClassCastException e) {
			throw new ClassCastException("Given rate is in the wrong type! " + rate.getClass() + " <--> " + combinationRateType.getRateClass());
		}
		
	}
	
	public WeightedCollection<String> getAllServices(Description description) {
		return allServices.get(description);
	}
	
	public Set<Description> getDescriptions() {
		return allServices.keySet();
	}
	
	public String getServiceRegistry(String serviceEndpoint) {
		return serviceRegistryData.get(serviceEndpoint);
	}
	
	public ratingType getCombinationRateType() {
		return combinationRateType;
	}
	
	public Object getRate() {
		return rating;
	}
}
