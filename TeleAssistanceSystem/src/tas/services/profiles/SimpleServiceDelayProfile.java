package tas.services.profiles;

import java.util.Random;

import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.auxiliary.ServiceDescription;

// Redundant class
public class SimpleServiceDelayProfile extends ServiceProfile{

	@ServiceProfileAttribute()
	public int delay;
    
	Random random = new Random();
	
	public SimpleServiceDelayProfile() {
		type = "Delay";
	}
	
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
	    
	    try {
		//Thread.sleep((random.nextInt(maxDelay - minDelay + 1) + minDelay));
			Thread.sleep(delay);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
		return true;
	}

	@Override
	public Object postInvokeOperation(String operationName, Object result,
			Object... args) {
		return result;
	}
}
