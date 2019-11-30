package service.auxiliary;

import java.io.Serializable;

/**
 * Definition of a service operation
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Operation implements Serializable{

	private static final long serialVersionUID = 1L;
	private String name;

	/**
	 * Return the operation name
	 * @return the operation name
	 */
	public String getOpName() {
		return name;
	}
	
	private Class<?>[] paramTypes;
	
	/**
	 * Return the type array of parameters
	 * @return the paramTypes
	 */
	public Class<?>[] getParamTypes() {
		return paramTypes;
	}

	private String returnType;
	
	/**
	 * Return the return type of the operation
	 * @return the return type
	 */
	public String getReturnType() {
		return returnType;
	}
	
	private double cost;
	
	/**
	 * Return the operation cost
	 * @return the operation cost
	 */
	public double getOpCost() {
	    return cost;
	}
	
	/**
	 * Set the operation cost
	 * @param opCost the new operation cost
	 */
	public void setOpCost(double opCost) {
	    this.cost = opCost;
	}


	/**
	 * Constructor
	 * @param opName the operation name
	 * @param paramTypes the type array of parameters
	 * @param returnType the return type
	 */
	public Operation(String opName,Class<?>[] paramTypes,String returnType){
		this.name=opName;
		this.paramTypes=paramTypes;
		this.returnType=returnType;
	}
	
	
	/**
	 * Override the "equals" method
	 */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Operation){
			Operation op=(Operation)obj;
			if(!name.equals(op.getOpName()))
				return false;
			if(!returnType.equals(op.getReturnType()))
				return false;
			for(int i=0;i<paramTypes.length;i++){
				if(!paramTypes[i].equals(op.getParamTypes()[i]))
					return false;
			}
			return true;
		}
		return false;
	}	
	
	/**
	 * Override the "toString" method
	 */
	@Override
	public String toString(){
		StringBuilder build=new StringBuilder();
		build.append(returnType+" "+name+" (");
		for(Class<?> type:paramTypes)
			build.append(type.getSimpleName()+",");
		build.delete(build.length()-1, build.length());
		build.append(")");
		return build.toString();
	}
}
