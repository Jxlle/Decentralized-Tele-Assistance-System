package tas.mape.probes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.util.Pair;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.ServiceCombination;
import tas.mape.system.entity.MAPEKSystemEntity;

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
	private List<Integer> protocolMessagesCount = new ArrayList<>();
	private HashMap<String, List<Double>> ratings = new HashMap<>();
	private HashMap<String, Knowledge> knowledges = new HashMap<>();
	private List<MAPEKSystemEntity> connectedEntities = new ArrayList<>();
	
	/**
	 * Register the system probe to the given entity planner probe
	 * @param entity the given system entity
	 */
	public void connect(MAPEKSystemEntity entity) {
		entity.getManagingSystem().getPlanner().getObserver().register(this);
		
		connectedEntities.add(entity);
		dataPoints.put(entity.getEntityName(), new ArrayList<>());
		chosenCombinations.put(entity.getEntityName(), new ArrayList<>());
		systemCycles.put(entity.getEntityName(), new ArrayList<>());
		ratings.put(entity.getEntityName(), new ArrayList<>());
	}
	
	/**
	 * Reset the system probe
	 */
	public void reset() {
		
		dataPoints.clear();
		chosenCombinations.clear();
		systemCycles.clear();
		knowledges.clear();
		ratings.clear();
		protocolMessagesCount.clear();
		
		// Unregister from connected entities
		for (MAPEKSystemEntity entity : connectedEntities) {
			entity.getManagingSystem().getPlanner().getObserver().unRegister(this);
		}
		
		connectedEntities.clear();
	}
	
	/**
	 * Return the probe ratings data
	 * @return the probe ratings data
	 */
	public HashMap<String, List<Double>> getRatings() {
		return ratings;
	}
	
	/**
	 * Return the probe protocol message count data
	 * @return the probe protocol message count data
	 */
	public List<Integer> getProtocolMessageCount() {
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
	public MAPEKSystemEntity getConnectedEntity(String entityName) {
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
	 * Return the maximum failure rate value in the probe data
	 * @return the maximum failure rate value in the probe data
	 */
	public double getMaxFailureRate() {
		
		double max = 0;
		
		for (List<Pair<Double, Double>> pointList : dataPoints.values()) {
			for (Pair<Double, Double> point : pointList) {
				
				if (point.getKey() > max) {
					max = point.getKey();
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
		
		// Add protocol message count to the data
		if (protocolMessagesCount.size() > chosenCombinations.get(knowledge.getParentEntityName()).size()) {
			int currentCount = protocolMessagesCount.get(chosenCombinations.get(knowledge.getParentEntityName()).size());
			
			if (currentCount < protocolMessages) {
				protocolMessagesCount.remove(chosenCombinations.get(knowledge.getParentEntityName()).size());
				protocolMessagesCount.add(protocolMessages);
			}
		}
		else {
			protocolMessagesCount.add(protocolMessages);
		}
		
		// Add knowledge to data
		knowledges.put(knowledge.getParentEntityName(), knowledge);
		
		// Add chosen service combination to the data
		List<ServiceCombination> EntityChosenCombinations = chosenCombinations.get(knowledge.getParentEntityName());
		EntityChosenCombinations.add(serviceCombination);
		
		// Add system cycle information to the data
		List<Integer> systemCyclesData = systemCycles.get(knowledge.getParentEntityName());
		systemCyclesData.add(knowledge.getSystemCycle());
	}

	/**
	 * Update the service combination data for all previous received service combinations
	 */
	@Override
	public void adaptationFinished() {	
		for (String entity : knowledges.keySet()) {
			
			int cycle = systemCycles.get(entity).get(systemCycles.get(entity).size() - 1);
			ServiceCombination currentCombination = chosenCombinations.get(entity).get(cycle - 1);
			Double combinationCost = sca.getRealServiceCombinationCost(currentCombination);
			Double combinationFailureRate = sca.getRealServiceCombinationFailureRate(currentCombination, knowledges.get(entity));
			
			// Add data points to the data
			List<Pair<Double, Double>> entityDataPoints = dataPoints.get(entity);
			entityDataPoints.add(new Pair<Double, Double>(combinationFailureRate, combinationCost));
			
			List<Double> entityRatings = ratings.get(entity);
			entityRatings.add(sca.getRealRatingValue(currentCombination, knowledges.get(entity), combinationFailureRate));
		}
	}
}
