package application.model;

import java.math.BigDecimal;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class PerformanceEntry {
	
    private  SimpleStringProperty service;
    private  SimpleIntegerProperty invocationNum;
    private  SimpleIntegerProperty failNum;
    private  SimpleDoubleProperty avgResponseTime;
    
    private double total=0.0;
    
    public PerformanceEntry(){}
    
    public PerformanceEntry(String service){
    	this.service=new SimpleStringProperty(service);
    	this.invocationNum=new SimpleIntegerProperty(0);
    	this.failNum=new SimpleIntegerProperty(0);
    	this.avgResponseTime=new SimpleDoubleProperty(0);
    }
    
    public PerformanceEntry(String service, int invocationNum, int failNum,double avgResponseTime){
    	this.service=new SimpleStringProperty(service);
    	this.invocationNum=new SimpleIntegerProperty(invocationNum);
    	this.failNum=new SimpleIntegerProperty(failNum);
    	this.avgResponseTime=new SimpleDoubleProperty(avgResponseTime);
    }

    public void addResponseTime(double responseTime){
    	total+=responseTime;
    }
    
    public void setService(String service){
    	this.service=new SimpleStringProperty(service);
    }
    
	public String getService() {
		return service.get();
	}

	public void setInvocationNum(int invocationNum) {
    	this.invocationNum=new SimpleIntegerProperty(invocationNum);
	}
	
	public int getInvocationNum() {
		return invocationNum.get();
	}

	public void setFailNum(int failNum){
		this.failNum=new SimpleIntegerProperty(failNum);
	}
	
	public int getFailNum() {
		return failNum.get();
	}

	public void setAvgResponseTime(){
		if(invocationNum.get()==failNum.get())
	    	this.avgResponseTime=new SimpleDoubleProperty(-1);
		else{
			BigDecimal bd = new  BigDecimal(total/(invocationNum.get()-failNum.get())); 
			this.avgResponseTime=new SimpleDoubleProperty(bd.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
		}
	}
	
	public double getAvgResponseTime() {
		return avgResponseTime.get();
	}
}
