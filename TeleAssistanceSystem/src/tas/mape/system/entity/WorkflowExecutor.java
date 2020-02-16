package tas.mape.system.entity;

import java.util.List;
import java.util.Random;

import profile.InputProfile;
import profile.SystemProfileValue;
import profile.SystemProfileVariable;
import profile.ProfileExecutor;
import service.adaptation.effectors.WorkflowEffector;
import service.composite.CompositeServiceClient;
import service.registry.ServiceRegistry;
import service.utility.Time;
import tas.data.systemprofile.SystemProfile;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.services.assistance.AssistanceService;
import tas.services.assistance.AssistanceServiceCostProbe;
import tas.services.qos.MinCostQoS;
import tas.services.qos.PreferencesQoS;
import tas.services.qos.ReliabilityQoS;

public class WorkflowExecutor {

	// Fields	
	// The amount of workflow execution cycles that are executed before the possibility of analyzer execution
	public static int workflowCycles = 100;
	
    private boolean isStopped = false;
    private boolean isPaused = false;
    private int currentSteps;
    
    private SystemEntity systemEntity;
    private AssistanceService assistanceService;
    private AssistanceServiceCostProbe monitor;
    private WorkflowEffector workflowEffector;
    private String workflowPath, profilePath;
    
	public WorkflowExecutor(List<ServiceRegistry> serviceRegistries) {
		initializeWorkFlowExecuter(serviceRegistries);
	}
	
	/**
	 * Set the parent system entity to the given entity
	 * @param entityName the given entity
	 */
	public void setSystemEntity(SystemEntity systemEntity) throws IllegalStateException {
		
		if (systemEntity != null) {
			throw new IllegalStateException("A system entity can only be assigned once!");
		}
		
		this.systemEntity = systemEntity;
	}
	
	/**
	 * Return the parent system entity
	 * @return the parent system entity
	 */
	public SystemEntity getSystemEntity() {
		return systemEntity;
	}
    
    public synchronized void stop(){
    	isStopped = true;
    }
    
    private synchronized void start(){
    	isStopped = false;
    }
    
    public synchronized void pause(){
    	isPaused = true;
    }
    
    public synchronized void go(){
    	isPaused = false;
    	this.notifyAll();
    }
    
    public boolean isStopped(){
    	return isStopped;
    }
    
    public boolean isPaused(){
    	return isPaused;
    }
    
    public int getCurrentSteps(){
    	return currentSteps;
    }
    
    public void setWorkflowPath(String workflowPath) {
    	this.workflowPath = workflowPath;
		assistanceService.setWorkflow(workflowPath);
    }
    
    public String getWorkflowPath() {
    	return workflowPath;
    }
    
    public void setProfilePath(String profilePath) {
    	this.profilePath = profilePath;
		ProfileExecutor.readFromXml(profilePath, systemEntity.getEntityName());
    }
    
    public String getProfilePath() {
    	return profilePath;
    }
    
    public AssistanceService getAssistanceService() {
    	return assistanceService;
    }
    
    public void stopAssistanceService() {
    	assistanceService.stopService();
    }
	
    public void initializeWorkFlowExecuter(List<ServiceRegistry> serviceRegistries) {
	
		// Assistance Service. Workflow is provided by TAS_gui through executeWorkflow method
		assistanceService = new AssistanceService("TeleAssistanceService", "service.assistance", "resources/TeleAssistanceWorkflow.txt", serviceRegistries);
		assistanceService.startService();
		//assistanceService.register(serviceRegistry);
		monitor = new AssistanceServiceCostProbe();
		assistanceService.getCostProbe().register(monitor);
		assistanceService.getWorkflowProbe().register(monitor);
		//assistanceService.getWorkflowProbe().register(new AssistanceServiceDelayProbe());
		// assistanceService.getServiceInvocationProbe().register(monitor);
		assistanceService.addQosRequirement("ReliabilityQoS", new ReliabilityQoS());
		assistanceService.addQosRequirement("PreferencesQoS", new PreferencesQoS());
		assistanceService.addQosRequirement("CostQoS", new MinCostQoS());
		
		workflowEffector = new WorkflowEffector(assistanceService);
    }
	
    public void executeWorkflow() {

    	SystemProfile profile = SystemProfileDataHandler.activeProfile;
		CompositeServiceClient client = new CompositeServiceClient("service.assistance");
		workflowEffector.refreshAllServices();
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
		    		break;
		    	}
			
		    }
		    
		    stop();
		    System.out.println("Finished executing the workflow.");
		}
    }
}
