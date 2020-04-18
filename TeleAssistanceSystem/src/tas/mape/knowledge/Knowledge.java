package tas.mape.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.util.Pair;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.StaticTree;
import tas.data.inputprofile.InputProfileDataHandler;
import tas.data.inputprofile.SystemRequirementType;
import tas.mape.planner.PlanComponent;

/**
 * Class that represents the knowledge component in a MAPE-K component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 * @note Service descriptions should actually be a copy of the real cache service descriptions, so the components can't change the load directly.
 * This is skipped here because it slows down the execution of the workflow entity.
 */

public class Knowledge {
	
	// System cycle
	private int cycle;
	
	// Name of the system entity where this component is a part of
	private String parentEntityName;
	
	// Tree structure of the workflow, used to calculate total service combination failure rate
	private StaticTree<Description> workflowServiceTree;
	
	// Delta value used for updating the service failure rate map
	private int loadFailureDelta;
	
	// the current system requirement type
	private SystemRequirementType requirementType;
	
	// List of used registry endpoints by the workflow
	private List<String> registryEndpoints;
	
	// Currently active goals
	private List<Goal> goals;
	
	// Map containing service usage chance from analyzing the workflow
	private Map<Description, Double> servicesUsageChance;
	
	// Map containing information about the approximated service failure rates
	private Map<String, TreeMap<Integer, Double>> approximatedServiceFailureRates;
	
	// List of plan components containing information about needed changes to the cache as a result of changes in the service registries
	private List<PlanComponent> cachePlanComponents;
	
	// Map of services that are blacklisted. Blacklisted services are not considered when creating service combinations. 
	private Map<String, ServiceDescription> serviceBlackList;
	
	// Map of the usable services. These are the services that may be used if they aren't blacklisted.
	private Map<Description, List<ServiceDescription>> usableServices;
	
	// Map of used services. This is a map of all usable services without the blacklisted service entries.
	private Map<Description, List<ServiceDescription>> usedServices;
	
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
	 * Set the parent entity name to the given name
	 * @param parentEntityName the given name
	 * @throws IllegalStateException throws when this knowledge is already part of a system entity
	 */
	public void setParentEntityName(String parentEntityName) throws IllegalStateException {
		if (this.parentEntityName != null) {
			throw new IllegalStateException("This knowledge is already part of a system entity!");
		}
		
		this.parentEntityName = parentEntityName;
	}
		
	/**
	 * Return the current system cycle.
	 * @return the current system cycle.
	 */
	public int getSystemCycle() {
		return cycle;
	}
	
	/**
	 * Return the loadFailureDelta value
	 * @return the loadFailureDelta value
	 */
	public int getLoadFailureDelta() {
		return loadFailureDelta;
	}
	
	/**
	 * Increase the current system cycle.
	 */
	public void increaseSystemCycle() {
		cycle++;
	}
	
	/**
	 * Reset the current system cycle
	 */
	public void resetSystemCycle() {
		cycle = 0;
	}
	
	/**
	 * Return the stored workflow service tree
	 * @return the stored workflow service tree
	 */
	public StaticTree<Description> getWorkflowServiceTree() {
		return workflowServiceTree;
	}
	
	/**
	 * Set the current workflow service tree to the given tree
	 * @param workflowServiceTree the given tree
	 */
	public void setWorkflowServiceTree(StaticTree<Description> workflowServiceTree) {
		this.workflowServiceTree = workflowServiceTree;
	}
	
	/**
	 * Return the parent entity name
	 * @return the parent entity name
	 */
	public String getParentEntityName() {
		return parentEntityName;
	}
	
	/**
	 * Set the current requirement type to the given type
	 * @param requirementType the given requirement type
	 */
	public void setRequirementType(SystemRequirementType requirementType) {
		this.requirementType = requirementType;
	}
	
	/**
	 * Return the current requirement type
	 * @return the current requirement type
	 */
	public SystemRequirementType getSystemRequirement() {
		return requirementType;
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
		return new ArrayList<>(registryEndpoints);
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
		
		//System.err.print("UPDATE FAIL TABLE: " + serviceEndpoint + ", KEY " + loadKeyHigh + ", NEW FAIL RATE: " + failureRate + "\n");
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
				
				//System.err.print("FAIL TABLE GET: " + serviceEndpoint + ", LOAD: " + load + ", KEY: " + loadKey + ", VALUE: " + serviceFailureTable.get(getNearestKey(serviceFailureTable, loadKey)) + "\n");
				return serviceFailureTable.get(getNearestKey(serviceFailureTable, loadKey));
			}
			
			//System.err.print("FAIL TABLE GET: " + serviceEndpoint + ", LOAD: " + load + ", KEY: " + loadKey + ", VALUE: " + serviceFailureTable.lowerEntry(loadKey).getValue() + "\n");
			
			// Lower key will always exist
			return serviceFailureTable.lowerEntry(loadKey).getValue();
		}
		
		//System.err.print("FAIL TABLE GET: " + serviceEndpoint + ", LOAD: " + load + ", KEY: " + loadKey + ", VALUE: " + serviceFailureTable.get(loadKey) + "\n");
				
		return serviceFailureTable.get(loadKey);
	}
	
	/**
	 * Reset all approximated service failure rates to the default values
	 */
	public void resetApproximatedServiceFailureRates() {
		
		for (String endpoint : approximatedServiceFailureRates.keySet()) {
			Double defaultFailRate = approximatedServiceFailureRates.get(endpoint).get(0);
			TreeMap<Integer, Double> failureRates = new TreeMap<>();
			failureRates.put(0, defaultFailRate);
			approximatedServiceFailureRates.put(endpoint, failureRates);
		}
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
	 * Return the service description load based of a given service description and use percentage (use chance)
	 * @param description the given service description of the service whose load is calculated
	 * @param usePercentage the given use percentage (use chance)
	 * @return the service load
	 */
	public double getServiceDescriptionLoad(Description description, double usePercentage) {
		return servicesUsageChance.get(description) * usePercentage * InputProfileDataHandler.activeProfile.getWorkflowCycles();
	}
	
	/**
	 * Return the service usage chance of the given service description
	 * @param description the given service description
	 * @return the service usage chance 
	 */
	public Double getServiceUsageChance(Description description) {
		return servicesUsageChance.get(description);
	}
	
	/**
	 * Return the system goals
	 * @return the system goals
	 */
	public List<Goal> getGoals() {
		return new ArrayList<>(goals);
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
		return new ArrayList<>(cachePlanComponents);
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
