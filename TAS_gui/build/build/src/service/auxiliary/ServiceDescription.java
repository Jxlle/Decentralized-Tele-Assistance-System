package service.auxiliary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Definition of formated service description
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class ServiceDescription implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String RESPONSE_TIME = "ResponseTime";

    private int registerID = -1;
    private String endpoint;
    private String type;
    private String name;

    private HashMap<String, Object> customProperties = new HashMap<String, Object>();
    private List<Operation> opList = new ArrayList<Operation>();

    /**
     * Constructor
     * @param serviceName the service name
     * @param serviceEndpoint the service endpoint
     * @param responseTime the response time
     */
    public ServiceDescription(String serviceName, String serviceEndpoint, int responseTime) {
    	this.name = serviceName;
    	this.endpoint = serviceEndpoint;
    	setResponseTime(responseTime);
    }
    
    /**
     * Return the register id
     * @return the register id 
     */
    public int getRegisterID() {
    	return registerID;
    }

    /**
     * Set the register id
     * @param registerID the new register id
     */
    public void setRegisterID(int registerID) {
    	this.registerID = registerID;
    }

    /**
     * Return the service endpoint
     * @return the service endpoint
     */
    public String getServiceEndpoint() {
    	return endpoint;
    }

    /**
     * Set the service endpoint
     * @param serviceEndpoint the service endpoint
     */
    public void setServiceEndpoint(String serviceEndpoint) {
    	this.endpoint = serviceEndpoint;
    }

    /**
     * Return the service type
     * @return the service type
     */
    public String getServiceType() {
    	return type;
    }

    /**
     * Set the service type
     * @param serviceType the new service type
     */
    public void setServiceType(String serviceType) {
    	this.type = serviceType;
    }
    
    /**
     * Return the service name
     * @return the service name
     */
    public String getServiceName() {
    	return name;
    }

    /**
     * Set the service name
     * @param serviceName the new service name
     */
    public void setServiceName(String serviceName) {
    	this.name = serviceName;
    }

    /**
     * Constructor
     * @param serviceName the service name
     * @param serviceEndpoint the service endpoint
     */
    public ServiceDescription(String serviceName, String serviceEndpoint) {
    	this.name = serviceName;
    	this.endpoint = serviceEndpoint;
    }

    /**
     * Return service operations
     * @return list of service operations
     */
    public List<Operation> getOperationList() {
    	return opList;
    }

    /**
     * Check this service has a specific operation or not
     * @param opName the operation name
     * @return true if the service has the operation, otherwise false
     */
    public boolean containsOperation(String opName) {
    	for (int i = 0; i < opList.size(); i++) {
    		if (opList.get(i).getOpName().equals(opName))
    			return true;
    	}
    	return false;
    }

    /**
     * Set operation
     * @param opList list of operations
     */
    public void setOperationList(List<Operation> opList) {
    	this.opList = opList;
    }
    
    /**
     * Return operation
     * @param opName the operation name
     * @return the found operation
     */
    public Operation getOperation(String opName){
    	for(Operation op: opList){
    		if (op.getOpName().equals(opName)){
    			return op;
    		}
    	}
    	return null;
    }

    /**
     * Get custom properties of this service
     * @return the custom properties
     */
    public HashMap<String, Object> getCustomProperties() {
    	return customProperties;
    }

    /**
     * Get response time if this service has custom property "RESPONSE_TIME"
     * @return the response time
     */
    public int getResponseTime() {
    	if (customProperties.containsKey(RESPONSE_TIME))
    		return (int) customProperties.get(RESPONSE_TIME);
    	return 0;
    }

    /**
     * Set the response time
     * @param responseTime the new response time
     */
    public void setResponseTime(int responseTime) {
    	customProperties.put(RESPONSE_TIME, responseTime);
    }

    /**
     * Get the cost of an operation
     * @param opName the operation name
     * @return the operation cost
     */
    public double getOperationCost(String opName){
    	return getOperation(opName).getOpCost();
    }
    
    /**
     *  Set the operation cost
     * @param opName the operation name
     * @param cost the new operation cost
     */
    public void setOperationCost(String opName, double cost) {
    	getOperation(opName).setOpCost(cost);
    }
    
    /**
     * Clone a same Object
     */
    public Object clone() {
    	ObjectInputStream is = null;
    	ObjectOutputStream os = null;
    	try {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		os = new ObjectOutputStream(bos);
    		os.writeObject(this);
    		ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
    		is = new ObjectInputStream(bin);
    		Object clone = is.readObject();
    		return clone;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		try {
    			if (os != null)
    				os.close();
    			if (is != null)
    				is.close();
    		} catch (Exception ex) {
    		}
    	}
    	return null;
    }

    /**
     * Override the default "hashCode" method
     */
    @Override
    public int hashCode() {
    	return endpoint.hashCode() + type.hashCode();
    }

    /**
     * Override the default "equals" method
     */
    @Override
    public boolean equals(Object obj) {
		if (obj instanceof ServiceDescription) {
			ServiceDescription service = (ServiceDescription) obj;
			if (registerID == service.getRegisterID()
					&& endpoint.equals(service.getServiceEndpoint())
					&& type.equals(service.getServiceType())) {

				for (int i = 0; i < opList.size(); i++) {
					if (!opList.get(i)
							.equals(service.getOperationList().get(i)))
						return false;
				}

				for (Map.Entry<String, Object> entry : customProperties
						.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (!service.getCustomProperties().containsKey(key))
						return false;
					else if (!service.getCustomProperties().get(key)
							.equals(value))
						return false;
				}
				return true;
			}
		}
		return false;
    }
}
