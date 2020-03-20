package tas.mape.probes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.util.Pair;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.ServiceCombination;
import tas.mape.system.entity.SystemEntity;

/**
 * Class used as a probe that handles service combination data used to generate graphs
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class SystemRunProbe implements PlannerProbeInterface {
	
	private ServiceCombinationQoSAnalyzer sca = new ServiceCombinationQoSAnalyzer();
	private HashMap<String, List<ServiceCombination>> chosenCombinations = new HashMap<>();
	private HashMap<String, List<Pair<Double, Double>>> dataPoints = new HashMap<>();
	private HashMap<String, List<Integer>> systemCycles = new HashMap<>();
	private HashMap<String, List<Integer>> protocolMessagesCount = new HashMap<>();
	private List<SystemEntity> connectedEntities = new ArrayList<>();
	
	/**
	 * Register the system probe to the given entity planner probe
	 * @param entity the given system entity
	 */
	public void connect(SystemEntity entity) {
		entity.getManagingSystem().getProbe().register(this);
		connectedEntities.add(entity);
		dataPoints.put(entity.getEntityName(), new ArrayList<>());
		chosenCombinations.put(entity.getEntityName(), new ArrayList<>());
		systemCycles.put(entity.getEntityName(), new ArrayList<>());
		protocolMessagesCount.put(entity.getEntityName(), new ArrayList<>());
	}
	
	/**
	 * Reset the system probe
	 */
	public void reset() {
		
		dataPoints.clear();
		chosenCombinations.clear();
		systemCycles.clear();
		
		// Unregister from connected entities
		for (SystemEntity entity : connectedEntities) {
			entity.getManagingSystem().getProbe().unRegister(this);
		}
		
		connectedEntities.clear();
	}
	
	/**
	 * Return the probe protocol message count data
	 * @return the probe protocol message count data
	 */
	public HashMap<String, List<Integer>> getProtocolMessageCount() {
		return protocolMessagesCount;
	}
	
	/**
	 * Return the probe data points
	 * @return the probe data points
	 */
	public HashMap<String, List<Pair<Double, Double>>> getDataPoints() {
		return dataPoints;
	}
	
	/**
	 * Return the probe chosen service combinations data
	 * @return the probe chosen service combinations data
	 */
	public HashMap<String, List<ServiceCombination>> getChosenCombinations() {
		return chosenCombinations;
	}
	
	/**
	 * Return the probe system cycles data
	 * @return the probe system cycles data
	 */
	public HashMap<String, List<Integer>> getSystemCycles() {
		return systemCycles;
	}
	
	/**
	 * Return the connected entity with the given entity name
	 * @param entityName
	 * @return the connected entity with the given entity name
	 */
	public SystemEntity getConnectedEntity(String entityName) {
		return connectedEntities.stream().filter(x -> x.getEntityName().equals(entityName)).findFirst().orElse(null);
	}
	
	/**
	 * Return the maximum cost value in the probe data
	 * @return the maximum cost value in the probe data
	 */
	public double getMaxCost() {
		
		double max = 0;
		
		for (List<Pair<Double, Double>> pointList : dataPoints.values()) {
			for (Pair<Double, Double> point : pointList) {
				
				if (point.getValue() > max) {
					max = point.getValue();
				}
				
			}
		}
		
		return max;
	}

	/**
	 * Update the current service combination data with the given service combination, knowledge
	 * @param serviceCombination the chosen service combination
	 * @param knowledge the knowledge used to choose the service combination
	 * @param protocolMessages the amount of messages that were sent during the protocol
	 */
	@Override
	public void serviceCombinationChosen(ServiceCombination serviceCombination, Knowledge knowledge, int protocolMessages) {
		
		Double combinationCost = sca.getRealServiceCombinationCost(serviceCombination);
		Double combinationFailureRate = sca.getRealServiceCombinationFailureRate(serviceCombination, knowledge);
		
		// Add data points to the data
		List<Pair<Double, Double>> entityDataPoints = dataPoints.get(knowledge.getParentEntityName());
		entityDataPoints.add(new Pair<Double, Double>(combinationFailureRate, combinationCost));
		
		// Add chosen service combination to the data
		List<ServiceCombination> EntityChosenCombinations = chosenCombinations.get(knowledge.getParentEntityName());
		EntityChosenCombinations.add(serviceCombination);
		
		// Add system cycle information to the data
		List<Integer> systemCyclesData = systemCycles.get(knowledge.getParentEntityName());
		systemCyclesData.add(knowledge.getSystemCycle());
		
		// Add protocol message count to the data
		List<Integer> protocolMessageCount = protocolMessagesCount.get(knowledge.getParentEntityName());
		protocolMessageCount.add(protocolMessages);
	}
}
