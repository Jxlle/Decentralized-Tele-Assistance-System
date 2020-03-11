package tas.data.serviceinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import service.atomic.AtomicService;
import service.registry.ServiceRegistry;
import tas.services.alarm.AlarmService;
import tas.services.drug.DrugService;
import tas.services.medical.MedicalAnalysisService;
import tas.services.profiles.ServiceFailureLoadProfile;

/**
 * Class used to hold data about the used services and registries in the system.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class GlobalServiceInfo {
    
	// TODO Look what types do in GUI
    private static List<AtomicService> services = new ArrayList<>();
    private static List<ServiceRegistry> serviceRegistries = new ArrayList<>();
    private static LinkedHashMap<String, String> serviceTypes = new LinkedHashMap<>();
    private static List<Class<?>> serviceProfileClasses = Arrays.asList(
    	//ServiceDelayProfile.class,
    	//SimpleServiceFailureProfile.class,
    	ServiceFailureLoadProfile.class
    );
    
    /**
     * Private constructor
     */
    private GlobalServiceInfo() {}
    
    /**
     * Start all services
     */
    public static void startServices() {
    	for (AtomicService service : services) {
    		service.startService();
    	}
    }
    
    /**
     * Stop all services
     */
    public void stopServices() {
    	for (AtomicService service : services) {
    		service.stopService();
    	}
    }
    
    /**
     * Reset all service loads
     */
    public static void resetServiceLoads() {
    	for (AtomicService service : services) {
    		service.getServiceDescription().resetLoad();
    	}
    }
    
    /**
     * Set the currently used services to the given service list
     * @param services the given service list
     */
    public static void setServiceRegistries(List<ServiceRegistry> serviceRegistries) {
    	GlobalServiceInfo.serviceRegistries = serviceRegistries;
    }
    
    /**
     * Return the service registry with the given endpoint if there is one.
     * Otherwise return null.
     * @param registryEndpoint the given service registry endpoint
     * @return the matched service registry endpoint or null if there was no registry match
     */
    public static ServiceRegistry getServiceRegistry(String registryEndpoint) {
    	return serviceRegistries.stream().filter(x -> x.getServiceDescription().getServiceEndpoint().equals(registryEndpoint)).findFirst().orElse(null);
    }
    
    /**
     * Return a list of all currently used service registries
     * @return a list of all currently used service registries
     */
    public static List<ServiceRegistry> getServiceRegistries() {
    	return serviceRegistries;
    }
    
    /**
     * Set the currently used services to the given service list
     * @param services the given service list
     */
    public static void setServices(List<AtomicService> services) {
    	GlobalServiceInfo.services = services;
    }
    
    /**
     * Return the service with the given endpoint if there is one.
     * Otherwise return null.
     * @param serviceEndpoint the given service endpoint
     * @return the matched service endpoint or null if there was no service match
     */
    public static AtomicService getService(String serviceEndpoint) {
    	return services.stream().filter(x -> x.getServiceDescription().getServiceName().equals(serviceEndpoint)).findFirst().orElse(null);
    }
    
    /**
     * Return a list of all currently used services
     * @return a list of all currently used services
     */
    public static List<AtomicService> getServices() {
    	return services;
    }
    
    /**
     * Return a linked hash map of all service types
     * @return a linked hash map of all service types
     */
    public static LinkedHashMap<String,String> getServiceTypes() {
    	return serviceTypes;
    }
    
    /**
     * Return the service profile classes list
     * @return the service profile classes list
     */
	public static List<Class<?>> getServiceProfileClasses() {
		return serviceProfileClasses;
	}

	/**
	 * Set the service profile classes list to the given list
	 * @param serviceProfileClasses the given service profile classes list
	 */
	public static void setServiceProfileClasses(List<Class<?>> serviceProfileClasses) {
		GlobalServiceInfo.serviceProfileClasses = serviceProfileClasses;
	}
    
    /**
     * Save the current service data to a given file
     * @param file the given file
     */
    public static void saveData(File file) {
    	GlobalServiceInfoWriter.writeToXml(file);
    }
    
    /**
     * Load service data from a given file
     * @param file the given file
     */
    public static void loadData(File file) {
    	ChangeToDefaultServices();
    	//GlobalServiceInfoLoader.loadFromXml(file);
    }
    
    /**
     * Method mostly used for testing.
     * Changes the service data of this global service info object to the default data used when starting the simulator.
     * Can be used to restore the default XML file when accidentally deleted.
     */
    public static void ChangeToDefaultServices() {
    	
    	// Reset data
    	services = new ArrayList<>();
    	serviceRegistries = new ArrayList<>();
    	serviceTypes = new LinkedHashMap<>();
    	
    	// Service registries
		ServiceRegistry serviceRegistry = new ServiceRegistry("ServiceRegistry", "se.lnu.service.registry");
		serviceRegistry.startService();
		
		ServiceRegistry serviceRegistry2 = new ServiceRegistry("ServiceRegistry2", "se.lnu.service.registry2");
		serviceRegistry2.startService();
		
		serviceRegistries.add(serviceRegistry);
		serviceRegistries.add(serviceRegistry2);
	
		
		// Alarm Services
		AlarmService alarm1 = new AlarmService("AlarmService1", "service.alarmService1");
		alarm1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		alarm1.getServiceDescription().setOperationCost("triggerAlarm", 4.0);
		alarm1.getServiceDescription().getCustomProperties().put("FailureRate", 0.11);
		alarm1.startService();
		alarm1.register(serviceRegistry2);
	
		AlarmService alarm2 = new AlarmService("AlarmService2", "service.alarmService2");
		alarm2.getServiceDescription().getCustomProperties().put("Cost", 12.0);
		alarm2.getServiceDescription().getCustomProperties().put("preferred", true);
		alarm2.getServiceDescription().setOperationCost("triggerAlarm", 12.0);
		alarm2.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		alarm2.startService();
		alarm2.register(serviceRegistry);
	
		AlarmService alarm3 = new AlarmService("AlarmService3", "service.alarmService3");
		alarm3.getServiceDescription().getCustomProperties().put("Cost", 2.0);
		alarm3.getServiceDescription().setOperationCost("triggerAlarm", 2.0);
		alarm3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
		alarm3.startService();
		alarm3.register(serviceRegistry);
		
		services.add(alarm1);
		services.add(alarm2);
		services.add(alarm3);
	
		
		// Medical Analysis Services
		MedicalAnalysisService medicalAnalysis1 = new MedicalAnalysisService("MedicalService1", "service.medical1");
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("preferred", false);
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		medicalAnalysis1.getServiceDescription().setOperationCost("analyzeData", 4.0);
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("FailureRate", 0.12);
		medicalAnalysis1.startService();
		medicalAnalysis1.register(serviceRegistry);
	
		MedicalAnalysisService medicalAnalysis2 = new MedicalAnalysisService("MedicalService2", "service.medical2");
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("preferred", true);
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("Cost", 14.0);
		medicalAnalysis2.getServiceDescription().setOperationCost("analyzeData", 14.0);
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		medicalAnalysis2.startService();
		medicalAnalysis2.register(serviceRegistry);
	
		MedicalAnalysisService medicalAnalysis3 = new MedicalAnalysisService("MedicalService3", "service.medical3");
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("preferred", false);
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("Cost", 2.15);
		medicalAnalysis3.getServiceDescription().setOperationCost("analyzeData", 2.15);
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
		medicalAnalysis3.startService();
		medicalAnalysis3.register(serviceRegistry);
		
		services.add(medicalAnalysis1);
		services.add(medicalAnalysis2);
		services.add(medicalAnalysis3);
	
		
		// Drug Services
		DrugService drugService = new DrugService("DrugService", "service.drug");
		drugService.getServiceDescription().getCustomProperties().put("preferred", true);
		drugService.getServiceDescription().getCustomProperties().put("Cost", 2.0);
		drugService.getServiceDescription().setOperationCost("changeDoses", 5.0);
		drugService.getServiceDescription().setOperationCost("changeDrug", 5.0);
		drugService.getServiceDescription().getCustomProperties().put("FailureRate", 0.25);
		drugService.startService();
		drugService.register(serviceRegistry);
		
		services.add(drugService);
    }
}
