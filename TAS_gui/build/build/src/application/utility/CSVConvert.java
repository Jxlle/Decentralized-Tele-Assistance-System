package application.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSVConvert {

	static int count=0;

	public static void execute(String readerPath, String writerPath){
		
		HashMap<String,Integer> serviceMap=new HashMap<>();
		
		serviceMap.put("MedicalService1", count++);
		serviceMap.put("MedicalService2", count++);
		serviceMap.put("MedicalService3", count++);
		
		serviceMap.put("AlarmService1", count++);
		serviceMap.put("AlarmService2", count++);
		serviceMap.put("AlarmService3", count++);
		
		serviceMap.put("DrugService", count++);
		
		serviceMap.put("AssistanceService", count++);
		
		
		FileManager reader=new FileManager(readerPath);
		reader.setMode(FileManager.READING);
		reader.open();
		
		FileManager writer=new FileManager(writerPath);
		writer.setMode(FileManager.WRITING);
		writer.open();
		
		String line;
		int invocationNum;
		String service;
		
		StringBuilder build=new StringBuilder();
		double totalTime=0;
		int result=1;
		int[] services={0,0,0,0,0,0,0,0};
		
		
		while((line=reader.readLine())!=null){
			//System.out.println(line);
						
			String[] str=line.split(",");	
			
			if(str.length>=3){
				
				// set invocation num
				//invocationNum=Integer.parseInt(str[0]);
				//build.append(String.valueOf(invocationNum)+",");
				
				// add service
				service=str[1];
				if(Boolean.parseBoolean(str[2])){
					
					// set result
					services[serviceMap.get(service)]=1;
					
					// add response time
					if(!service.equals("AssistanceService"))
						totalTime+=Double.parseDouble(str[5]);

				}
				else{
					
					// set result false
					result=0;
				}
				
				
				if(service.equals("AssistanceService")){
					
					double cost=Double.parseDouble(str[3]);
					
					// set invocation num
					build.append(str[0]+",");
					
					// set services
					for(int j=0;j<services.length;j++)
						build.append(services[j]+",");
					
					//set result
					//build.append(result?1:0+",");
					build.append(result+",");
					
					//set time
					build.append(totalTime+",");
					
					// set cost
					build.append(cost+",");
					
					System.out.println(build.toString());
					writer.writeLine(build.toString());
					
					build=new StringBuilder();
					totalTime=0;
					result=1;
					for(int j=0;j<services.length;j++)
						services[j]=0;
				}
			}
			
		}
			
		reader.close();
		writer.close();	
	}

	public static void main(String[] args){
		
		String readerPath="results" + File.separator + "result.csv";
		String writerPath="results" + File.separator + "result2.csv";
		
		new File(writerPath).delete();
		 
		execute(readerPath,writerPath);
	}

}
