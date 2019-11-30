package tas.mape;

import java.util.Map;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Enum representing a plan component type.
 * @note Supports multiple effectors
 */
public enum PlanComponentType {
	
	INCREASE_LOAD("workflowEffector", "increaseServiceLoad", new Class<?>[] { String.class, int.class }),
	SET_USED_SERVICES("workflowEffector", "updateUsedServicesInfo", new Class<?>[] { Map.class });
	
	private final String effectorIdentifier;
	private final String methodName;
	private final Class<?>[] methodParameters;
	
	private PlanComponentType(String effectorIdentifier, String methodName, Class<?>[] methodParameters) {
		this.effectorIdentifier = effectorIdentifier;
		this.methodName = methodName;
		this.methodParameters = methodParameters;
	}
	
	public String getEffectorIdentifier() {
		return effectorIdentifier;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public Class<?>[] getMethodParameters() {
		return methodParameters;
	}
}
