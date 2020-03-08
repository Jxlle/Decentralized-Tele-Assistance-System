package tas.services.profiles;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javafx.util.Pair;
import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.auxiliary.ServiceDescription;
import tas.data.systemprofile.SystemProfileDataHandler;

/**
 * A class used for letting services fail depending on their future load.
 * This class doesn't calculate the fail rate depending on the current load, but
 * it uses the approximated future load of a service and increases its failure rate depending
 * on the (load, failure rate) table.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class ServiceFailureLoadProfile extends ServiceProfile {
	
	@ServiceProfileAttribute()
	public TreeMap<Double, Double> failureRate;
	
	/**
	 * Create a new service failure load profile.
	 */
	public ServiceFailureLoadProfile() {
		treeMapText = new Pair<String, String>("Load", "Additional success rate (%)");
		defaultEnabled = true;
		type = "Failure";
		constructFailRates();
	}
	
	// Construct default fail rate table
	private void constructFailRates() {
		failureRate = new TreeMap<>();
		failureRate.put(0.0, 1.0);
		failureRate.put(0.2, 0.95);
		failureRate.put(0.35, 0.90);
		failureRate.put(0.50, 0.85);
		failureRate.put(0.65, 0.70);
		failureRate.put(0.7, 0.60);
	}
	
	// TODO Scale with workflow cycles
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
		
		// Service doesn't fail if it can't fail :) 
		if (!description.getCustomProperties().containsKey("FailureRate")) {
			return true;
		}
		
		// Calculate success rate based on the table value and the service load
		double rate = 1 - (double) description.getCustomProperties().get("FailureRate");
		double usePercentage = description.getLoad() / SystemProfileDataHandler.activeProfile.getWorkflowCycles();
		
		Map.Entry<Double, Double> entry = failureRate.ceilingEntry(usePercentage);
		
		if (entry == null) {
			entry = failureRate.floorEntry(usePercentage);
		}
		
		rate *= entry.getValue();
		
		Random rand = new Random();
		
		if (rand.nextDouble() > rate) {
			//System.err.print("failure load profile [FAILURE] " + description.getLoad() + " " + description.getServiceName() + " " + operationName + "\n");
			return false;
		}
		
		// Service didn't fail
		return true;
	}

	/**
	 * Does nothing
	 */
	@Override
	public Object postInvokeOperation(String operationName, Object result,
			Object... args) {
		return result;
	}
}
