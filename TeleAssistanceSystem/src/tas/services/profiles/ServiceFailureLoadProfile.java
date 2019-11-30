package tas.services.profiles;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import service.atomic.ServiceProfile;
import service.auxiliary.ServiceDescription;


public class ServiceFailureLoadProfile extends ServiceProfile {
	
	public TreeMap<Integer, Double> failureRate;
	
	public ServiceFailureLoadProfile() {
		type = "Failure";
		constructFailRates();
	}
	
	// TODO User defined in future? (XML)
	// TODO Different fail rates per service?
	private void constructFailRates() {
		// Test fail rate
		failureRate = new TreeMap<>();
		failureRate.put(0, 1.0);
		failureRate.put(50, 1.0);
		failureRate.put(75, 0.95);
		failureRate.put(100, 0.85);
		failureRate.put(125, 0.70);
		failureRate.put(180, 0.60);
	}
	
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
		
		if (!description.getCustomProperties().containsKey("FailureRate")) {
			return true;
		}
		
		double rate = 0.0;
		rate = (double) description.getCustomProperties().get("FailureRate");
		Map.Entry<Integer, Double> entry = failureRate.ceilingEntry(description.getLoad());
		
		if (entry == null) {
			entry = failureRate.floorEntry(description.getLoad());
		}
		
		rate = rate * entry.getValue();
		
		Random rand=new Random();
		
		if(rand.nextDouble() < rate){
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
