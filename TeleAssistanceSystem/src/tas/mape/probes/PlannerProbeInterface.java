package tas.mape.probes;

import tas.mape.knowledge.Knowledge;
import tas.mape.planner.ServiceCombination;

/**
 * Interface used for probes that collect system run data for generating graphs
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public interface PlannerProbeInterface {
	
	/**
	 * Probe the choice of a specific service combination
	 * @param serviceCombination the chosen service combination
	 * @param knowledge the knowledge used to choose the service combination
	 */
	public void serviceCombinationChosen(ServiceCombination serviceCombination, Knowledge knowledge, int protocolMessages);
	
	/**
	 * Probe that a system run is finished
	 */
	public void systemRunFinished();
}
