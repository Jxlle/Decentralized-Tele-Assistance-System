package tas.mape.system.entity;

import java.util.List;
import tas.mape.analyzer.Analyzer;
import tas.mape.executor.Executor;
import tas.mape.knowledge.Knowledge;
import tas.mape.monitor.Monitor;
import tas.mape.planner.Planner;
/**
 * Class representing a component that can execute a MAPEK-loop.
 * This is a managing system for the managed system, the workflow executor.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class MAPEKComponent {
	
	/**
	 * Private class used as a builder to build MAPE-K component objects.
	 * This class is used to avoid a big constructor for the MAPE-K component.
	 */
	public static class Builder {
		
		// Components
		private Monitor monitor;
		private Analyzer analyzer;
		private Planner planner;
		private Executor executor;
		private Knowledge knowledge;
		
		/**
		 * Create a new builder with an initialized executor
		 */
		public Builder() {
			executor = new Executor();
		}
		
		/**
		 * Creates a new builder where the knowledge component with its given parameters has been initialized
		 * @param loadFailureDelta the given load failure delta 
		 * @param serviceRegistryEndpoints the given registry endpoints used by the workflow
		 * @return the new Builder object with initialized knowledge
		 */
		public Builder initializeKnowledge(int loadFailureDelta, List<String> serviceRegistryEndpoints) {
			
			knowledge = new Knowledge(loadFailureDelta, serviceRegistryEndpoints);
			return this;
		}
		
		/**
		 * Creates a new builder where the planner component with its given parameters has been initialized.
		 * @return the new Builder object with initialized planner
		 * @throws InstantiationException throw when the knowledge field is null
		 * @throws InstantiationException throw when the executor field is null
		 */
		public Builder initializePlanner() throws InstantiationException {
			
			if (knowledge == null) {
				throw new InstantiationException("Knowledge field is null!");
			}
			
			if (executor == null) {
				throw new InstantiationException("Executor field is null!");
			}
			
			planner = new Planner(knowledge, executor);
			return this;
		}
		
		/**
		 * Creates a new builder where the analyzer component with its given parameters has been initialized.
		 * @param combinationLimit the given combination limit that will decide how much service combinations 
		 *        will be chosen in the execute step
	 * @param serviceGenerationStrategy the given service generation strategy
		 * @return the new Builder object with initialized analyzer
		 * @throws InstantiationException throw when the knowledge field is null
		 * @throws InstantiationException throw when the planner field is null
		 */
		public Builder initializeAnalyzer(int combinationLimit, int serviceGenerationStrategy) 
				throws InstantiationException {
			
			if (knowledge == null) {
				throw new InstantiationException("Knowledge field is null!");
			}
			
			if (planner == null) {
				throw new InstantiationException("Planner field is null!");
			}
			
			analyzer = new Analyzer(knowledge, planner, combinationLimit, serviceGenerationStrategy);
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
			
			if (monitor == null) {
				throw new IllegalStateException("The build process can't be started, some components haven't been initialized!");
			}
			
			component.monitor = monitor;
			component.analyzer = analyzer;
			component.planner = planner;
			component.executor = executor;
			component.knowledge = knowledge;
			
			return component;
		}	
	}
	
	// MAPE-K components
	private Monitor monitor;
	private Analyzer analyzer;
	private Planner planner;
	private Executor executor;
	private Knowledge knowledge;
	
	/**
	 * Create a new MAPE-K component.
	 * @note The constructor is private because the only way to create this component is through the builder class.
	 */
	private MAPEKComponent() {}
	
	/**
	 * the monitor component
	 * @return the monitor component
	 */
	public Monitor getMonitor() {
		return monitor;
	}
	
	/**
	 * the analyzer component
	 * @return the analyzer component
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}
	
	/**
	 * the planner component
	 * @return the planner component
	 */
	public Planner getPlanner() {
		return planner;
	}
	
	/**
	 * Return the executor component
	 * @return the executor component
	 */
	public Executor getExecutor() {
		return executor;
	}
	
	/**
	 * Return the knowledge component
	 * @return the knowledge component
	 */
	public Knowledge getKnowledge() {
		return knowledge;
	}
	
	// The methods below are used to simulate the MAPEK loop for multiple workflow entities 'concurrently'
	// instead of using multiple threads. Using multiple threads is possible, but can introduce race conditions.
	
	/**
	 * Execute the monitor component
	 */
	public void executeMonitor() {
		//System.err.print("Executing monitor...\n");
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
	 * Execute the executor component
	 */
	public void executeExecutor() {
		planner.triggerExecutor();
	}
}
