package tas.mape.planner;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;

/**
 * Class representing a combination of service descriptions (unique for a service) chosen by the analyzer component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class ServiceCombination implements Comparable<ServiceCombination> { 
	
	// Map representing all the services in this service combination. The map depicts which services (endpoint + weight) can be used for which 
	// description (method type + operation name) and each usable service has a use chance.
	private Map<Description, WeightedCollection<ServiceDescription>> allServices;
	
	/**
	 * Map representing the properties of the service combination, for example 'cost' or 'failure rate'
	 */
	private Map<String, Double> properties = new HashMap<>();
	
	// Type of the service combination rating 
	private RatingType ratingType;
	
	// Service combination rating 
	private Comparable<?> rating;
	
	/**
	 * Create a new service combination with a given services map, a rating type and a rating
	 * @param allServices the given services map
	 * @param combinationRatingType the given rating type
	 * @param rating the given rating
	 */
	public ServiceCombination(Map<Description, WeightedCollection<ServiceDescription>> allServices, RatingType ratingType, Comparable<?> rating) {
		
		this.allServices = allServices;
		this.ratingType = ratingType;
		
		if (rating.getClass() != ratingType.getTypeClass()) {
			throw new ClassCastException("Given rating is in the wrong type! " + rating.getClass() + " <--> " + ratingType.getTypeClass());
		}
		
		this.rating = rating;
	}
	
	/**
	 * Create a clone of this service combination with a new given rating
	 * @param rating the rating of the clone
	 * @return the cloned service combination
	 */
	public ServiceCombination GetCloneNewRating(Comparable<?> rating) {
		return new ServiceCombination(allServices, ratingType, rating);
	}
	
	/**
	 * Return all service descriptions in this service combination for a given service type and operation name
	 * @param description the service type and operation name
	 * @return the chosen service descriptions
	 */
	public WeightedCollection<ServiceDescription> getAllServices(Description description) {
		return allServices.get(description);
	}
	
	/**
	 * Return the 'all services' map
	 * @return the services map
	 * @note Should probably return a deep clone
	 */
	public Map<Description, WeightedCollection<ServiceDescription>> getAllServices() {
		return allServices;
	}
	
	/**
	 * Generate and return an 'all service endpoints' map
	 * @return the service endpoints map
	 */
	public Map<Description, WeightedCollection<String>> getAllServiceEndpoints() {
		
		Map<Description, WeightedCollection<String>> serviceEndpointMap = new HashMap<>();
		
		for (Entry<Description, WeightedCollection<ServiceDescription>> entry : allServices.entrySet()) {
			WeightedCollection<String> strings = new WeightedCollection<String>();
			WeightedCollection<ServiceDescription> serviceDescriptions = entry.getValue();
			
			for (ServiceDescription serviceDescription : serviceDescriptions.getItems()) {
				strings.add(serviceDescription.getServiceEndpoint(), serviceDescriptions.getWeight(serviceDescription));
			}
			
			serviceEndpointMap.put(entry.getKey(), strings);
		}
		
		return serviceEndpointMap;
	}
	
	/**
	 * Add a given property value for the given property
	 * @param property the given property
	 * @param value the given property value
	 */
	public void addProperty(String property, double value) {
		properties.put(property,  value);
	}
	
	/**
	 * Return the property value for the given property
	 * @param property the given property
	 * @return the property value for the given property
	 */
	public Double getProperty(String property) {
		return properties.get(property);
	}
	
	/**
	 * Return all service type and operation name keys of this service combination
	 * @return the service type and operation name keys
	 */
	public Set<Description> getDescriptions() {
		return allServices.keySet();
	}
	
	/**
	 * Return the service combination type
	 * @return the service combination type
	 */
	public RatingType getRatingType() {
		return ratingType;
	}
	
	/**
	 * Return whether the given service combination has the same collection 
	 * @param other the given service combination
	 * @return whether the given service combination has the same collection 
	 */
	public boolean hasSameCollection(ServiceCombination other) {
		return allServices.equals(other.allServices);
	}
	
	/**
	 * Return the service combination rating
	 * @param <T>
	 * @return the service combination rating
	 */
	@SuppressWarnings("unchecked")
	public <T> Comparable<T> getRating() {
		return (Comparable<T>) rating;
	}

	/**
	 * Compare two service combinations
	 */
	@Override
	public int compareTo(ServiceCombination other) {
		return this.getRating().compareTo(other.getRating());
	}
	
	@Override
	public String toString() {
		
		String combinationString = "";
		
		for (Description descr : allServices.keySet()) {
			combinationString += "[" + descr.toString() +"]:\n";
			
			for (ServiceDescription service : allServices.get(descr).getItems()) {
				combinationString += "\t> service name: " + service.getServiceEndpoint() + "\n";
				combinationString += "\t\t>> service registry: " + service.getServiceRegistryEndpoint() + "\n";
				combinationString += "\t\t>> usage chance: " + (allServices.get(descr).getChance(service) * 100) + "%\n";
			}
			
			combinationString += "\n";
		}
		
		return combinationString;
	}
}
