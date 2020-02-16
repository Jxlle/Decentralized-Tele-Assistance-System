/**
 * 
 */
package profile;
import java.util.ArrayList;
import java.util.List;

/**
 * Combination of name and related list of values
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class SystemProfileVariable {

	private String name;
	private List<SystemProfileValue> values=new ArrayList<>();
	
	/**
	 * Construct
	 * @param name specific name
	 * @param values related list of values
	 */
	public SystemProfileVariable(String name, List<SystemProfileValue> values){
		this.name=name;
		this.values=values;
	}
	
	
	/**
	 * Return name
	 * @return the name of this variable
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Construct
	 * @param name specific name
	 */
	public SystemProfileVariable(String name){
		this.name=name;
	}
	
	/**
	 * Add new input profile value to the inherent list
	 * @param value the value to be added
	 */
	public void addValue(SystemProfileValue value){
		this.values.add(value);
	}
	
	/**
	 * Return list of values
	 * @return the list of values
	 */
	public List<SystemProfileValue> getValues(){
		return this.values;
	}
}
