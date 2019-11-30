package service.atomic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import service.auxiliary.AbstractService;
import service.auxiliary.AtomicServiceConfiguration;
import service.auxiliary.Configuration;
import service.auxiliary.OperationAborted;
import service.auxiliary.Param;
import service.auxiliary.ServiceFailed;
import service.auxiliary.ServiceOperation;
import service.auxiliary.TimeOutError;

/**
 *
 * Inheriting from AbstractService,
 * providing a logical entity for creating atomic services
 * 
 */
public abstract class AtomicService extends AbstractService {		
	private List<ServiceProfile> serviceProfiles=new ArrayList<>();
	
	/**
	 * Constructor
	 * @param serviceName  the service name
	 * @param serviceEndpoint the source endpoint
	 * @param responseTime the response time for incoming messages
	 */
    public AtomicService(String serviceName, String serviceEndpoint, int responseTime) {
    	super(serviceName, serviceEndpoint, responseTime);
    }

    /**
     * Constructor 
     * @param serviceName the service name
     * @param serviceEndpoint the source endpoint
     */
    public AtomicService(String serviceName, String serviceEndpoint) {
    	super(serviceName, serviceEndpoint);
    }

    /**
     * Remove a service profile from the list with the unique index
     * @param index the profile index
     */
    public void removeServiceProfile(int index){
    	serviceProfiles.remove(index);
    }
    
    /**
     * Remove a service profile from the list 
     * @param serviceProfile the service profile
     */
    public void removeServiceProfile(ServiceProfile serviceProfile){
    	serviceProfiles.remove(serviceProfile);
    }
    
    /**
     * Add a service profile to the list
     * @param serviceProfile the service profile
     */
    public void addServiceProfile(ServiceProfile serviceProfile){
    	this.serviceProfiles.add(serviceProfile);
    }
    
    /**
     * Get the list of service profiles
     * @return  list of service profiles
     */
    public List<ServiceProfile> getServiceProfiles(){
    	return this.serviceProfiles;
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

							int serviceProfileNum = this.serviceProfiles.size();
							
							// execute "preInvokeOperation" in service profiles one after another
							// if the current result is false, stop executing the next one
							boolean flag = true;
							
							for (int i = 0; i < serviceProfileNum; i++) {
								if (!(flag = serviceProfiles.get(i).preInvokeOperation(opName, args))){
									return new ServiceFailed();
									//break;
								}
							}

							// execute "postInvokeOperation" in service profiles one after another
							// the previous result is one parameter for the current invocation
							Object result;
							if (flag) {
								result = operation.invoke(this, args);
								for (int i = 0; i < serviceProfileNum; i++) {
									result = serviceProfiles.get(i).postInvokeOperation(opName,result, args);
								}
								return result;
							} else {
								return new OperationAborted(null);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("The operation name or params are not valid. Please check and send again!");
				}
			}
		}
		return null;
    }

    @Override
    protected void readConfiguration() {
		try {
			Annotation annotation = this.getClass().getAnnotation(AtomicServiceConfiguration.class);
			if (annotation != null&& annotation instanceof AtomicServiceConfiguration) {
				AtomicServiceConfiguration CSConfiguration = (AtomicServiceConfiguration) annotation;
				this.configuration = new Configuration(
						CSConfiguration.MultipeThreads(),
						CSConfiguration.MaxNoOfThreads(),
						CSConfiguration.MaxQueueSize());
			} else
				this.configuration = new Configuration(false, 1, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
