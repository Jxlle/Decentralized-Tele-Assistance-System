/**
 * 
 */
package tas.services.profiles;

import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.auxiliary.ServiceDescription;
import service.utility.Time;

/**
 * @author yfruan
 * @email  ry222ad@student.lnu.se
 *
 */
public class ServiceFailureProfile extends ServiceProfile {
	
	@ServiceProfileAttribute()
	public TreeMap<Integer,Double> failureRate=new TreeMap<>();

	public ServiceFailureProfile(ServiceDescription description){
		
		if (description.getCustomProperties().containsKey("FailureRate")) {
			failureRate.put(0, (double) description.getCustomProperties().get("FailureRate"));
		}
		else {
			failureRate.put(0, 0.0);
		}
		
		type = "Failure";
	}
	
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {	
		
		if (failureRate.size() == 1 && description.getCustomProperties().containsKey("FailureRate")) {
			failureRate.put(failureRate.firstKey(), (double) description.getCustomProperties().get("FailureRate"));
		}
		
		int invocationNum=Time.steps.get();
		double rate=0;
		
		Iterator<Integer> iter=failureRate.keySet().iterator();
		int num;
		
		while(iter.hasNext()){
			num=iter.next();
			if(invocationNum>=num)
				rate=failureRate.get(num);
			if(invocationNum<num)
				break;
		}
		
		//System.out.println(rate);

		
		Random rand=new Random();
		if(rand.nextDouble()<rate){
			//System.out.println(rate);
			return false;
		}
		
		return true;
	}
	
	/*@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
		
		if (description.getCustomProperties())
		
		double rate = (double) description.getCustomProperties().get("FailureRate");
		
		Random rand=new Random();
		if(rand.nextDouble() < rate){
			return false;
		}
		
		return true;
	}*/

	@Override
	public Object postInvokeOperation(String operationName, Object result,
			Object... args) {
		return result;
	}
	
}
