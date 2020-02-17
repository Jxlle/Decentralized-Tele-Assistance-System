package tas.data.serviceinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import service.atomic.AtomicService;
import service.registry.ServiceRegistry;
import tas.services.alarm.AlarmService;
import tas.services.assistance.AssistanceServiceCostProbe;
import tas.services.drug.DrugService;
import tas.services.medical.MedicalAnalysisService;
import tas.services.profiles.ServiceDelayProfile;
import tas.services.profiles.ServiceFailureLoadProfile;
import tas.services.profiles.SimpleServiceFailureProfile;

/**
 * Class used to hold data about the used services and registries in the system.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class GlobalServiceInfo {
    
	// TODO Look what types do in GUI
    private List<AtomicService> services = new ArrayList<>();
    private List<Class<?>> serviceProfileClasses = new ArrayList<>();
    private List<ServiceRegistry> serviceRegistries = new ArrayList<>();
    private LinkedHashMap<String, String> serviceTypes = new LinkedHashMap<>();
    
    /**
     * Create a new global service info object
     */
    public GlobalServiceInfo() {
    	initializeProfileClasses();
    }
    
    /**
     * Return the used service profile classes
     * @return the used service profile classes
     */
    public List<Class<?>> getServiceProfileClasses() {
    	return this.serviceProfileClasses;
    }
    
    /**
     * Start all services
     */
    public void startServices() {
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
     * Set the currently used services to the given service list
     * @param services the given service list
     */
    public void setServiceRegistries(List<ServiceRegistry> serviceRegistries) {
    	this.serviceRegistries = serviceRegistries;
    }
    
    /**
     * Return the service registry with the given endpoint if there is one.
     * Otherwise return null.
     * @param registryEndpoint the given service registry endpoint
     * @return the matched service registry endpoint or null if there was no registry match
     */
    public ServiceRegistry getServiceRegistry(String registryEndpoint) {
    	return serviceRegistries.stream().filter(x -> x.getServiceDescription().getServiceEndpoint().equals(registryEndpoint)).findFirst().orElse(null);
    }
    
    /**
     * Return a list of all currently used service registries
     * @return a list of all currently used service registries
     */
    public List<ServiceRegistry> getServiceRegistries() {
    	return serviceRegistries;
    }
    
    /**
     * Set the currently used services to the given service list
     * @param services the given service list
     */
    public void setServices(List<AtomicService> services) {
    	this.services = services;
    }
    
    /**
     * Return the service with the given endpoint if there is one.
     * Otherwise return null.
     * @param serviceEndpoint the given service endpoint
     * @return the matched service endpoint or null if there was no service match
     */
    public AtomicService getService(String serviceEndpoint) {
    	return services.stream().filter(x -> x.getServiceDescription().getServiceName().equals(serviceEndpoint)).findFirst().orElse(null);
    }
    
    /**
     * Return a list of all currently used services
     * @return a list of all currently used services
     */
    public List<AtomicService> getServices() {
    	return services;
    }
    
    /**
     * Return a linked hash map of all service types
     * @return a linked hash map of all service types
     */
    public LinkedHashMap<String,String> getServiceTypes() {
    	return this.serviceTypes;
    }
    
    /**
     * Save the current service data to a given file
     * @param file the given file
     */
    public void saveData(File file) {
    	GlobalServiceInfoWriter.writeToXml(this, file);
    }
    
    /**
     * Load service data from a given file
     * @param file the given file
     */
    public void loadData(File file) {
    	GlobalServiceInfoLoader.loadFromXml(this, file);
    }
    
    /**
     * Method mostly used for testing.
     * Changes the service data of this global service info object to the default data used when starting the simulator.
     * Can be used to restore the default XML file when accidentally deleted.
     */
    public void ChangeToDefaultServices() {
    	
    	// Reset data
    	services = new ArrayList<>();
    	serviceRegistries = new ArrayList<>();
    	serviceTypes = new LinkedHashMap<>();
    	
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
		drugService.startService();
		drugService.register(serviceRegistry);
		
		services.add(drugService);
    }
    
    /**
     * Initialize the usable/interactive service profile classes
     */
    private void initializeProfileClasses() {
		serviceProfileClasses.add(ServiceDelayProfile.class);
		serviceProfileClasses.add(SimpleServiceFailureProfile.class);
		serviceProfileClasses.add(ServiceFailureLoadProfile.class);
    }
}
