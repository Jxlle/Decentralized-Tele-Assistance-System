package tas.mape.managingsystem;

import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;
import tas.mape.analyzer.AbstractWorkflowQoSRequirement;
import tas.mape.analyzer.Analyzer;
import tas.mape.executer.Executer;
import tas.mape.knowledge.Knowledge;
import tas.mape.monitor.Monitor;
import tas.mape.planner.Planner;

public class MAPEKComponent {
	
	// The amount of workflow execution cycles that are executed before the possibility of analyzer execution
	private static int amountOfCycles = 100;
	
	private Monitor monitor;
	private Analyzer analyzer;
	private Planner planner;
	private Executer executer;
	private Knowledge knowledge;
	private CompositeService compositeService;
	
	// TODO Make builder class or remove some constructor parameters and add them as method.
	
	public MAPEKComponent(int loadFailureDelta, int combinationLimit, double minFailureDelta, double failureChange, String currentQoSRequirement, 
			String plannerEndpoint, Map<String, AbstractWorkflowQoSRequirement> QoSRequirementClasses, Map<String, Integer> QoSStrategies, 
			Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
		
		initializeComponents(loadFailureDelta, combinationLimit, minFailureDelta, failureChange, currentQoSRequirement, plannerEndpoint, QoSRequirementClasses, QoSStrategies, usableServicesAndChance);
	}
	
	private void initializeComponents(int loadFailureDelta, int combinationLimit, double minFailureDelta, double failureChange, String currentQoSRequirement, 
			String plannerEndpoint, Map<String, AbstractWorkflowQoSRequirement> QoSRequirementClasses, Map<String, Integer> QoSStrategies, 
			Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
		
		knowledge = new Knowledge(amountOfCycles, loadFailureDelta, currentQoSRequirement, QoSRequirementClasses, usableServicesAndChance);
		monitor = new Monitor(compositeService, knowledge, analyzer, minFailureDelta, failureChange);
		analyzer = new Analyzer(knowledge, planner, combinationLimit, QoSStrategies);
		planner = new Planner(plannerEndpoint, executer);
		executer = new Executer(compositeService);
	}
	
	// The methods below are used to simulate the mape loop for multiple workflow entities 'concurrently'
	// instead of using multiple threads. Using multiple threads is possible, but can introduce race conditions.
	
	/**
	 * Execute the monitor component
	 */
	public void ExecuteMonitor() {
		monitor.execute();
	}
	
	/**
	 * Execute the analyzer component
	 */
	public void ExecuteAnalyzer() {
		monitor.triggerAnalyzer();
	}
	
	/**
	 * Execute the planner component
	 */
	public void ExecutePlanner() {
		analyzer.triggerPlanner();
	}
	
	/**
	 * Execute the executer component
	 */
	public void ExecuteExecuter() {
		planner.triggerExecuter();
	}
}
