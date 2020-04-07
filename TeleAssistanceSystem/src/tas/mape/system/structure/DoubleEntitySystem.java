package tas.mape.system.structure;

import tas.data.serviceinfo.GlobalServiceInfo;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.communication.protocol.PlannerProtocolDoNothing;
import tas.mape.planner.Planner;
import tas.mape.system.entity.SystemEntity;

/**
 * Class representing a double-loop system containing two system entities.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class DoubleEntitySystem extends AbstractMultiEntitySystem<SystemEntity, PlannerMessage, Planner> {

	/**
	 * Create a new double-loop system with two given system entities
	 * @param systemEntities the given system entities
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	public DoubleEntitySystem(SystemEntity[] systemEntities) throws IllegalArgumentException {
		super(systemEntities);
	}
	
	/**
	 * Execute the double-loop system with a given amount of execution cycles and no protocol
	 * The system will execute both entities without ever communicating.
	 * @param executionCycles the given amount of execution cycles
	 */
	@Override
	public void executeSystem(int executionCycles) {
		executeSystem(executionCycles, new PlannerProtocolDoNothing(), 0, 100);
	}
	
	/**
	 * Execute the double-loop system with a given amount of execution cycles following a certain protocol
	 * @param executionCycles the given amount of execution cycles
	 * @param protocol the given protocol
	 * @param maxIterations the given maximum amount of iterations
	 * @param messageContentPercentage percentage indicating how much of the maximum amount of information the protocol uses in its messages
	 */
	@Override
	public void executeSystem(int executionCycles, AbstractProtocol<PlannerMessage, Planner> protocol, int maxIterations, int messageContentPercentage) {
				
		// System entities
		SystemEntity entity1 = getSystemEntity(0);
		SystemEntity entity2 = getSystemEntity(1);
		
		// Set system protocol
		entity1.getManagingSystem().setProtocol(protocol);
		entity2.getManagingSystem().setProtocol(protocol);
		
		for (int i = 0; i < executionCycles; i++) {
			
			// Update current execution cycle
			currentExecutionCycle = i;
			
			System.out.println("EXECUTION CYCLE " + (i + 1) + " STARTED.-------------------------------------------");
			
			// Execute MAPE-K loop until the communication step
			entity1.getManagingSystem().executeMonitor();
			entity2.getManagingSystem().executeMonitor();
			entity1.getManagingSystem().executeAnalyzer();
			entity2.getManagingSystem().executeAnalyzer();
			entity1.getManagingSystem().executePlanner();
			entity2.getManagingSystem().executePlanner();
			
			// Execute protocol, planners communicate in this step
			protocol.executeProtocol(maxIterations, messageContentPercentage);
			
			// Execute executers
			entity1.getManagingSystem().executeExecutor();
			entity2.getManagingSystem().executeExecutor();
			
			// Execute workflow
			System.err.println("executing workflow 1");
			entity1.getManagedSystem().executeWorkflow();
			System.err.println("executing workflow 2");
			entity2.getManagedSystem().executeWorkflow();
			
			// notify the probe that the system run is finished (can be any entity, they contain the same probe)
			entity1.getManagingSystem().getProbe().systemRunFinished();
			
			// Reset all service loads after each execution cycle
			GlobalServiceInfo.resetServiceLoads();
			
			// Stop execution if forced
			if (isStopped) {
				entity1.getManagedSystem().stop();
				entity2.getManagedSystem().stop();
				break;
			}
		}
		
		// Reset system protocol
		entity1.getManagingSystem().setProtocol(null);
		entity2.getManagingSystem().setProtocol(null);
		
		// Reset approximated failure rates table after the run
		entity1.getManagingSystem().resetApproximatedServiceFailureRates();
		entity2.getManagingSystem().resetApproximatedServiceFailureRates();
		
		// Reset system cycles
		entity1.getManagingSystem().resetSystemCycle();
		entity2.getManagingSystem().resetSystemCycle();
		
		// Reset current execution cycle
		currentExecutionCycle = 0;
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	@Override
	public int getSystemEntityCount() {
		return 2;
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	@Override
	public int getTotalFinishedWorkflowCycles() {
		
		// System entities
		SystemEntity entity1 = getSystemEntity(0);
		SystemEntity entity2 = getSystemEntity(1);
		
		if (entity1.getManagedSystem().getCurrentSteps() > 0) {
			return entity1.getManagedSystem().getCurrentSteps();
		}

		return entity2.getManagedSystem().getCurrentSteps() + SystemProfileDataHandler.activeProfile.getWorkflowCycles();
	}
}
