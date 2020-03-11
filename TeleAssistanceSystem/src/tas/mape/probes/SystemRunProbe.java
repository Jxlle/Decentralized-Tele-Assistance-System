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
	private HashMap<String, List<Pair<Double, Double>>> dataPoints = new HashMap<>();
	private List<SystemEntity> connectedEntities = new ArrayList<>();
	
	/**
	 * Register the system probe to the given entity planner probe
	 * @param entity the given system entity
	 */
	public void connect(SystemEntity entity) {
		entity.getManagingSystem().getProbe().register(this);
		connectedEntities.add(entity);
		dataPoints.put(entity.getEntityName(), new ArrayList<>());
	}
	
	/**
	 * Reset the system probe
	 */
	public void reset() {
		
		dataPoints.clear();
		
		// Unregister from connected entities
		for (SystemEntity entity : connectedEntities) {
			entity.getManagingSystem().getProbe().unRegister(this);
		}
	}
	
	/**
	 * Return the probe data points
	 * @return the probe data points
	 */
	public HashMap<String, List<Pair<Double, Double>>> getDataPoints() {
		return dataPoints;
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
	 * Update the current service combination data with the given
	 * @param serviceCombination the chosen service combination
	 * @param knowledge the knowledge used to choose the service combination
	 */
	@Override
	public void serviceCombinationChosen(ServiceCombination serviceCombination, Knowledge knowledge) {
		
		Double combinationCost = sca.getRealServiceCombinationCost(serviceCombination);
		Double combinationFailureRate = sca.getRealServiceCombinationFailureRate(serviceCombination, knowledge);
		
		List<Pair<Double, Double>> entityDataPoints = dataPoints.get(knowledge.getParentEntityName());
		entityDataPoints.add(new Pair<Double, Double>(combinationFailureRate, combinationCost));
	}
}
