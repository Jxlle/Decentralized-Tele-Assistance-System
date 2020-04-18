package tas.mape.system.entity;

/**
 * Class representing a system entity consisting of a workflow executer (managed system) 
 * and a MAPE-K component (managing system).
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class MAPEKSystemEntity extends AbstractSystemEntity<LocalServiceSystem, MAPEKFeedbackLoop> {
	
	/**
	 * Create a new system entity with a given managed and managing system and link them together
	 * @param entityName the given system entity name
	 * @param managedSystem the given workflow executer
	 * @param managingSystem the given MAPE-K component
	 */
	public MAPEKSystemEntity(String entityName, LocalServiceSystem managedSystem, MAPEKFeedbackLoop managingSystem) {
		super(entityName, managedSystem, managingSystem);
		this.managingSystem.getKnowledge().setParentEntityName(entityName);
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
		getManagingSystem().getExecutor().initializeEffectors(getManagedSystem().getAssistanceService());
		getManagingSystem().getMonitor().connectProbes(getManagedSystem().getAssistanceService());
	}
}
