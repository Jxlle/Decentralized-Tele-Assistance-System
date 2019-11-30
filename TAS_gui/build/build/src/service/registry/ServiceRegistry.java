package service.registry;

import java.util.*;

import service.atomic.AtomicService;
import service.auxiliary.ServiceDescription;
import service.auxiliary.ServiceOperation;
import service.auxiliary.ServiceRegistryInterface;

/**
 * Service for registering and finding services
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class ServiceRegistry extends AtomicService implements ServiceRegistryInterface{
    
	private HashMap<Integer, ServiceDescription> serviceList = new HashMap<>();
	private Map<String, Set<ServiceDescription>> services = new HashMap<String, Set<ServiceDescription>>(); 
	private int serviceCount = 0;

	/**
	 * Constructor
	 */
    public ServiceRegistry() {
    	super(NAME, ADDRESS);
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
    public int register(ServiceDescription description) {
    	serviceCount++;
    	description.setRegisterID(serviceCount);
    	serviceList.put(serviceCount, description);
    	String type = description.getServiceType();
    	if (services.containsKey(type)) {
    		services.get(type).add(description);
    	} else {
    		Set<ServiceDescription> set = new HashSet<ServiceDescription>();
    		set.add(description);
    		services.put(type, set);
    	}	
    	System.out.println("Service " + description.getServiceType() + " is registered.");	
    	return serviceCount;
    }

    @ServiceOperation
    public void unRegister(int registerID) {
    	ServiceDescription service = serviceList.get(registerID);
    	serviceList.remove(registerID);
    	Set<ServiceDescription> set = services.get(service.getServiceType());
    	set.remove(service);
    	if (set.size() == 0)
    		services.remove(service.getServiceType());	
    	System.out.println("Service " + service.getServiceType() + " is registered.");
    }

    @ServiceOperation
    public List<ServiceDescription> lookup(String type, String opName) {
    	System.out.println("Lookup " + type + "." + opName);
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
    
    public void removeService(ServiceDescription description){
    	String type=description.getServiceType();
    	int registerID=description.getRegisterID();
    	
    	if(serviceList.containsKey(registerID))
    		serviceList.remove(registerID);
    	
    	if(services.containsKey(type))
    		services.get(type).remove(description);
    }
    
    public void addService(ServiceDescription description){
    	String type=description.getServiceType();
    	int registerID=description.getRegisterID();
    	serviceList.put(registerID, description);
    	services.get(type).add(description);
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
    
    public static void main(String[] args) {
    	ServiceRegistry serviceRegistry=new ServiceRegistry();
    	serviceRegistry.startService();
    }
}
