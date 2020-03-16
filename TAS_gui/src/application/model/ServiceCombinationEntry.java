package application.model;

import java.util.Map;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.planner.ServiceCombination;

public class ServiceCombinationEntry {

    private SimpleIntegerProperty cycle;
    private SimpleDoubleProperty totalCost;
    private SimpleDoubleProperty totalFailRate;
    private String usedServicesInfo;
    
	public ServiceCombinationEntry(int cycle, double totalCost, double totalFailRate, ServiceCombination serviceCombination) {
		this.cycle = new SimpleIntegerProperty(cycle);
		this.totalCost = new SimpleDoubleProperty(totalCost);
		this.totalFailRate = new SimpleDoubleProperty(totalFailRate);
		generateServiceCombinationInfo(serviceCombination);
	}
	
	public int getCycle() {
		return cycle.get();
	}
	
	public double getTotalCost() {
		return totalCost.get();
	}
	
	public double getTotalFailRate() {
		return totalFailRate.get();
	}
	
	public String getUsedServicesInfo() {
		return usedServicesInfo;
	}
	
	private void generateServiceCombinationInfo(ServiceCombination serviceCombination) {
		
		usedServicesInfo = "";
		Map<Description, WeightedCollection<ServiceDescription>> allServices = serviceCombination.getAllServices();
		
		for (Description descr : allServices.keySet()) {
			usedServicesInfo += "[" + descr.toString() +"]:\n";
			
			for (ServiceDescription service : allServices.get(descr).getItems()) {
				usedServicesInfo += "\t> service name: " + service.getServiceEndpoint() + "\n";
				usedServicesInfo += "\t\t>> service registry: " + service.getServiceRegistryEndpoint() + "\n";
				usedServicesInfo += "\t\t>> usage chance: " + (allServices.get(descr).getChance(service) * 100) + "%\n";
			}
			
			usedServicesInfo += "\n";
		}
		
	}
}
