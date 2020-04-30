package tas.system.structure;

import tas.system.entity.AbstractSystemEntity;

/**
 * Abstract class representing a single entity system containing one system entity.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public abstract class AbstractSingleEntitySystem<T extends AbstractSystemEntity<?, ?>> extends AbstractSystem<T> {

	/**
	 * Create a new abstract single entity system with given system entities
	 * @param systemEntities the given system entities
	 * @param systemEntityCount the amount of entities that the system supports
	 * @throws IllegalArgumentException throw when the given
	 *         amount of entities is not supported by the system
	 */
	protected AbstractSingleEntitySystem(T[] systemEntities) throws IllegalArgumentException {
		super(1, systemEntities);
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
	@Override
	public int getSystemEntityCount() {
		return 1;
	}
}
