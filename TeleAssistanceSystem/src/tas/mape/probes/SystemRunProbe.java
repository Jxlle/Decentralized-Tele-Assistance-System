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
	
	ServiceCombinationQoSAnalyzer sca = new ServiceCombinationQoSAnalyzer();
	HashMap<String, List<Pair<Double, Double>>> dataPoints = new HashMap<>();
	
	/**
	 * Register the system probe to the given entity planner probe
	 * @param entity the given system entity
	 */
	public void connect(SystemEntity entity) {
		entity.getManagingSystem().getProbe().register(this);
		dataPoints.put(entity.getEntityName(), new ArrayList<>());
	}
	
	/**
	 * Reset the system probe
	 */
	public void reset() {
		dataPoints.clear();
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
		entityDataPoints.add(new Pair<Double, Double>(combinationCost, combinationFailureRate));
	}
}
