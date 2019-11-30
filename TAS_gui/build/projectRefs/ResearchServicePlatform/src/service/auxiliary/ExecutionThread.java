package service.auxiliary;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ExecutionThread extends Thread{
	
	private static Set<Integer> tokens=new HashSet<>();
	
	private String name;
	private Runnable task;
	private double time;
	private int token;
	
	public ExecutionThread(String name,Runnable task){
		this.name=name;
		this.task=task;
		
		this.token=new Random().nextInt();
		while(tokens.contains(token))
			this.token=new Random().nextInt();
	}
	
	public ExecutionThread(String name,Runnable task,int token){
		this.name=name;
		this.task=task;
		this.token=token;
	}
	
	public int getToken(){
		return token;
	}
	
	public void run(){
		task.run();
	}
	
	public String getThreadName(){
		return this.name;
	}
	
	public double getCurrentTime(){
		return this.time;
	}
	
	public void incrementTime(double value){
		this.time+=value;
	}
	
	
}
