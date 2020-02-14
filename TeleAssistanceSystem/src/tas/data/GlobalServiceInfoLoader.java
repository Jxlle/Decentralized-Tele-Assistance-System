package tas.data;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import service.atomic.AtomicService;
import service.registry.ServiceRegistry;

/**
 * Class used to load the global service info data from a file.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class GlobalServiceInfoLoader {
	
	/**
	 * Load the important data from a given file into the given global service info object
	 * @param serviceInfo the given global service info object
	 * @param file the given file
	 * @throws IllegalStateException throws when the given file contains illegal service endpoints
	 */
	public static void loadFromXml(GlobalServiceInfo serviceInfo, File file) throws IllegalStateException {
		try {
			
			List<AtomicService> servicesList = new ArrayList<>();
			List<ServiceRegistry> serviceRegistriesList = new ArrayList<>();
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element configuration = doc.getDocumentElement();
            
            // Load service data
            NodeList services = configuration.getElementsByTagName("Service");
            
            for (int i = 0; i < services.getLength(); i++) {
            	
            	Element service = (Element) services.item(i);
            	String serviceEndpoint = service.getElementsByTagName("Endpoint").item(0).getTextContent();
            	String serviceType = service.getElementsByTagName("Type").item(0).getTextContent();
            	String serviceName = service.getElementsByTagName("Name").item(0).getTextContent();
            	
            	Class<?> serviceClass = Class.forName(serviceType);
            	Constructor<?> cons = serviceClass.getConstructor(String.class, String.class);
            	AtomicService serviceObject = (AtomicService) cons.newInstance(serviceName, serviceEndpoint);
            	
            	NodeList properties = service.getElementsByTagName("Property");
            	
            	for (int j = 0; j < properties.getLength(); j++) {
            		
            		Element property = (Element) properties.item(j);
            		String propertyName = property.getElementsByTagName("Name").item(0).getTextContent();
            		String propertyType = property.getElementsByTagName("Type").item(0).getTextContent();
            		String propertyValueString = property.getElementsByTagName("Value").item(0).getTextContent();
            		
            		Object propertyValue = getPropertyValue(propertyType, propertyValueString);
            		serviceObject.getServiceDescription().getCustomProperties().put(propertyName, propertyValue);
            	}
            	
            	servicesList.add(serviceObject);
            	serviceInfo.setServices(servicesList);
            	serviceInfo.startServices();
            }
            
            // Load registry data
            NodeList registries = configuration.getElementsByTagName("Registry");
            
            for (int i = 0; i < registries.getLength(); i++) {
            	
            	Element registry = (Element) registries.item(i);
            	String registryEndpoint = registry.getElementsByTagName("Endpoint").item(0).getTextContent();
            	String registryName = registry.getElementsByTagName("Name").item(0).getTextContent();
            	ServiceRegistry registryObject = new ServiceRegistry(registryName, registryEndpoint);
            	registryObject.startService();
            	
            	Element serviceEndpointList = (Element) registry.getElementsByTagName("ServiceList").item(0);
            	NodeList endpoints = serviceEndpointList.getElementsByTagName("Endpoint");
            	
            	for (int j = 0; j < endpoints.getLength(); j++) {
            		
            		Element endpoint = (Element) endpoints.item(j);
            		String endpointString = endpoint.getTextContent();
            		AtomicService endpointService = servicesList.stream()
            				.filter(x -> x.getServiceDescription().getServiceEndpoint().equals(endpointString)).findFirst().orElse(null);
            		
            		if (endpointService == null) {
            			throw new IllegalStateException("The given XML file contains illegal service endpoints!");
            		}
            		
            		endpointService.register(registryObject);
            	}
            	
            	serviceRegistriesList.add(registryObject);
            }
            
            serviceInfo.setServiceRegistries(serviceRegistriesList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return the property value object based on the given value type and the given string value
	 * @param type the given value type
	 * @param value the given text value
	 * @return the property value object
	 * @throws IllegalArgumentException throws when the given property value type can't be processed
	 */
	private static Object getPropertyValue(String type, String value) throws IllegalArgumentException {
		
		switch (type) {
			case "Double":
				return Double.valueOf(value);
				
			case "Integer":
				return Integer.valueOf(value);
				
			case "Boolean":
				return Boolean.valueOf(value);
				
			case "String":
				return value;
				
			default:
				throw new IllegalArgumentException("Property value type can't be processed!");
		}
	}
}
