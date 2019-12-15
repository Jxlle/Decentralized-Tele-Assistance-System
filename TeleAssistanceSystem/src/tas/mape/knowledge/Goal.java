package tas.mape.knowledge;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class representing a system goal
 */
public class Goal {
	
	// Fields
	private GoalType type;
	private GoalRelation relation;
	private double value;
	
	// Available goal types
	public enum GoalType {
		COST,
		FAILURE_RATE;
	}
	
	// Available goal relations
	public enum GoalRelation {
		HIGHER_THAN,
		HIGHER_OR_EQUAL_TO,
		LOWER_THAN,
		LOWER_OR_EQUAL_TO
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
}
