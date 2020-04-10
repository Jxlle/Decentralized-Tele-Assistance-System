package tas.mape.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.util.Pair;
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
import tas.mape.probes.PlannerObserver;

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
	private PlannerObserver observer;
	private boolean executed, protocolFinished;
	private AbstractProtocol<PlannerMessage, Planner> protocol;
	private List<ServiceCombination> availableServiceCombinations;
	private ServiceCombination currentServiceCombination;
	private List<PlanComponent> plan;
	private Map<String, Map<String, Integer>> loadBuffer;
	
	/**
	 * Create a planner with a given knowledge and executor
	 * @param knowledge the given knowledge
	 * @param executor the given executor
	 */
	public Planner(Knowledge knowledge, Executor executor) {
		super("planner_" + id);
		this.knowledge = knowledge;
		this.executor = executor;
		observer = new PlannerObserver();
		id++;
	}
	
	/**
	 * Return the planner probe
	 * @return the planner probe
	 */
	public PlannerObserver getObserver() {
		return observer;
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
		loadBuffer = new HashMap<>();
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
		return new ArrayList<>(availableServiceCombinations);
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
		//System.out.println(serviceCombination);
		currentServiceCombination = serviceCombination;
	}
	
	/**
	 * Return the currently used service combination
	 * @return the currently used service combination
	 */
	public ServiceCombination getCurrentServiceCombination() {
		return currentServiceCombination;
	}
	
	/**
	 * Add given message content from a given sender to the load buffer
	 * @param sender the given message sender
	 * @param content the given message content
	 */
	public void addToLoadBuffer(String sender, PlannerMessageContent content) {
		loadBuffer.put(sender,  content.getPublicServiceUsage());
	}
	
	public Pair<List<ServiceCombination>, Integer> getLeastOffendingCombinations(List<ServiceCombination> combinations) {	
		
		int LeastOffences = Integer.MAX_VALUE;
		List<ServiceCombination> bestCombinations = new ArrayList<>();
		
		for (ServiceCombination combination : combinations) {		
			int offences = getServiceCombinationOffences(combination);
			if (offences < LeastOffences) {
				LeastOffences = offences;
			}
		}
		
		for (ServiceCombination combination : combinations) {	
			
			int offences = getServiceCombinationOffences(combination);
			
			if (offences == LeastOffences) {
				bestCombinations.add(combination);
			}
		}
		
		return new Pair<List<ServiceCombination>, Integer>(bestCombinations, LeastOffences);
	}
	
	public int getServiceCombinationOffences(ServiceCombination combination) {
		
		int offences = 0;
		
		for (Description description : combination.getDescriptions()) {			
			WeightedCollection<ServiceDescription> serviceUsage = combination.getAllServices(description);
			for (ServiceDescription service : serviceUsage.getItems()) {	
				if (getFullLoadMap().get(service.getServiceEndpoint()) != null) {
					offences++;
				}		
			}
		}
		
		return offences;
	}
	
	public Map<String, Integer> getFullLoadMap() {
		
		Map<String, Integer> fullLoadMap = new HashMap<>();
		
		for (Map<String, Integer> loads : loadBuffer.values()) {
			for (String key : loads.keySet()) {
				if (fullLoadMap.get(key) != null) {
					//System.out.println("UPDATE: " + fullLoadMap.get(key) + " " +  loads.get(key));
					fullLoadMap.put(key, fullLoadMap.get(key) + loads.get(key));
				}
				else {
					fullLoadMap.put(key, loads.get(key));
				}
			}
		}
		
		//System.out.println(fullLoadMap);
		return fullLoadMap;
	}
	
	/**
	 * Calculate the new service combinations list based on the current load buffer.
	 * This is calculated by re-rating the service combinations based on failure rates calculated 
	 * from the load buffer.
	 * @return the newly calculated service combinations
	 */
	public List<ServiceCombination> calculateNewServiceCombinations() {	
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(knowledge.getSystemRequirement());
		return requirementClass.getNewServiceCombinations(availableServiceCombinations, getFullLoadMap(), knowledge);
	}
	
	/**
	 * Method that is called when the protocol is finished, given protocol data.
	 * This method indicates that the protocol is finished.
	 * @param protocolMessages the amount of messages that were sent during the protocol
	 */
	public void finishedProtocol(int protocolMessages) {
		protocolFinished = true;
		observer.serviceCombinationChosen(currentServiceCombination, knowledge, protocolMessages);
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
		
		List<ServiceDescription> availableDescriptions = new ArrayList<>();
		List<ServiceDescription> usedDescriptions = new ArrayList<>();
		
		for (Description description : serviceCombination.getDescriptions()) {
			
			WeightedCollection<ServiceDescription> serviceUsage = serviceCombination.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {
				if (!availableDescriptions.contains(service)) {
					availableDescriptions.add(service);
				}
			}
		}
		
		int usedDescriptionsCount = (int) Math.ceil(availableDescriptions.size() * (messageContentPercentage / (double) 100));
		
		// If new registry endpoint list is shorter than old, take random registry endpoints from the list
		if (usedDescriptionsCount == availableDescriptions.size()) {
			usedDescriptions = availableDescriptions;
		}
		else {		
			
			List<ServiceDescription> availables = new ArrayList<ServiceDescription>(availableDescriptions);
			for (int i = 0; i < usedDescriptionsCount; i++) {				
				int randomIndex = new Random().nextInt(availables.size());
				usedDescriptions.add(availables.get(randomIndex));
				availables.remove(randomIndex);
			}
			
			//System.out.println("CHANGED Registry endpoints, \n\tcount: " + usedDescriptionsCount + "\n\tnormal: " + availableDescriptions +" \n\tchanged: " +  usedDescriptions);
		}
		
		Map<String, Integer> serviceLoads = getServiceLoads(serviceCombination, usedDescriptions);
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
	 * Calculate the service loads for each service in a given service combination that are present in the given
	 * description list
	 * @param serviceCombination the given service combination 
	 * @param descriptions the given service descriptions
	 * @return the calculated service loads map containing endpoint (key), load (value) data
	 */
	private Map<String, Integer> getServiceLoads(ServiceCombination serviceCombination, List<ServiceDescription> descriptions) {
		
		Map<String, Double> serviceLoads = new HashMap<String, Double>();
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		for (Description description : serviceCombination.getDescriptions()) {
			
			WeightedCollection<ServiceDescription> serviceUsage = serviceCombination.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {	
				if (descriptions.stream().anyMatch(x -> x.equals(service))) {
					double serviceLoad = knowledge.getServiceDescriptionLoad(description, serviceUsage.getChance(service));
					serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? serviceLoad : v + serviceLoad);
				}		
			}
		}
		
		for (Map.Entry<String, Double> entry : serviceLoads.entrySet()) {
			result.put(entry.getKey(), (int) Math.ceil(entry.getValue()));
		}
		
		return result;
	}
	
	/**
	 * Calculate the service loads for each service in a given service combination
	 * @param serviceCombination the given service combination 
	 * @return the calculated service loads map
	 */
	private Map<String, Integer> getServiceLoads(ServiceCombination serviceCombination) {
		
		Map<String, Double> serviceLoads = new HashMap<String, Double>();
		Map<String, Integer> serviceLoadsInt = new HashMap<String, Integer>();
		
		for (Description description : serviceCombination.getDescriptions()) {
			
			WeightedCollection<ServiceDescription> serviceUsage = serviceCombination.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {
				double serviceLoad = knowledge.getServiceDescriptionLoad(description, serviceUsage.getChance(service));
				serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? serviceLoad : v + serviceLoad);		
			}
		}
		
		for (Map.Entry<String, Double> entry : serviceLoads.entrySet()) {
			//System.err.println("LOAD " + entry.getKey() + ": " + (int) Math.ceil(entry.getValue()));
			serviceLoadsInt.put(entry.getKey(), (int) Math.ceil(entry.getValue()));
		}
		
		return serviceLoadsInt;
	}
}
