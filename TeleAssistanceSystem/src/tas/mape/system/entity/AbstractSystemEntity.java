package tas.mape.system.entity;

/**
 * Abstract class representing a system entity consisting of a managed system and a managing system.
 * The managed system manages the managing system.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 *
 * @param <T> the managed system type
 * @param <E> the managing system type
 */
public abstract class AbstractSystemEntity<T, E> {

	// The unique system entity name
	protected String entityName;
	
	// The managed system
	protected final T managedSystem;
	
	// The managing system
	protected final E managingSystem;
	
	/**
	 * Create a new abstract system entity with a given name, managed system and managing system
	 * @param entityName the given system entity name
	 * @param managedSystem the given managed system
	 * @param managingSystem the given managing system
	 */
	protected AbstractSystemEntity(String entityName, T managedSystem, E managingSystem) {
		
		this.entityName = entityName;
		this.managedSystem = managedSystem;
		this.managingSystem = managingSystem;
		LinkSystems();
	}
	
	/**
	 * Return the system entity name
	 * @return te system entity name
	 */
	public String getEntityName() {
		return entityName;
	}
	
	/**
	 * Set the system entity name to the given name
	 * @param entityName the given name
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
	/**
	 * Return the managed system 
	 * @return the managed system
	 */
	public T getManagedSystem() {
		return managedSystem;
	}
	
	/**
	 * Return the managing system
	 * @return the managing system
	 */
	public E getManagingSystem() {
		return managingSystem;
	}
	
	/**
	 * Link the managed and managing system so the managing system
	 * can manage the managed system.
	 */
	protected abstract void LinkSystems();	
}
