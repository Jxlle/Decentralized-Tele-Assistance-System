package tas.mape;

import service.composite.CompositeService;

public class MAPEKComponent {
	
	// The amount of workflow execution cycles that are executed before the possibility of analyzer execution
	private static int amountOfCycles = 100;
	
	private Monitor monitor;
	private Analyzer analyzer;
	private Planner planner;
	private Executer executer;
	private Knowledge knowledge;
	private CompositeService compositeService;
	
	public MAPEKComponent(int loadFailureDelta, String plannerEndpoint) {
		initializeComponents(loadFailureDelta, plannerEndpoint);
	}
	
	private void initializeComponents(int loadFailureDelta, String plannerEndpoint) {
		
		//knowledge = new Knowledge(loadFailureDelta);
		monitor = new Monitor(compositeService, knowledge, analyzer, amountOfCycles);
		//analyzer = new Analyzer(knowledge, planner);
		planner = new Planner(plannerEndpoint, executer);
		executer = new Executer(compositeService);
	}
	
	// The methods below are used to simulate the mape loop for multiple workflow entities 'concurrently'
	// instead of using multiple threads
	
	public void ExecuteMonitor() {
		monitor.execute();
	}
	
	public void ExecuteAnalyzer() {
		monitor.triggerAnalyzer();
	}
	
	public void ExecutePlanner() {
		analyzer.triggerPlanner();
	}
	
	public void ExecuteExecuter() {
		planner.triggerExecuter();
	}
}
