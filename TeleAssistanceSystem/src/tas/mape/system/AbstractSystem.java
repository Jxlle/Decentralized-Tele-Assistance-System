package tas.mape.system;

import tas.mape.system.entity.AbstractSystemEntity;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing a system containing system entities.
 * @param <T> the system entity type
 */
public abstract class AbstractSystem<T extends AbstractSystemEntity<?, ?>> {
	
	// Array containing the entities of this system
	private T[] systemEntities;
	
	/**
	 * Create a new abstract system with given system entities
	 * @param systemEntities the given system entities
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	@SafeVarargs
	protected AbstractSystem(T... systemEntities) throws IllegalArgumentException {
		
		if (systemEntities.length > getSystemEntityCount()) {
			throw new IllegalArgumentException("This system doesn't support " + systemEntities.length + " entities!");
		}
		
		this.systemEntities = systemEntities;
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
	protected abstract int getSystemEntityCount();
}
