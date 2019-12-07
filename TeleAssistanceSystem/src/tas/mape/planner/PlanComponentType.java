package tas.mape.planner;

import java.util.Map;

import service.auxiliary.ServiceDescription;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Enum representing a plan component type.
 * @note Supports multiple effectors
 */
public enum PlanComponentType {
	
	// Available types
	INCREASE_LOAD("workflowEffector", "increaseServiceLoad", new Class<?>[] { String.class, int.class }),
	SET_USED_SERVICES("workflowEffector", "updateUsedServicesInfo", new Class<?>[] { Map.class }),
	ADD_TO_CACHE("workflowEffector", "addService", new Class<?>[] { ServiceDescription.class }),
	REMOVE_FROM_CACHE("workflowEffector", "removeService", new Class<?>[] { ServiceDescription.class });
	
	// Properties
	private final String effectorIdentifier;
	private final String methodName;
	private final Class<?>[] methodParameterTypes;
	
	/**
	 * Create a new plan component type with a given effector id, method name and method parameter types
	 * @param effectorIdentifier the given effector id
	 * @param methodName the given method name
	 * @param methodParameterTypes the given method parameter types
	 */
	private PlanComponentType(String effectorIdentifier, String methodName, Class<?>[] methodParameterTypes) {
		this.effectorIdentifier = effectorIdentifier;
		this.methodName = methodName;
		this.methodParameterTypes = methodParameterTypes;
	}
	
	/**
	 * Return the effector identifier
	 * @return the effector identifier
	 */
	public String getEffectorIdentifier() {
		return effectorIdentifier;
	}
	
	/**
	 * Return the method name
	 * @return the method name
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Return the parameter types
	 * @return the parameter types
	 */
	public Class<?>[] getMethodParameterTypes() {
		return methodParameterTypes;
	}
}
