package tas.data.serviceinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
 * @note Service operation costs are currently not used, only the custom cost property
 */
public class GlobalServiceInfo {
	
    private static List<AtomicService> services = new ArrayList<>();
    private static List<ServiceRegistry> serviceRegistries = new ArrayList<>();
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
    	//GlobalServiceInfoLoader.loadFromXml(file);
    	ChangeToDefaultServices();
    	GlobalServiceInfoWriter.writeToXml(file);
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
    	
    	// Service registries
		ServiceRegistry privateServiceRegistry1 = new ServiceRegistry("Private Service Registry 1", "service.private1.registry");
		privateServiceRegistry1.startService();
		serviceRegistries.add(privateServiceRegistry1);
		
		ServiceRegistry privateServiceRegistry2 = new ServiceRegistry("Private Service Registry 2", "service.private2.registry");
		privateServiceRegistry2.startService();
		serviceRegistries.add(privateServiceRegistry2);
		
		ServiceRegistry privateServiceRegistry3 = new ServiceRegistry("Private Service Registry 3", "service.private3.registry");
		privateServiceRegistry3.startService();
		serviceRegistries.add(privateServiceRegistry3);
		
		ServiceRegistry publicServiceRegistry1 = new ServiceRegistry("Public Service Registry 1", "service.public1.registry");
		publicServiceRegistry1.startService();
		serviceRegistries.add(publicServiceRegistry1);
		
		ServiceRegistry publicServiceRegistry2 = new ServiceRegistry("Public Service Registry 2", "service.public2.registry");
		publicServiceRegistry2.startService();
		serviceRegistries.add(publicServiceRegistry2);
		
		
		// Alarm Services
		AlarmService alarm1 = new AlarmService("AlarmService1", "service.alarmService1");
		alarm1.getServiceDescription().getCustomProperties().put("Cost", 6.0);
		alarm1.getServiceDescription().getCustomProperties().put("FailureRate", 0.05);
		alarm1.startService();
		alarm1.register(privateServiceRegistry1);
		services.add(alarm1);
		
		AlarmService alarm2 = new AlarmService("AlarmService2", "service.alarmService2");
		alarm2.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		alarm2.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		alarm2.startService();
		alarm2.register(privateServiceRegistry1);
		services.add(alarm2);
		
		AlarmService alarm3 = new AlarmService("AlarmService3", "service.alarmService3");
		alarm3.getServiceDescription().getCustomProperties().put("Cost", 6.0);
		alarm3.getServiceDescription().getCustomProperties().put("FailureRate", 0.05);
		alarm3.startService();
		alarm3.register(privateServiceRegistry2);
		services.add(alarm3);
	
		AlarmService alarm4 = new AlarmService("AlarmService4", "service.alarmService4");
		alarm4.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		alarm4.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		alarm4.startService();
		alarm4.register(privateServiceRegistry2);
		services.add(alarm4);
		
		AlarmService alarm5 = new AlarmService("AlarmService5", "service.alarmService5");
		alarm5.getServiceDescription().getCustomProperties().put("Cost", 6.0);
		alarm5.getServiceDescription().getCustomProperties().put("FailureRate", 0.05);
		alarm5.startService();
		alarm5.register(privateServiceRegistry3);
		services.add(alarm5);
	
		AlarmService alarm6 = new AlarmService("AlarmService6", "service.alarmService6");
		alarm6.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		alarm6.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		alarm6.startService();
		alarm6.register(privateServiceRegistry3);
		services.add(alarm6);
		
		AlarmService alarm7 = new AlarmService("AlarmService7", "service.alarmService7");
		alarm7.getServiceDescription().getCustomProperties().put("Cost", 3.0);
		alarm7.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		alarm7.startService();	
		alarm7.register(publicServiceRegistry1);
		services.add(alarm7);
		
		AlarmService alarm8 = new AlarmService("AlarmService8", "service.alarmService8");
		alarm8.getServiceDescription().getCustomProperties().put("Cost", 3.0);
		alarm8.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		alarm8.startService();	
		alarm8.register(publicServiceRegistry2);
		services.add(alarm8);
	
		
		// Medical Analysis Services
		MedicalAnalysisService medicalAnalysis1 = new MedicalAnalysisService("MedicalService1", "service.medical1");
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		medicalAnalysis1.getServiceDescription().getCustomProperties().put("FailureRate", 0.11);
		medicalAnalysis1.startService();
		medicalAnalysis1.register(privateServiceRegistry1);
		services.add(medicalAnalysis1);
	
		MedicalAnalysisService medicalAnalysis2 = new MedicalAnalysisService("MedicalService2", "service.medical2");
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		medicalAnalysis2.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		medicalAnalysis2.startService();
		medicalAnalysis2.register(privateServiceRegistry1);
		services.add(medicalAnalysis2);
	
		MedicalAnalysisService medicalAnalysis3 = new MedicalAnalysisService("MedicalService3", "service.medical3");
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		medicalAnalysis3.getServiceDescription().getCustomProperties().put("FailureRate", 0.11);
		medicalAnalysis3.startService();
		medicalAnalysis3.register(privateServiceRegistry2);
		services.add(medicalAnalysis3);
		
		MedicalAnalysisService medicalAnalysis4 = new MedicalAnalysisService("MedicalService4", "service.medical4");
		medicalAnalysis4.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		medicalAnalysis4.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		medicalAnalysis4.startService();
		medicalAnalysis4.register(privateServiceRegistry2);
		services.add(medicalAnalysis4);
		
		MedicalAnalysisService medicalAnalysis5 = new MedicalAnalysisService("MedicalService5", "service.medical5");
		medicalAnalysis5.getServiceDescription().getCustomProperties().put("Cost", 4.0);
		medicalAnalysis5.getServiceDescription().getCustomProperties().put("FailureRate", 0.11);
		medicalAnalysis5.startService();
		medicalAnalysis5.register(privateServiceRegistry3);
		services.add(medicalAnalysis5);
		
		MedicalAnalysisService medicalAnalysis6 = new MedicalAnalysisService("MedicalService6", "service.medical6");
		medicalAnalysis6.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		medicalAnalysis6.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
		medicalAnalysis6.startService();
		medicalAnalysis6.register(privateServiceRegistry3);
		services.add(medicalAnalysis6);
		
		MedicalAnalysisService medicalAnalysis7 = new MedicalAnalysisService("MedicalService7", "service.medical7");
		medicalAnalysis7.getServiceDescription().getCustomProperties().put("Cost", 3.0);
		medicalAnalysis7.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		medicalAnalysis7.startService();
		medicalAnalysis7.register(publicServiceRegistry1);
		services.add(medicalAnalysis7);
		
		MedicalAnalysisService medicalAnalysis8 = new MedicalAnalysisService("MedicalService8", "service.medical8");
		medicalAnalysis8.getServiceDescription().getCustomProperties().put("Cost", 3.0);
		medicalAnalysis8.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		medicalAnalysis8.startService();
		medicalAnalysis8.register(publicServiceRegistry2);
		services.add(medicalAnalysis8);
	
		
		// Drug Services
		DrugService drugService = new DrugService("DrugService1", "service.drug1");
		drugService.getServiceDescription().getCustomProperties().put("Cost", 6.0);
		drugService.getServiceDescription().getCustomProperties().put("FailureRate", 0.12);
		drugService.startService();
		drugService.register(privateServiceRegistry1);
		services.add(drugService);
		
		DrugService drugService2 = new DrugService("DrugService2", "service.drug2");
		drugService2.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		drugService2.getServiceDescription().getCustomProperties().put("FailureRate", 0.08);
		drugService2.startService();
		drugService2.register(privateServiceRegistry1);
		services.add(drugService2);
		
		DrugService drugService3 = new DrugService("DrugService3", "service.drug3");
		drugService3.getServiceDescription().getCustomProperties().put("Cost", 6.0);
		drugService3.getServiceDescription().getCustomProperties().put("FailureRate", 0.12);
		drugService3.startService();
		drugService3.register(privateServiceRegistry2);
		services.add(drugService3);
		
		DrugService drugService4 = new DrugService("DrugService4", "service.drug4");
		drugService4.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		drugService4.getServiceDescription().getCustomProperties().put("FailureRate", 0.08);
		drugService4.startService();
		drugService4.register(privateServiceRegistry2);
		services.add(drugService4);
		
		DrugService drugService5 = new DrugService("DrugService5", "service.drug5");
		drugService5.getServiceDescription().getCustomProperties().put("Cost", 6.0);
		drugService5.getServiceDescription().getCustomProperties().put("FailureRate", 0.12);
		drugService5.startService();
		drugService5.register(privateServiceRegistry3);
		services.add(drugService5);
		
		DrugService drugService6 = new DrugService("DrugService6", "service.drug6");
		drugService6.getServiceDescription().getCustomProperties().put("Cost", 5.0);
		drugService6.getServiceDescription().getCustomProperties().put("FailureRate", 0.08);
		drugService6.startService();
		drugService6.register(privateServiceRegistry3);
		services.add(drugService6);
		
		DrugService drugService7 = new DrugService("DrugService7", "service.drug7");
		drugService7.getServiceDescription().getCustomProperties().put("Cost", 3.0);
		drugService7.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		drugService7.startService();
		drugService7.register(publicServiceRegistry1);
		services.add(drugService7);
		
		DrugService drugService8 = new DrugService("DrugService8", "service.drug8");
		drugService8.getServiceDescription().getCustomProperties().put("Cost", 3.0);
		drugService8.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
		drugService8.startService();
		drugService8.register(publicServiceRegistry2);
		services.add(drugService8);
    }
}
