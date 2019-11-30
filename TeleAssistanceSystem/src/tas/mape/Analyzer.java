package tas.mape;

import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class Analyzer {
	
	private Knowledge knowledge;
	private Planner planner;
	
	public Analyzer(Knowledge knowledge, Planner planner) {
		this.knowledge = knowledge;
		this.planner = planner;
	}

	public void execute() {
		
		String QoSRequirement = knowledge.getCurrentQoSRequirement();
		Map<Description, List<ServiceDescription>> services = knowledge.getUsableServices();
		
	}
	
	public void triggerPlanner() {
		planner.execute();
	}	
}
