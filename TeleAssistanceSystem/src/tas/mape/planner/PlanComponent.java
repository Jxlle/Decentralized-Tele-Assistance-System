package tas.mape.planner;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class representing the component of a plan
 */
public class PlanComponent {
	
	// Fields
	// Component type
	private PlanComponentType planComponentType;
	
	// Parameter values for the to be executed component method
	private Object[] parameterValues;
	
	/**
	 * Create a new plan component with a given type and parameter values
	 * @param planComponentType the type of the component
	 * @param parameterValues the parameter values of the component
	 */
	public PlanComponent(PlanComponentType planComponentType, Object... parameterValues) {
		this.planComponentType = planComponentType;
		this.parameterValues = parameterValues;
	}
	
	/**
	 * Return the plan component type
	 * @return the plan component type
	 */
	public PlanComponentType getPlanComponentType() {
		return planComponentType;
	}
	
	/**
	 * Return the plan component parameter values
	 * @return the plan component parameter values
	 */
	public Object[] getParameterValues() {
		return parameterValues;
	}
}
