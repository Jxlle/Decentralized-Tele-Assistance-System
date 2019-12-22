package tas.mape.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.auxiliary.AbstractMessage;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.message.PlannerMessageContent;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.communication.protocol.PlannerTwoComponentProtocol;
import tas.mape.executer.Executer;
import tas.mape.knowledge.Knowledge;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents the planner component in a MAPE-K component
 */
public class Planner extends CommunicationComponent<PlannerMessage> {

	// Fields
	private Executer executer;
	private Knowledge knowledge;
	private Boolean executed;
	private AbstractProtocol<PlannerMessage, Planner> protocol;
	private List<ServiceCombination> availableServiceCombinations;
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
	}
	
	/**
	 * Return the available service combinations
	 * @return the available service combinations
	 */
	public List<ServiceCombination> getAvailableServiceCombinations() {
		return availableServiceCombinations;
	}
	
	/**
	 * Make a plan for the executer to execute based on a given service combination
	 * and data in the knowledge component.
	 * @param serviceCombination the given service combination
	 */
	public void makePlan(ServiceCombination serviceCombination) {
		
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
	 * Trigger the executer when the planner has been executed
	 */
	public void triggerExecuter() {
		if (executed) {
			executer.execute(plan);
			executed = false;
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
