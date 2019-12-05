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
// TODO "plannerrequired" and "executed" add needed
public class Analyzer {
	
	// Properties
	private Knowledge knowledge;
	private Planner planner;
	private Boolean executed;
	private int combinationLimit;
	private Map<String, Integer> QoSStrategies;
	private List<ServiceCombination> chosenServicesList;
	
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
		
		executed = true;
	}
	
	/**
	 * Trigger the planner
	 */
	public void triggerPlanner() {
		if (executed) {
			planner.execute(chosenServicesList);
			executed = false;
		}
	}
	
	/**
	 * Return the chosen services of the analyzer given a QoS requirement name and a map of usable services.
	 * This method applies a strategy for a certain QoS requirement and returns the N (based on local property) best service combinations based on that.
	 * @param requirementName the QoS requirement name
	 * @param usableServices a map of services that can be used per description
	 * @return The list of the best N chosen service combinations
	 */
	private List<ServiceCombination> chooseServices(String requirementName, Map<Description, List<ServiceDescription>> usableServices) {
		
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(requirementName);
		int strategy = getQoSStrategy(requirementName);
		
		return requirementClass.applyStrategy(strategy, combinationLimit, usableServices);
	}
}
