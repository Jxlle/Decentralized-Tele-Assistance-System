package tas.mape.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.message.PlannerMessageContent;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.executer.Executer;
import tas.mape.knowledge.Knowledge;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents the planner component in a MAPE-K component
 * @note Planners that communicate should use the same protocol
 */
public class Planner extends CommunicationComponent<PlannerMessage> {

	// Fields
	private Executer executer;
	private Knowledge knowledge;
	private boolean executed, protocolFinished;
	private AbstractProtocol<PlannerMessage, Planner> protocol;
	private List<ServiceCombination> availableServiceCombinations;
	private ServiceCombination currentServiceCombination;
	private List<PlanComponent> plan;
	
	/**
	 * Create a planner with a given endpoint and executer
	 * @param endpoint the given endpoint (identifier)
	 * @param executer the given executer
	 */
	public Planner(String endpoint, Executer executer) {
		super(endpoint);
		this.executer = executer;
	}
	
	/**
	 * Set the currently used protocol to a given protocol
	 * @param protocol the given protocol
	 */
	public void setProtocol(AbstractProtocol<PlannerMessage, Planner> protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * Return the currently used protocol
	 * @return the currently used protocol
	 */
	public AbstractProtocol<PlannerMessage, Planner> getProtocol() {
		return protocol;
	}
	
	/**
	 * Execute the planner, set the available service combinations to a given list
	 * @param availableServiceCombinations the given list of service combinations
	 */
	public void execute(List<ServiceCombination> availableServiceCombinations) {
		this.availableServiceCombinations = availableServiceCombinations;
		protocolFinished = false;
	}
	
	/**
	 * Return the available service combinations
	 * @return the available service combinations
	 */
	public List<ServiceCombination> getAvailableServiceCombinations() {
		return availableServiceCombinations;
	}
	
	/**
	 * Set the available service combinations to a given list of service combinations
	 * @param availableServiceCombinations the given list of service combinations
	 */
	public void setAvailableServiceCombinations(List<ServiceCombination> availableServiceCombinations) {
		this.availableServiceCombinations = availableServiceCombinations;
	}
	
	/**
	 * Set the currently used service combination for the plan to the given service combination
	 * @param serviceCombination the given service combination
	 */
	public void setCurrentServiceCombination(ServiceCombination serviceCombination) {
		currentServiceCombination = serviceCombination;
	}
	
	/**
	 * Calculate the new service combinations list based on a given planner message content object.
	 * This is calculated by re-rating the service combinations based on failure rates calculated 
	 * from the message content.
	 * @param content the given planner message content object
	 * @return the newly calculated service combinations
	 */
	public List<ServiceCombination> calculateNewServiceCombinations(PlannerMessageContent content) {
		String requirementName = knowledge.getCurrentQoSRequirement();
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(requirementName);
		return requirementClass.getNewServiceCombinations(availableServiceCombinations, getFailureRates(content), knowledge.getGoals());
	}
	
	/**
	 * Method that is called when the protocol is finished.
	 * This method indicates that the protocol is finished.
	 */
	public void finishedProtocol() {
		protocolFinished = true;
		makePlan(currentServiceCombination);
	}
	
	/**
	 * Trigger the executer when the planner has been executed and
	 * when the protocol is finished when there is one
	 */
	public void triggerExecuter() {
		if (executed && (protocolFinished || protocol == null)) {
			executer.execute(plan);
			executed = false;
			protocolFinished = false;
		}
	}

	/**
	 * Receive a given message and handle the response with the currently used protocol 
	 * @param message the received message
	 */
	@Override
	public void receiveMessage(PlannerMessage message) throws NullPointerException {
		
		if (protocol == null) {
			throw new NullPointerException("Planner can't handle message message receivement, no protocol selected.");
		}
		
		protocol.receiveAndHandleMessage(message, this);
	}
	
	/**
	 * Generate planner message content that includes a map of service loads for each service inside 
	 * the given service combination that has a registry endpoint in the given list of registry endpoints
	 * @param serviceCombination the given service combination
	 * @param registryEndpoints the given registry endpoints
	 * @return the planner message content
	 */
	public PlannerMessageContent generateMessageContent(ServiceCombination serviceCombination, List<String> registryEndpoints) {
		Map<String, Integer> serviceLoads = getServiceLoads(serviceCombination, registryEndpoints);
		return new PlannerMessageContent(serviceLoads);
	}
	
	/**
	 * Unpack the given content of a planner message and return the calculated failure rates
	 * @param content the given planner message content
	 * @return the calculated failure rates map
	 */
	public Map<String, Double> getFailureRates(PlannerMessageContent content) {
		
		Map<String, Double> failureRates = new HashMap<String, Double>();
		
		for (Map.Entry<String, Integer> entry : content.getPublicServiceUsage().entrySet()) {
			failureRates.put(entry.getKey(), knowledge.getApproximatedServiceFailureRate(entry.getKey(), entry.getValue()));
		}
		
		return failureRates;
	}
	
	/**
	 * Make a plan for the executer to execute based on a given service combination
	 * and data in the knowledge component.
	 * @param serviceCombination the given service combination
	 */
	private void makePlan(ServiceCombination serviceCombination) {
		
		plan = new ArrayList<PlanComponent>();	
		plan.add(new PlanComponent(PlanComponentType.SET_USED_SERVICES, serviceCombination.getAllServices()));
		Map<String, Integer> serviceLoads = getServiceLoads(serviceCombination);
		
		for (String loadEndpoint : serviceLoads.keySet()) {
			plan.add(new PlanComponent(PlanComponentType.INCREASE_LOAD, loadEndpoint, serviceLoads.get(loadEndpoint)));
		}
		
		// Extra: update cache with new registry info
		if (knowledge.getCachePlanComponents().size() != 0) {
			
			for (PlanComponent registryPlanComponent : knowledge.getCachePlanComponents()) {
				plan.add(registryPlanComponent);
			}
			
			knowledge.resetRegistryPlanComponents();
		}
		
		executed = true;
	}
	
	/**
	 * Calculate the service loads for each service in a given service combination that have a 
	 * registry endpoint present in a given list of registry endpoints
	 * @param serviceCombination the given service combination 
	 * @param registryEndpoints the given registry endpoints
	 * @return the calculated service loads map
	 */
	private Map<String, Integer> getServiceLoads(ServiceCombination serviceCombination, List<String> registryEndpoints) {
		
		Map<String, Integer> serviceLoads = new HashMap<String, Integer>();
		
		for (Description description : serviceCombination.getDescriptions()) {
			
			WeightedCollection<ServiceDescription> serviceUsage = serviceCombination.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {	
				
				if (registryEndpoints.contains(service.getServiceRegistryEndpoint())) {
					int serviceLoad = knowledge.getServiceLoad(description, serviceUsage.getChance(service));
					serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? 1 : v + serviceLoad);
				}		
			}
		}
		
		return serviceLoads;
	}
	
	/**
	 * Calculate the service loads for each service in a given service combination
	 * @param serviceCombination the given service combination 
	 * @return the calculated service loads map
	 */
	private Map<String, Integer> getServiceLoads(ServiceCombination serviceCombination) {
		
		Map<String, Integer> serviceLoads = new HashMap<String, Integer>();
		
		for (Description description : serviceCombination.getDescriptions()) {
			
			WeightedCollection<ServiceDescription> serviceUsage = serviceCombination.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {		
				int serviceLoad = knowledge.getServiceLoad(description, serviceUsage.getChance(service));
				serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? 1 : v + serviceLoad);		
			}
		}
		
		return serviceLoads;
	}
}
