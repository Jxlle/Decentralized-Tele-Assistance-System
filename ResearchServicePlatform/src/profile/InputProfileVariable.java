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
public class InputProfileVariable {

	private String name;
	private List<InputProfileValue> values=new ArrayList<>();
	
	/**
	 * Construct
	 * @param name specific name
	 * @param values related list of values
	 */
	public InputProfileVariable(String name, List<InputProfileValue> values){
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
	public InputProfileVariable(String name){
		this.name=name;
	}
	
	/**
	 * Add new input profile value to the inherent list
	 * @param value the value to be added
	 */
	public void addValue(InputProfileValue value){
		this.values.add(value);
	}
	
	/**
	 * Return list of values
	 * @return the list of values
	 */
	public List<InputProfileValue> getValues(){
		return this.values;
	}
}
