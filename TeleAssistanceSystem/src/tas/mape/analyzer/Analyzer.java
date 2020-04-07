package tas.mape.analyzer;

import java.util.List;

import profile.SystemRequirementType;
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
	private boolean executed;
	private int combinationLimit;
	private RatingType ratingType;
	private int serviceGenerationStrategy;
	private List<ServiceCombination> chosenServicesList;
	
	/**
	 * Create a new analyzer with a given knowledge and planner component, a combination limit and a service generation strategy
	 * @param knowledge the given knowledge component
	 * @param planner the given planner component
	 * @param combinationLimit the given combination limit that will decide how much service combinations will be chosen in the execute step
	 * @param serviceGenerationStrategy the given service generation strategy
	 */
	public Analyzer(Knowledge knowledge, Planner planner, int combinationLimit, int serviceGenerationStrategy) {
		this.knowledge = knowledge;
		this.planner = planner;
		this.combinationLimit = combinationLimit;
		this.serviceGenerationStrategy = serviceGenerationStrategy;
	}
	
	/**
	 * Set the planner rating type to the given rating type
	 * @param ratingType the given rating type
	 */
	public void setRatingType(RatingType ratingType) {
		this.ratingType = ratingType;
	}
	
	/**
	 * Return the service generation strategy number 
	 * @return the service generation strategy number
	 */
	public int getServiceGenerationStrategy() {
		return serviceGenerationStrategy;
	}
	
	/**
	 * Return the combination limit
	 * @return the combination limit
	 */
	public int getCombinationLimit() {
		return combinationLimit;
	}
	
	/**
	 * Set the service generation strategy
	 * @param serviceGenerationStrategy the given service generation strategy
	 */
	public void setServiceGenerationStrategy(int serviceGenerationStrategy) {
		this.serviceGenerationStrategy = serviceGenerationStrategy;
	}

	/**
	 * Execute the analyzer
	 * @throws IllegalStateException throws when no service combinations are found
	 */
	public void execute() throws IllegalStateException {	
		
		// Choose service combinations
		chosenServicesList = chooseServices();
		
		// throw exception when no service combinations are found
		if (chosenServicesList.size() == 0) {
			throw new IllegalStateException("No service combinations are found!");
		}
		
		executed = true;
	}
	
	/**
	 * Trigger the planner when the analyzer has been executed.
	 */
	public void triggerPlanner() {
		if (executed) {
			//System.err.print("Executing planner...\n");
			planner.execute(chosenServicesList);
			executed = false;
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
		
		SystemRequirementType requirement = knowledge.getSystemRequirement();
		AbstractWorkflowQoSRequirement requirementClass = knowledge.getQoSRequirementClass(requirement);
		
		return requirementClass.getServiceCombinations(serviceGenerationStrategy, combinationLimit, ratingType, knowledge);
	}
}
