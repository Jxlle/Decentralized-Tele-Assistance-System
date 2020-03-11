package tas.mape.test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import profile.SystemProfileValue;
import profile.SystemProfileVariable;
import profile.SystemRequirementType;
import service.adaptation.effectors.WorkflowEffector;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.StaticTree;
import service.auxiliary.WeightedCollection;
import service.registry.ServiceRegistry;
import tas.data.serviceinfo.GlobalServiceInfo;
import tas.data.serviceinfo.GlobalServiceInfoLoader;
import tas.data.serviceinfo.GlobalServiceInfoWriter;
import tas.data.systemprofile.ProtocolType;
import tas.data.systemprofile.SystemProfile;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.data.systemprofile.SystemType;
import tas.mape.analyzer.CostAndReliabilityReq;
import tas.mape.analyzer.CostReq;
import tas.mape.analyzer.ReliabilityReq;
import tas.mape.communication.message.ComponentMessageHost;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.executor.Executor;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalRelation;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.planner.PlanComponent;
import tas.mape.planner.PlanComponentType;
import tas.mape.planner.Planner;
import tas.mape.planner.ServiceCombination;
import tas.mape.system.entity.SystemEntity;
import tas.mape.system.structure.DoubleLoopSystem;
import tas.mape.system.structure.SoloLoopSystem;
import tas.mape.planner.RatingType;
import tas.services.alarm.AlarmService;
import tas.services.assistance.AssistanceService;
import tas.services.assistance.AssistanceServiceCostProbe;
import tas.services.drug.DrugService;
import tas.services.medical.MedicalAnalysisService;
import tas.services.qos.MinCostQoS;
import tas.services.qos.PreferencesQoS;
import tas.services.qos.ReliabilityQoS;
import tas.workflowAnalyzer.WorkflowAnalyzer;

@SuppressWarnings("unused")
public class TEST {
	
    protected static ServiceRegistry serviceRegistry, serviceRegistry2;
    protected static AssistanceService assistanceService;
    protected static AssistanceServiceCostProbe monitor;
    protected static WorkflowEffector workflowEffector;

    protected static AlarmService alarm1;
    protected static AlarmService alarm2;
    protected static AlarmService alarm3;
    
    protected static DrugService drugService;

    protected static MedicalAnalysisService medicalAnalysis1;
    protected static MedicalAnalysisService medicalAnalysis2;
    protected static MedicalAnalysisService medicalAnalysis3;
    
    private static List<ServiceRegistry> serviceRegistries;
	
	public static void main(String[] args) {	
		init();
	}
	
	public static void init() {
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
		
		workflowEffector = new WorkflowEffector(assistanceService);
		
		
		// TESTING CODE
		assistanceService.updateCache();
		
		for (String service : assistanceService.getCache().getServiceDescriptions().keySet()) {
			System.out.print(service + " \n");
		}
		
		//System.out.print(assistanceService.getCache().getServiceWithEndpoint("service.medical3").getServiceName() + " \n");
		
		
		List<PlanComponent> plan = new ArrayList<PlanComponent>();
		PlanComponentType planType1 = PlanComponentType.INCREASE_LOAD;
		PlanComponent planComp1 = new PlanComponent(planType1, "service.drug", 2);
		plan.add(planComp1);
		
		Executor executer = new Executor();
		executer.initializeEffectors(assistanceService);
		executer.execute(plan);
		
		
		WeightedCollection<String> items = new WeightedCollection<String>();
		items.add("50% chance", 50);
		items.add("25% chance", 25);
		items.add("25% chance 2", 25);
		
		Map<String, Integer> kansenTest = new HashMap<String, Integer>();
		
		
		for (int i = 0; i < 10000; i++) {
			String itemName = items.next();
			int newValue = 0;
			
			if (kansenTest.get(itemName) != null) {
				newValue = kansenTest.get(itemName) + 1;
			}
			
			kansenTest.put(itemName, newValue);
		}
		
		for (String s : kansenTest.keySet()) {
			System.err.print(s + " " + kansenTest.get(s) + " \n");
		}
		
		items.remove("50% chance");
		System.err.print("----------------------------------------------\n");
		
		items.increaseWeight("25% chance", 1);
		for (Integer s : items.getWeights()) {
			System.err.print(s + "\n");
		}
		
		System.err.print("----------------------------------------------\n");
		System.err.print(items.getChance("25% chance") + "\n");
		
		
		@SuppressWarnings("unused")
		CostReq cr = new CostReq();
		ReliabilityReq rr = new ReliabilityReq();
		CostAndReliabilityReq carr = new CostAndReliabilityReq();
		
		// ANALYZER TEST
		// TODO CACHES PRIVATE
		assistanceService.getCache().getServiceWithEndpoint("service.alarmService3");
		List<Goal> goals = new ArrayList<>();
		Goal costGoal = new Goal(GoalType.COST, GoalRelation.LOWER_OR_EQUAL_TO, 20);
		Goal reliabilityGoal = new Goal(GoalType.FAILURE_RATE, GoalRelation.LOWER_OR_EQUAL_TO, 0.2);
		goals.add(costGoal);
		goals.add(reliabilityGoal);
		List<ServiceCombination> services1 = null;
		//List<ServiceCombination> services1 = carr.getServiceCombinations(2, 100, RatingType.CLASS, goals, assistanceService.getCache().caches);
		Map<String, Double> serviceFailureRates = new HashMap<>();
		serviceFailureRates.put("service.medical2", 1.0);
		//List<ServiceCombination> services = carr.getNewServiceCombinations(services1, serviceFailureRates, goals);
		//List<Map<Description, ServiceDescription>> services2 = mcr.getAllServiceCombinations(assistanceService.getCache().caches);
		
		/*for (ServiceCombination comb : services1) {
			System.err.print("-----------------------------------------------------------------------------------------\n");
			
			comb.getDescriptions();
			
			for (Description d : comb.getDescriptions()) {
				
				System.err.print("["+ d.toString() + "] \n");
				
				for (ServiceDescription service : comb.getAllServices(d).getItems()) {
					System.err.print(" -> [name] " + service.getServiceEndpoint() + ", [usage chance] " + comb.getAllServices(d).getChance(service) + "\n");
				}
				
				System.err.print(" \n");
			}
			
			System.err.print("-------------------------------------------------------------------------------------------\n");
			System.err.print("score: " + comb.getRating() + " \n");
		}
		
		System.err.print("\n total combinations: " + services1.size());*/
		
		
		/*for (Map<Description, ServiceDescription> map : services2) {
			System.err.print("-----------------------------------------------------------------------------------------\n");
			
			for (Description d : map.keySet()) {
				
				System.err.print("["+ d.toString() + "] : " + map.get(d).getServiceEndpoint() + "\n");
				
			}
			
			System.err.print("-------------------------------------------------------------------------------------------\n");
		}*/
		
		/*Planner p1 = new Planner("mape.planner1", executer);
		Planner p2 = new Planner("mape.planner2", executer);
		ComponentMessageHost host = new ComponentMessageHost();
		host.register(p1);
		host.register(p2);
		System.err.print("host size " + host.getListeners().size() + " \n");
		//p2.setEndpoint("mape.planner2new");
		p1.sendMessage(new PlannerMessage(1, p2.getEndpoint(), p1.getEndpoint(), "test", null, null));
		
		
		ServiceCombination combination = new ServiceCombination(null, RatingType.NUMBER, 1.0);

		
		System.err.print("------------------------------------------------------------ \n");
		System.err.print("test: " + combination.getRating().getClass() + "\n");*/
		
		
		/*WorkflowAnalyzer wpi = new WorkflowAnalyzer();
		Map<Description, Pair<List<ServiceDescription>, Double>> test = wpi.analyzeWorkflow(5000, "resources/TeleAssistanceWorkflow.txt", "resources/files/PreferredQoS.xml", assistanceService);
		
		for (Description d : test.keySet()) {
			System.err.print("------------------------------------------------------------ \n");
			
			for (ServiceDescription sd : test.get(d).getKey()) {
				System.err.print("service name: " + sd.getServiceName() + " \n");
			}
			
			System.err.print("=== \n");
			System.err.print("description usage chance :" + test.get(d).getValue() + " \n");
			System.err.print("------------------------------------------------------------ \n");
		}*/
		
		//SystemEntity e = new SystemEntity(null, null);
		//SoloLoopSystem s = new SoloLoopSystem(e);
		// TODO system testing
		
		/*GlobalServiceInfo info = new GlobalServiceInfo();
		File file = new File("resources" + File.separator + "DefaultServiceData.xml");
		//GlobalServiceInfoWriter.writeToXml(info, file);
		info.loadData(file);
		//GlobalServiceInfoLoader.loadFromXml(info, file);*/
		
		SystemProfile profile = new SystemProfile();//50, 250, SystemRequirementType.COST, RatingType.SCORE, DoubleLoopSystem.class, new ArrayList<>());
		profile.setExecutionCycles(10);
		profile.setWorkflowCycles(2000);
		profile.setRatingType(RatingType.SCORE);
		profile.setSystemType(SystemType.DOUBLE_LOOP);
		profile.addEntity("Test Entity", SystemRequirementType.COST_AND_RELIABILITY);
		profile.addEntity("Test Entity 2", SystemRequirementType.COST_AND_RELIABILITY);
		profile.setProtocolType(ProtocolType.STANDARD_PROTOCOL);
		SystemProfileVariable patientId = new SystemProfileVariable("patientId");
		patientId.addValue(new SystemProfileValue(1, 1.0));
		SystemProfileVariable pick = new SystemProfileVariable("pick");
		pick.addValue(new SystemProfileValue(1, 0.75));
		pick.addValue(new SystemProfileValue(2, 0.25));
		profile.addVariable(patientId);
		profile.addVariable(pick);
		
		SystemProfileDataHandler.writeToXml(profile, "test");
		
		
		
		StaticTree tree = new StaticTree();
		Description d1 = new Description("test", "test");
		Description d2 = new Description("test", "test");
		Description d3 = new Description("test2", "test1");
		Description d4 = new Description("test3", "test4");
		
		tree.addNodes(Arrays.asList(d1, d3));
		System.out.println("----------------------");
		tree.addNodes(Arrays.asList(d1, d3));
		System.out.println("----------------------");
		tree.addNodes(Arrays.asList(d4, d2));
		System.out.println("----------------------");
		
		System.out.println("TREE SIZE: " + tree.getTreeSize());

	}
}
