package tas.mape.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.util.Pair;
import profile.SystemProfileValue;
import profile.SystemProfileVariable;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import service.composite.CompositeService;
import service.composite.CompositeServiceClient;
import service.utility.Time;
import tas.data.systemprofile.SystemProfile;
import tas.data.systemprofile.SystemProfileDataHandler;

/**
 * Class used to analyze workflows and gather information for the knowledge component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class WorkflowAnalyzer {
	
	// Fields
	public static int analyzerCycles = 5000;
	private static int currentSteps;
	private static boolean isStopped, hasBeenStopped;
	private static WorkflowAnalyzerProbe workflowAnalyzerProbe = new WorkflowAnalyzerProbe();
	
	/**
	 * Private constructor
	 */
	private WorkflowAnalyzer() {}
	
    /**
     * Indicate that the analyzer has been stopped
     */
    public synchronized static void stop() {
    	isStopped = true;
    }
    
    /**
     * Indicate that the analyzer has been started
     */
    public synchronized static void start() {
    	isStopped = false;
    }
    
    /**
     * Return the current amount of taken steps in the analyzer
     * @return the current amount of taken steps in the analyzer
     */
    public static int getCurrentSteps() {
    	return currentSteps;
    }
    
    /**
     * Analyze a given workflow and return a result used in the knowledge component
     * @param workflowPath the workflow path
     * @param compositeService the composite service
     * @return a map containing a usage chance and list of used services for each used service type & operation combination
     */
    public static Map<Description, Pair<List<ServiceDescription>, Double>> analyzeWorkflow(CompositeService compositeService) { 
    	
    	// Initialize workflow probe
    	workflowAnalyzerProbe.reset();
    	compositeService.getWorkflowProbe().register(workflowAnalyzerProbe);
    	
    	// Get input profile
    	SystemProfile profile = SystemProfileDataHandler.activeProfile;
    	
    	// Execute workflow and gather data
    	compositeService.setTestMode(true);
		executeWorkflow(profile, compositeService);
		compositeService.setTestMode(false);
		compositeService.getWorkflowProbe().unRegister(workflowAnalyzerProbe);
		
		// Return null if the analyzer has been stopped early
		if (hasBeenStopped) {
			hasBeenStopped = false;
			return null;
		}
		
		// Calculate and return result
		WeightedCollection<Description> usedDescriptions = workflowAnalyzerProbe.getUsedDescriptions();
		Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance = new HashMap<>();
		
		for (Description description : usedDescriptions.getItems()) {
			
			double descriptionChance = usedDescriptions.getWeight(description) / (double) analyzerCycles;
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
	private static void executeWorkflow(SystemProfile profile, CompositeService compositeService) {

		CompositeServiceClient client = new CompositeServiceClient(compositeService.getServiceDescription().getServiceEndpoint());
		Time.steps.set(0);
		
		if (profile != null) {
		    SystemProfileVariable variable = profile.getVariable("pick");
		    List<SystemProfileValue> values = variable.getValues();

		    int patientId = (int) profile.getVariable("patientId").getValues().get(0).getData();
		    int pick;
		    
		    start();
		    Random rand = new Random();

		    for (currentSteps = 0; currentSteps < analyzerCycles; currentSteps++) {
		    	
			    Time.steps.incrementAndGet();   	    	
				double probability = rand.nextDouble();
				double valueProbability = 0;
				
				for (int j = 0; j < values.size(); j++) {
					
				    if ((values.get(j).getRatio() + valueProbability) > probability) {
				    	pick = (int) values.get(j).getData();
						//System.err.print("--------------------ANALYZING CYCLE [" + (currentSteps + 1) + "]--------------------\n");
				    	client.invokeCompositeService(new Object[]{patientId, pick});
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
