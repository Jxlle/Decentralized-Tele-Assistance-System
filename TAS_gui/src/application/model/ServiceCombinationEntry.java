package application.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ServiceCombinationEntry {

    private SimpleIntegerProperty cycle;
    private SimpleStringProperty services;
    private SimpleDoubleProperty totalCost;
    private SimpleDoubleProperty totalFailRate;
    
	public ServiceCombinationEntry(int cycle, double totalCost, double totalFailRate) {
		this.cycle = new SimpleIntegerProperty(cycle);
		this.totalCost = new SimpleDoubleProperty(totalCost);
		this.totalFailRate = new SimpleDoubleProperty(totalFailRate);
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
}
