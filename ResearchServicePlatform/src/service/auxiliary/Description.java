package service.auxiliary;

public class Description {
	
	String serviceType;
	String opName;
	
	public Description(String serviceType, String opName) {
		this.serviceType = serviceType;
		this.opName = opName;
	}
	
	@Override
	public int hashCode(){
		return serviceType.hashCode() + opName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null) return false;
		if (obj == this) return true;
		
		if (obj instanceof Description) {
			Description description=(Description)obj;
			if(description.serviceType.equals(serviceType) && description.opName.equals(opName))
				return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
	    return serviceType + "." + opName;
	}
}
