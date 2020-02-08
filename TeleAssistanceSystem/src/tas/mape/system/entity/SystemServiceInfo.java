package tas.mape.system.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import service.atomic.AtomicService;
import service.registry.ServiceRegistry;
import tas.services.alarm.AlarmService;
import tas.services.drug.DrugService;
import tas.services.medical.MedicalAnalysisService;
import tas.services.profiles.ServiceDelayProfile;
import tas.services.profiles.ServiceFailureLoadProfile;
import tas.services.profiles.SimpleServiceFailureProfile;

//TODO
//XML Services loader
public class SystemServiceInfo {

    private ServiceRegistry serviceRegistry, serviceRegistry2;
    private AlarmService alarm1, alarm2, alarm3;
    private MedicalAnalysisService medicalAnalysis1, medicalAnalysis2, medicalAnalysis3;
    private DrugService drugService;
    
    private Map<String, AtomicService> atomicServices = new HashMap<>();
    private List<Class<?>> serviceProfileClasses = new ArrayList<>();
    private List<ServiceRegistry> serviceRegistries = new ArrayList<>();
    private LinkedHashMap<String, String> serviceTypes = new LinkedHashMap<>();
	
    public SystemServiceInfo() {
    	initializeServices();
    }
    
    public void addAllServices(AtomicService... services){
    	for (AtomicService service:services) {
    		atomicServices.put(service.getServiceDescription().getServiceName(), service);	
    		this.serviceTypes.put(service.getServiceDescription().getServiceName(), 
    				service.getServiceDescription().getServiceType());
    	}
    	
    }
    
    public List<ServiceRegistry> getRegistries() {
    	return serviceRegistries;
    }
    
    public LinkedHashMap<String,String> getServiceTypes(){
    	return this.serviceTypes;
    }
    
    public List<Class<?>> getServiceProfileClasses(){
    	return this.serviceProfileClasses;
    }
    
    public AtomicService getService(String name){
    	return atomicServices.get(name);
    }
    
    public void initializeServices() {

		serviceRegistry = new ServiceRegistry("ServiceRegistry", "se.lnu.service.registry");
		serviceRegistry.startService();
		
		serviceRegistry2 = new ServiceRegistry("ServiceRegistry2", "se.lnu.service.registry2");
		serviceRegistry2.startService();
		
		serviceRegistries.add(serviceRegistry);
		serviceRegistries.add(serviceRegistry2);
	
		// Alarm Services
		alarm1 = new AlarmService("AlarmService1", "service.alarmService1");
		alarm1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		alarm1.getServiceDescription().setOperationCost("triggerAlarm", 4.0);
		alarm1.getServiceDescription().getCustomProperties().put("FailureRate", 0.11);
		alarm1.startService();
		alarm1.register(serviceRegistry2);
	
		alarm2 = new AlarmService("AlarmService2", "service.alarmService2");
		alarm2.getServiceDescription().getCustomProperties().put("Cost", 12.0);
		alarm2.getServiceDescription().getCustomProperties().put("preferred", true);
		alarm2.getServiceDescription().setOperationCost("triggerAlarm", 12.0);
		alarm2.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		//alarm2.addServiceProfile(new ServiceFailureProfile(0.04));
		alarm2.startService();
		alarm2.register(serviceRegistry);
	
		alarm3 = new AlarmService("AlarmService3", "service.alarmService3");
		alarm3.getServiceDescription().getCustomProperties().put("Cost", 2.0);
		alarm3.getServiceDescription().setOperationCost("triggerAlarm", 2.0);
		alarm3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
		//alarm3.addServiceProfile(new ServiceFailureProfile(0.18));
		alarm3.startService();
		alarm3.register(serviceRegistry);
	
		// Medical Analysis Services
		medicalAnalysis1 = new MedicalAnalysisService("MedicalService1", "service.medical1");
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("preferred", false);
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		medicalAnalysis1.getServiceDescription().setOperationCost("analyzeData", 4.0);
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("FailureRate", 0.12);
		//medicalAnalysis1.addServiceProfile(new ServiceFailureProfile(0.12));
		//medicalAnalysis1.addServiceProfile(new ServiceFailureProfile(0.12));
		medicalAnalysis1.startService();
		medicalAnalysis1.register(serviceRegistry);
	
		medicalAnalysis2 = new MedicalAnalysisService("MedicalService2", "service.medical2");
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("preferred", true);
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("Cost", 14.0);
		medicalAnalysis2.getServiceDescription().setOperationCost("analyzeData", 14.0);
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		medicalAnalysis2.startService();
		medicalAnalysis2.register(serviceRegistry);
	
		medicalAnalysis3 = new MedicalAnalysisService("MedicalService3", "service.medical3");
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("preferred", false);
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("Cost", 2.15);
		medicalAnalysis3.getServiceDescription().setOperationCost("analyzeData", 2.15);
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
		medicalAnalysis3.startService();
		medicalAnalysis3.register(serviceRegistry);
	
		// Drug Services
		drugService = new DrugService("DrugService", "service.drug");
		drugService.getServiceDescription().getCustomProperties().put("preferred", true);
		drugService.getServiceDescription().getCustomProperties().put("Cost", 2.0);
		drugService.getServiceDescription().setOperationCost("changeDoses", 5.0);
		drugService.getServiceDescription().setOperationCost("changeDrug", 5.0);
		//drugService.getServiceDescription().getCustomProperties().put("FailureRate", 0.01);
		drugService.startService();
		drugService.register(serviceRegistry);
		
		this.addAllServices(alarm1, alarm2, alarm3, medicalAnalysis1, medicalAnalysis2, medicalAnalysis3, drugService);
		
		this.serviceProfileClasses.add(ServiceDelayProfile.class);
		this.serviceProfileClasses.add(SimpleServiceFailureProfile.class);
		this.serviceProfileClasses.add(ServiceFailureLoadProfile.class);
    }
    
    public void stopServices() {
    	
		serviceRegistry.stopService();
	
		alarm1.stopService();
		alarm2.stopService();
		alarm3.stopService();
	
		medicalAnalysis1.stopService();
		medicalAnalysis2.stopService();
		medicalAnalysis3.stopService();
	
		drugService.stopService();
    }
}
