package tas.mape.system.entity;

/**
 * Class representing a system entity consisting of a workflow executer (managed system) 
 * and a MAPE-K component (managing system).
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class SystemEntity extends AbstractSystemEntity<WorkflowExecutor, MAPEKComponent> {
	
	/**
	 * Create a new system entity with a given managed and managing system and link them together
	 * @param entityName the given system entity name
	 * @param managedSystem the given workflow executer
	 * @param managingSystem the given MAPE-K component
	 */
	public SystemEntity(String entityName, WorkflowExecutor managedSystem, MAPEKComponent managingSystem) {
		super(entityName, managedSystem, managingSystem);
		getManagingSystem().setSystemEntity(this);
	}
	
	/**
	 * Set the system entity name to the given name
	 * @param entityName the given name
	 */
	@Override
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Link the workflow executer and the MAPE-K component by
	 * connecting the MAPE-K probes and effectors
	 */
	@Override
	protected void LinkSystems() {
		getManagingSystem().initializeExecutorEffectors(getManagedSystem().getAssistanceService());
		getManagingSystem().connectMonitorProbes(getManagedSystem().getAssistanceService());
	}
}
