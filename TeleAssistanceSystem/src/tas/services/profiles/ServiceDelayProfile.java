package tas.services.profiles;

import java.util.Iterator;
import java.util.TreeMap;

import javafx.util.Pair;
import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.auxiliary.ServiceDescription;
import service.utility.SimClock;
import service.utility.Time;
	
public class ServiceDelayProfile extends ServiceProfile{
	
	@ServiceProfileAttribute()
	public TreeMap<Integer,Double> delay=new TreeMap<>();
	
	public ServiceDelayProfile(double value){
		delay.put(0,value);
		treeMapText = new Pair<String, String>("Invocations", "Delay");
		type = "Delay";
	}

	public ServiceDelayProfile(){
		this(0.0);
	}

	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
				
		int invocationNum=Time.steps.get();
		double delayValue=0;
		
		Iterator<Integer> iter=delay.keySet().iterator();
		int num;
		while(iter.hasNext()){
			num=iter.next();
			if(invocationNum>=num)
				delayValue=delay.get(num);
			if(invocationNum<num)
				break;
		}
		
		/*
	    try {
			Thread.sleep(delayValue);
	    } catch (InterruptedException e) {
	    	e.printStackTrace();
	    }*/
		
    	SimClock.increment(delayValue);
		
		return true;
	}

	@Override
	public Object postInvokeOperation(String operationName, Object result,
			Object... args) {
		return result;
	}
}
