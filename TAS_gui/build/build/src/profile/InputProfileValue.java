/**
 * 
 */
package profile;

/**
 * Combination of data and related ratio
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class InputProfileValue {
	
	private Object data;
	private double ratio;
	
	/** 
	 * Constructor
	 * @param data the data
	 * @param ratio the definition of how many times to execute with this data
	 */
	public InputProfileValue(Object data, double ratio){
		this.data=data;
		this.ratio=ratio;
	}
	
	/**
	 * Return data
	 * @return the data of the value
	 */
	public Object getData(){
		return this.data;
	}
	
	/**
	 * Return ration
	 * @return the ration of the value
	 */
	public double getRatio(){
		return this.ratio;
	}
	
	/**
	 * Set the ratio
	 * @param ratio the new ratio
	 */
	public void setRatio(double ratio){
		this.ratio=ratio;
	}
	
	/**
	 * Set the data
	 * @param data the new data
	 */
	public void setData(Object data){
		this.data=data;
	}
}
