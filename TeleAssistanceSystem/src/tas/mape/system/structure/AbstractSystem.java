package tas.mape.system.structure;

import tas.mape.system.entity.AbstractSystemEntity;

/**
 * Abstract class representing a system containing system entities.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 *
 * @param <T> the system entity type
 */
public abstract class AbstractSystem<T extends AbstractSystemEntity<?, ?>> {
	
	// Array containing the entities of this system
	private T[] systemEntities;
	
	// Variable representing the current execution cycle
	protected int currentExecutionCycle;
	
	/**
	 * Create a new abstract system with given system entities
	 * @param systemEntities the given system entities
	 * @throws IllegalArgumentException throw when the given
	 *         amount of entities is not supported by the system
	 */
	protected AbstractSystem(T[] systemEntities) throws IllegalArgumentException {
		
		if (systemEntities.length > getSystemEntityCount()) {
			throw new IllegalArgumentException("This system doesn't support " + systemEntities.length + " entities! "
					+ "It supports " + getSystemEntityCount() + " entities.");
		}
		
		this.systemEntities = systemEntities;
	}
	
	/**
	 * Return the current execution cycle
	 * @return the current execution cycle
	 */
	public int getCurrentExecutionCycle() {
		return currentExecutionCycle;
	}
	
	/**
	 * Return the system entities
	 * @return the system entities
	 */
	public T[] getSystemEntities() {
		return systemEntities;
	}
	
	/**
	 * Return the system entity at the given index in the entity array
	 * @param index the given entity index
	 * @return the system entity at the given index
	 */
	public T getSystemEntity(int index) {
		return systemEntities[index];
	}
	
	/**
	 * Execute the system with a given amount of execution cycles
	 * @param executionCycles the given amount of execution cycles
	 */
	public abstract void executeSystem(int executionCycles);
	
	/**
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	public int getSystemEntityCount() {
		return -1;
	};
}
