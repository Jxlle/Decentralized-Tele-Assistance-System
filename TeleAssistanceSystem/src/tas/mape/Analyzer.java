package tas.mape;

import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *  
 * Class that represents the analyzer component in a MAPE-K component
 */
public class Analyzer {
	
	// Properties
	private Knowledge knowledge;
	private Planner planner;
	private int combinationLimit;
	private Map<String, Integer> QoSStrategies;
	private List<Map<Description, WeightedCollection<String>>> chosenServicesList;
	
	/**
	 * Create a new analyzer with a given knowledge and planner component, a combination limit and a map of QoS strategies.
	 * @param knowledge the knowledge component
	 * @param planner the planner component
	 * @param combinationLimit the combination limit that will decide how much service combinations will be chosen in the execute step
	 * @param QoSStrategies a map containing the strategy number for each QoS reuirement
	 */
	public Analyzer(Knowledge knowledge, Planner planner, int combinationLimit, Map<String, Integer> QoSStrategies) {
		this.knowledge = knowledge;
		this.planner = planner;
		this.combinationLimit = combinationLimit;
		this.QoSStrategies = QoSStrategies;
	}
	
	/**
	 * Return the strategy number for a given QoS requirement name
	 * @param requirementName the QoS requirement name
	 * @return the QoS strategy number
	 */
	public int getQoSStrategy(String requirementName) {
		return QoSStrategies.get(requirementName);
	}
	
	/**
	 * Update or add a certain QoS strategy using a given QoS requirement name and strategy number
	 * @param requirementName the QoS requirement name
	 * @param strategy the strategy number
	 */
	public void setQoSStrategy(String requirementName, int strategy) {
		QoSStrategies.put(requirementName, strategy);
	}

	/**
	 * Execute the analyzer
	 */
	public void execute() {	
		
		// Get QoS requirement and chosen strategy
		String requirementName = knowledge.getCurrentQoSRequirement();
		
		// Choose service options
		Map<Description, List<ServiceDescription>> usableServices = knowledge.getUsableServices();
		chosenServicesList = chooseServices(requirementName, usableServices);
	}
	
	/**
	 * Trigger the planner
	 */
	public void triggerPlanner() {
		planner.execute(chosenServicesList);
	}
	
	/**
	 * Return the chosen services of the analyzer given a QoS requirement name and a map of usable services.
	 * This method applies a strategy for a certain QoS requirement and returns the N (based on local property) best service combinations based on that.
	 * @param requirementName the QoS requirement name
	 * @param usableServices a map of services that can be used per description
	 * @return a list of the N best service combinations where each element consists of a map that depicts which services (endpoint + weight) must be used for which description (method type + name)
	 */
	private List<Map<Description, WeightedCollection<String>>> chooseServices(String requirementName, Map<Description, List<ServiceDescription>> usableServices) {
		
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(requirementName);
		int strategy = getQoSStrategy(requirementName);
		
		return requirementClass.applyStrategy(strategy, combinationLimit, usableServices);
	}
}
