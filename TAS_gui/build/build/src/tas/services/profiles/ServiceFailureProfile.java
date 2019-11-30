/**
 * 
 */
package tas.services.profiles;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.utility.Time;

/**
 * @author yfruan
 * @email  ry222ad@student.lnu.se
 *
 */
public class ServiceFailureProfile extends ServiceProfile {

	@ServiceProfileAttribute()
	public TreeMap<Integer,Double> failureRate=new TreeMap<>();

	public ServiceFailureProfile(){
		failureRate.put(0, 0.0);
	}
	
	public ServiceFailureProfile(double value){
		failureRate.put(0, value);
	}
	
	@Override
	public boolean preInvokeOperation(String operationName, Object... args) {
		
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

	@Override
	public Object postInvokeOperation(String operationName, Object result,
			Object... args) {
		return result;
	}
}
