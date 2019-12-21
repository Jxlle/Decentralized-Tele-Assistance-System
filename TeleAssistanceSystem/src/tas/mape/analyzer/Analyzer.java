package tas.mape.analyzer;

import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.Planner;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *  
 * Class that represents the analyzer component in a MAPE-K component
 */
public class Analyzer {
	
	// Fields
	private Knowledge knowledge;
	private Planner planner;
	private Boolean plannerRequired, executed;
	private RatingType ratingType;
	private int combinationLimit;
	private Map<String, Integer> QoSStrategies;
	private List<ServiceCombination> chosenServicesList;
	
	/**
	 * Create a new analyzer with a given knowledge and planner component, a combination limit and the service combination rating type.
	 * @param knowledge the given knowledge component
	 * @param planner the given planner component
	 * @param combinationLimit the given combination limit that will decide how much service combinations will be chosen in the execute step
	 * @param ratingType the given type of the rating for service combinations 
	 * @param QoSStrategies a map containing the strategy number for each QoS requirement
	 */
	public Analyzer(Knowledge knowledge, Planner planner, int combinationLimit, RatingType ratingType, Map<String, Integer> QoSStrategies) {
		this.knowledge = knowledge;
		this.planner = planner;
		this.combinationLimit = combinationLimit;
		this.ratingType = ratingType;
		this.QoSStrategies = QoSStrategies;
	}
	
	/**
	 * Return the strategy number for a given QoS requirement name
	 * @param requirementName the given QoS requirement name
	 * @return the strategy number
	 */
	public Integer getQoSStrategy(String requirementName) {
		return QoSStrategies.get(requirementName);
	}
	
	/**
	 * Update or add a certain QoS strategy number using a given QoS requirement name and strategy number
	 * @param requirementName the given QoS requirement name
	 * @param strategy the given strategy number
	 */
	public void setQoSStrategy(String requirementName, Integer strategy) {
		QoSStrategies.put(requirementName, strategy);
	}

	/**
	 * Execute the analyzer
	 */
	public void execute() {	
		
		// Choose service combinations
		chosenServicesList = chooseServices();
		
		// Check if planner is required
		if (chosenServicesList.size() > 0) {
			plannerRequired = true;
		}
		
		executed = true;
	}
	
	/**
	 * Trigger the planner when the analyzer has been executed and when the planner is needed.
	 */
	public void triggerPlanner() {
		if (plannerRequired && executed) {
			planner.execute(chosenServicesList);
			executed = false;
			plannerRequired = false;
		}
	}
	
	/**
	 * Return the chosen services of the analyzer.
	 * This method applies a strategy for a certain QoS requirement and returns the N (based on combination limit) best 
	 * service combinations rated with the analyzer rating type.
	 * @return The list of the best N chosen service combinations
	 */
	private List<ServiceCombination> chooseServices() {
		
		String requirementName = knowledge.getCurrentQoSRequirement();
		Map<Description, List<ServiceDescription>> usableServices = knowledge.getUsableServices();
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(requirementName);
		List<Goal> goals = knowledge.getGoals();
		
		return requirementClass.getServiceCombinations(getQoSStrategy(requirementName), combinationLimit, ratingType, goals, usableServices);
	}
}
