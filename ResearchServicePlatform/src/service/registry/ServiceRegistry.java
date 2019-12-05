package service.registry;

import java.util.*;

import service.adaptation.probes.ServiceRegistryProbe;
import service.atomic.AtomicService;
import service.auxiliary.ServiceDescription;
import service.auxiliary.ServiceOperation;
import service.auxiliary.ServiceRegistryInterface;

/**
 * Service for registering and finding services
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class ServiceRegistry extends AtomicService implements ServiceRegistryInterface{
    
	private ServiceRegistryProbe serviceRegistryProbe;
	private HashMap<Integer, ServiceDescription> serviceList = new HashMap<>();
	private Map<String, Set<ServiceDescription>> services = new HashMap<String, Set<ServiceDescription>>(); 
	private static int serviceCount = 0;

	/**
	 * Constructor
	 */
    public ServiceRegistry(String serviceName, String serviceEndpoint) {
    	super(serviceName, serviceEndpoint);
    	serviceRegistryProbe = new ServiceRegistryProbe();
    }
    
    
    /**
     * Return the service registry probe
     * @return the service registry probe
     */
    @ServiceOperation
    public ServiceRegistryProbe getRegistryProbe() {
    	return serviceRegistryProbe;
    }

    /**
     * Return list of service names
     * @return list of service names
     */
    public List<String> getAllServices(){
    	List<String> allServices=new ArrayList<>();
    	for (ServiceDescription service : serviceList.values()) {
    		allServices.add(service.getServiceName());
    	}
    	return allServices;
    }
    
    @ServiceOperation
    public void register(ServiceDescription description) {
    	serviceCount++;
    	description.setRegisterID(serviceCount);
    	description.setServiceRegistryEndpoint(this.getServiceDescription().getServiceEndpoint());
    	serviceList.put(serviceCount, description);
    	String type = description.getServiceType();
    	if (services.containsKey(type)) {
    		services.get(type).add(description);
    	} else {
    		Set<ServiceDescription> set = new HashSet<ServiceDescription>();
    		set.add(description);
    		services.put(type, set);
    	}	
    	
    	serviceRegistryProbe.notifyServiceAddedToRegistry(description);
    	System.out.println("Service " + description.getServiceType() + " is registered with ID " + description.getRegisterID() + " and registry endpoint " + description.getServiceRegistryEndpoint() + " .");	
    }

    @ServiceOperation
    public void unRegister(int registerID) {
    	ServiceDescription description = serviceList.get(registerID);
    	serviceList.remove(registerID);
    	Set<ServiceDescription> set = services.get(description.getServiceType());
    	set.remove(description);
    	if (set.size() == 0)
    		services.remove(description.getServiceType());	
    	
    	serviceRegistryProbe.notifyServiceRemovedFromRegistry(description);
    	System.out.println("Service " + description.getServiceType() + " is unregistered.");
    }

    @ServiceOperation
    public List<ServiceDescription> lookup(String type, String opName) {
    	List<ServiceDescription> list = new ArrayList<ServiceDescription>();
    	if (services.containsKey(type)) {
    		Set<ServiceDescription> set = services.get(type);
    		Iterator<ServiceDescription> iter = set.iterator();
    		while (iter.hasNext()) {
    			ServiceDescription service = iter.next();
    			if (service.containsOperation(opName))
    				list.add(service);
    		}
    	}
    	return list;
    }
    
    public ServiceDescription getService(String serviceName){
    	for(ServiceDescription description:serviceList.values()){
    		if(description.getServiceName().equals(serviceName))
    			return description;
    	}
    	return null;
    }
    
    public ServiceDescription getService(int registerId){
    	return serviceList.get(registerId);
    }
    
    /**
     * Return list of service descriptions
     * @return list of service descriptions
     */
    @ServiceOperation
    public List<ServiceDescription> getAllServiceDescriptions() {
    
    	List<ServiceDescription> serviceDescriptions = new ArrayList<ServiceDescription>();
    	
    	for (int i = 1; i <= serviceCount; i++) {
    		
    		ServiceDescription serviceDescription = getService(i);
    		
    		if (serviceDescription != null) {
    			serviceDescriptions.add(serviceDescription);
    		}
    	}
    	
    	return serviceDescriptions;
    }
    
    public void removeService(ServiceDescription description){
    	String type=description.getServiceType();
    	int registerID=description.getRegisterID();
    	
    	if(serviceList.containsKey(registerID))
    		serviceList.remove(registerID);
    	
    	if(services.containsKey(type))
    		services.get(type).remove(description);
    	
    	serviceRegistryProbe.notifyServiceRemovedFromRegistry(description);
    }
    
    public void addService(ServiceDescription description){
    	String type=description.getServiceType();
    	int registerID=description.getRegisterID();
    	serviceList.put(registerID, description);
    	services.get(type).add(description);
    	
    	serviceRegistryProbe.notifyServiceAddedToRegistry(description);
    }
    
    /**
     * Update service description
     * @param description the new service description
     */
    @ServiceOperation
    public void update(ServiceDescription description){
    	ServiceDescription oldDescription=serviceList.remove(description.getRegisterID());
    	Set<ServiceDescription> descriptions=services.get(description.getServiceType());
    	descriptions.remove(oldDescription);
    	descriptions.add(description);
    	//System.out.println("Update service description");
    }
}
