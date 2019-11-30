package application.model;

import java.math.BigDecimal;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ReliabilityEntry {
	
    private  SimpleStringProperty service;
    private  SimpleIntegerProperty invocationNum;
    private  SimpleIntegerProperty failNum;
    private  SimpleDoubleProperty failRate;
    private  SimpleDoubleProperty successRate;
    
    public ReliabilityEntry(){
    	
    }
    
    public ReliabilityEntry(String service){
    	this.service=new SimpleStringProperty(service);
    	this.invocationNum=new SimpleIntegerProperty(0);
    	this.failNum=new SimpleIntegerProperty(0);
    }
    
    public ReliabilityEntry(String service, int invocationNum, int failNum){
    	this.service=new SimpleStringProperty(service);
    	this.invocationNum=new SimpleIntegerProperty(invocationNum);
    	this.failNum=new SimpleIntegerProperty(failNum);
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

	public void setRate(){
		BigDecimal bd = new  BigDecimal(failNum.get()/(double)invocationNum.get()); 
    	this.failRate=new SimpleDoubleProperty(bd.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
    	bd=new BigDecimal(1-this.failRate.get());
    	this.successRate=new SimpleDoubleProperty(bd.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
	}
	
	public double getFailRate() {
		return failRate.get();
	}
	
	public double getSuccessRate(){
		return successRate.get();
	}

}
