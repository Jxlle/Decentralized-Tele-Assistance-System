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
	
	// Variable representing the amount of entities that the system supports
	protected int systemEntityCount;
	
	// Boolean indicating that execution has been stopped
	protected boolean isStopped;
	
	/**
	 * Create a new abstract system with given system entities
	 * @param systemEntities the given system entities
	 * @param systemEntityCount the amount of entities that the system supports
	 * @throws IllegalArgumentException throw when the given
	 *         amount of entities is not supported by the system
	 */
	protected AbstractSystem(int systemEntityCount, T[] systemEntities) throws IllegalArgumentException {
		
		if (systemEntities.length > systemEntityCount) {
			throw new IllegalArgumentException("This system doesn't support " + systemEntities.length + " entities! "
					+ "It supports " + systemEntityCount + " entities.");
		}
		
		this.systemEntityCount = systemEntityCount;
		this.systemEntities = systemEntities;
	}
	
	/**
	 * Stop execution
	 */
	public void stop() {
		isStopped = true;
		stopEntities();
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
	 * Return the amount of needed entities in the system
	 * @return the amount of needed entities in the system
	 */
	public int getSystemEntityCount() {
		return systemEntityCount;
	};
	
	/**
	 * Return the current amount of finished workflow cycles
	 * @return the current amount of finished workflow cycles
	 */
	public abstract int getTotalFinishedWorkflowCycles();
	
	/**
	 * Stop all entity execution
	 */
	public abstract void stopEntities();
}
