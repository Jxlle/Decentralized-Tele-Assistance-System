package tas.mape.system.entity;

import java.util.List;
import java.util.Random;

import service.composite.CompositeServiceClient;
import service.registry.ServiceRegistry;
import service.utility.Time;
import tas.data.inputprofile.InputProfileValue;
import tas.data.inputprofile.InputProfileVariable;
import tas.data.inputprofile.InputProfile;
import tas.data.inputprofile.InputProfileDataHandler;
import tas.services.assistance.AssistanceService;
import tas.services.assistance.AssistanceServiceCostProbe;

public class LocalServiceSystem {

	// Fields	
	public static int amountOfWorkflows = 0; 
	
    private boolean isStopped = false;
    private boolean isPaused = false;
    private int currentSteps;
    
    private AssistanceService assistanceService;
    private AssistanceServiceCostProbe probe;
    private String workflowPath;
    
	public LocalServiceSystem(List<ServiceRegistry> serviceRegistries) {
		amountOfWorkflows++;
		initializeWorkFlowExecutor(serviceRegistries);
	}
    
    public synchronized void stop(){
    	isStopped = true;
    	currentSteps = 0;
    }
    
    private synchronized void start() {
    	isStopped = false;
    }
    
    public synchronized void pause() {
    	isPaused = true;
    }
    
    public synchronized void go() {
    	isPaused = false;
    	this.notifyAll();
    }
    
    public boolean isStopped()  {
    	return isStopped;
    }
    
    public boolean isPaused() {
    	return isPaused;
    }
    
    public int getCurrentSteps() {
    	return currentSteps;
    }
    
    public AssistanceServiceCostProbe getProbe() {
    	return probe;
    }
    
    public void setWorkflowPath(String workflowPath) {
    	this.workflowPath = workflowPath;
		assistanceService.setWorkflow(workflowPath);
    }
    
    public String getWorkflowPath() {
    	return workflowPath;
    }
    
    public AssistanceService getAssistanceService() {
    	return assistanceService;
    }
    
    public void stopAssistanceService() {
    	assistanceService.stopService();
    }
	
    public void initializeWorkFlowExecutor(List<ServiceRegistry> serviceRegistries) {
	
		// Assistance Service. Workflow is provided by TAS_gui through executeWorkflow method
		assistanceService = new AssistanceService("TeleAssistanceService", "service.assistance" + amountOfWorkflows, "resources/TeleAssistanceWorkflow.txt", serviceRegistries);
		assistanceService.startService();
		probe = new AssistanceServiceCostProbe();
		assistanceService.getCostProbe().register(probe);
		assistanceService.getWorkflowProbe().register(probe);
		//assistanceService.getWorkflowProbe().register(new AssistanceServiceDelayProbe());
		// assistanceService.getServiceInvocationProbe().register(monitor);
		assistanceService.updateCache();
    }
	
    public void executeWorkflow() {

    	InputProfile profile = InputProfileDataHandler.activeProfile;
		CompositeServiceClient client = new CompositeServiceClient(assistanceService.getServiceDescription().getServiceEndpoint());
		Time.steps.set(0);
	
		if (profile != null) {
			
		    InputProfileVariable variable = profile.getVariable("pick");
		    List<InputProfileValue> values = variable.getValues();
	
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
						client.invokeCompositeService(new Object[]{patientId, pick});
						break;
				    } 
				    else {
						valueProbability = valueProbability + values.get(j).getRatio();
				    }
				}	
	    	
		    	if (isStopped) {
		    		break;
		    	}
		    }
		    
		    stop();
		    System.out.println("Finished executing the workflow.");
		}
    }
}
