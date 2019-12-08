package tas.mape.planner;

import java.util.Map;
import java.util.Set;

import org.stringtemplate.v4.compiler.STParser.namedArg_return;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents a combination of service descriptions (unique for a service) chosen by the analyzer component
 */
public class ServiceCombination {
	
	// Map representing all the services in this service combination. The map depicts which services (endpoint + weight) can be used for which 
	// description (method type + operation name) and each usable service has a use chance.
	Map<Description, WeightedCollection<ServiceDescription>> allServices;
	ratingType combinationRateType;
	Object rating;
	
	/**
	 * Create a new service combination with a given services map, a rating type and a rating
	 * @param allServices the given services map
	 * @param combinationRateType the given rating type
	 * @param rating the given rating
	 */
	public ServiceCombination(Map<Description, WeightedCollection<ServiceDescription>> allServices, ratingType combinationRateType, Object rating) {
		
		this.allServices = allServices;
		this.combinationRateType = combinationRateType;
		
		try {
			this.rating = combinationRateType.getTypeClass().cast(rating);
		} catch(ClassCastException e) {
			throw new ClassCastException("Given rate is in the wrong type! " + rating.getClass() + " <--> " + combinationRateType.getTypeClass());
		}
		
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
	public ratingType getCombinationRateType() {
		return combinationRateType;
	}
	
	/**
	 * Return the service combination rating
	 * @return the service combination rating
	 */
	public Object getRating() {
		return rating;
	}
}
