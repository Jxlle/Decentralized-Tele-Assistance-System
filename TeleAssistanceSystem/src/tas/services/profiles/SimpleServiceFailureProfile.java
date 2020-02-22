/**
 * 
 */
package tas.services.profiles;

import java.util.Random;

import service.atomic.ServiceProfile;
import service.auxiliary.ServiceDescription;


public class SimpleServiceFailureProfile extends ServiceProfile {
	
	public SimpleServiceFailureProfile() {
		type = "Failure";
	}
	
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
		
		Random rand = new Random();
		double failrate = 0;
		
		if (description.getCustomProperties().containsKey("FailureRate")) {
			failrate = (double) description.getCustomProperties().get("FailureRate");
		}
		
		if (rand.nextDouble() <= failrate) {
			return false;
		}
		
		return true;
	}

	@Override
	public Object postInvokeOperation(String operationName, Object result,
			Object... args) {
		return result;
	}
}
