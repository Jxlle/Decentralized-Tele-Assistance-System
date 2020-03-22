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
    private SimpleIntegerProperty protocolMessageCount;
    private String usedServicesInfo;
    
	public ServiceCombinationEntry(int cycle, double totalCost, double totalFailRate, int protocolMessageCount, ServiceCombination serviceCombination) {
		this.cycle = new SimpleIntegerProperty(cycle);
		this.totalCost = new SimpleDoubleProperty(totalCost);
		this.totalFailRate = new SimpleDoubleProperty(totalFailRate);
		this.protocolMessageCount = new SimpleIntegerProperty(protocolMessageCount);
		usedServicesInfo = serviceCombination.toString();
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
	
	public int getProtocolMessageCount() {
		return protocolMessageCount.get();
	}
	
	public String getUsedServicesInfo() {
		return usedServicesInfo;
	}
}
