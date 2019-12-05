package tas.mape.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.util.Pair;
import profile.InputProfileValue;
import profile.InputProfileVariable;
import profile.ProfileExecutor;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import service.composite.CompositeService;
import service.composite.CompositeServiceClient;
import service.utility.Time;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class used to analyze workflows and gather information for the knowledge component
 */
public class WorkflowAnalyzer {
	
	private int currentSteps;
	private boolean isStopped, hasBeenStopped;
	private WorkflowAnalyzerProbe workflowAnalyzerProbe = new WorkflowAnalyzerProbe();
	
    /**
     * Indicate that the analyzer has been stopped
     */
    public synchronized void stop(){
    	isStopped = true;
    }
    
    /**
     * Indicate that the analyzer has been started
     */
    public synchronized void start(){
    	isStopped = false;
    }
    
    /**
     * Analyze a given workflow and return a result used in the knowledge component
     * @param workflowCycles the amount of workflow cycles to be executed
     * @param workflowPath the workflow path
     * @param profilePath the profile path
     * @param compositeService the composite service
     * @return a map containing a usage chance and list of used services for each used service type & operation combination
     */
    public Map<Description, Pair<List<ServiceDescription>, Double>> analyzeWorkflow(int workflowCycles, String workflowPath, String profilePath, CompositeService compositeService) { 
    	
    	// Initialize workflow probe
    	workflowAnalyzerProbe.reset();
    	compositeService.getWorkflowProbe().register(workflowAnalyzerProbe);
    	
    	// Execute workflow and gather data
    	compositeService.setTestMode(true);
		executeWorkflow(workflowCycles, workflowPath, profilePath, compositeService);
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
			
			double descriptionChance = usedDescriptions.getWeight(description) / (double) workflowCycles;
			Pair<List<ServiceDescription>, Double> pair = new Pair<List<ServiceDescription>, Double>(compositeService.getCache().getServiceDescriptions(description), descriptionChance);
			usableServicesAndChance.put(description, pair);
		}
		
		return usableServicesAndChance;
		
		// TODO in new invokeservice compositeservice method, add all services in cache to list first, or a certain amount
    }
	
    /**
     * Execute a given workflow for N cycles using a given composite service, workflow path, profile path and amount of workflow cycles
     * @param workflowCycles the amount of workflow cycles to be executed
     * @param workflowPath the workflow path
     * @param profilePath the profile path
     * @param compositeService the composite service
     */
	private void executeWorkflow(int workflowCycles, String workflowPath, String profilePath, CompositeService compositeService) {

		CompositeServiceClient client = new CompositeServiceClient("service.assistance");
		compositeService.updateCache();
		compositeService.setWorkflow(workflowPath);
		Time.steps.set(0);

		ProfileExecutor.readFromXml(profilePath);
		
		if (ProfileExecutor.profile != null) {
		    InputProfileVariable variable = ProfileExecutor.profile.getVariable("pick");
		    List<InputProfileValue> values = variable.getValues();

		    int patientId = (int) ProfileExecutor.profile.getVariable("patientId").getValues().get(0).getData();
		    int pick;
		    
		    start();
		    Random rand = new Random();
		    int index = 0;
		    for (currentSteps = 0; currentSteps < workflowCycles; currentSteps++) {
		    	
			    Time.steps.incrementAndGet();   	    	
				double probability = rand.nextDouble();
				double valueProbability = 0;
				
				for (int j = 0; j < values.size(); j++) {
					
				    if ((values.get(j).getRatio() + valueProbability) > probability) {
				    	pick = (int) values.get(j).getData();
				    	index++;
				    	client.invokeCompositeService(ProfileExecutor.profile.getQosRequirement(), patientId, pick);
				    	break;
				    } else {
						valueProbability = valueProbability + values.get(j).getRatio();
				    }
				}
		    	
		    	if (isStopped) {
		    		hasBeenStopped = true;
		    		break;
		    	}			
		    }
		    
	    	System.err.print("count: " + index);
		    
		    stop();
		}
	}
}
