package tas.mape.system.entity;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class representing a system entity consisting of a workflow executer (managed system) 
 * and a MAPE-K component (managing system).
 */
public class SystemEntity extends AbstractSystemEntity<WorkflowExecuter, MAPEKComponent> {
	
	/**
	 * Create a new system entity with a given managed and managing system and link them together
	 * @param managedSystem the given workflow executer
	 * @param managingSystem the given MAPE-K component
	 */
	public SystemEntity(WorkflowExecuter managedSystem, MAPEKComponent managingSystem) {
		super(managedSystem, managingSystem);
	}

	/**
	 * Link the workflow executer and the MAPE-K component by
	 * connecting the MAPE-K probes and effectors
	 */
	@Override
	protected void LinkSystems() {
		getManagingSystem().initializeExecuterEffectors(getManagedSystem().getAssistanceService());
		getManagingSystem().connectMonitorProbes(getManagedSystem().getAssistanceService());
	}
}
