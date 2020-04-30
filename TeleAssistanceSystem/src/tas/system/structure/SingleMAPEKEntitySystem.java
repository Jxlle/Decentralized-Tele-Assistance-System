package tas.system.structure;

import tas.data.serviceinfo.GlobalServiceInfo;
import tas.system.entity.MAPEKSystemEntity;

/**
 * Class representing a single MAPEK entity system containing one system entity.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class SingleMAPEKEntitySystem extends AbstractSingleEntitySystem<MAPEKSystemEntity> {

	/**
	 * Create a new solo-loop system with a given system entity
	 * @param systemEntity the given system entity
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	public SingleMAPEKEntitySystem(MAPEKSystemEntity systemEntity) throws IllegalArgumentException {
		super(new MAPEKSystemEntity[] {systemEntity});
	}

	/**
	 * Execute the solo-loop system with a given amount of execution cycles
	 * @param executionCycles the given amount of execution cycles
	 */
	@Override
	public void executeSystem(int executionCycles) {
		
		System.err.println("----------------------------------------------------------------------------------");
		
		// System entity
		MAPEKSystemEntity entity = getSystemEntity(0);
		
		for (int i = 0; i < executionCycles; i++) {
			
			// Update current execution cycle
			currentExecutionCycle = i;
			
			// Execute MAPE-K loop
			entity.getManagingSystem().executeMonitor();
			entity.getManagingSystem().executeAnalyzer();
			entity.getManagingSystem().executePlanner();
			entity.getManagingSystem().executeExecutor();
			
			// notify the probe that adaptation is finished
			entity.getManagingSystem().getPlanner().getObserver().adaptationFinished();
			
			// Execute workflow
			entity.getManagedSystem().executeWorkflow();
			
			// Reset all service loads after each execution cycle
			GlobalServiceInfo.resetServiceLoads();
			
			// Stop execution if forced
			if (isStopped) {
				break;
			}
		}
		
		// Reset approximated failure rates table after the run
		entity.getManagingSystem().getKnowledge().resetApproximatedServiceFailureRates();
		entity.getManagingSystem().getKnowledge().resetSystemCycle();
		
		// Reset current execution cycle
		currentExecutionCycle = 0;
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	@Override
	public int getTotalFinishedWorkflowCycles() {
		return getSystemEntity(0).getManagedSystem().getCurrentSteps();
	}

	@Override
	public void stopEntities() {	
		getSystemEntity(0).getManagedSystem().stop();
	}
}
