package tas.mape.system.structure;

import tas.mape.system.entity.SystemEntity;
import tas.mape.system.entity.WorkflowExecutor;

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
	public SoloLoopSystem(SystemEntity... systemEntity) throws IllegalArgumentException {
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
		
		for (int i = 0; i < executionCycles; i++) {
			
			// Execute workflow
			entity.getManagedSystem().executeWorkflow();
			
			// Execute MAPE-K loop
			entity.getManagingSystem().executeMonitor();
			entity.getManagingSystem().executeAnalyzer();
			entity.getManagingSystem().executePlanner();
			entity.getManagingSystem().executeExecutor();
		}
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	public static int getSystemEntityCount() {
		return 1;
	}
}
