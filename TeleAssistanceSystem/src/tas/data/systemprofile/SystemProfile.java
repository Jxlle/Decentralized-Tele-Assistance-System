package tas.data.systemprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import profile.SystemProfileVariable;
import profile.SystemRequirementType;
import tas.mape.planner.RatingType;

/**
 * Class storing data for how to execute the workflow
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be), 
 * based on the old input profile code of Yifan Ruan (ry222ad@student.lnu.se)
 */
public class SystemProfile {
	
	private int executionCycles, workflowCycles, maxProtocolIterations;
	private SystemType systemType;
	private ProtocolType protocolType;
	private RatingType ratingType;
	private HashMap<String, SystemRequirementType> entityRequirementTypes = new HashMap<>();
	private List<String> participatingEntities = new ArrayList<>();
	private List<SystemProfileVariable> variables = new ArrayList<>();
	
	/**
	 * Create an empty system profile
	 */
	public SystemProfile() {}
	
	/**
	 * Return the system profile system type
	 * @return the system profile system type
	 */
	public SystemType getSystemType() {
		return systemType;
	}
	
	/**
	 * Set the system profile system type to the given type
	 * @param systemType the given system type
	 */
	public void setSystemType(SystemType systemType) {
		this.systemType = systemType;
	}
	
	/**
	 * Return the system profile protocol type
	 * @return the system profile protocol type
	 */
	public ProtocolType getProtocolType() {
		return protocolType;
	}
	
	/**
	 * Set the system profile protocol class to the given type
	 * @param protocolType the given protocol type
	 */
	public void setProtocolType(ProtocolType protocolType) {
		this.protocolType = protocolType;
	}
	
	/**
	 * Return the system profile max amount of protocol iterations
	 * @return the system profile max amount of protocol iterations
	 */
	public int getMaxProtocolIterations() {
		return maxProtocolIterations;
	}
	
	/**
	 * Set the system profile max amount of protocol iterations to the given value
	 * @param executionCycles the given max amount of protocol iterations
	 */
	public void setMaxProtocolIterations(int maxProtocolIterations) {
		this.maxProtocolIterations = maxProtocolIterations;
	}
	
	/**
	 * Return the system profile amount of execution cycles
	 * @return the system profile amount of execution cycles
	 */
	public int getExecutionCycles() {
		return executionCycles;
	}
	
	/**
	 * Set the system profile amount of execution cycles to the given value
	 * @param executionCycles the given amount of execution cycles
	 */
	public void setExecutionCycles(int executionCycles) {
		this.executionCycles = executionCycles;
	}
	
	/**
	 * Return the system profile amount of workflow cycles
	 * @return the system profile amount of workflow cycles
	 */
	public int getWorkflowCycles() {
		return workflowCycles;
	}
	
	/**
	 * Set the system profile amount of workflow cycles to the given value
	 * @param workflowCycles the given amount of workflow cycles
	 */
	public void setWorkflowCycles(int workflowCycles) {
		this.workflowCycles = workflowCycles;
	}
	
	/**
	 * Return the system profile rating type
	 * @return the system profile rating type
	 */
	public RatingType getRatingType() {
		return ratingType;
	}
	
	/**
	 * Set the system profile rating type to the given system rating type
	 * @param ratingtype the given system rating type
	 */
	public void setRatingType(RatingType ratingType) {
		this.ratingType = ratingType;
	}
	
	/**
	 * Return the requirement type of a certain participating entity with the given entity name
	 * @param entityName the given entity name
	 * @return the entity requirement type 
	 */
	public SystemRequirementType getEntityRequirementType(String entityName) {
		return entityRequirementTypes.get(entityName);
	}
	
	/**
	 * Set the requirement type of a certain participating entity to the given type
	 * @param entityName the given entity name whose value needs to be set
	 * @param requirementType the given requirement type
	 * @throws IllegalArgumentException throws when the given entity name doesn't exist in the profile
	 */
	public void setRequirementType(String entityName, SystemRequirementType requirementType) {
		
		if (entityRequirementTypes.get(entityName) == null) {
			throw new IllegalArgumentException("The given entity name doesn't exist in the profile!");
		}
		
		entityRequirementTypes.put(entityName, requirementType);
	}
	
	/**
	 * Add a given entity name and requirement type to the profile
	 * @param entityName the given entity name
	 * @param requirementType the given requirement type
	 * @throws IllegalStateException throws when the system type has not yet been set
	 * @throws IllegalStateException throws when the maximum amount of entities has already been reached
	 */
	public void addEntity(String entityName, SystemRequirementType requirementType) throws IllegalStateException {
		
		if (systemType == null) {
			throw new IllegalStateException("System type needs to be set before changing the participating entities!");
		}
		
		if (participatingEntities.size() == systemType.getMaxEntities()) {
			throw new IllegalStateException("The maximum amount of entities has already been reached!");
		}
		
		participatingEntities.add(entityName);
		entityRequirementTypes.put(entityName,  requirementType);
	}
	
	/**
	 * Remove a given entity name and requirement type from the profile
	 * @param entityName the given entity name
	 */
	public void removeEntity(String entityName) {
		participatingEntities.remove(entityName);
		entityRequirementTypes.remove(entityName);
	}
	
	/**
	 * Clear the participating entities list
	 */
	public void clearEntities() {
		participatingEntities.clear();
	}
	
	/**
	 * Return the amount of participating entities
	 * @return the amount of participating entities
	 */
	public int getAmountOfParticipatingEntities() {
		return participatingEntities.size();
	}
	
	/**
	 * Return the participating entity at the given index in the system profile list
	 * @param index the given index
	 * @return the participating entity at the given index
	 */
	public String getParticipatingEntity(int index) {
		return participatingEntities.get(index);
	}
	
	/**
	 * Add new input profile variable
	 * @param variable the specific variable
	 */
	public void addVariable(SystemProfileVariable variable) {
		this.variables.add(variable);
	}
	
	/**
	 * Get list of variable names
	 * @return the variable names
	 */
	public List<String> getVariableNames() {
		List<String> variableNames = new ArrayList<>();
		variables.forEach(variable -> {
			variableNames.add(variable.getName());
		});
		
		return variableNames;
	}
	
	/**
	 * Return the variable with the name
	 * @param variableName the name of input profile variable
	 * @return the input profile variable
	 */
	public SystemProfileVariable getVariable(String variableName) {
		
		for (SystemProfileVariable variable:variables) {
			if (variable.getName().equals(variableName))
				return variable;
		}
		
		return null;
	}
}
