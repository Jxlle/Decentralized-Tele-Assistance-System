package tas.mape.system.entity;

import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import profile.SystemRequirementType;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.composite.CompositeService;
import tas.mape.analyzer.Analyzer;
import tas.mape.communication.message.ComponentMessageHost;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.executor.Executor;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Knowledge;
import tas.mape.monitor.Monitor;
import tas.mape.planner.Planner;
import tas.mape.planner.RatingType;

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
		 * @param plannerEndpoint the given planner end point
		 * @return the new Builder object with initialized planner
		 * @throws InstantiationException throw when the knowledge field is null
		 * @throws InstantiationException throw when the executor field is null
		 */
		public Builder initializePlanner(String plannerEndpoint) throws InstantiationException {
			
			if (knowledge == null) {
				throw new InstantiationException("Knowledge field is null!");
			}
			
			if (executor == null) {
				throw new InstantiationException("Executor field is null!");
			}
			
			planner = new Planner(plannerEndpoint, knowledge, executor);
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
	
	// The methods below are exact same methods found in the components. These are
	// implemented so the user of this class can change component parameters without mis-using the internal components.
	
	/**
	 * Initialize the executor components with the given composite service.
	 * @param compositeService the given composite service
	 */
	public void initializeExecutorEffectors(CompositeService compositeService) {
		executor.initializeEffectors(compositeService);
	}
	
	/**
	 * Connect all monitor probes with the probes of the given composite service
	 * @param compositeService the given composite service
	 */
	public void connectMonitorProbes(CompositeService compositeService) {
		monitor.connectProbes(compositeService);
	}
	
	/**
	 * Set minimum failure delta in the monitor to the given value
	 * @param minFailureDelta the new minimum failure delta
	 */
	public void setMinFailureDelta(double minFailureDelta) {
		monitor.setMinFailureDelta(minFailureDelta);
	}
	
	/**
	 * Return the minimum failure delta of the monitor
	 * @return the minimum failure delta of the monitor
	 */
	public double getMinFailureDelta() {
		return monitor.getMinFailureDelta();
	}
	
	/**
	 * Set monitor failure change to the given value
	 * @param failureChange the new monitor failure change
	 */
	public void setFailureChange(double failureChange) {
		monitor.setFailureChange(failureChange);
	}
	
	/**
	 * Reset monitor workflow probes
	 */
	public void resetMonitorProbes() {
		monitor.resetProbes();
	}
	
	/**
	 * Return the monitor failure change
	 * @return the monitor failure change
	 */
	public double getFailureChange() {
		return monitor.getFailureChange();
	}
	
	/**
	 * Set the planner rating type to the given rating type
	 * @param ratingType the given rating type
	 */
	public void setRatingType(RatingType ratingType) {
		analyzer.setRatingType(ratingType);
	}
	
	/**
	 * Return the service generation strategy number 
	 * @return the service generation strategy number
	 */
	public Integer getServiceGenerationStrategy() {
		return analyzer.getServiceGenerationStrategy();
	}
	
	/**
	 * Set the service generation strategy
	 * @param serviceGenerationStrategy the given service generation strategy
	 */
	public void setServiceGenerationStrategy(int serviceGenerationStrategy) {
		analyzer.setServiceGenerationStrategy(serviceGenerationStrategy);
	}
	
	/**
	 * Set the planner endpoint to a new given endpoint
	 * @param endpoint the new planner endpoint
	 * */
	public void setEndpoint(String endpoint) {
		planner.setEndpoint(endpoint);
	}
	
	/**
	 * Return the planner message host
	 * @return the planner message host
	 */
	public ComponentMessageHost<PlannerMessage> getMessageHost() {
		return planner.getMessageHost();
	}
	
	/**
	 * Set the planner message host to a new given message host
	 * @param messageHost the new planner message host
	 */
	public void setMessageHost(ComponentMessageHost<PlannerMessage> messageHost) {
		planner.setMessageHost(messageHost);
	}
	
	/**
	 * Set the currently used planner protocol to a given protocol and add this planner
	 * to the protocol components
	 * @param protocol the new planner protocol
	 */
	public void setProtocol(AbstractProtocol<PlannerMessage, Planner> protocol) {
		planner.setProtocol(protocol);
	}
	
	/**
	 * Return the currently used planner protocol
	 * @return the currently used planner protocol
	 */
	public AbstractProtocol<PlannerMessage, Planner> getProtocol() {
		return planner.getProtocol();
	}
	
	/**
	 * Initialize used services, usable services and their usage changes
	 * @param usableServicesAndChance map containing usable service information with additional usage chance
	 */
	public void setUsedServicesAndChances(Map<Description, Pair<List<ServiceDescription>, Double>> usableServicesAndChance) {
		knowledge.setUsedServicesAndChances(usableServicesAndChance);
	}
	
	/**
	 * Add a given service to the blacklist and update the usable services accordingly
	 * @param blacklistedService the given service to be blacklisted
	 */
	public void addServiceToBlacklist(ServiceDescription blacklistedService) {
		knowledge.addServiceToBlacklist(blacklistedService);
	}
	
	/**
	 * Remove a given service from the blacklist and update the usable services accordingly
	 * @param blacklistedService the given service to be freed
	 */
	public void removeServiceFromBlacklist(ServiceDescription freedService) {
		knowledge.removeServiceFromBlacklist(freedService);
	}
	
	/**
	 * Return whether a given service is blacklisted
	 * @param service the given service
	 * @return whether a given service is blacklisted
	 */
	public boolean isBlacklisted(ServiceDescription service) {
		return knowledge.isBlacklisted(service);
	}
	
	/**
	 * Return the used registry endpoints
	 * @return the used registry endpoints
	 */
	public List<String> getRegistryEndpoints() {
		return knowledge.getRegistryEndpoints();
	}
	
	/**
	 * Return the system goals
	 * @return the system goals
	 */
	public List<Goal> getGoals() {
		return knowledge.getGoals();
	}
	
	/**
	 * Add a given goal to the list of goals
	 * @param goal the given goal
	 */
	public void addGoal(Goal goal) {
		knowledge.addGoal(goal);
	}
	
	/**
	 * Remove a given goal from the list of goals
	 * @param goal the given goal
	 */
	public void removeGoal(Goal goal) {
		knowledge.removeGoal(goal);
	}
	
	/**
	 * Change the list of goals to a given list of goals
	 * @param goals the given list of goals
	 */
	public void changeGoals(List<Goal> goals) {
		knowledge.changeGoals(goals);
	}
	
	/**
	 * Reset the goals
	 */
	public void resetGoals() {
		knowledge.resetGoals();
	}
	
	/**
	 * Reset all approximated service failure rates to the default values
	 */
	public void resetApproximatedServiceFailureRates() {
		knowledge.resetApproximatedServiceFailureRates();
	}
	
	/**
	 * Set the current requirement type to the given type
	 * @param requirementType the given requirement type
	 */
	public void setRequirementType(SystemRequirementType requirementType) {
		knowledge.setRequirementType(requirementType);
	}
	
	// The methods below are used to simulate the mape loop for multiple workflow entities 'concurrently'
	// instead of using multiple threads. Using multiple threads is possible, but can introduce race conditions.
	
	/**
	 * Execute the monitor component
	 */
	public void executeMonitor() {
		System.err.print("Executing monitor...\n");
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
