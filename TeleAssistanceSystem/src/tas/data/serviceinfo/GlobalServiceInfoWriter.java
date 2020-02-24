package tas.data.serviceinfo;

import java.io.File;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import service.atomic.AtomicService;
import service.registry.ServiceRegistry;

/**
 * Class used to write the global service info data to a file.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class GlobalServiceInfoWriter {
	
	/**
	 * Write the important data from the global service info object to a given file
	 * @param file the given file
	 */
	public static void writeToXml(File file) {
		try {
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("ServiceInfo");
			doc.appendChild(root);
			
			// Write service data
			Element services = doc.createElement("Services");
			root.appendChild(services);
			
			for (AtomicService service : GlobalServiceInfo.getServices()) {
				
				Element serviceRoot  = doc.createElement("Service");
				services.appendChild(serviceRoot);
				
				Element endpoint = doc.createElement("Endpoint");
				endpoint.appendChild(doc.createTextNode(service.getServiceDescription().getServiceEndpoint()));				
				serviceRoot.appendChild(endpoint);
				
				Element type = doc.createElement("Type");
				type.appendChild(doc.createTextNode(service.getClass().getName()));
				serviceRoot.appendChild(type);
				
				Element name = doc.createElement("Name");
				name.appendChild(doc.createTextNode(service.getServiceDescription().getServiceName()));
				serviceRoot.appendChild(name);	
				
				Element properties = doc.createElement("Properties");
				serviceRoot.appendChild(properties);
						
				for (Entry<String, Object> pair : service.getServiceDescription().getCustomProperties().entrySet()) {
					
					Element property = doc.createElement("Property");
					properties.appendChild(property);
				
					Element propertyName = doc.createElement("Name");
					propertyName.appendChild(doc.createTextNode(pair.getKey()));
					property.appendChild(propertyName);
				
					Element propertyType = doc.createElement("Type");
					property.appendChild(propertyType);
					propertyType.appendChild(doc.createTextNode(pair.getValue().getClass().getSimpleName()));
					
					Element propertyValue = doc.createElement("Value");
					property.appendChild(propertyValue);
					propertyValue.appendChild(doc.createTextNode(pair.getValue().toString()));
				}
			}
			
			// Write registry data
			Element registries = doc.createElement("Registries");
			root.appendChild(registries);
			
			for (ServiceRegistry registry : GlobalServiceInfo.getServiceRegistries()) {
				
				Element registryRoot = doc.createElement("Registry");
				registries.appendChild(registryRoot);
				
				Element endpoint = doc.createElement("Endpoint");
				endpoint.appendChild(doc.createTextNode(registry.getServiceDescription().getServiceEndpoint()));				
				registryRoot.appendChild(endpoint);
				
				Element name = doc.createElement("Name");
				name.appendChild(doc.createTextNode(registry.getServiceDescription().getServiceName()));
				registryRoot.appendChild(name);	
				
				Element serviceList = doc.createElement("ServiceList");
				registryRoot.appendChild(serviceList);
				
				for (String serviceEndpoint : registry.getAllServiceEndpoints()) {
					Element serviceEndpointElement = doc.createElement("Endpoint");
					serviceEndpointElement.appendChild(doc.createTextNode(serviceEndpoint));
					serviceList.appendChild(serviceEndpointElement);
				}
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
