package tas.mape.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.util.Pair;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.planner.PlanComponent;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents the knowledge component in a MAPE-K component
 */
public class Knowledge {
	
	private String currentQoSRequirement;
	// TODO Goals
	private Map<String, Double> goals;
	private Map<Description, Double> servicesUsageChance;
	private Map<String, HashMap<Integer, Double>> approximatedServiceFailureRates;
	private Map<String, AbstractWorkflowQoSRequirement> QoSRequirementClasses;
	private int amountOfCycles, loadFailureDelta;
	
	// List of plan components containing information about needed changes to the cache as a result of changes in the service registries
	private List<PlanComponent> cachePlanComponents;
	
	// Service descriptions should actually be a copy of the real cache service descriptions, so the components can't change the load directly.
	// This is skipped here because it slows down the execution of the workflow entity.
	private Map<Description, List<ServiceDescription>> usableServices;
	
	// TODO
	public Knowledge(int amountOfCycles, int loadFailureDelta, String currentQoSRequirement, Map<String, AbstractWorkflowQoSRequirement> QoSRequirementClasses, Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
		
		this.amountOfCycles = amountOfCycles;
		this.loadFailureDelta = loadFailureDelta;
		this.currentQoSRequirement = currentQoSRequirement;
		this.QoSRequirementClasses = QoSRequirementClasses;
		
		for (Description description : usableServicesAndChance.keySet()) {
			servicesUsageChance.put(description, usableServicesAndChance.get(description).getValue());
			usableServices.put(description, usableServicesAndChance.get(description).getKey());
		}
		
		initializeApproximatedServicesFailureRates();
		cachePlanComponents = new ArrayList<>();
	}
	
	/**
	 * Set the current system QoS requirement
	 * @param currentQoSRequirement the new QoS requirement
	 */
	public void setCurrentQoSRequirement(String currentQoSRequirement) {
		this.currentQoSRequirement = currentQoSRequirement;
	}
	
	/**
	 * Return the current system QoS requirement
	 * @return the current system QoS requirement
	 */
	public String getCurrentQoSRequirement() {
		return currentQoSRequirement;
	}

	/**
	 * Return the requirement class that represents the given requirement name
	 * @param requirementName the given requirement name
	 * @return the QoS requirement class
	 */
	public AbstractWorkflowQoSRequirement getQoSRequirementClass(String requirementName) {
		return QoSRequirementClasses.get(requirementName);
	}
	
	/**
	 * Add a new requirement class linked to a given requirement name
	 * @param requirementName the given requirement name
	 * @param requirementClass the new requirement class
	 * @throws IllegalArgumentException throw when the given requirement name is already linked to a requirement class
	 */
	public void AddQoSRequirementClass(String requirementName, AbstractWorkflowQoSRequirement requirementClass) throws IllegalArgumentException {
	
		if (QoSRequirementClasses.get(requirementName) == null) {
			throw new IllegalArgumentException("The given requirement name is already linked to a requirement class!");
		}
		
		QoSRequirementClasses.put(requirementName, requirementClass);
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
		return (int) (servicesUsageChance.get(description) * usePercentage * amountOfCycles);
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
