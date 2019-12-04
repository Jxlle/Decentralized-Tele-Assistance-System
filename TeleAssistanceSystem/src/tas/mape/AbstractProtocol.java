package tas.mape;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractProtocol {
	
	List<CommunicationComponent> components;
	
	public abstract void executeProtocol(List<CommunicationComponent> components); 
	
	public abstract void startProtocol(CommunicationComponent startComponent);
	
	public abstract void setResult(CommunicationComponent component);
	
	
	
	/*{
	    this.components = components;
		Method method = null;
		
		try {
			method = this.getClass().getMethod("executeProtocol" + components.size() + "Components");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		try {
			method.invoke(this);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}*/
}
