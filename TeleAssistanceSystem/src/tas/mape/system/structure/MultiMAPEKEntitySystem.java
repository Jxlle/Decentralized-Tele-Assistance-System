package tas.mape.system.structure;

import tas.data.serviceinfo.GlobalServiceInfo;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.planner.Planner;
import tas.mape.system.entity.MAPEKSystemEntity;

public class MultiMAPEKEntitySystem extends AbstractMultiEntitySystem<MAPEKSystemEntity, PlannerMessage, Planner> {

	/**
	 * Create a new multi entity MAPEK system with the given system entities
	 * @param systemEntities the given system entities
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	public MultiMAPEKEntitySystem(int systemEntityCount, MAPEKSystemEntity[] systemEntities) throws IllegalArgumentException {
		super(systemEntityCount, systemEntities);
	}
	
	/**
	 * Execute the system with a given amount of execution cycles following a certain protocol
	 * @param executionCycles the given amount of execution cycles
	 * @param protocol the given protocol
	 * @param maxIterations the given maximum amount of iterations
	 * @param messageContentPercentage percentage indicating how much of the maximum amount of information the protocol uses in its messages
	 */
	@Override
	public void executeSystem(int executionCycles, AbstractProtocol<PlannerMessage, Planner> protocol, int maxIterations, int messageContentPercentage) {
		
		// Set system protocol
		for (MAPEKSystemEntity entity : getSystemEntities()) {
			entity.getManagingSystem().setProtocol(protocol);
		}
		
		for (int i = 0; i < executionCycles; i++) {
			
			// Update current execution cycle
			currentExecutionCycle = i;
			
			// Execute MAPE-K loop for each entity until the communication step
			for (MAPEKSystemEntity entity : getSystemEntities()) {
				entity.getManagingSystem().executeMonitor();
				entity.getManagingSystem().executeAnalyzer();
				entity.getManagingSystem().executePlanner();
			}
			
			// Execute protocol, planners communicate in this step
			protocol.executeProtocol(maxIterations, messageContentPercentage);
			
			// Execute executers
			for (MAPEKSystemEntity entity : getSystemEntities()) {
				entity.getManagingSystem().executeExecutor();
			}
			
			// notify the probe that adaptation is finished (can be any entity, they contain the same probe)
			getSystemEntity(0).getManagingSystem().getProbe().adaptationFinished();
			
			// Execute workflow
			for (MAPEKSystemEntity entity : getSystemEntities()) {
				entity.getManagedSystem().executeWorkflow();
			}
			
			// Reset all service loads after each execution cycle
			GlobalServiceInfo.resetServiceLoads();
			
			// Stop execution if forced
			if (isStopped) {
				break;
			}
		}
		
		// Reset system protocol, approximated failure rates table and system cycles
		for (MAPEKSystemEntity entity : getSystemEntities()) {
			entity.getManagingSystem().setProtocol(null);
			entity.getManagingSystem().resetApproximatedServiceFailureRates();
			entity.getManagingSystem().resetSystemCycle();
		}
		
		// Reset current execution cycle
		currentExecutionCycle = 0;
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	@Override
	public int getTotalFinishedWorkflowCycles() {
		
		int currentSteps = 0;
		
		for (MAPEKSystemEntity entity : getSystemEntities()) {		
			if (entity.getManagedSystem().getCurrentSteps() > 0) {
				currentSteps += entity.getManagedSystem().getCurrentSteps();
				break;
			}
			
			currentSteps += SystemProfileDataHandler.activeProfile.getWorkflowCycles();
		}
		
		return currentSteps;
	}

	@Override
	public void stopEntities() {
		for (MAPEKSystemEntity entity : getSystemEntities()) {
			entity.getManagedSystem().stop();
		}
	}
}
