package tas.services.profiles;

import java.util.Random;

import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.utility.Time;
	
public class SimpleServiceDelayProfile extends ServiceProfile{

	//@ServiceProfileAttribute()
	//public int minDelay;
	
	//@ServiceProfileAttribute()
	//public int maxDelay;
	
	@ServiceProfileAttribute()
	public int delay;
    
	Random random = new Random();
	
	@Override
	public boolean preInvokeOperation(String operationName, Object... args) {
	    
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
