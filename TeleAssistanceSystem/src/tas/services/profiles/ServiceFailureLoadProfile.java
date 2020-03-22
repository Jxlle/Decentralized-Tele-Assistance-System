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
	public TreeMap<Integer, Double> failureRate;
	
	/**
	 * Create a new service failure load profile.
	 */
	public ServiceFailureLoadProfile() {
		treeMapText = new Pair<String, String>("Use Percentage", "Success Rate Modifier");
		defaultEnabled = true;
		type = "Failure";
		constructFailRates();
	}
	
	// Construct default fail rate table
	private void constructFailRates() {
		failureRate = new TreeMap<>();
		failureRate.put(0, 1.0);
		failureRate.put(20, 0.98);
		failureRate.put(35, 0.95);
		failureRate.put(50, 0.91);
		failureRate.put(65, 0.85);
		failureRate.put(70, 0.8);
	}
	
	public Map.Entry<Integer, Double> getTableEntry(ServiceDescription description) {
		
		int usePercentage = (int) ((description.getLoad() / (double) (SystemProfileDataHandler.activeProfile.getWorkflowCycles() * SystemProfileDataHandler.activeProfile.getAmountOfParticipatingEntities())) * 100);
		//System.err.println("percentage for " + description.getServiceEndpoint() + ": " + usePercentage + " " + description.getLoad() + " " + (double) (SystemProfileDataHandler.activeProfile.getWorkflowCycles() * SystemProfileDataHandler.activeProfile.getAmountOfParticipatingEntities()));
		
		Map.Entry<Integer, Double> entry = failureRate.ceilingEntry(usePercentage);
		
		if (entry == null) {
			entry = failureRate.floorEntry(usePercentage);
		}
		
		return entry;
	}
	
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
		
		// Service doesn't fail if it can't fail :) 
		if (!description.getCustomProperties().containsKey("FailureRate")) {
			return true;
		}
		
		// Calculate success rate based on the table value and the service load
		double rate = 1 - (double) description.getCustomProperties().get("FailureRate");
		rate *= getTableEntry(description).getValue();
		
		Random rand = new Random();
		
		//System.err.println("FAIL RATE of " + description.getServiceEndpoint() + ", " +  (1 - rate));
		if (rand.nextDouble() >= rate) {
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
