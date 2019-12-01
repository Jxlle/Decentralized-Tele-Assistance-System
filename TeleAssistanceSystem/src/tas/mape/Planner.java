package tas.mape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.auxiliary.AbstractMessage;
import service.auxiliary.Description;
import service.auxiliary.WeightedCollection;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class Planner extends CommunicationComponent {

	Executer executer;
	Knowledge knowledge;
	List<PlanComponent> plan;
	
	public Planner(String endpoint, Executer executer) {
		super(endpoint);
		this.executer = executer;
	}
	
	public void execute(List<Map<Description, WeightedCollection<String>>> chosenServicesList) {
		// TODO
		communicateWith(chosenServicesList);
	}
	
	public void communicateWith(List<Map<Description, WeightedCollection<String>>> chosenServicesList) {
		// TODO
		
		Map<String, Integer> serviceLoads = getServiceLoads(chosenServicesList.get(0));
		makePlan(chosenServicesList.get(0), serviceLoads);
	}
	
	private void makePlan(Map<Description, WeightedCollection<String>> chosenServices, Map<String, Integer> serviceLoads) {
		
		plan = new ArrayList<PlanComponent>();	
		plan.add(new PlanComponent(PlanComponentType.SET_USED_SERVICES, chosenServices));
		
		for (String loadEndpoint : serviceLoads.keySet()) {
			plan.add(new PlanComponent(PlanComponentType.INCREASE_LOAD, loadEndpoint, serviceLoads.get(loadEndpoint)));
		}
		
	}
	
	private Map<String, Integer> getServiceLoads(Map<Description, WeightedCollection<String>> chosenServices) {
		
		Map<String, Integer> serviceLoads = new HashMap<String, Integer>();
		
		for (Description description : chosenServices.keySet()) {
			
			WeightedCollection<String> serviceUsage = chosenServices.get(description);
			
			for (String serviceEndpoint : serviceUsage.getItems()) {		
				int serviceLoad = knowledge.getServiceLoad(description, serviceUsage.getChance(serviceEndpoint));
				serviceLoads.compute(serviceEndpoint, (k, v) -> (v == null) ? 1 : v + serviceLoad);		
			}
		}
		
		return serviceLoads;
	}
	
	/**
	 * Trigger the executer
	 */
	public void triggerExecuter() {
		executer.execute(plan);
	}

	@Override
	public void receiveMessage(AbstractMessage message) {
		System.err.print("RECEIVED " + message + " " + getEndpoint());
		// TODO Auto-generated method stub
		// TODO transform message into planner message
	}
}
