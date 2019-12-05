package tas.mape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.auxiliary.AbstractMessage;
import service.auxiliary.Description;
import service.auxiliary.WeightedCollection;
import tas.mape.knowledge.Knowledge;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents the planner component in a MAPE-K component
 */
public class Planner extends CommunicationComponent {

	Executer executer;
	Knowledge knowledge;
	private Boolean executed;
	List<PlanComponent> plan;
	List<ServiceCombination> chosenServicesList;
	
	public Planner(String endpoint, Executer executer) {
		super(endpoint);
		this.executer = executer;
	}
	
	public void execute(List<ServiceCombination> chosenServicesList) {
		this.chosenServicesList = chosenServicesList;
		executed = true;
	}
	
	private void makePlan(Map<Description, WeightedCollection<String>> chosenServices, Map<String, Integer> serviceLoads) {
		
		plan = new ArrayList<PlanComponent>();	
		plan.add(new PlanComponent(PlanComponentType.SET_USED_SERVICES, chosenServices));
		
		for (String loadEndpoint : serviceLoads.keySet()) {
			plan.add(new PlanComponent(PlanComponentType.INCREASE_LOAD, loadEndpoint, serviceLoads.get(loadEndpoint)));
		}
		
		// Extra: update cache with new registry info
		if (knowledge.getRegistryPlanComponents().size() != 0) {
			
			for (PlanComponent registryPlanComponent : knowledge.getRegistryPlanComponents()) {
				plan.add(registryPlanComponent);
			}
			
		}
		
	}
	
	private Map<String, Integer> getServiceLoads(ServiceCombination chosenServices) {
		
		Map<String, Integer> serviceLoads = new HashMap<String, Integer>();
		
		for (Description description : chosenServices.getDescriptions()) {
			
			WeightedCollection<String> serviceUsage = chosenServices.getAllServices(description);
			
			for (String serviceEndpoint : serviceUsage.getItems()) {		
				int serviceLoad = knowledge.getServiceLoad(description, serviceUsage.getChance(serviceEndpoint));
				serviceLoads.compute(serviceEndpoint, (k, v) -> (v == null) ? 1 : v + serviceLoad);		
			}
		}
		
		return serviceLoads;
	}
	
	private List<Pair<String, Double>> getPublicServiceChances(ServiceCombination chosenServices, List<String> registryEndpoints) {
		
		List<Pair<String, Double>> serviceChances = new ArrayList<>();
		
		for (String registryEndpoint : registryEndpoints) {
			
			for (Description description : chosenServices.getDescriptions()) {		
				
				for (String serviceEndpoint : chosenServices.getAllServices(description).getItems()) {
					
					if (chosenServices.getServiceRegistry(serviceEndpoint).equals(registryEndpoint)) {
						serviceChances.add(new Pair<String, Double>(serviceEndpoint, chosenServices.getAllServices(description).getChance(serviceEndpoint)));
					}
					
				}
				
			}
			
		}
		
		return serviceChances;
	}
	
	/**
	 * Trigger the executer
	 */
	public void triggerExecuter() {
		if (executed) {
			executer.execute(plan);
			executed = false;
		}
	}

	@Override
	public void receiveMessage(AbstractMessage message) {
		System.err.print("RECEIVED " + message + " " + getEndpoint());
		// TODO transform message into planner message, use protocol
	}
}
