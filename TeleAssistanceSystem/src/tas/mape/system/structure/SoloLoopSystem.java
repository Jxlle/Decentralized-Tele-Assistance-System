package tas.mape.system.structure;

import tas.data.serviceinfo.GlobalServiceInfo;
import tas.mape.system.entity.SystemEntity;

/**
 * Class representing a solo-loop system containing one system entity.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class SoloLoopSystem extends AbstractSystem<SystemEntity> {

	/**
	 * Create a new solo-loop system with a given system entity
	 * @param systemEntity the given system entity
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	public SoloLoopSystem(SystemEntity systemEntity) throws IllegalArgumentException {
		super(systemEntity);
	}

	/**
	 * Execute the solo-loop system with a given amount of execution cycles
	 * @param executionCycles the given amount of execution cycles
	 */
	@Override
	public void executeSystem(int executionCycles) {
		
		// System entity
		SystemEntity entity = getSystemEntity(0);
		
		for (int i = 0; i < 10; i++) {
			System.err.print("--------------------CYCLE START [" + (i + 1) + "]--------------------\n");
			
			// Execute MAPE-K loop
			entity.getManagingSystem().executeMonitor();
			entity.getManagingSystem().executeAnalyzer();
			entity.getManagingSystem().executePlanner();
			entity.getManagingSystem().executeExecutor();
			
			// Execute workflow
			System.err.print("Executing workflow...\n");
			entity.getManagedSystem().executeWorkflow();
			
			// Reset all service loads after each execution cycle
			GlobalServiceInfo.resetServiceLoads();
		}
		
		
		System.err.print("--------------------CYCLES ENDED--------------------\n");
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	@Override
	public int getSystemEntityCount() {
		return 1;
	}
}
