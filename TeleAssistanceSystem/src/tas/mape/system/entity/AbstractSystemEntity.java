package tas.mape.system.entity;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing a system entity consisting of a managed system and a managing system.
 * The managed system manages the managing system.
 * @param <T> the managed system type
 * @param <E> the managing system type
 */
public abstract class AbstractSystemEntity<T, E> {

	// The managed system
	private T managedSystem;
	
	// The managing system
	private E managingSystem;
	
	/**
	 * Create a new abstract system entity
	 * @param managedSystem
	 * @param managingSystem
	 */
	protected AbstractSystemEntity(T managedSystem, E managingSystem) {
		
		this.managedSystem = managedSystem;
		this.managingSystem = managingSystem;
		LinkSystems();
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
