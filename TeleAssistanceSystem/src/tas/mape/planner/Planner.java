package tas.mape.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.message.PlannerMessageContent;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.executor.Executor;
import tas.mape.knowledge.Knowledge;
import tas.mape.probes.PlannerProbe;

/**
 * Class that represents the planner component in a MAPE-K component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 * 
 * @note Planners that communicate should use the same protocol
 */
public class Planner extends CommunicationComponent<PlannerMessage> {

	// Fields
	private static int id;
	private Executor executor;
	private Knowledge knowledge;
	private PlannerProbe probe;
	private boolean executed, protocolFinished;
	private AbstractProtocol<PlannerMessage, Planner> protocol;
	private List<ServiceCombination> availableServiceCombinations;
	private ServiceCombination currentServiceCombination;
	private List<PlanComponent> plan;
	
	/**
	 * Create a planner with a given knowledge and executor
	 * @param knowledge the given knowledge
	 * @param executor the given executor
	 */
	public Planner(Knowledge knowledge, Executor executor) {
		super("planner_" + id);
		this.knowledge = knowledge;
		this.executor = executor;
		probe = new PlannerProbe();
		id++;
	}
	
	/**
	 * Return the planner probe
	 * @return the planner probe
	 */
	public PlannerProbe getProbe() {
		return probe;
	}
	
	/**
	 * Set the currently used protocol to a given protocol and add this planner
	 * to the protocol components
	 * @param protocol the new protocol
	 */
	public void setProtocol(AbstractProtocol<PlannerMessage, Planner> protocol) {
		
		// Remove the planner from the old protocol
		if (this.protocol != null) {
			this.protocol.removeComponent(this);
		}
		
		if (protocol != null) {
			protocol.addComponent(this);
		}
		
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
		
		// If no protocol is used, just use the best service combination
		if (protocol == null) {
			setCurrentServiceCombination(availableServiceCombinations.get(0));
			finishedProtocol(0);
		}
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
		System.out.println(serviceCombination);
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
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(knowledge.getSystemRequirement());
		return requirementClass.getNewServiceCombinations(availableServiceCombinations, content.getPublicServiceUsage(), knowledge);
	}
	
	/**
	 * Method that is called when the protocol is finished, given protocol data.
	 * This method indicates that the protocol is finished.
	 * @param protocolMessages the amount of messages that were sent during the protocol
	 */
	public void finishedProtocol(int protocolMessages) {
		protocolFinished = true;
		probe.serviceCombinationChosen(currentServiceCombination, knowledge, protocolMessages);
		makePlan(currentServiceCombination);
	}
	
	/**
	 * Trigger the executor when the planner has been executed and
	 * when the protocol is finished when there is one
	 */
	public void triggerExecutor() {
		if (executed && protocolFinished) {
			executor.execute(plan);
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
	 * @param messageContentPercentage percentage of information that is given out
	 * @return the planner message content
	 */
	public PlannerMessageContent generateMessageContent(ServiceCombination serviceCombination, List<String> registryEndpoints, int messageContentPercentage) {
		
		int registryEndpointsCount = (int) Math.ceil(registryEndpoints.size() * (messageContentPercentage / 100));
		List<String> registryEndpointsNew = new ArrayList<>();
		
		if (registryEndpointsCount == registryEndpoints.size()) {
			registryEndpointsNew = registryEndpoints;
		}
		else {		
			registryEndpointsNew = new ArrayList<>();
			
			for (int i = 0; i < registryEndpointsCount; i++) {				
				int randomIndex = new Random().nextInt(registryEndpoints.size());
				registryEndpointsNew.add(registryEndpoints.get(randomIndex));
				registryEndpoints.remove(randomIndex);
			}
		}
		
		Map<String, Integer> serviceLoads = getServiceLoads(serviceCombination, registryEndpoints);
		return new PlannerMessageContent(serviceLoads);
	}
	
	/**
	 * Return the currently stored registry endpoints in the knowledge component
	 * @return the currently stored registry endpoints in the knowledge component
	 */
	public List<String> getRegistryEndpoints() {
		return knowledge.getRegistryEndpoints();
	}
	
	/**
	 * Make a plan for the executor to execute based on a given service combination
	 * and data in the knowledge component.
	 * @param serviceCombination the given service combination
	 */
	private void makePlan(ServiceCombination serviceCombination) {
		
		plan = new ArrayList<PlanComponent>();	
		plan.add(new PlanComponent(PlanComponentType.SET_USED_SERVICES, serviceCombination.getAllServiceEndpoints()));
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
	 * @return the calculated service loads map containing endpoint (key), load (value) data
	 */
	private Map<String, Integer> getServiceLoads(ServiceCombination serviceCombination, List<String> registryEndpoints) {
		
		Map<String, Integer> serviceLoads = new HashMap<String, Integer>();
		
		for (Description description : serviceCombination.getDescriptions()) {
			
			WeightedCollection<ServiceDescription> serviceUsage = serviceCombination.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {	
				
				if (registryEndpoints.contains(service.getServiceRegistryEndpoint())) {
					int serviceLoad = knowledge.getServiceLoad(description, serviceUsage.getChance(service));
					serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? serviceLoad : v + serviceLoad);
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
				//System.err.print("LOAD " + knowledge.getServiceLoad(description, serviceUsage.getChance(service)) + " " + service.getServiceEndpoint() +"\n");
				int serviceLoad = knowledge.getServiceLoad(description, serviceUsage.getChance(service));
				serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? serviceLoad : v + serviceLoad);		
			}
		}
		
		return serviceLoads;
	}
}
