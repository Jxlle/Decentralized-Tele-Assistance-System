package tas.mape.probes;

import service.adaptation.probes.AbstractProbe;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.ServiceCombination;

/**
 * Class that monitors chosen service combinations in the planner
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class PlannerProbe extends AbstractProbe<PlannerProbeInterface> {
    
	/**
	 * Notify subscribed probes that a service combination has been chosen.
	 * @param serviceCombination the chosen service combination
	 * @param knowledge the knowledge used to choose the service combination
	 * @param protocolMessages the amount of messages that were sent during the protocol
	 */
	public void serviceCombinationChosen(ServiceCombination serviceCombination, Knowledge knowledge, int protocolMessages) {
    	for (PlannerProbeInterface plannerProbeInterface : subscribers) {
    		plannerProbeInterface.serviceCombinationChosen(serviceCombination, knowledge, protocolMessages);
    	}
	}
	
}
