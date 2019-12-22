package tas.mape.system.structure;

import java.util.ArrayList;
import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.communication.protocol.PlannerProtocolDoNothing;
import tas.mape.planner.Planner;
import tas.mape.system.entity.MAPEKComponent;
import tas.mape.system.entity.SystemEntity;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a double-loop system containing two system entities.
 */
public class DoubleLoopSystem extends AbstractMultiLoopSystem<SystemEntity, PlannerMessage, Planner> {

	/**
	 * Create a new double-loop system with two given system entities
	 * @param systemEntities the given system entities
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	public DoubleLoopSystem(SystemEntity[] systemEntities) throws IllegalArgumentException {
		super(systemEntities);
	}
	
	/**
	 * Execute the double-loop system with a given amount of execution cycles and no protocol
	 * The system will execute both entities without ever communicating.
	 * @param executionCycles the given amount of execution cycles
	 */
	@Override
	public void executeSystem(int executionCycles) {
		executeSystem(executionCycles, new PlannerProtocolDoNothing());
	}
	
	/**
	 * Execute the double-loop system with a given amount of execution cycles following a certain protocol
	 * @param executionCycles the given amount of execution cycles
	 * @param protocol the given protocol
	 */
	@Override
	public void executeSystem(int executionCycles, AbstractProtocol<PlannerMessage, Planner> protocol) {
		
		// System entities
		SystemEntity entity1 = getSystemEntity(0);
		SystemEntity entity2 = getSystemEntity(1);
		
		// Set chosen protocol
		entity1.getManagingSystem().getPlanner().setProtocol(protocol);
		entity2.getManagingSystem().getPlanner().setProtocol(protocol);
		
		// List of planners
		List<Planner> planners = new ArrayList<>();
		planners.add(entity1.getManagingSystem().getPlanner());
		planners.add(entity2.getManagingSystem().getPlanner());
		
		for (int i = 0; i < executionCycles; i++) {
			
			// Execute workflow
			for (int j = 0; j < MAPEKComponent.workflowCycles; j++) {
				entity1.getManagedSystem().executeWorkflow();
				entity2.getManagedSystem().executeWorkflow();
			}
			
			// Execute MAPE-K loop until the communication step
			entity1.getManagingSystem().executeMonitor();
			entity2.getManagingSystem().executeMonitor();
			entity1.getManagingSystem().executeAnalyzer();
			entity2.getManagingSystem().executeAnalyzer();
			entity1.getManagingSystem().executePlanner();
			entity2.getManagingSystem().executePlanner();
			
			// Execute protocol, planners communicate in this step
			protocol.executeProtocol(planners);
			
			// Execute executers
			entity1.getManagingSystem().executeExecuter();
			entity2.getManagingSystem().executeExecuter();
		}
	}

	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	@Override
	protected int getSystemEntityCount() {
		return 2;
	}
}
