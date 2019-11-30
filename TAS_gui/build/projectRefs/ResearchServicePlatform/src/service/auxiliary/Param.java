package service.auxiliary;

/**
 * Combination of type and value for a parameter
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Param{
	private Class<?> type;
	private Object value;
	
	/**
	 * Constructor
	 * @param value value of the parameter
	 */
	public Param(Object value){
		if(value!=null){
			this.type=value.getClass();
			this.value=value;}
		else{
			this.type=null;
			this.value=null;
		}
	}
	
	/**
	 * Return the parameter type
	 * @return the parameter type
	 */
	public Class<?> getType() {
	    return type;
	}
	
	/**
	 * Return the parameter value
	 * @return the parameter value
	 */
	public Object getValue() {
	    return value;
	}
	
	/**
	 * Set the parameter value
	 * @param value the new value
	 */
	public void setValue(Object value) {
	    this.value = value;
	}
	
	/**
	 * Set the parameter type
	 * @param type the new type
	 */
	public void setType(Class<?> type) {
	    this.type = type;
	}
	
	/**
	 * Override the default "toString" method
	 */
	@Override
	public String toString() {
	    return value.toString();
	}
}
