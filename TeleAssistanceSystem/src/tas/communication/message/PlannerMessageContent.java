package tas.communication.message;

import java.util.Map;

/**
 * Class representing the message content that planners will send to each other for communication.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class PlannerMessageContent implements ComponentMessageContent {
	
	// Map containing the load for each used public service
	Map<String, Integer> publicServiceUsage;
	
	/**
	 * Create a new planner message content object with a given public service usage map
	 * @param publicServiceUsage the given public service usage map
	 */
	public PlannerMessageContent(Map<String, Integer> publicServiceUsage) {
		this.publicServiceUsage = publicServiceUsage;
	}
	
	/**
	 * Return the public service usage map
	 * @return the public service usage map
	 */
	public Map<String, Integer> getPublicServiceUsage() {
		return publicServiceUsage;
	}
}
