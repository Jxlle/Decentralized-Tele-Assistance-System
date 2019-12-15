/**
 * 
 */
package service.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import service.adaptation.probes.CacheProbe;
import service.auxiliary.Description;
import service.auxiliary.Operation;
import service.auxiliary.ServiceDescription;

/**
 * Cache for available services
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 *
 */
public class SDCache {
	
    public Map<Description, List<ServiceDescription>> caches = new ConcurrentHashMap<Description, List<ServiceDescription>>();
    private int maxCacheSize;
    //private int refreshPeriod=10*Time.scale;   
    private int refreshPeriod = 10;         
    private Timer timer = null;    
    private CacheProbe cacheProbe = new CacheProbe();
    
    /**
     * Return cache refresh period
     * @return the refresh period
     */
	public int getRefreshPeriod() {
		return refreshPeriod;
	}


	/**
	 * Set cache refresh period
	 * @param refreshPeriod the new refresh period
	 * @return true if set successfully, otherwise false
	 */
	public boolean setRefreshPeriod(int refreshPeriod) {
		this.refreshPeriod = refreshPeriod;
		if (timer != null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		
		if(refreshPeriod>0){
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
	      	  	public void run() {
	      	  		refresh();
	      	  	}
			}, refreshPeriod, refreshPeriod);
			return true;
		}
		return false;
	}
	
	/**
	 * Return the cache probe
	 * @return the cache probe
	 */
	public CacheProbe getCacheProbe() {
		return cacheProbe;
	}

	/**
	 * Return available services
	 * @return list of available service names
	 */
	public Set<String> getServices(){
		Set<String> services=new HashSet<>();
		for (List<ServiceDescription> serviceList : caches.values()) {
			for(ServiceDescription service:serviceList)
				services.add(service.getServiceName());
		}
		return services;
	}
	
	public Map<String,ServiceDescription> getServiceDescriptions(){
		Map<String,ServiceDescription> services=new HashMap<>();
		for (List<ServiceDescription> serviceList : caches.values()) {
			for(ServiceDescription service:serviceList){
				services.put(service.getServiceName(), service);
			}
		}
		return services;
	}

	public ServiceDescription getServiceWithEndpoint(String endpoint){
		for (List<ServiceDescription> serviceList : caches.values()) {
			for(ServiceDescription service:serviceList){
				if(service.getServiceEndpoint().equals(endpoint))
					return service;
			}
		}
		return null;
	}
	
	public void addService(ServiceDescription service){
		String serviceType=service.getServiceType();
		service.getOperationList().forEach(operation->{
			Description description=new Description(serviceType,operation.getOpName());
			if(!caches.containsKey(description)){
				caches.put(description, new ArrayList<>());
			}
			
			caches.get(description).add(service);
			cacheProbe.notifyServiceAddedToCache(description, service);
		});
	}
	
	public List<ServiceDescription> getServiceDescriptions(Description description) {
		return caches.get(description);
	}
	
	
	/**
	 * Return max size of the cache
	 * @return the max cache size
	 */
	public int getMaxCacheSize() {
		return maxCacheSize;
	}

	/**
	 * Set max size of the cache
	 * @param maxCacheSize the new max cache size
	 */
	public void setMaxCacheSize(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}

	/**
	 * Add new services
	 * @param serviceType the service type
	 * @param opName the operation name 
	 * @param serviceDescriptions new list of service descriptions
	 * @return true if added successfully, otherwise false
	 */
	public boolean add(String serviceType,String opName,List<ServiceDescription> serviceDescriptions){
		if(maxCacheSize<=caches.size() && maxCacheSize>0)
			return false;
		List<ServiceDescription> services=new ArrayList<>();
		for(ServiceDescription serviceDescription : serviceDescriptions)
			services.add((ServiceDescription)serviceDescription.clone());
		
		Description description = new Description(serviceType,opName);
		caches.put(description,services);
		
		for (ServiceDescription serviceDescription : services) {
			cacheProbe.notifyServiceAddedToCache(description, serviceDescription);
		}
		
    	return true;
    }
    
	/**
	 * Get services with specific type and operation
	 * @param serviceType the service type
	 * @param opName the operation name
	 * @return list of found service descriptions
	 */
    public List<ServiceDescription> get(String serviceType,String opName){
    	Description description=new Description(serviceType,opName);
    	if(caches.containsKey(description)){
    		List<ServiceDescription> services=new ArrayList<>();
    		for(ServiceDescription serviceDescription : caches.get(description)){
    			//System.out.println(serviceDescription);
				services.add((ServiceDescription)serviceDescription);
    		}
    		return services;
    	} 	
    	return null;
    }
    
    /**
     * Remove a service from cache with its description
     * @param service the service description
     */
    public void remove(ServiceDescription service) {
    	for (Operation operation : service.getOperationList())
    		remove(service, operation.getOpName());
    		
    }
    
    /**
     * Remove a service from cache with its type and operation name
     * @param serviceType the service type
     * @param opName the operation name
     * @return true if removed successfully, otherwise false
     */
    public boolean remove(String serviceType,String opName){
    	
    	Description description=new Description(serviceType, opName);
    	
    	if(caches.containsKey(description)){
    		
    		for (ServiceDescription serviceDescription : caches.get(description)) {
    			cacheProbe.notifyServiceRemovedFromCache(description, serviceDescription);
    		}
    		
    		caches.remove(description);
    		
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Remove a service from cache with its type, operation name and service description
     * @param serviceType the service type
     * @param opName  the operation name
     * @param service the service description
     * @return true if removed successfully, otherwise false
     */
    public boolean remove(String serviceType, String opName, ServiceDescription service){
    	
    	Description description = new Description(serviceType, opName);
    	
    	if(caches.containsKey(description)) {
    		for(ServiceDescription serviceDescription : caches.get(description)){
    			if(serviceDescription.equals(service)) {
    				cacheProbe.notifyServiceRemovedFromCache(description, serviceDescription);
    				return caches.get(description).remove(serviceDescription);
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * Remove a service from cache with its description and operation name
     * @param service the service description
     * @param opName the operation name
     * @return true if removed successfully, otherwise false
     */
    public boolean remove(ServiceDescription service, String opName){
    	return this.remove(service.getServiceType(), opName,service);
    }
    
    /**
     * Return current cahche size
     * @return the current cache size
     */
    public int size(){
    	return caches.size();
    }
    
    /**
     * Check cache has specific service 
     * @param serviceType the service type
     * @param opName the operation name
     * @return true if cache contains a service with same type and operation, otherwise false
     */
    public boolean containsCache(String serviceType,String opName){
	Description description = new Description(serviceType, opName);
    	if (caches.containsKey(description))
    	{
    	    List<ServiceDescription> descriptions = caches.get(description);
    	    return descriptions!= null && descriptions.size() > 0;
    	}
    	return false;
    }
    
    /**
     * Refresh the cache
     */
    public void refresh(){
        caches.clear();
    }

    /**
     * Update services containing same operation with new service description
     * @param oldService the old service description
     * @param newService the new service description
     * @param opName the operation name
     * @return true if updated successfully, otherwise false
     */
    public boolean update(ServiceDescription oldService, ServiceDescription newService, String opName){
    	Description description=new Description(oldService.getServiceType(),opName);
    	if(caches.containsKey(description)){
    		List<ServiceDescription> services=caches.get(description);
    		if(services.contains(oldService)){
    			//System.out.println(services.indexOf(oldService));
    			services.set(services.indexOf(oldService),newService);
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Return the service description
     * @param registerId the service register id
     * @return the requested service description
     */
    public ServiceDescription getServiceDescription(int registerId){
    	for (List<ServiceDescription> serviceList : caches.values()) {
    		for(ServiceDescription service:serviceList){
    			if (service.getRegisterID() == registerId)
    				return service;
    		}
    	}	
    	return null;
    }
    
    /**
     * Remove service from cache with its register id
     * @param registerId the register id of a service to be removed
     */
    public void remove(int registerId) {
    	ServiceDescription serviceDescription = getServiceDescription(registerId);
    	if (serviceDescription != null){
    		remove(serviceDescription);
    	}
    	else{
    		System.err.println("Service not found with registeration Id:" + registerId);
    		System.err.println("Service cannot be removed.");
    	}
    }
}
