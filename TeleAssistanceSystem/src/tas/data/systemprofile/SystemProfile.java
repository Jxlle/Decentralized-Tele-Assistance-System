package tas.data.systemprofile;

import java.util.ArrayList;
import java.util.List;

import profile.SystemProfileVariable;
import profile.SystemRequirementType;
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
	private Class<? extends AbstractSystem<SystemEntity>> systemClass;
	private List<SystemProfileVariable> variables = new ArrayList<>();
	private SystemRequirementType requirementType;
	private RatingType ratingType;
	
	/**
	 * Create a new system profile with given data
	 * @param executionCycles the given amount of execution cycles the system will execute
	 * @param workflowCycles the given amount of cycles that the workflow will execute in one execution cycle
	 * @param requirementType the given system requirement type
	 * @param ratingType the given rating type
	 * @param systemClass the given system class used to depict which system is used for workflow execution
	 * @param variables the given list of profile variables
	 */
	public SystemProfile(int executionCycles, int workflowCycles, SystemRequirementType requirementType, RatingType ratingType, 
			Class<? extends AbstractSystem<SystemEntity>> systemClass, List<SystemProfileVariable> variables) {
		this.executionCycles = executionCycles;
		this.workflowCycles = workflowCycles;
		this.requirementType = requirementType;
		this.ratingType = ratingType;
		this.systemClass = systemClass;
		this.variables = variables;
	}
	
	/**
	 * Create an empty system profile
	 */
	public SystemProfile() {}
	
	/**
	 * Return the system profile system class
	 * @return the system profile system class
	 */
	public Class<? extends AbstractSystem<SystemEntity>> getSystemClass() {
		return systemClass;
	}
	
	/**
	 * Set the system profile system class to the given class
	 * @param systemClass the given system class
	 */
	public void setSystemClass(Class<? extends AbstractSystem<SystemEntity>> systemClass) {
		this.systemClass = systemClass;
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
		List<String> variableNames=new ArrayList<>();
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
