package tas.mape.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.util.Pair;
import profile.InputProfile;
import profile.SystemProfileValue;
import profile.SystemProfileVariable;
import profile.ProfileExecutor;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import service.composite.CompositeService;
import service.composite.CompositeServiceClient;
import service.utility.Time;
import tas.data.systemprofile.SystemProfile;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.mape.system.entity.WorkflowExecutor;

/**
 * Class used to analyze workflows and gather information for the knowledge component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class WorkflowAnalyzer {
	
	// Fields
	private int currentSteps;
	private boolean isStopped, hasBeenStopped;
	private WorkflowAnalyzerProbe workflowAnalyzerProbe = new WorkflowAnalyzerProbe();
	
    /**
     * Indicate that the analyzer has been stopped
     */
    public synchronized void stop() {
    	isStopped = true;
    }
    
    /**
     * Indicate that the analyzer has been started
     */
    public synchronized void start() {
    	isStopped = false;
    }
    
    /**
     * Analyze a given workflow and return a result used in the knowledge component
     * @param workflowPath the workflow path
     * @param entityName the name of the system entity
     * @param compositeService the composite service
     * @return a map containing a usage chance and list of used services for each used service type & operation combination
     */
    public Map<Description, Pair<List<ServiceDescription>, Double>> analyzeWorkflow(String entityName, CompositeService compositeService) { 
    	
    	// Initialize workflow probe
    	workflowAnalyzerProbe.reset();
    	compositeService.getWorkflowProbe().register(workflowAnalyzerProbe);
    	
    	// Get input profile
    	SystemProfile profile = SystemProfileDataHandler.activeProfile;
    	
    	// Execute workflow and gather data
    	compositeService.setTestMode(true);
		executeWorkflow(profile, compositeService);
		compositeService.setTestMode(false);
		
		// Return null if the analyzer has been stopped early
		if (hasBeenStopped) {
			hasBeenStopped = false;
			return null;
		}
		
		// Calculate and return result
		WeightedCollection<Description> usedDescriptions = workflowAnalyzerProbe.getUsedDescriptions();
		Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance = new HashMap<>();
		
		for (Description description : usedDescriptions.getItems()) {
			
			double descriptionChance = usedDescriptions.getWeight(description) / (double) profile.getVariable("workflowCycles").getValues().get(0).getData();
			Pair<List<ServiceDescription>, Double> pair = new Pair<List<ServiceDescription>, Double>(compositeService.getCache().getServiceDescriptions(description), descriptionChance);
			usableServicesAndChance.put(description, pair);
		}
		
		return usableServicesAndChance;
		
		// TODO 
		// - in new invokeservice compositeservice method, add all services in cache to list first, or a certain amount
		// - do not reset cache when disabling services in registry in gui to aquire correct chance values
    }
	
    /**
     * Execute a given workflow for N cycles using a given input profile and composite service
     * @param profile the given input profile
     * @param compositeService the given composite service
     */
	private void executeWorkflow(SystemProfile profile, CompositeService compositeService) {

		CompositeServiceClient client = new CompositeServiceClient("service.assistance");
		compositeService.updateCache();
		Time.steps.set(0);
		
		if (profile != null) {
		    SystemProfileVariable variable = profile.getVariable("pick");
		    List<SystemProfileValue> values = variable.getValues();

		    int patientId = (int) profile.getVariable("patientId").getValues().get(0).getData();
		    int pick;
		    
		    start();
		    Random rand = new Random();

		    for (currentSteps = 0; currentSteps < profile.getWorkflowCycles(); currentSteps++) {
		    	
			    Time.steps.incrementAndGet();   	    	
				double probability = rand.nextDouble();
				double valueProbability = 0;
				
				for (int j = 0; j < values.size(); j++) {
					
				    if ((values.get(j).getRatio() + valueProbability) > probability) {
				    	pick = (int) values.get(j).getData();
				    	// TODO
				    	client.invokeCompositeService("delete", patientId, pick);
				    	break;
				    } 
				    else {
						valueProbability = valueProbability + values.get(j).getRatio();
				    }
				}
		    	
		    	if (isStopped) {
		    		hasBeenStopped = true;
		    		break;
		    	}			
		    }
		    
		    stop();
		}
	}
}
