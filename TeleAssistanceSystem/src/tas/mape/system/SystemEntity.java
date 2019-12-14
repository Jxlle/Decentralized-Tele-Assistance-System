package tas.mape.system;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a workflow entity consisting of a workflow executer (managed system) and a MAPE-K component (managing system).
 */
public class SystemEntity {
	
	WorkflowExecuter managedSystem;
	MAPEKComponent managingSystem;
	
	/**
	 * Create a new system entity with a given managed and managing system and link them together
	 * @param managedSystem the given workflow executer
	 * @param managingSystem the given MAPE-K component
	 */
	public SystemEntity(WorkflowExecuter managedSystem, MAPEKComponent managingSystem) {
		
		this.managedSystem = managedSystem;
		this.managingSystem = managingSystem;
		
		managingSystem.initializeExecuterEffectors(managedSystem.getCompositeService());
		managingSystem.connectMonitorProbes(managedSystem.getCompositeService());
	}
}
