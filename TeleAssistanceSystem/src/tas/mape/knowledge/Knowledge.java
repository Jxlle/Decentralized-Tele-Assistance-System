package tas.mape.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.util.Pair;
import profile.SystemRequirement;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.analyzer.CostAndReliabilityReq;
import tas.mape.analyzer.CostReq;
import tas.mape.analyzer.ReliabilityReq;
import tas.mape.planner.PlanComponent;
import tas.mape.system.entity.SystemEntity;
import tas.mape.system.entity.WorkflowExecutor;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents the knowledge component in a MAPE-K component
 */
public class Knowledge {
	
	// Parent system entity this component belongs to
	private SystemEntity systemEntity;
	
	// List of used registry endpoints by the workflow
	private List<String> registryEndpoints;
	
	// Currently active goals
	private List<Goal> goals;
	
	// Map containing service usage chance from analyzing the workflow
	private Map<Description, Double> servicesUsageChance;
	
	// Map containing information about the approximated service failure rates
	private Map<String, HashMap<Integer, Double>> approximatedServiceFailureRates;
	
	// Delta value used for updating the service failure rate map
	private int loadFailureDelta;
	
	// List of plan components containing information about needed changes to the cache as a result of changes in the service registries
	private List<PlanComponent> cachePlanComponents;
	
	// Service descriptions should actually be a copy of the real cache service descriptions, so the components can't change the load directly.
	// This is skipped here because it slows down the execution of the workflow entity.
	private Map<Description, List<ServiceDescription>> usableServices;
	
	// map containing all possible requirement and abstract workflow requirement classes pairs
	private static HashMap<SystemRequirement, AbstractWorkflowQoSRequirement> QoSRequirementClasses = new HashMap<SystemRequirement, AbstractWorkflowQoSRequirement>() {
		private static final long serialVersionUID = 1L;
	{
        put(SystemRequirement.COST, new CostReq());
        put(SystemRequirement.RELIABILITY, new ReliabilityReq());
        put(SystemRequirement.COST_AND_RELIABILITY, new CostAndReliabilityReq());
    }};;
	
	/**
	 * Create a new knowledge with given starting information
	 * @param loadFailureDelta the given difference between two load values in the approximated failure table
	 * @param serviceRegistryEndpoints the given registry endpoints used by the workflow
	 */
	public Knowledge(int loadFailureDelta, List<String> serviceRegistryEndpoints) {
		
		this.loadFailureDelta = loadFailureDelta;	
		this.registryEndpoints = serviceRegistryEndpoints;
		cachePlanComponents = new ArrayList<>();
		initializeApproximatedServicesFailureRates();
	}
	
	/**
	 * Initialize usable services and their usage changes
	 * @param usableServicesAndChance map containing usable service information with additional usage chance
	 */
	public void setUsableServicesAndChances(Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
		
		servicesUsageChance = new HashMap<>();
		usableServices = new HashMap<>();
		
		for (Description description : usableServicesAndChance.keySet()) {
			servicesUsageChance.put(description, usableServicesAndChance.get(description).getValue());
			usableServices.put(description, usableServicesAndChance.get(description).getKey());
		}
	}
	
	/**
	 * Set the parent system entity to the given entity
	 * @param entityName the given entity
	 */
	public void setSystemEntity(SystemEntity systemEntity) throws IllegalStateException {
		
		if (systemEntity != null) {
			throw new IllegalStateException("A system entity can only be assigned once!");
		}
		
		this.systemEntity = systemEntity;
	}
	
	/**
	 * Return the parent system entity
	 * @return the parent system entity
	 */
	public SystemEntity getSystemEntity() {
		return systemEntity;
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
	public AbstractWorkflowQoSRequirement getQoSRequirementClass(SystemRequirement requirement) {
		return QoSRequirementClasses.get(requirement);
	}
	
	/**
	 * Add a new requirement class linked to a given requirement name
	 * @param requirement the given requirement
	 * @param requirementClass the new requirement class
	 * @throws IllegalArgumentException throw when the given requirement name is already linked to a requirement class
	 */
	public void AddQoSRequirementClass(SystemRequirement requirement, AbstractWorkflowQoSRequirement requirementClass) throws IllegalArgumentException {
	
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
		
		int loadKeyHigh = (load / loadFailureDelta) + 1;
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
		
		HashMap<Integer, Double> serviceFailureTable = approximatedServiceFailureRates.get(serviceEndpoint);
		int loadKeyHigh = (load / loadFailureDelta) + 1;
		
		if (serviceFailureTable.get(loadKeyHigh) == null) {
			serviceFailureTable.put(loadKeyHigh, serviceFailureTable.get(0));
		}
					
		return serviceFailureTable.get(loadKeyHigh);
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
	 * Add a given service to the usable services map for a given service type and operation name.
	 * Its approximated failure rate is also removed initialized.
	 * @param description the given service type and operation name
	 * @param serviceDescription the given service to be added
	 */
	public void addUsableService(Description description, ServiceDescription serviceDescription) {	
		if (usableServices.get(description) != null) {
			usableServices.get(description).add(serviceDescription);
			InitializeApproximatedServiceFailureRates(serviceDescription);
		}
	}
	
	/**
	 * Remove a given service from the usable services map for a given service type and operation name.
	 * Its approximated failure rate is also removed.
	 * @param description the given service type and operation name
	 * @param serviceDescription the given service to be deleted
	 */
	public void removeUsableService(Description description, ServiceDescription serviceDescription) throws IllegalArgumentException {
		
		if (usableServices.get(description) != null) {
			usableServices.get(description).remove(serviceDescription);
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
		return (int) (servicesUsageChance.get(description) * usePercentage * WorkflowExecutor.workflowCycles);
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
		for (List<ServiceDescription> serviceDescriptions : usableServices.values()) {
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
		if (approximatedServiceFailureRates.get(serviceDescription.getServiceEndpoint()) != null) {
			
			// New TreeMap
			approximatedServiceFailureRates.put(serviceDescription.getServiceEndpoint(), new HashMap<>());
			
			Map<Integer, Double> failureRates = new TreeMap<>();
			double serviceFailureRate = 0;
			
			if (serviceDescription.getCustomProperties().containsKey("FailureRate")) {
				serviceFailureRate = (double) serviceDescription.getCustomProperties().get("FailureRate");
			}		
			
			// Fill first value in failure rate map with the default fail rate of the service
			failureRates.put(0, serviceFailureRate);
		}
	}
}
