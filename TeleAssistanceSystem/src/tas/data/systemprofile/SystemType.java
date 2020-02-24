package tas.data.systemprofile;

import tas.mape.system.structure.AbstractSystem;
import tas.mape.system.structure.DoubleLoopSystem;
import tas.mape.system.structure.SoloLoopSystem;

/**
 * Enum representing the different system types
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public enum SystemType {
	
	// Available system types
	SOLO_LOOP(SoloLoopSystem.class, 1),
	DOUBLE_LOOP(DoubleLoopSystem.class, 2);
	
	private Class<? extends AbstractSystem<?>> systemClass;
	private int maxEntities;
	
	/**
	 * Create a new system type with a given system class
	 * @param systemClass the given class representing the class that is used to create the system object
	 * @param maxEntities The maximum amount of participating system entities
	 */
	private SystemType(Class<? extends AbstractSystem<?>> systemClass, int maxEntities) {
		this.systemClass = systemClass;
		this.maxEntities = maxEntities;
	}
	
	/**
	 * Return the system class
	 * @return the system class
	 */
	public Class<? extends AbstractSystem<?>> getSystemClass() {
		return systemClass;
	}
	
	/**
	 * Return the maximum amount of participating system entities
	 * @return
	 */
	public int getMaxEntities() {
		return maxEntities;
	}
}
