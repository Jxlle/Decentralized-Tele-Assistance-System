package tas.mape.knowledge;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class representing a system goal
 * @note An entity should never contain 2 goals with the same type
 */
public class Goal {
	
	// Fields
	private GoalType type;
	private GoalRelation relation;
	private double value;
	
	// Goal type enum
	public enum GoalType {
				
		// Available goal types
		COST("Cost"),
		FAILURE_RATE("FailureRate");
		
		// String representation of the goal type
		private String requirementName;
		
		/**
		 * Create a new goal type with a given requirement name
		 * @param requirementName the given requirement name
		 */
		private GoalType(String requirementName) {
			this.requirementName = requirementName;
		}
		
		/**
		 * Return the requirement name
		 * @return the requirement name
		 */
		public String getRequirementName() {
			return requirementName;
		}
		
		/**
		 * Return a goal type from a given requirement name
		 * @param reqName the given requirement name
		 * @return the found goal type
		 * @throws IllegalArgumentException throw when no goal type with the given requirement name was found
		 */
	    public static GoalType fromString(String reqName) throws IllegalArgumentException {
	        for (GoalType goalType : GoalType.values()) {
	            if (goalType.requirementName.equalsIgnoreCase(reqName)) {
	                return goalType;
	            }
	        }
	        
	        throw new IllegalArgumentException("No goal type with requirement name " + reqName + " was found!");
	    }
	}
	
	// Available goal relations
	public enum GoalRelation {
		HIGHER_THAN,
		HIGHER_OR_EQUAL_TO,
		LOWER_THAN,
		LOWER_OR_EQUAL_TO;
	}
	
	/**
	 * Create a goal with a given type, relation and value
	 * @param type the given goal type
	 * @param relation the given goal relation
	 * @param value the given goal value
	 */
	public Goal(GoalType type, GoalRelation relation, double value) {
		this.type = type;
		this.relation = relation;
		this.value = value;
	}
	
	/**
	 * Return the goal type
	 * @return the goal type
	 */
	public GoalType getType() {
		return type;
	}
	
	/**
	 * Set the goal type to a given type
	 * @param type the given type
	 */
	public void setType(GoalType type) {
		this.type = type;
	}
	
	/**
	 * Return the goal relation
	 * @return the goal relation
	 */
	public GoalRelation getRelation() {
		return relation;
	}
	
	/**
	 * Set the goal relation to a given relation
	 * @param relation the given relation
	 */
	public void setRelation(GoalRelation relation) {
		this.relation = relation;
	}
	
	/**
	 * Return the goal value
	 * @return the goal value
	 */
	public double getValue() {
		return value;
	}
	
	/**
	 * Set the goal value to a given value
	 * @param value the given value
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * Check if a given value is valid for the goal
	 * @param givenValue the given value
	 * @return whether the given value is valid
	 * @throws throw when the goal relation check isn't implemented
	 */
	public boolean validValue(double givenValue) throws IllegalStateException {
		
		switch (relation) {
		
		case HIGHER_THAN:
			
			if (givenValue > value) {
				return true;
			}
			else {
				return false;
			}
				
		case HIGHER_OR_EQUAL_TO:
			
			if (givenValue >= value) {
				return true;
			}
			else {
				return false;
			}
			
		case LOWER_THAN:

			if (givenValue < value) {
				return true;
			}
			else {
				return false;
			}
			
		case LOWER_OR_EQUAL_TO:
			
			if (givenValue <= value) {
				return true;
			}
			else {
				return false;
			}
			
		default:
			
			throw new IllegalStateException("Goal relation check isn't implemented!");
			
		}
	}
}
