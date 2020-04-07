package tas.mape.system.entity;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tas.mape.knowledge.Goal;

/**
 * Class used to write MAPEK system entity data to a file.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class MAPEKSystemEntityWriter {

	/**
	 * Private constructor
	 */
	private MAPEKSystemEntityWriter() {}
	
	/**
	 * Write the important data from the given MAPEK system entity to a given file
	 * @param file the given file
	 */
	public static void writeToXml(MAPEKSystemEntity entity, File file) {
		try {
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("MAPEKSystemEntity");
			doc.appendChild(root);
					
			// Basic system entity properties
			Element name = doc.createElement("Name");
			name.appendChild(doc.createTextNode(entity.getEntityName()));
			root.appendChild(name);
			
			Element workflowPath = doc.createElement("WorkflowPath");
			workflowPath.appendChild(doc.createTextNode(entity.getManagedSystem().getWorkflowPath()));
			root.appendChild(workflowPath);
			
			Element analyzerStrategy = doc.createElement("AnalyzerStrategy");
			analyzerStrategy.appendChild(doc.createTextNode(entity.getManagingSystem().getAnalyzer().getServiceGenerationStrategy().toString()));
			root.appendChild(analyzerStrategy);
			
		    Element monitorFailureChange = doc.createElement("MonitorFailureChange");
		    monitorFailureChange.appendChild(doc.createTextNode(String.valueOf(entity.getManagingSystem().getMonitor().getFailureChange())));
		    root.appendChild(monitorFailureChange);
			
		    Element monitorMinFailureDelta = doc.createElement("MonitorMinFailureDelta");
		    monitorMinFailureDelta.appendChild(doc.createTextNode(String.valueOf(entity.getManagingSystem().getMonitor().getMinFailureDelta())));
		    root.appendChild(monitorMinFailureDelta);
			
		    Element analyzerCombinationLimit = doc.createElement("AnalyzerCombinationLimit");
		    analyzerCombinationLimit.appendChild(doc.createTextNode(String.valueOf(entity.getManagingSystem().getMonitor().getMinFailureDelta())));
		    root.appendChild(analyzerCombinationLimit);
			
		    Element knowledgeLoadFailureDelta = doc.createElement("KnowledgeLoadFailureDelta");
		    knowledgeLoadFailureDelta.appendChild(doc.createTextNode(String.valueOf(entity.getManagingSystem().getKnowledge().getLoadFailureDelta())));
		    root.appendChild(knowledgeLoadFailureDelta);
			
			// Used service registries
			Element registryRoot = doc.createElement("Registries");
			root.appendChild(registryRoot);
			
			for (String registryEndpoint : entity.getManagingSystem().getKnowledge().getRegistryEndpoints()) {
			    Element registryEndpointElement = doc.createElement("RegistryEndpoint");
			    registryEndpointElement.appendChild(doc.createTextNode(registryEndpoint));
			    registryRoot.appendChild(registryEndpointElement);
			}
			
			// System entity goals
			Element goals = doc.createElement("Goals");
			root.appendChild(goals);
			
			for (Goal goal : entity.getManagingSystem().getKnowledge().getGoals()) {
				
				Element goalRoot = doc.createElement("Goal");
				goals.appendChild(goalRoot);
				
			    Element type = doc.createElement("Type");
			    type.appendChild(doc.createTextNode(goal.getType().toString()));
			    goalRoot.appendChild(type);
			    
			    Element relation = doc.createElement("Relation");
			    relation.appendChild(doc.createTextNode(goal.getRelation().toString()));
			    goalRoot.appendChild(relation);
			    
			    Element value = doc.createElement("Value");
			    value.appendChild(doc.createTextNode(String.valueOf(goal.getValue())));
			    goalRoot.appendChild(value);
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
