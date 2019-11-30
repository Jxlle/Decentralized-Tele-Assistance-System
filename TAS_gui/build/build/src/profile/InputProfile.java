/**
 * 
 */
package profile;
import java.util.ArrayList;
import java.util.List;

/**
 * Profile for how to execute the workflow
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class InputProfile {
	
	protected int maxSteps;
	protected String qosRequirement;
	protected List<InputProfileVariable> variables=new ArrayList<>();
	protected List<Requirement> requirements=new ArrayList<>();
		
	/**
	 * Constructor without any parameters
	 */
	public InputProfile(){
		variables=new ArrayList<>();
		requirements=new ArrayList<>();
	}
	
	/**
	 * Constructor with parameters
	 * @param maxSteps  steps for executing the workflow
	 * @param qosRequirement  the specific QoS requirements to be satisfied
	 * @param variables   specific variables
	 */
	public InputProfile(int maxSteps, String qosRequirement,List<InputProfileVariable> variables){
		this.maxSteps=maxSteps;
		this.qosRequirement=qosRequirement;
		this.variables=variables;
	}
	
	public void addRequirement(Requirement requirement){
		this.requirements.add(requirement);
	}
	
	public List<Requirement> getRequirements(){
		return this.requirements;
	}
	
	public Requirement getRequirement(String reqName){
		for(Requirement req:requirements){
			if(req.getName().equals(reqName))
				return req;
		}
		return null;
	}
	
	public List<String> getRequirementNames(){
		List<String> reqNames=new ArrayList<>();
		if(requirements==null){
			requirements=new ArrayList<>();
			return reqNames;
		}
		requirements.forEach(req->{
			reqNames.add(req.getName());
		});
		return reqNames;
	}

	/**
	 * Get the max steps for executing the workflow
	 * @return the max steps
	 */
	public int getMaxSteps() {
		return maxSteps;
	}

	/**
	 * Set the max steps
	 * @param maxSteps the max steps
	 */
	public void setMaxSteps(int maxSteps){
		this.maxSteps=maxSteps;
	}
	
	/**
	 * Get QoS requirement
	 * @return the QoS requirements
	 */
	public String getQosRequirement() {
		return qosRequirement;
	}
	
	/**
	 * Set the QoS requirement
	 * @param qosRequirement the QoS requirement
	 */
	public void setQosRequirement(String qosRequirement){
		this.qosRequirement=qosRequirement;
	}
	
	/**
	 * Add new input profile variable
	 * @param variable the specific variable
	 */
	public void addVariable(InputProfileVariable variable){
		this.variables.add(variable);
	}

	
	/**
	 * Get list of variable names
	 * @return the variable names
	 */
	public List<String> getVariableNames(){
		List<String> variableNames=new ArrayList<>();
		variables.forEach(variable->{
			variableNames.add(variable.getName());
		});
		return variableNames;
	}
	
	
	/**
	 * Return the variable with the name
	 * @param variableName the name of input profile variable
	 * @return the input profile variable
	 */
	public InputProfileVariable getVariable(String variableName){
		for(InputProfileVariable variable:variables){
			if(variable.getName().equals(variableName))
				return variable;
		}
		return null;
	}

}
