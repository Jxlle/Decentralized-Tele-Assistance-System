package service.utility;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import service.auxiliary.ExecutionThread;

public class SimClock {
	
	private static volatile boolean parallel=false;
	private static double time;
	
	private static Map<Integer,Double> parallelTimes=new ConcurrentHashMap<>();
	
	public static void increment(double value){
		if(parallel){
			
			int token=((ExecutionThread)Thread.currentThread()).getToken();
			
			synchronized(parallelTimes){
				if(parallelTimes.containsKey(token))
					parallelTimes.put(token, parallelTimes.get(token)+value);
				else
					parallelTimes.put(token, value);
			}
			
						
			//((ExecutionThread)Thread.currentThread()).incrementTime(value);
			
			/*
			if(Thread.currentThread() instanceof ExecutionThread){
				((ExecutionThread)Thread.currentThread()).incrementTime(value);
			}*/
		}
		else
			time+=value;
	}
	
	
	public static double getCurrentTime(){
		if(parallel){
			int token=((ExecutionThread)Thread.currentThread()).getToken();
			if(parallelTimes.containsKey(token))
				return parallelTimes.get(token);
			else
				return 0;
		}
		return time;
	}
	
	public static boolean isParallel(){
		return parallel;
	}

	public static void beginParallel(){
		parallel=true;
		parallelTimes=new ConcurrentHashMap<>();
	}
	
	public static void endParallel(){
		parallel=false;
		double maxTime=0.0;
		for(double value:parallelTimes.values()){
			if(maxTime<value)
				maxTime=value;
		}
		time+=maxTime;
		
		//System.out.println("MaxTime:"+maxTime);
	}
}
