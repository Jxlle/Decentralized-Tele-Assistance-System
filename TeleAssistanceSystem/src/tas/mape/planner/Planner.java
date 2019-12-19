package tas.mape.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.auxiliary.AbstractMessage;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.executer.Executer;
import tas.mape.knowledge.Knowledge;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents the planner component in a MAPE-K component
 */
public class Planner extends CommunicationComponent {

	// Fields
	private Executer executer;
	private Knowledge knowledge;
	private Boolean executed;
	private AbstractProtocol protocol;
	private List<PlanComponent> plan;
	private List<ServiceCombination> chosenServicesList;
	
	public Planner(String endpoint, Executer executer) {
		super(endpoint);
		this.executer = executer;
	}
	
	public AbstractProtocol getProtocol() {
		return protocol;
	}
	
	public void setProtocol(AbstractProtocol protocol) {
		this.protocol = protocol;
	}
	
	public void execute(List<ServiceCombination> chosenServicesList) {
		this.chosenServicesList = chosenServicesList;
	}
	
	public void makePlan(Map<Description, WeightedCollection<String>> chosenServices, Map<String, Integer> serviceLoads) {
		
		plan = new ArrayList<PlanComponent>();	
		plan.add(new PlanComponent(PlanComponentType.SET_USED_SERVICES, chosenServices));
		
		for (String loadEndpoint : serviceLoads.keySet()) {
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
	
	public Map<String, Integer> getServiceLoads(ServiceCombination chosenServices) {
		
		Map<String, Integer> serviceLoads = new HashMap<String, Integer>();
		
		for (Description description : chosenServices.getDescriptions()) {
			
			WeightedCollection<ServiceDescription> serviceUsage = chosenServices.getAllServices(description);
			
			for (ServiceDescription service : serviceUsage.getItems()) {		
				int serviceLoad = knowledge.getServiceLoad(description, serviceUsage.getChance(service));
				serviceLoads.compute(service.getServiceEndpoint(), (k, v) -> (v == null) ? 1 : v + serviceLoad);		
			}
		}
		
		return serviceLoads;
	}
	
	public List<Pair<String, Double>> getPublicServiceChances(ServiceCombination chosenServices, List<String> registryEndpoints) {
		
		List<Pair<String, Double>> serviceChances = new ArrayList<>();
		
		for (String registryEndpoint : registryEndpoints) {
			
			for (Description description : chosenServices.getDescriptions()) {		
				
				for (ServiceDescription service : chosenServices.getAllServices(description).getItems()) {
					
					if (service.getServiceRegistryEndpoint().equals(registryEndpoint)) {
						serviceChances.add(new Pair<String, Double>(service.getServiceEndpoint(), chosenServices.getAllServices(description).getChance(service)));
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
	public void receiveMessage(AbstractMessage message) throws NullPointerException {
		
		if (protocol == null) {
			throw new NullPointerException("Planner can't handle message receivement, no protocol selected.");
		}
		
		//protocol.receiveAndHandleMessage(message, this);
	}
}
