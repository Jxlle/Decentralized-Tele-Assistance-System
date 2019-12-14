package tas.mape.system;

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

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a component that can execute a MAPEK-loop.
 * This is a managing system for the managed system, the workflow executer.
 */
public class MAPEKComponent {
	
	/**
	 * Private class used as a builder to build MAPE-K component objects.
	 * This class is used to avoid a big constructor for the MAPE-K component.
	 */
	public static class Builder {
		
		private Monitor monitor;
		private Analyzer analyzer;
		private Planner planner;
		private Executer executer;
		private Knowledge knowledge;
		
		/**
		 * Create a new builder with an initialized executer
		 */
		public Builder() {
			executer = new Executer();
		}
		
		/**
		 * Creates a new builder where the knowledge component with its given parameters has been initialized
		 * @param loadFailureDelta the given load failure delta 
		 * @param currentQoSRequirement the given current QoS requirement
		 * @param goals the given goals map
		 * @param QoSRequirementClasses the given QoS requirement classes map
		 * @param usableServicesAndChance the given usable services with usage chance map
		 * @return
		 */
		public Builder initializeKnowledge(int loadFailureDelta, String currentQoSRequirement, Map<String, Double> goals, Map<String, AbstractWorkflowQoSRequirement> QoSRequirementClasses, Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
			
			knowledge = new Knowledge(MAPEKComponent.amountOfCycles, loadFailureDelta, currentQoSRequirement, goals, QoSRequirementClasses, usableServicesAndChance);
			return this;
		}
		
		/**
		 * Creates a new builder where the planner component with its given parameters has been initialized.
		 * @param plannerEndpoint the given planner end point
		 * @return the new Builder object with initialized planner
		 * @throws InstantiationException throw when the executer field is null
		 */
		public Builder initializePlanner(String plannerEndpoint) throws InstantiationException {
			
			if (executer == null) {
				throw new InstantiationException("Executer field is null!");
			}
			
			planner = new Planner(plannerEndpoint, executer);
			return this;
		}
		
		/**
		 * Creates a new builder where the analyzer component with its given parameters has been initialized.
		 * @param combinationLimit the given combination limit
		 * @param QoSStrategies the given QoS strategies map
		 * @return the new Builder object with initialized analyzer
		 * @throws InstantiationException throw when the knowledge field is null
		 * @throws InstantiationException throw when the planner field is null
		 */
		public Builder initializeAnalyzer(int combinationLimit, Map<String, Integer> QoSStrategies) throws InstantiationException {
			
			if (knowledge == null) {
				throw new InstantiationException("Knowledge field is null!");
			}
			
			if (planner == null) {
				throw new InstantiationException("Planner field is null!");
			}
			
			analyzer = new Analyzer(knowledge, planner, combinationLimit, QoSStrategies);
			return this;
		}
		
		/**
		 * Creates a new builder where the monitor component with its given parameters has been initialized.
		 * @param minFailureDelta the given minimum failure delta
		 * @param failureChange the given failure change
		 * @return the new Builder object with initialized monitor
		 * @throws InstantiationException throw when the knowledge field is null
		 * @throws InstantiationException throw when the analyzer field is null
		 */
		public Builder initializeMonitor(double minFailureDelta, double failureChange) throws InstantiationException {
			
			if (knowledge == null) {
				throw new InstantiationException("Knowledge field is null!");
			}
			
			if (analyzer == null) {
				throw new InstantiationException("Analyzer field is null!");
			}
			
			monitor = new Monitor(knowledge, analyzer, minFailureDelta, failureChange);
			return this;
		}
		
		/**
		 * Creates and returns a MAPE-K component built with the initialized data.
		 * @return the built MAPE-K component
		 * @throws IllegalStateException throw when some components haven't been initialized.
		 */
		public MAPEKComponent build() throws IllegalStateException {
			
			MAPEKComponent component = new MAPEKComponent();
			
			if (monitor == null || analyzer == null || planner == null) {
				throw new IllegalStateException("The build process can't be started, some components haven't been initialized!");
			}
			
			component.monitor = monitor;
			component.analyzer = analyzer;
			component.planner = planner;
			component.executer = executer;
			
			return component;
		}	
	}
	
	// The amount of workflow execution cycles that are executed before the possibility of analyzer execution
	private static int amountOfCycles = 100;
	
	private Monitor monitor;
	private Analyzer analyzer;
	private Planner planner;
	private Executer executer;
	
	/**
	 * Create a new MAPE-K component.
	 * @note The constructor is private because the only way to create this component is through the builder class.
	 */
	private MAPEKComponent() {}
	
	/**
	 * Initialize the executer components with the given composite service.
	 * @param compositeService the given composite service
	 */
	public void initializeExecuterEffectors(CompositeService compositeService) {
		executer.initializeEffectors(compositeService);
	}
	
	/**
	 * Connect all monitor probes with the probes of the given composite service
	 * @param compositeService the given composite service
	 */
	public void connectMonitorProbes(CompositeService compositeService) {
		monitor.connectProbes(compositeService);
	}
	
	// The methods below are used to simulate the mape loop for multiple workflow entities 'concurrently'
	// instead of using multiple threads. Using multiple threads is possible, but can introduce race conditions.
	
	/**
	 * Execute the monitor component
	 */
	public void executeMonitor() {
		monitor.execute();
	}
	
	/**
	 * Execute the analyzer component
	 */
	public void executeAnalyzer() {
		monitor.triggerAnalyzer();
	}
	
	/**
	 * Execute the planner component
	 */
	public void executePlanner() {
		analyzer.triggerPlanner();
	}
	
	/**
	 * Execute the executer component
	 */
	public void executeExecuter() {
		planner.triggerExecuter();
	}
}
