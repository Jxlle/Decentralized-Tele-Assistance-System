package tas.mape;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class PlanComponent {
	
	private PlanComponentType planComponentType;
	private Object[] parameterValues;
	
	public PlanComponent(PlanComponentType planComponentType, Object... parameterValues) {
		this.planComponentType = planComponentType;
		this.parameterValues = parameterValues;
	}
	
	public PlanComponentType getPlanComponentType() {
		return planComponentType;
	}
	
	public Object[] getParameterValues() {
		return parameterValues;
	}
}
