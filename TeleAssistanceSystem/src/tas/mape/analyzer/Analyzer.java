package tas.mape.analyzer;

import java.util.List;
import java.util.Map;

import profile.SystemRequirementType;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Knowledge;
import tas.mape.planner.Planner;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;

/**
 * Class that represents the analyzer component in a MAPE-K component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class Analyzer {
	
	// Fields
	private Knowledge knowledge;
	private Planner planner;
	private boolean plannerRequired, executed;
	private int combinationLimit;
	private RatingType ratingType;
	private Map<SystemRequirementType, Integer> requirementStrategies;
	private List<ServiceCombination> chosenServicesList;
	
	/**
	 * Create a new analyzer with a given knowledge and planner component, a combination limit and the service combination rating type.
	 * @param knowledge the given knowledge component
	 * @param planner the given planner component
	 * @param combinationLimit the given combination limit that will decide how much service combinations will be chosen in the execute step
	 * @param RequirementStrategies a map containing the strategy number for each requirement
	 */
	public Analyzer(Knowledge knowledge, Planner planner, int combinationLimit, Map<SystemRequirementType, Integer> requirementStrategies) {
		this.knowledge = knowledge;
		this.planner = planner;
		this.combinationLimit = combinationLimit;
		this.requirementStrategies = requirementStrategies;
	}
	
	/**
	 * Set the planner rating type to the given rating type
	 * @param ratingType the given rating type
	 */
	public void setRatingType(RatingType ratingType) {
		this.ratingType = ratingType;
	}
	
	/**
	 * Return the strategy number for a given system requirement
	 * @param requirement the given system requirement
	 * @return the strategy number
	 */
	public Integer getRequirementStrategy(SystemRequirementType requirement) {
		return requirementStrategies.get(requirement);
	}
	
	/**
	 * Update or add a certain requirement strategy number using a given system requirement and strategy number
	 * @param requirement the given system requirement
	 * @param strategy the given strategy number
	 */
	public void setRequirementStrategy(SystemRequirementType requirement, Integer strategy) {
		requirementStrategies.put(requirement, strategy);
	}
	
	/**
	 * Remove the requirement strategy with the given system requirement
	 * @param requirement the given system requirement
	 */
	public void removeRequirementStrategy(SystemRequirementType requirement) {
		requirementStrategies.remove(requirement);
	}
	
	/**
	 * Clear the requirement strategies map
	 */
	public void clearRequirementStrategies() {
		requirementStrategies.clear();
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
	 * @throws IllegalStateException throws when the rating type has not been set
	 */
	private List<ServiceCombination> chooseServices() throws IllegalStateException {
		
		if (ratingType == null) {
			throw new IllegalStateException("The rating type has not been set!");
		}
		
		SystemRequirementType requirement = SystemProfileDataHandler.activeProfile.getRequirementType();
		Map<Description, List<ServiceDescription>> usableServices = knowledge.getUsableServices();
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(requirement);
		List<Goal> goals = knowledge.getGoals();
		
		return requirementClass.getServiceCombinations(getRequirementStrategy(requirement), combinationLimit, ratingType, goals, usableServices);
	}
}
