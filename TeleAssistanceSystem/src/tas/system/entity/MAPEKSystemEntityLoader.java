package tas.system.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import service.registry.ServiceRegistry;
import tas.data.serviceinfo.GlobalServiceInfo;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalRelation;
import tas.mape.knowledge.Goal.GoalType;
import tas.system.entity.MAPEKFeedbackLoop.Builder;

public class MAPEKSystemEntityLoader {
	
	/**
	 * Private constructor
	 */
	private MAPEKSystemEntityLoader() {}
	
	/**
	 * Load the important data from a given file and return a MAPEK system entity generated with the data
	 * @param file the given file
	 * @return the generated system entity
	 */
	public static MAPEKSystemEntity loadFromXml(File file) {
		
		try {
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element configuration = doc.getDocumentElement();
            
            // Basic system entity properties
            String entityName = configuration.getElementsByTagName("Name").item(0).getTextContent();
            String workflowPath = configuration.getElementsByTagName("WorkflowPath").item(0).getTextContent();
            int analyzerStrategy = Integer.valueOf(configuration.getElementsByTagName("AnalyzerStrategy").item(0).getTextContent());
            double monitorFailureChange = Double.valueOf(configuration.getElementsByTagName("MonitorFailureChange").item(0).getTextContent());
            double monitorMinFailureDelta = Double.valueOf(configuration.getElementsByTagName("MonitorMinFailureDelta").item(0).getTextContent());
            int analyzerCombinationLimit = Integer.valueOf(configuration.getElementsByTagName("AnalyzerCombinationLimit").item(0).getTextContent());
            int knowledgeLoadFailureDelta = Integer.valueOf(configuration.getElementsByTagName("KnowledgeLoadFailureDelta").item(0).getTextContent());
            
            // Used service registries
            NodeList registryNodes = configuration.getElementsByTagName("RegistryEndpoint");
            List<String> registryEndpoints = new ArrayList<>();
            
            for (int i = 0; i < registryNodes.getLength(); i++) {
            	registryEndpoints.add(registryNodes.item(i).getTextContent());
            }
            
            // System entity goals
            NodeList goalNodes = configuration.getElementsByTagName("Goal");
            List<Goal> goals = new ArrayList<>();
            
            for (int i = 0; i < goalNodes.getLength(); i++) {
            	
        		Element goal = (Element) goalNodes.item(i);
        		GoalType type = GoalType.valueOf(goal.getElementsByTagName("Type").item(0).getTextContent());
        		GoalRelation relation = GoalRelation.valueOf(goal.getElementsByTagName("Relation").item(0).getTextContent());
        		double value = Double.valueOf(goal.getElementsByTagName("Value").item(0).getTextContent());
            	
            	goals.add(new Goal(type, relation, value));
            }
            
            // Build and return system entity
    		MAPEKFeedbackLoop.Builder builder = new Builder();
    		
    		try {
    			builder.initializeKnowledge(knowledgeLoadFailureDelta, registryEndpoints)
    			 	   .initializePlanner()
    				   .initializeAnalyzer(analyzerCombinationLimit, analyzerStrategy)
    				   .initializeMonitor(monitorMinFailureDelta, monitorFailureChange);
    		} catch (InstantiationException e) {
    			e.printStackTrace();
    		}
    		
    		List<ServiceRegistry> registryList = new ArrayList<>();
    		
    		for (String registryEndpoint : registryEndpoints) {
    			registryList.add(GlobalServiceInfo.getServiceRegistry(registryEndpoint));
    		}
    		
    		
    		MAPEKFeedbackLoop component = builder.build();
    		LocalServiceSystem workflowExecutor = new LocalServiceSystem(registryList);
    		workflowExecutor.setWorkflowPath(workflowPath);	
    		MAPEKSystemEntity systemEntity = new MAPEKSystemEntity(entityName, workflowExecutor, component);
    		
    		for (Goal goal : goals) {
    			systemEntity.getManagingSystem().getKnowledge().addGoal(goal);
    		}
            
    		return systemEntity;
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
