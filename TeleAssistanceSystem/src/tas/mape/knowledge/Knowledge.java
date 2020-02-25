package tas.mape.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.util.Pair;
import profile.SystemRequirementType;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.analyzer.CostAndReliabilityReq;
import tas.mape.analyzer.CostReq;
import tas.mape.analyzer.ReliabilityReq;
import tas.mape.planner.PlanComponent;

/**
 * Class that represents the knowledge component in a MAPE-K component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 * @note Service descriptions should actually be a copy of the real cache service descriptions, so the components can't change the load directly.
 * This is skipped here because it slows down the execution of the workflow entity.
 */

// TODO Return copies but not the original lists for security
public class Knowledge {
	
	// List of used registry endpoints by the workflow
	private List<String> registryEndpoints;
	
	// Currently active goals
	private List<Goal> goals;
	
	// Map containing service usage chance from analyzing the workflow
	private Map<Description, Double> servicesUsageChance;
	
	// Map containing information about the approximated service failure rates
	private Map<String, TreeMap<Integer, Double>> approximatedServiceFailureRates;
	
	// Delta value used for updating the service failure rate map
	private int loadFailureDelta;
	
	// List of plan components containing information about needed changes to the cache as a result of changes in the service registries
	private List<PlanComponent> cachePlanComponents;
	
	// hash map of services that are blacklisted. Blacklisted services are not considered when creating service combinations. 
	private Map<String, ServiceDescription> serviceBlackList;
	
	private Map<Description, List<ServiceDescription>> usableServices;
	
	// Map of used services. This is a map of all used services minus the blacklisted service entries.
	private Map<Description, List<ServiceDescription>> usedServices;
	
	// map containing all possible requirement and abstract workflow requirement classes pairs
	private static HashMap<SystemRequirementType, AbstractWorkflowQoSRequirement> QoSRequirementClasses = new HashMap<SystemRequirementType, AbstractWorkflowQoSRequirement>() {
		private static final long serialVersionUID = 1L;
	{
        put(SystemRequirementType.COST, new CostReq());
        put(SystemRequirementType.RELIABILITY, new ReliabilityReq());
        put(SystemRequirementType.COST_AND_RELIABILITY, new CostAndReliabilityReq());
    }};;
	
	/**
	 * Create a new knowledge with given starting information
	 * @param loadFailureDelta the given difference between two load values in the approximated failure table
	 * @param serviceRegistryEndpoints the given registry endpoints used by the workflow
	 */
	public Knowledge(int loadFailureDelta, List<String> serviceRegistryEndpoints) {
		
		this.loadFailureDelta = loadFailureDelta;	
		this.registryEndpoints = serviceRegistryEndpoints;
		serviceBlackList = new HashMap<>();
		cachePlanComponents = new ArrayList<>();
		usedServices = new HashMap<>();
		usableServices = new HashMap<>();
		goals = new ArrayList<>();
	}
	
	/**
	 * Initialize used services, usable services and their usage changes
	 * @param usableServicesAndChance map containing usable service information with additional usage chance
	 */
	public void setUsedServicesAndChances(Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
		
		servicesUsageChance = new HashMap<>();
		usedServices = new HashMap<>();
		
		// Initialize used services
		for (Description description : usableServicesAndChance.keySet()) {
			servicesUsageChance.put(description, usableServicesAndChance.get(description).getValue());
			usedServices.put(description, usableServicesAndChance.get(description).getKey());
		}
		
		// Initialize usable services
		usableServices = new HashMap<>(usedServices);
		List<Pair<Description, ServiceDescription>> toBeDeleted = new ArrayList<>();
		 
		if (serviceBlackList.values().size() > 0) {
			for (Description description : usableServices.keySet()) {
				for (ServiceDescription service : usableServices.get(description)) {
					if (serviceBlackList.get(service.getServiceEndpoint()) != null) {
						toBeDeleted.add(new Pair<>(description, service));
					}
				}
			}
		}
		
		for (Pair<Description, ServiceDescription> pair : toBeDeleted) {
			usableServices.get(pair.getKey()).remove(pair.getValue());
		}
		
		// Initialize approximated failure rates
		initializeApproximatedServicesFailureRates();
	}
	
	/**
	 * Add a given service to the blacklist and update the usable services accordingly
	 * @param blacklistedService the given service to be blacklisted
	 */
	public void addServiceToBlacklist(ServiceDescription blacklistedService) {
		
		serviceBlackList.put(blacklistedService.getServiceEndpoint(), blacklistedService);
		List<Pair<Description, ServiceDescription>> toBeDeleted = new ArrayList<>();
		
		for (Description description : usableServices.keySet()) {
			for (ServiceDescription service : usableServices.get(description)) {
				if (serviceBlackList.get(service.getServiceEndpoint()) != null) {
					toBeDeleted.add(new Pair<>(description, service));
				}
			}
		}
		
		for (Pair<Description, ServiceDescription> pair : toBeDeleted) {
			usableServices.get(pair.getKey()).remove(pair.getValue());
		}
	}
	
	/**
	 * Remove a given service from the blacklist and update the usable services accordingly
	 * @param blacklistedService the given service to be freed
	 */
	public void removeServiceFromBlacklist(ServiceDescription freedService) {
		serviceBlackList.remove(freedService.getServiceEndpoint());
		List<Pair<Description, ServiceDescription>> toBeAdded = new ArrayList<>();
		
		for (Description description : usedServices.keySet()) {
			for (ServiceDescription service : usedServices.get(description)) {
				if (service.equals(freedService)) {
					toBeAdded.add(new Pair<>(description, service));
				}
			}
		}
		
		for (Pair<Description, ServiceDescription> pair : toBeAdded) {
			
			if (usableServices.get(pair.getKey()) == null) {
				usableServices.put(pair.getKey(), new ArrayList<>());
			}
			
			usableServices.get(pair.getKey()).add(pair.getValue());
		}
	}
	
	/**
	 * Return whether a given service is blacklisted
	 * @param service the given service
	 * @return whether a given service is blacklisted
	 */
	public boolean isBlacklisted(ServiceDescription service) {
		return serviceBlackList.get(service.getServiceEndpoint()) != null;
	}
	
	/**
	 * Return the used registry endpoints
	 * @return the used registry endpoints
	 */
	public List<String> getRegistryEndpoints() {
		return registryEndpoints;
	}

	/**
	 * Return the requirement class that represents the given requirement name
	 * @param requirement the given requirement
	 * @return the QoS requirement class
	 */
	public AbstractWorkflowQoSRequirement getQoSRequirementClass(SystemRequirementType requirement) {
		return QoSRequirementClasses.get(requirement);
	}
	
	/**
	 * Add a new requirement class linked to a given requirement name
	 * @param requirement the given requirement
	 * @param requirementClass the new requirement class
	 * @throws IllegalArgumentException throw when the given requirement name is already linked to a requirement class
	 */
	public void AddQoSRequirementClass(SystemRequirementType requirement, AbstractWorkflowQoSRequirement requirementClass) throws IllegalArgumentException {
	
		if (QoSRequirementClasses.get(requirement) == null) {
			throw new IllegalArgumentException("The given requirement is already linked to a requirement class!");
		}
		
		QoSRequirementClasses.put(requirement, requirementClass);
	}
	
	/**
	 * Set the approximated service failure rate for a given service to the given failure rate at the given load
	 * @param serviceEndpoint the given service endpoint
	 * @param load the given load
	 * @param failureRate the given failure rate
	 * @throws IllegalArgumentException throw when the approximated service failure rate for the given serviceEndpoint was not found
	 */
	public void setApproximatedServiceFailureRate(String serviceEndpoint, int load, double failureRate) throws IllegalArgumentException {
		
		if (approximatedServiceFailureRates.get(serviceEndpoint) == null) {
			throw new IllegalArgumentException("The approximated service failure rate for " + serviceEndpoint + " was not found! \n");
		}
		
		int loadKeyHigh = (load / loadFailureDelta) + ((load % loadFailureDelta == 0) ? 0 : 1);
		
		System.err.print("UPDATE FAIL TABLE: " + serviceEndpoint + ", KEY " + loadKeyHigh + ", NEW FAIL RATE: " + failureRate + "\n");
		approximatedServiceFailureRates.get(serviceEndpoint).put(loadKeyHigh, failureRate);
	}
	
	/**
	 * Get the approximated service failure rate for a given service at the given load.
	 * If the failure rate is not present in the table, then update the table.
	 * @param serviceEndpoint the given service endpoint
	 * @param load the given load
	 * @throws IllegalArgumentException throw when the approximated service failure rate for the given serviceEndpoint was not found
	 */
	public Double getApproximatedServiceFailureRate(String serviceEndpoint, int load) throws IllegalArgumentException {
		
		if (approximatedServiceFailureRates.get(serviceEndpoint) == null) {
			throw new IllegalArgumentException("The approximated service failure rate for " + serviceEndpoint + " was not found! \n");
		}
		
		TreeMap<Integer, Double> serviceFailureTable = approximatedServiceFailureRates.get(serviceEndpoint);
		int loadKey = (load / loadFailureDelta) + ((load % loadFailureDelta == 0) ? 0 : 1);
		
		// Entry does not exist
		if (serviceFailureTable.get(loadKey) == null) {
			
			// Find nearest entry
			if (serviceFailureTable.higherEntry(loadKey) != null) {
				
				System.err.print("FAIL TABLE GET: " + serviceEndpoint + ", LOAD: " + load + ", KEY: " + loadKey + ", VALUE: " + serviceFailureTable.get(getNearestKey(serviceFailureTable, loadKey)) + "\n");
				return serviceFailureTable.get(getNearestKey(serviceFailureTable, loadKey));
			}
			
			System.err.print("FAIL TABLE GET: " + serviceEndpoint + ", LOAD: " + load + ", KEY: " + loadKey + ", VALUE: " + serviceFailureTable.lowerEntry(loadKey).getValue() + "\n");
			
			// Lower key will always exist
			return serviceFailureTable.lowerEntry(loadKey).getValue();
		}
		
		System.err.print("FAIL TABLE GET: " + serviceEndpoint + ", LOAD: " + load + ", KEY: " + loadKey + ", VALUE: " + serviceFailureTable.get(loadKey) + "\n");
				
		return serviceFailureTable.get(loadKey);
	}
	
	/**
	 * Returns the list of usable services
	 * @return the list of usable services
	 * @note Should actually return a deep copy of the list, but slows down execution
	 */
	public Map<Description, List<ServiceDescription>> getUsableServices() {
		return usableServices;
	}
	
	/**
	 * Add a given service to the used services map for a given service type and operation name.
	 * Its approximated failure rate is also initialized.
	 * @param description the given service type and operation name
	 * @param serviceDescription the given service to be added
	 */
	public void addUsedService(Description description, ServiceDescription serviceDescription) {	
		if (usedServices.get(description) != null) {
			usedServices.get(description).add(serviceDescription);
			InitializeApproximatedServiceFailureRates(serviceDescription);
		}
	}
	
	/**
	 * Remove a given service from the used services map for a given service type and operation name.
	 * Its approximated failure rate is also removed.
	 * @param description the given service type and operation name
	 * @param serviceDescription the given service to be deleted
	 */
	public void removeUsedService(Description description, ServiceDescription serviceDescription) throws IllegalArgumentException {
		
		if (usedServices.get(description) != null) {
			
			// Also remove service from usable services list if possible
			if (usableServices.get(description) != null) {
				usableServices.get(description).remove(serviceDescription);
			}
			
			usedServices.get(description).remove(serviceDescription);
			approximatedServiceFailureRates.remove(serviceDescription.getServiceEndpoint());
		}
	}
	
	/**
	 * Return the service load based of a given service description and use percentage (use chance)
	 * @param description the given service description of the service whose load is calculated
	 * @param usePercentage the given use percentage (use chance)
	 * @return the service load
	 */
	public int getServiceLoad(Description description, double usePercentage) {
		return (int) (servicesUsageChance.get(description) * usePercentage * SystemProfileDataHandler.activeProfile.getWorkflowCycles());
	}
	
	/**
	 * Return the system goals
	 * @return the system goals
	 */
	public List<Goal> getGoals() {
		return goals;
	}
	
	/**
	 * Add a given goal to the list of goals
	 * @param goal the given goal
	 */
	public void addGoal(Goal goal) {
		goals.add(goal);
	}
	
	/**
	 * Remove a given goal from the list of goals
	 * @param goal the given goal
	 */
	public void removeGoal(Goal goal) {
		goals.remove(goal);
	}
	
	/**
	 * Change the list of goals to a given list of goals
	 * @param goals the given list of goals
	 */
	public void changeGoals(List<Goal> goals) {
		this.goals = goals;
	}
	
	/**
	 * Reset the goals
	 */
	public void resetGoals() {
		goals.clear();
	}
	
	/**
	 * Return the cache plan components 
	 * @return the cache plan components 
	 */
	public List<PlanComponent> getCachePlanComponents() {
		return cachePlanComponents;
	}
	
	/**
	 * Add a given plan component to the cache plan components
	 * @param cachePlanComponent the given plan component
	 */
	public void addCachePlanComponents(PlanComponent cachePlanComponent) {
		cachePlanComponents.add(cachePlanComponent);
	}
	
	/**
	 * Reset the cache plan components
	 */
	public void resetRegistryPlanComponents() {
		cachePlanComponents.clear();
	}
	
	/**
	 * Initialize the approximated failure rates for all currently usable services.
	 */
	private void initializeApproximatedServicesFailureRates() {
		
		// New HashMap
		approximatedServiceFailureRates = new HashMap<>();
		
		// Loop over all service descriptions
		for (List<ServiceDescription> serviceDescriptions : usedServices.values()) {
			for (ServiceDescription serviceDescription : serviceDescriptions) {		
				System.err.print(serviceDescription.getServiceEndpoint() + ", INIT APPROX FAIL RATE\n");
				InitializeApproximatedServiceFailureRates(serviceDescription);
			}
		}
	}
	
	/**
	 * Initialize a table that contains the approximated failure rates for each currently usable service.
	 * This table is initialized for each service with a map that represents the failure rate for a given load.
	 * The default failure rate for the service is added as an initial value in the map.
	 */
	private void InitializeApproximatedServiceFailureRates(ServiceDescription serviceDescription) {
		
		// If service description is new
		if (approximatedServiceFailureRates.get(serviceDescription.getServiceEndpoint()) == null) {
			
			TreeMap<Integer, Double> failureRates = new TreeMap<>();
			double serviceFailureRate = 0;
			
			if (serviceDescription.getCustomProperties().containsKey("FailureRate")) {
				serviceFailureRate = (double) serviceDescription.getCustomProperties().get("FailureRate");
			}		
			
			// Fill first value in failure rate map with the default fail rate of the service
			failureRates.put(0, serviceFailureRate);
			
			// New TreeMap
			approximatedServiceFailureRates.put(serviceDescription.getServiceEndpoint(), failureRates);
		}
	}
	
	/**
	 * Find the nearest key to the given key in the given tree map
	 * @param map the given tree map
	 * @param key the given key
	 * @return the nearest key
	 */
	private int getNearestKey(TreeMap<Integer, ?> map, int key) {
		
		int higherKey = map.higherKey(key);
		int lowerKey = map.lowerKey(key);
		
		return (higherKey - key <= key - lowerKey) ? higherKey : lowerKey; 
	}
}
