package application.model;

import java.math.BigDecimal;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CostEntry {

    private SimpleStringProperty service;
    private SimpleIntegerProperty invocationNum;
    private SimpleDoubleProperty totalCost;
    
    public CostEntry(String service){
    	this.service=new SimpleStringProperty(service);
    	this.invocationNum=new SimpleIntegerProperty(0);
    	this.totalCost=new SimpleDoubleProperty(0);
    }
    
    public CostEntry(String service,int invocationNum, double totalCost){
    	this.service=new SimpleStringProperty(service);
    	this.invocationNum = new SimpleIntegerProperty(invocationNum);
    	this.totalCost=new SimpleDoubleProperty(totalCost);
    }

	public String getService() {
		return service.get();
	}

	public void setService(String service) {
    	this.service=new SimpleStringProperty(service);
	}

	public int getInvocationNum() {
		return invocationNum.get();
	}

	public void setInvocationNum(int invocationNum) {
		this.invocationNum = new SimpleIntegerProperty(invocationNum);
	}

	public double getTotalCost() {
	    return totalCost.get();
	}

	public void setTotalCost(double totalCost) {
	    BigDecimal bd = new  BigDecimal(totalCost); 
	    this.totalCost=new SimpleDoubleProperty(bd.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
	}
}
