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
import tas.communication.CommunicationComponent;
import tas.communication.message.PlannerMessage;
import tas.communication.message.PlannerMessageContent;
import tas.communication.protocol.AbstractProtocol;
import tas.mape.analyzer.Analyzer;
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
public class Planner extends CommunicationComponent<PlannerMessage, Map<String, Map<String, Integer>>> {

	// Fields
	private static int id;
	private Executor executor;
	private Knowledge knowledge;
	private Analyzer analyzer;
	private PlannerObserver observer;
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
	 * Set the analyzer to the given analyzer
	 * @param analyzer the given analyzer
	 */
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
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
		
		knowledge.resetLoadKeys();
		this.availableServiceCombinations = availableServiceCombinations;
		currentServiceCombination = null;
		buffer = new HashMap<>();
		protocolFinished = false;
		
		// If no protocol is used, just use the best service combination
		if (protocol == null) {
			setCurrentServiceCombination(getBestRandomServiceCombination());
			finishedProtocol(0);
		}
	}
	
	/**
	 * Method that is called when the protocol is finished, given protocol data.
	 * This method indicates that the protocol is finished.
	 * @param protocolMessages the amount of messages that were sent during the protocol
	 */
	public void finishedProtocol(int protocolMessages) {
		protocolFinished = true;
		observer.serviceCombinationChosen(currentServiceCombination, knowledge, protocolMessages);
		setLoadKeys(currentServiceCombination);
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
	 * Return the currently stored registry endpoints in the knowledge component
	 * @return the currently stored registry endpoints in the knowledge component
	 */
	public List<String> getRegistryEndpoints() {
		return knowledge.getRegistryEndpoints();
	}
	
	private void setLoadKeys(ServiceCombination serviceCombination) {
		
		Map<String, Integer> loadKeys = getFullLoadMapPlusOwnLoads(serviceCombination, getRegistryEndpoints(), ""); 

		for (String loadEndpoint : loadKeys.keySet()) {
			System.out.println("added load key: " + loadEndpoint + " " + loadKeys.get(loadEndpoint));
			knowledge.addLoadKey(loadEndpoint, loadKeys.get(loadEndpoint));
		}
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
			System.out.println("added load " + loadEndpoint + " " + serviceLoads.get(loadEndpoint));
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
	 * Return a randomly chosen service combination with the best possible rating.
	 * @note a score rating will always return the same combination 
	 * @return a randomly chosen service combination with the best possible rating
	 */
	public ServiceCombination getBestRandomServiceCombination() {
		
		List<ServiceCombination> bestCombinations = new ArrayList<>();
		
		for (ServiceCombination s : availableServiceCombinations) {
			if (s.getRating().equals(availableServiceCombinations.get(0).getRating())) {
				bestCombinations.add(s);
			}
		}
		
		return bestCombinations.get(AbstractProtocol.random.nextInt(bestCombinations.size()));
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
		buffer.put(sender,  content.getPublicServiceUsage());
	}
	
	/**
	 * Get the service combinations in the given list that have the least services in common with
	 * the services in the buffer.
	 * @param combinations the given combination list
	 * @return the service combinations in the given list that have the least services in common with
	 * 	       the services in the buffer.
	 */
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
	
	/**
	 * Calculate the amount of services in the given service combination that are also in the buffer. 
	 * @param combination the given service combination
	 * @return the amount of services in the given service combination that are also in the buffer. 
	 */
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
	
	/**
	 * Generate a map containing service usage from the current buffer content.
	 * @return the full service usage map 
	 */
	public Map<String, Integer> getFullLoadMap() {
		return getFullLoadMap("");
	}
	
	/**
	 * Generate a map containing service usage from the current buffer content, without the load
	 * data from the given receiver endpoint.
	 * @param receiverEndpoint the given receiver endpoint
	 * @return the full service usage map without the receiver data
	 */
	private Map<String, Integer> getFullLoadMap(String receiverEndpoint) {
		Map<String, Integer> fullLoadMap = new HashMap<>();
		
		for (String endpoint : buffer.keySet()) {
			if (endpoint != receiverEndpoint) {
				for (String key : buffer.get(endpoint).keySet()) {
					if (fullLoadMap.get(key) != null) {
						fullLoadMap.put(key, fullLoadMap.get(key) + buffer.get(endpoint).get(key));
					}
					else {
						fullLoadMap.put(key, buffer.get(endpoint).get(key));
					}
				}
			}
		}
		
		return fullLoadMap;
	}
	
	private Map<String, Integer> getFullLoadMapPlusOwnLoads(ServiceCombination serviceCombination, List<String> registryEndpoints, String receiverEndpoint) {
		Map<String, Integer> personalServiceLoads = generateMessageContent(serviceCombination, registryEndpoints, 100).getPublicServiceUsage();
		Map<String, Integer> fullLoadMap = getFullLoadMap(receiverEndpoint);
		
		for (Map.Entry<String, Integer> entry : personalServiceLoads.entrySet()) {
			if (fullLoadMap.get(entry.getKey()) != null) {
				fullLoadMap.put(entry.getKey(), fullLoadMap.get(entry.getKey()) + personalServiceLoads.get(entry.getKey()));
			}
			else {
				fullLoadMap.put(entry.getKey(), personalServiceLoads.get(entry.getKey()));
			}
		}
		
		return fullLoadMap;
	}
	
	/**
	 * Calculate the new service combinations list based on the current load buffer.
	 * This is calculated by re-rating the service combinations based on failure rates calculated 
	 * from the load buffer.
	 * @return the newly calculated service combinations
	 */
	public List<ServiceCombination> calculateNewServiceCombinations() {	
		return analyzer.calculateNewServiceCombinations(getFullLoadMap());
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
		}
		
		Map<String, Integer> serviceLoads = getServiceLoads(serviceCombination, usedDescriptions);
		return new PlannerMessageContent(serviceLoads);
	}
	
	/**
	 * Generate planner message content that includes a map of service loads for each service inside 
	 * the given service combination and the current load map, without the data of the receiver.
	 * @param serviceCombination the given service combination
	 * @param registryEndpoints the given registry endpoints
	 * @param receiverEndpoint The endpoint of the receiver this message content is going to be sent to
	 * @param messageContentPercentage percentage of information that is given out
	 * @return the planner message content
	 */
	public PlannerMessageContent generateMessageContentEverything(ServiceCombination serviceCombination, List<String> registryEndpoints, String receiverEndpoint, int messageContentPercentage) {
		Map<String, Integer> fullLoadMap = getFullLoadMapPlusOwnLoads(serviceCombination, registryEndpoints, receiverEndpoint);
		Map<String, Integer> usedLoads = new HashMap<>();
		List<String> services = new ArrayList<>(fullLoadMap.keySet());
		
		int usedDescriptionsCount = (int) Math.ceil(fullLoadMap.size() * (messageContentPercentage / (double) 100));
		
		if (usedDescriptionsCount == fullLoadMap.size()) {
			usedLoads = fullLoadMap;
		}
		else {		
			for (int i = 0; i < usedDescriptionsCount; i++) {				
				int randomIndex = new Random().nextInt(services.size());
				usedLoads.put(services.get(randomIndex), fullLoadMap.get(services.get(randomIndex)));
				services.remove(randomIndex);
			}
		}
		
		return new PlannerMessageContent(usedLoads);
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
