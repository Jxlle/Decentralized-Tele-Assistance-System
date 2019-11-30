package tas.mape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.composite.SDCache;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 */
public class Knowledge {
	
	private String currentQoSRequirement;
	private Map<Description, Double> servicesUsageChance;
	private Map<String, Map<Integer, Double>> approximatedServiceFailureRates;
	private Map<String, Double> defaultFailureRates;
	private Map<Description, List<ServiceDescription>> usableServices;
	private int loadFailureDelta, workflowCyclesMax;
	
	// TODO
	public Knowledge(int loadFailureDelta, String currentQoSRequirement, Map<Description, Double> servicesUsageChance) {
		this.loadFailureDelta = loadFailureDelta;
		this.currentQoSRequirement = currentQoSRequirement;
		this.servicesUsageChance = servicesUsageChance;
		this.defaultFailureRates = defaultFailureRates;
	}
	
	public void setCurrentQoSRequirement(String currentQoSRequirement) {
		this.currentQoSRequirement = currentQoSRequirement;
	}
	
	public String getCurrentQoSRequirement() {
		return currentQoSRequirement;
	}
	
	public void setApproximatedServiceFailureRate(String serviceEndpoint, int load, double failureRate) {
		Map<Integer, Double> approximatedFailureRate = approximatedServiceFailureRates.get(serviceEndpoint);
		approximatedFailureRate.put(load, failureRate);
		approximatedServiceFailureRates.put(serviceEndpoint, approximatedFailureRate);
	}
	
	public Double getApproximatedServiceFailureRate(String serviceEndpoint, int load, double failureRate) {
		
		if (approximatedServiceFailureRates.get(serviceEndpoint) != null) {
			
			int keyLoad = load / loadFailureDelta * loadFailureDelta;
			Map<Integer, Double> serviceFailureTable = approximatedServiceFailureRates.get(serviceEndpoint);
			
			if (serviceFailureTable.get(keyLoad) != null) {
				
				Double approximatedServiceFailureRate = serviceFailureTable.get(keyLoad);			
				return approximatedServiceFailureRate;
			}
	
		}
		
		return defaultFailureRates.get(serviceEndpoint);

	}
	
	public Map<Description, List<ServiceDescription>> getUsableServices() {
		return usableServices;
	}
	
	public int getServiceLoad(Description description, double usePercentage) {
		return (int) (servicesUsageChance.get(description) * usePercentage * workflowCyclesMax);
	}
}
