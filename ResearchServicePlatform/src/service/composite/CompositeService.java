package service.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import service.adaptation.probes.CostProbe;
import service.adaptation.probes.WorkflowProbe;
import service.atomic.AtomicService;
import service.adaptation.probes.ServiceRegistryProbe;
import service.auxiliary.AbstractService;
import service.auxiliary.CompositeServiceConfiguration;
import service.auxiliary.Configuration;
import service.auxiliary.Description;
import service.auxiliary.LocalOperation;
import service.auxiliary.Param;
import service.auxiliary.ServiceDescription;
import service.auxiliary.ServiceOperation;
import service.auxiliary.TimeOutError;
import service.auxiliary.WeightedCollection;
import service.registry.ServiceRegistry;
import service.workflow.WorkflowEngine;

/**
 * Providing an abstraction to create composite services
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 * @author Unknown other person(s)
 */
public class CompositeService extends AbstractService {

    private String workflow;
    
    // Initializing probes
    private CostProbe costProbe = new CostProbe();
    private WorkflowProbe workflowProbe = new WorkflowProbe();
    // Initializing effectors
    //private ConfigurationEffector configurationEffector = new ConfigurationEffector(this);
    
    // This variable will effect only one thread/invocation of the workflow
    private AtomicBoolean stopRetrying = new AtomicBoolean(false);
    
    // Test mode allowing workflow analyzing
    private Boolean testMode = false;
    
    // List of all used service registries
    private List<RegistryData> serviceRegistriesData;
    
    // Map containing all service endpoints of the services currently used by the workflow + the accumulated chance that it's used
    private Map<Description, WeightedCollection<String>> usedServicesInfo;

    /**
     * Set the workflow 
     * @param workflow the new workflow
     */
    public void setWorkflow(String workflow) {
    	this.workflow = workflow;
    }

    // Cache containing the available services
    private SDCache cache;

    /**
     * Return the cache
     * @return the current cache
     */
    public SDCache getCache() {
    	return cache;
    }

    @Override
    protected void readConfiguration() {
		try {
			Annotation annotation = this.getClass().getAnnotation(
					CompositeServiceConfiguration.class);
			if (annotation != null
					&& annotation instanceof CompositeServiceConfiguration) {
				CompositeServiceConfiguration CSConfiguration = (CompositeServiceConfiguration) annotation;
				this.configuration = new Configuration(
						CSConfiguration.MultipeThreads(),
						CSConfiguration.MaxNoOfThreads(),
						CSConfiguration.MaxQueueSize(),
						CSConfiguration.Timeout(),
						CSConfiguration.IgnoreTimeOutError(),
						CSConfiguration.MaxRetryAttempts(),
						CSConfiguration.SDCacheMode(),
						CSConfiguration.SDCacheShared(),
						CSConfiguration.SDCacheTimeout(),
						CSConfiguration.SDCacheSize());
			} else {
				// the default configuration
				this.configuration = new Configuration(false, 1, 0, 0, false,
						1, true, true, -1, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Constructor
     * @param serviceName the service name
     * @param serviceEndpoint the service endpoint
     * @param workflow the workflow
     */
    public CompositeService(String serviceName, String serviceEndpoint, String workflow, List<ServiceRegistry> registries) {
		super(serviceName, serviceEndpoint);
		this.workflow = workflow;

		if (configuration.SDCacheMode == false){
		    System.err.println("Warning! Cache mode cannot be turned off.");
		}
		else if (configuration.SDCacheShared == false){
		    System.err.println("Warning! Cache mode sharing cannot be turned off.");
		}
		else if (configuration.SDCacheSize == 0){
		    System.err.println("Warning! Cache size cannot be equal to zero.");
		}
		
		//if (this.configuration.SDCacheMode) {
			cache = new SDCache();
		//}
			
		serviceRegistriesData = new ArrayList<RegistryData>();
		addRegistryData(registries);
    }
    
    /**
     * Add registry data 
     * @param registry the registry whose data needs to be added
     * @note Services in the given registry are not added to the cache
     */
    public void addRegistryData(ServiceRegistry registry) {
    	serviceRegistriesData.add(new RegistryData(registry.getServiceDescription().getServiceName(), registry.getServiceDescription().getServiceEndpoint()));
    }
    
    /**
     * Add registry data
     * @param registries the registries whose data needs to be added
     * @note Services in the given registries are not added to the cache
     */
    public void addRegistryData(List<ServiceRegistry> registries) {
    	
    	for (ServiceRegistry registry : registries) {
    		addRegistryData(registry);
    	}
    }
    
    /**
     * Remove registry data
     * @param registry the registry whose data needs to be removed
     */
    public void removeRegistryData(ServiceRegistry registry) {
    	
    	RegistryData data = new RegistryData(registry.getServiceDescription().getServiceName(), registry.getServiceDescription().getServiceEndpoint());
    	serviceRegistriesData.remove(data);	
    	
    	List<ServiceDescription> serviceDescriptions = registry.getAllServiceDescriptions();
    	
    	for (ServiceDescription description : serviceDescriptions) {
        	cache.remove(description);
    	}
    }
    
    /**
     * Remove registry data
     * @param registries the registries whose data needs to be removed
     */
    public void removeRegistryData(List<ServiceRegistry> registries) {
    	
    	for (ServiceRegistry registry : registries) {
    		removeRegistryData(registry);
    	}
    	
    }
    
    /**
     * Return the service registry probe for all attached registries
     * @return the service registry probes
     */
    public List<ServiceRegistryProbe> getServiceRegistryProbes() {
    	
    	List<ServiceRegistryProbe> serviceRegistryProbes = new ArrayList<ServiceRegistryProbe>();
    	
    	for (RegistryData registryData : serviceRegistriesData) {
    		serviceRegistryProbes.add((ServiceRegistryProbe) this.sendRequest(registryData.registryName, registryData.registryEndpoint, true, "getRegistryProbe"));
    	}
    	
    	return serviceRegistryProbes;
    }
    
    /**
     * Set the actively used workflow services
     * @param usedServicesInfo new service endpoints and accumulated use chances
     */
    public void setUsedServicesInfo(Map<Description, WeightedCollection<String>> usedServicesInfo) {
    	this.usedServicesInfo = usedServicesInfo;
    }
    
    /**
     * Set test mode of this composite service based on a given boolean
     * @param testMode the given boolean
     */
    public void setTestMode(Boolean testMode) {
    	this.testMode = testMode;
    }

    /**
     * Invoke this composite service to start a workflow with specific QoS requirements 
     * and initial parameters for the workflow
     * 
     * @param params  the initial parameters for the workflow
     * @return the result after executing the workflow
     */
    @ServiceOperation
    public Object invokeCompositeService(Object params[]) {
    	
    	// Activate no fail in test mode
    	if (testMode) {
    		AtomicService.setNoFail(true);
    	}
		
		// If SDCache shared is not on then a new cache object for the workflow should be created
		//ToDo: Cache is shared among all the workflow invocations. separate local cache is not supported yet.
		//SDCache sdCache = configuration.SDCacheShared == true ? cache : new SDCache() ;
		//WorkflowEngine engine = new WorkflowEngine(this, sdCache);
		WorkflowEngine engine = new WorkflowEngine(this);
		workflowProbe.notifyWorkflowStarted(params);
		Object result = engine.executeWorkflow(workflow, params);
		workflowProbe.notifyWorkflowEnded(result, params);
		AtomicService.setNoFail(false);
		
		return result;
    }

    @Override
    public Object invokeOperation(String opName, Param[] params) {
		// System.out.println(opName);
		for (Method operation : this.getClass().getMethods()) {
			if (operation.getAnnotation(ServiceOperation.class) != null) {
				try {
					if (operation.getName().equals(opName)) {
						Class<?>[] paramTypes = operation.getParameterTypes();
						int size = paramTypes.length;
						if (size == params.length) {
							Object[] args = new Object[size];
							for (int i = 0; i < size; i++) {
								args[i] = params[i].getValue();
							}
							
							return operation.invoke(this, args);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("The operation name or params are not valid. OpName: " + opName + ", params: " + params);
				}
			}
		}
		return null;
    }


    /**
     * Search through service registries to get the list of service descriptions
     * @param serviceType  the service type
     * @param opName the operation name
     * @return list of service descriptions with the same service type and operation name
     */
    @SuppressWarnings("unchecked")
	public List<ServiceDescription> lookupServiceOld(String serviceType, String opName) {
		List<ServiceDescription> serviceDescriptions = cache.get(serviceType,
				opName);
		
		if (serviceDescriptions == null || serviceDescriptions.size() == 0) {
			serviceDescriptions = new ArrayList<ServiceDescription>();
			
			for (RegistryData data : serviceRegistriesData) 
			{ 
				serviceDescriptions.addAll((List<ServiceDescription>) this.sendRequest(
						data.registryName, data.registryEndpoint, true,
						"lookup", serviceType, opName));
			}
			
			cache.add(serviceType, opName, serviceDescriptions);
		}
		
		if (serviceDescriptions == null || serviceDescriptions.size() == 0) {
			this.getWorkflowProbe().notifyServiceNotFound(serviceType, opName);
		    //serviceDescriptions = this.lookupService(serviceType, opName);
		}
		
		return serviceDescriptions;
    } 
    
    @SuppressWarnings("unchecked")
	public void updateCache() {
    	
    	cache.refresh();
    	
    	List<ServiceDescription> serviceDescriptions = new ArrayList<ServiceDescription>();
    	
    	for (RegistryData data : serviceRegistriesData) {	
			serviceDescriptions.addAll((List<ServiceDescription>) this.sendRequest(
					data.registryName, data.registryEndpoint, true,
					"getAllServiceDescriptions"));
    	}
    	
    	for (ServiceDescription description : serviceDescriptions) {
    		//System.err.print("ADDED " + description.getServiceEndpoint() + " \n");
        	cache.addService(description);
    	}
    	
    	//System.err.print("done adding services...\n");
    }
    
    public ServiceDescription lookupService(String serviceType, String opName) {
    	
    	Description description = new Description(serviceType, opName);	
    	String chosenServiceEndpoint = usedServicesInfo.get(description).next();
    	
    	if (chosenServiceEndpoint == null) {
    		return null;
    	}
    	
    	return cache.getServiceWithEndpoint(chosenServiceEndpoint);
    	
    }
    
    /**
     * Returns the cost probe
     * @return the cost probe for this composite service
     */
    public CostProbe getCostProbe() {
    	return costProbe;
    }
    
    /**
     * Return the workflow probe
     * @return the workflow probe for this composite service
     */
    public WorkflowProbe getWorkflowProbe() {
    	return workflowProbe;
    }
     
    /**
     * Return the configuration effector
     * @return the configuration effector for this composite service
     */
   // public ConfigurationEffector getConfigurationEffector() {
    //	return configurationEffector;
    //}

    /**
     * Returns true if composite service cache contains instances of the specific service type with operation name
     * @param serviceType the service type
     * @param opName the operation name
     * @return true if cache has service with the same type and operation, otherwise false
     */
    public boolean containServices(String serviceType, String opName) {
    	return cache.containsCache(serviceType, opName);
    }
    

    /**
     * Get service description using register ID of the service from cache
     * @param registerId the register id
     * @return the service description
     */
    public ServiceDescription getServiceDescription(int registerId){
    	return cache.getServiceDescription(registerId);
    }

    /**
     * Invoke a service operation
     * @param qosRequirement the applied QoS requirements
     * @param serviceName the service name
     * @param opName the operation name
     * @param params the parameters of the operation
     * @return the result
     */
    /*public Object invokeServiceOperation2(String qosRequirement, String serviceName, String opName, Object[] params) {

	int timeout = this.getConfiguration().timeout;
	Object resultVal;
	int retryAttempts = 0;
	stopRetrying.set(false);
	do {
	    List<ServiceDescription> services = lookupService(serviceName, opName);
	    
	    if (services == null || services.size() == 0) {
	    	System.out.println("ServiceName: " + serviceName + "." + opName + " not found!");
	    	getWorkflowProbe().notifyServiceNotFound(serviceName, opName);
	    	return new TimeOutError();
	    }
	    else {
	    	
		    // Apply strategy
		    ServiceDescription service = applyQoSRequirement(qosRequirement, services, opName, params);
		    
		    // Find registry probe
		    // Only one registry probe is searched. Same service in multiple probes will not trigger multiple probes.
		    RegistryData registryData = serviceRegistriesData.stream().filter(x -> x.registryEndpoint.equals(service.getServiceRegistryEndpoint())).findFirst().orElse(null);
		    ServiceRegistryProbe registryProbe = (ServiceRegistryProbe) this.sendRequest(registryData.registryName, registryData.registryEndpoint, true, "getRegistryProbe");

		    this.getWorkflowProbe().notifyServiceOperationInvoked(service, opName, params);	
		    
		    int maxResponseTime = timeout != 0 ? timeout : service.getResponseTime() * 3;
		    resultVal = this.sendRequest(service.getServiceName(), service.getServiceEndpoint(), true, maxResponseTime, opName, params);
		    
		    if (resultVal instanceof TimeOutError) {
		    	
		    	this.getWorkflowProbe().notifyServiceOperationTimeout(service, opName, params);
		    	registryProbe.notifyServiceFailed(service);

		    } else {
		    	
			    registryProbe.notifyServiceSucceeded(service);
		    	this.getWorkflowProbe().notifyServiceOperationReturned(service, resultVal, opName, params);
		    	this.getCostProbe().notifyCostSubscribers(service, opName);
		    }
		    
		    if (stopRetrying.get() == true){
				stopRetrying.set(false);
				break;
		    }

		    retryAttempts++;
	    }
	    
	} while (resultVal instanceof TimeOutError && retryAttempts < this.getConfiguration().maxRetryAttempts);

	return resultVal;
    }*/
    
    public Object invokeServiceOperation(String serviceName, String opName, Object[] params) {
    	int timeout = this.getConfiguration().timeout;
    	Object resultVal;
    	int retryAttempts = 0;
    	stopRetrying.set(false);
    	do {
    		
    		ServiceDescription service2 = null;
    		
    		if (testMode) {
    			service2 = lookupServiceOld(serviceName, opName).get(0);
    		}
    		else {
        	    service2 = lookupService(serviceName, opName);
    		}
    		
    		final ServiceDescription service = service2;
    	    
    	    // Only one registry probe is searched. Same service in multiple probes will not trigger multiple probes.
    	    RegistryData registryData = serviceRegistriesData.stream().filter(x -> x.registryEndpoint.equals(service.getServiceRegistryEndpoint())).findFirst().orElse(null);
    	    ServiceRegistryProbe registryProbe = (ServiceRegistryProbe) this.sendRequest(registryData.registryName, registryData.registryEndpoint, true, "getRegistryProbe");
    	    
    	    if (service == null) {
    	    	System.out.println("ServiceName: " + serviceName + "." + opName + " not found!");
    	    	getWorkflowProbe().notifyServiceNotFound(serviceName, opName);
    	    	resultVal = new TimeOutError();
    	    }

    	    this.getWorkflowProbe().notifyServiceOperationInvoked((ServiceDescription) service.clone(), opName, params);	    

    	    int maxResponseTime = timeout != 0 ? timeout : service.getResponseTime() * 3;
    	    resultVal = this.sendRequest(service.getServiceType(), service.getServiceEndpoint(), true, maxResponseTime, opName, params);

    	    if (resultVal instanceof TimeOutError) {
    	    	//System.err.print("FAILED :'( \n");
    	    	this.getWorkflowProbe().notifyServiceOperationTimeout((ServiceDescription) service.clone(), opName, params);
    	    	registryProbe.notifyServiceFailed((ServiceDescription) service.clone());
    	    } 
    	    else {  
    	    	//System.err.print("SUCCEEDED :) \n");
    	    	this.getWorkflowProbe().notifyServiceOperationReturned((ServiceDescription) service.clone(), resultVal, opName, params);
    	    	this.getCostProbe().notifyCostSubscribers((ServiceDescription) service.clone(), opName);
    	    	registryProbe.notifyServiceSucceeded((ServiceDescription) service.clone());
    	    }
    	    
    	    if (stopRetrying.get() == true){
    		stopRetrying.set(false);
    		break;
    	    }

    	    retryAttempts++;
    	    
    	} while (resultVal instanceof TimeOutError && retryAttempts < this.getConfiguration().maxRetryAttempts);
    	
    	return resultVal;
    }

    /**
     * Invoke the operation without the annotation "ServiceOperation"
     * @param opName the operation name
     * @param params the parameters of the operation
     * @return the result
     */
    public Object invokeLocalOperation(String opName, Object[] params) {
	for (Method operation : this.getClass().getMethods()) {
	    if (operation.getAnnotation(LocalOperation.class) != null) {
		if (operation.getName().equals(opName)) {
		    try {
			return operation.invoke(this, params);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	throw new RuntimeException("Local operation " + opName + " is not found.");
    }
    
    /**
     * If a service failed and composite service is retrying, this method will effect to stop retrying for that service. 
     * This method once called will effect only one service invocation/thread.
     */
    public void stopRetrying(){
	stopRetrying.set(true);
    }
    

    // Class for holding registry information
    // This class is used to support multiple registries for one composite service
    class RegistryData {
    	
    	String registryName;
    	String registryEndpoint;
    	
    	public RegistryData(String registryName, String registryEndpoint) {
    		this.registryName = registryName;
    		this.registryEndpoint = registryEndpoint;
    	}
    	
    	@Override
    	public int hashCode(){
    		return registryName.hashCode() + registryEndpoint.hashCode();
    	}
    	
    	@Override
		public boolean equals(Object obj){
    		
    		if (obj == null) return false;
    		if (obj == this) return true;
    		
			if(obj instanceof RegistryData){
				RegistryData data = (RegistryData)obj;
				if(data.registryName.equals(registryName) && data.registryEndpoint.equals(registryEndpoint))
					return true;
			}
			return false;
		}
		
		@Override
		public String toString(){
		    return "( " + registryName + ", " + registryEndpoint + ") ";
		}
    }
}
