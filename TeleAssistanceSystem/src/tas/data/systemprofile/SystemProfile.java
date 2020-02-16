package tas.data.systemprofile;

import java.util.ArrayList;
import java.util.List;

import profile.SystemProfileVariable;
import profile.SystemRequirementType;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.planner.Planner;
import tas.mape.planner.RatingType;
import tas.mape.system.entity.SystemEntity;
import tas.mape.system.structure.AbstractSystem;

/**
 * Class storing data for how to execute the workflow
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be), 
 * based on the old input profile code of Yifan Ruan (ry222ad@student.lnu.se)
 */
public class SystemProfile {
	
	private int executionCycles, workflowCycles;
	private SystemType systemType;
	private ProtocolType protocolType;
	private SystemRequirementType requirementType;
	private RatingType ratingType;
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
	 * Return the system profile requirement type
	 * @return the system profile requirement type
	 */
	public SystemRequirementType getRequirementType() {
		return requirementType;
	}
	
	/**
	 * Set the system profile requirement type to the given system requirement type
	 * @param requirementType the given system requirement type
	 */
	public void setRequirementType(SystemRequirementType requirementType) {
		this.requirementType = requirementType;
	}
	
	/**
	 * Return the system profile rating type 
	 * @return the system profile rating type 
	 */
	public RatingType getRatingType() {
		return ratingType;
	}
	
	/**
	 * Set the system profile rating type to the given rating type
	 * @param ratingType the given rating type
	 */
	public void setRatingType(RatingType ratingType) {
		this.ratingType = ratingType;
	}
	
	/**
	 * Add a given entity name to the participating entities list
	 * @param entityName the given entity name
	 */
	public void addEntity(String entityName) {
		participatingEntities.add(entityName);
	}
	
	/**
	 * Remove a given entity name from the participating entities list
	 * @param entityName the given entity name
	 */
	public void removeEntity(String entityName) {
		participatingEntities.remove(entityName);
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
