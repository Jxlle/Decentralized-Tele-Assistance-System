package tas.adaptation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import profile.InputProfileValue;
import profile.InputProfileVariable;
import profile.ProfileExecutor;
import service.adaptation.effectors.WorkflowEffector;
import service.atomic.AtomicService;
import service.composite.CompositeServiceClient;
import service.registry.ServiceRegistry;
import service.utility.Time;
import tas.mape.Analyzer;
import tas.mape.Executer;
import tas.mape.Knowledge;
import tas.mape.Monitor;
import tas.mape.Planner;
import tas.services.alarm.AlarmService;
import tas.services.assistance.AssistanceService;
import tas.services.assistance.AssistanceServiceCostProbe;
import tas.services.drug.DrugService;
import tas.services.medical.MedicalAnalysisService;
import tas.services.profiles.ServiceDelayProfile;
import tas.services.profiles.ServiceFailureProfile;
import tas.services.profiles.ServiceFailureLoadProfile;
import tas.services.profiles.SimpleServiceFailureProfile;
import tas.services.qos.MinCostQoS;
import tas.services.qos.PreferencesQoS;
import tas.services.qos.ReliabilityQoS;

public class TASStart {

    private HashMap<String, AdaptationEngine> adaptationEngines = new LinkedHashMap<>();

    protected ServiceRegistry serviceRegistry, serviceRegistry2;
    protected AssistanceService assistanceService;
    protected AssistanceServiceCostProbe monitor;
    protected WorkflowEffector workflowEffector;

    protected AlarmService alarm1;
    protected AlarmService alarm2;
    protected AlarmService alarm3;

    protected MedicalAnalysisService medicalAnalysis1;
    protected MedicalAnalysisService medicalAnalysis2;
    protected MedicalAnalysisService medicalAnalysis3;

    protected DrugService drugService;
    
    private boolean isStopped=false;
    private boolean isPaused=false;
    private int currentSteps;
    
    private Map<String,AtomicService> atomicServices=new HashMap<>();
    private List<Class<?>> serviceProfileClasses=new ArrayList<>();
    private List<ServiceRegistry> serviceRegistries;
    
    private LinkedHashMap<String,String> serviceTypes=new LinkedHashMap<>();
    
    public synchronized void stop(){
    	isStopped=true;
    }
    
    private synchronized void start(){
    	isStopped=false;
    }
    
    public synchronized void pause(){
    	isPaused=true;
    }
    
    public synchronized void go(){
    	isPaused=false;
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
    
    public void addAllServices(AtomicService... services){
    	for(AtomicService service:services){
    		atomicServices.put(service.getServiceDescription().getServiceName(), service);	
    		this.serviceTypes.put(service.getServiceDescription().getServiceName(), 
    				service.getServiceDescription().getServiceType());
    	}
    	
    }
    
    public LinkedHashMap<String,String> getServiceTypes(){
    	return this.serviceTypes;
    }
    
    public List<Class<?>> getServiceProfileClasses(){
    	return this.serviceProfileClasses;
    }
    
    public AtomicService getService(String name){
    	return atomicServices.get(name);
    }

    // TODO CHANGE GUI FOR MORE SERVICE REGISTRIES
    public List<ServiceRegistry> getServiceRegistries() {
    	return serviceRegistries;
    }

    public AssistanceService getAssistanceService() {
	return assistanceService;
    }

    public AssistanceServiceCostProbe getMonitor() {
	return monitor;
    }

    public TASStart() {

	initializeTAS();

	adaptationEngines.put("No Adaptation", new DefaultAdaptationEngine());
	adaptationEngines.put("Simple Adaptation", new MyAdaptationEngine(assistanceService));
    }

    public HashMap<String, AdaptationEngine> getAdaptationEngines() {
    	return adaptationEngines;
    }

    public void initializeTAS() {

	serviceRegistry = new ServiceRegistry("ServiceRegistry", "se.lnu.service.registry");
	serviceRegistry.startService();
	
	serviceRegistry2 = new ServiceRegistry("ServiceRegistry2", "se.lnu.service.registry2");
	serviceRegistry2.startService();
	
	serviceRegistries = new ArrayList<ServiceRegistry>();
	serviceRegistries.add(serviceRegistry);
	serviceRegistries.add(serviceRegistry2);

	// Alarm Services
	alarm1 = new AlarmService("AlarmService1", "service.alarmService1");
	alarm1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
	alarm1.getServiceDescription().setOperationCost("triggerAlarm", 4.0);
	alarm1.getServiceDescription().getCustomProperties().put("FailureRate", 0.11);
	alarm1.startService();
	alarm1.register(serviceRegistry2);

	alarm2 = new AlarmService("AlarmService2", "service.alarmService2");
	alarm2.getServiceDescription().getCustomProperties().put("Cost", 12.0);
	alarm2.getServiceDescription().getCustomProperties().put("preferred", true);
	alarm2.getServiceDescription().setOperationCost("triggerAlarm", 12.0);
	alarm2.getServiceDescription().getCustomProperties().put("FailureRate", 0.04);
	//alarm2.addServiceProfile(new ServiceFailureProfile(0.04));
	alarm2.startService();
	alarm2.register(serviceRegistry);

	alarm3 = new AlarmService("AlarmService3", "service.alarmService3");
	alarm3.getServiceDescription().getCustomProperties().put("Cost", 2.0);
	alarm3.getServiceDescription().setOperationCost("triggerAlarm", 2.0);
	alarm3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
	//alarm3.addServiceProfile(new ServiceFailureProfile(0.18));
	alarm3.startService();
	alarm3.register(serviceRegistry);

	// Medical Analysis Services
	medicalAnalysis1 = new MedicalAnalysisService("MedicalService1", "service.medical1");
	medicalAnalysis1.getServiceDescription().getCustomProperties().put("preferred", false);
	medicalAnalysis1.getServiceDescription().getCustomProperties().put("Cost", 4.0);
	medicalAnalysis1.getServiceDescription().setOperationCost("analyzeData", 4.0);
	medicalAnalysis1.getServiceDescription().getCustomProperties().put("FailureRate", 0.12);
	//medicalAnalysis1.addServiceProfile(new ServiceFailureProfile(0.12));
	//medicalAnalysis1.addServiceProfile(new ServiceFailureProfile(0.12));
	medicalAnalysis1.startService();
	medicalAnalysis1.register(serviceRegistry);

	medicalAnalysis2 = new MedicalAnalysisService("MedicalService2", "service.medical2");
	medicalAnalysis2.getServiceDescription().getCustomProperties().put("preferred", true);
	medicalAnalysis2.getServiceDescription().getCustomProperties().put("Cost", 14.0);
	medicalAnalysis2.getServiceDescription().setOperationCost("analyzeData", 14.0);
	medicalAnalysis2.getServiceDescription().getCustomProperties().put("FailureRate", 0.07);
	medicalAnalysis2.startService();
	medicalAnalysis2.register(serviceRegistry);

	medicalAnalysis3 = new MedicalAnalysisService("MedicalService3", "service.medical3");
	medicalAnalysis3.getServiceDescription().getCustomProperties().put("preferred", false);
	medicalAnalysis3.getServiceDescription().getCustomProperties().put("Cost", 2.15);
	medicalAnalysis3.getServiceDescription().setOperationCost("analyzeData", 2.15);
	medicalAnalysis3.getServiceDescription().getCustomProperties().put("FailureRate", 0.18);
	medicalAnalysis3.startService();
	medicalAnalysis3.register(serviceRegistry);

	// Drug Services
	drugService = new DrugService("DrugService", "service.drug");
	drugService.getServiceDescription().getCustomProperties().put("preferred", true);
	drugService.getServiceDescription().getCustomProperties().put("Cost", 2.0);
	drugService.getServiceDescription().setOperationCost("changeDoses", 5.0);
	drugService.getServiceDescription().setOperationCost("changeDrug", 5.0);
	//drugService.getServiceDescription().getCustomProperties().put("FailureRate", 0.01);
	drugService.startService();
	drugService.register(serviceRegistry);

	// Assistance Service. Workflow is provided by TAS_gui through executeWorkflow method
	assistanceService = new AssistanceService("TeleAssistanceService", "service.assistance", "resources/TeleAssistanceWorkflow.txt", Arrays.asList(serviceRegistry, serviceRegistry2));
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
	
	this.addAllServices(alarm1,alarm2,alarm3,medicalAnalysis1,medicalAnalysis2,medicalAnalysis3,drugService);
	
	//this.serviceProfileClasses.add(ServiceFailureProfile.class);
	this.serviceProfileClasses.add(ServiceDelayProfile.class);
	this.serviceProfileClasses.add(SimpleServiceFailureProfile.class);
	this.serviceProfileClasses.add(ServiceFailureLoadProfile.class);
	
	/*Knowledge knowledge = new Knowledge(10);
	Executer executer1 = new Executer(assistanceService);
	Planner planner = new Planner(executer1);
	Analyzer analyzer = new Analyzer(knowledge, planner);
	Monitor monitor = new Monitor(assistanceService, knowledge, analyzer, 100);*/
    }

    public void stopServices() {
	serviceRegistry.stopService();

	alarm1.stopService();
	alarm2.stopService();
	alarm3.stopService();

	medicalAnalysis1.stopService();
	medicalAnalysis2.stopService();
	medicalAnalysis3.stopService();

	drugService.stopService();
	assistanceService.stopService();

    }

    public void executeWorkflow(String workflowPath, String profilePath) {

	CompositeServiceClient client = new CompositeServiceClient("service.assistance");
	assistanceService.setWorkflow(workflowPath);
	workflowEffector.refreshAllServices();
	Time.steps.set(0);

	ProfileExecutor.readFromXml(profilePath);
	if (ProfileExecutor.profile != null) {
	    int maxSteps = (int) ProfileExecutor.profile.getMaxSteps();
	    InputProfileVariable variable = ProfileExecutor.profile.getVariable("pick");
	    List<InputProfileValue> values = variable.getValues();

	    int patientId = (int) ProfileExecutor.profile.getVariable("patientId").getValues().get(0).getData();
	    int pick;
	    // System.out.println("start executing workflow !!!");

	    start();
	    Random rand = new Random();
	    for (currentSteps = 0; currentSteps < maxSteps; currentSteps++) {
	    
	    	/*
	    	synchronized(this){
	    		while(isPaused()){
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	    		}
	    	}*/
	    	
	    	//System.out.println(((ExecutionThread)Thread.currentThread()).getThreadName());
	    	
	    	Time.steps.incrementAndGet();
	    	    	
		double probability = rand.nextDouble();
		double valueProbability = 0;
		for (int j = 0; j < values.size(); j++) {
		    if ((values.get(j).getRatio() + valueProbability) > probability) {
			pick = (int) values.get(j).getData();
			client.invokeCompositeService(ProfileExecutor.profile.getQosRequirement(), patientId, pick);
			break;
		    } else
			valueProbability = valueProbability + values.get(j).getRatio();
		}
		
    	
    	if(isStopped)
    		break;
		
	    }
	    stop();
	    System.out.println("finish executing workflow !!!");
	}
    }
   
}
