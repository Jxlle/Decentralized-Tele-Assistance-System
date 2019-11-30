package service.client;

import service.auxiliary.AbstractMessage;
import service.auxiliary.ExecutionThread;
import service.auxiliary.Response;
import service.auxiliary.Request;
import service.provider.MessageReceiver;
import service.provider.ServiceProvider;
import service.provider.ServiceProviderFactory;

/**
 * 
 * The client for service invocation
 */
public class AbstractServiceClient implements MessageReceiver {
    private String serviceEndpoint;
    private Object result = null;

    private static int clientId = 0;
    private String clientEndpoint;
    private ServiceProvider serviceProvider;
    
    /**
     * Constructor
     * @param serviceEndpoint the service endpoint
     */
    public AbstractServiceClient(String serviceEndpoint) {
    	String clientEndpoint = serviceEndpoint + ".#CLIENT#." + (clientId == 0 ? "" : clientId);
    	clientId++;
    	initialize(serviceEndpoint, clientEndpoint);
    }

    /**
     * Constructor
     * @param serviceEndpoint the service endpoint
     * @param clientEndpoint the client endpoint
     */
    public AbstractServiceClient(String serviceEndpoint, String clientEndpoint) {
    	initialize(serviceEndpoint, clientEndpoint);
    }

    private void initialize(String serviceEndpoint, String clientEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
		this.clientEndpoint = clientEndpoint;
		serviceProvider = ServiceProviderFactory.createServiceProvider();
		serviceProvider.startListening(clientEndpoint, this);
    }

    /**
     * Send a request to invoke a method
     * @param methodName the method name
     * @param params parameters for the method
     * @return  the method result
     */
    public synchronized Object sendRequest(String methodName, Object... params) {
		try {
			Request request = new Request(0, clientEndpoint, clientEndpoint,methodName, params);
			serviceProvider.sendMessage(request, serviceEndpoint);

			synchronized (this) {
				this.wait();
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    /**
     * Return the service endpoint
     * @return the service endpoint
     */
    public String getServiceEndpoint() {
    	return serviceEndpoint;
    }

    /**
     * Set the service endpoint
     * @param serviceEndpoint the new service endpoint
     */
    public void setServiceEndpoint(String serviceEndpoint) {
    	this.serviceEndpoint = serviceEndpoint;
    }

    @Override
    public void onMessage(AbstractMessage msg) {
		try {
			Response response = (Response) msg;
			if (response.getReturnType() != null) {
				Class<?> type = (Class<?>) response.getReturnType();
				result = type.cast(response.getReturnValue());
			} else {
				result = null;
			}
			synchronized (this) {
				this.notifyAll();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
