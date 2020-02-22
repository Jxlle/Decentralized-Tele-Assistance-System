package tas.services.profiles;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javafx.util.Pair;
import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.auxiliary.ServiceDescription;


public class ServiceFailureLoadProfile extends ServiceProfile {
	
	@ServiceProfileAttribute()
	public TreeMap<Integer, Double> failureRate;
	
	public ServiceFailureLoadProfile() {
		treeMapText = new Pair<String, String>("Load", "Additional success rate (%)");
		defaultEnabled = true;
		type = "Failure";
		constructFailRates();
	}
	
	// Construct default fail rate table
	private void constructFailRates() {
		failureRate = new TreeMap<>();
		failureRate.put(0, 1.0);
		failureRate.put(50, 1.0);
		failureRate.put(75, 1.05);
		failureRate.put(100, 0.85);
		failureRate.put(125, 0.70);
		failureRate.put(180, 0.60);
	}
	
	// TODO Scale with workflow cycles
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
		
		if (!description.getCustomProperties().containsKey("FailureRate")) {
			return true;
		}
		
		double rate = 0.0;
		rate = 1 - (double) description.getCustomProperties().get("FailureRate");
		Map.Entry<Integer, Double> entry = failureRate.ceilingEntry(description.getLoad());
		
		if (entry == null) {
			entry = failureRate.floorEntry(description.getLoad());
		}
		
		rate *= entry.getValue();
		
		Random rand = new Random();
		
		if(rand.nextDouble() > rate) {
			System.err.print("failure load profile [FAILURE] " + description.getLoad() + " " + description.getServiceName() + " " + operationName + "\n");
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
