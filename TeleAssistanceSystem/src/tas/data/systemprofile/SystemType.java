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
	SOLO_LOOP(SoloLoopSystem.class),
	DOUBLE_LOOP(DoubleLoopSystem.class);
	
	private Class<? extends AbstractSystem<?>> systemClass;
	
	/**
	 * Create a new system type with a given system class
	 * @param systemClass the given class representing the class that is used to create the system object
	 */
	private SystemType(Class<? extends AbstractSystem<?>> systemClass) {
		this.systemClass = systemClass;
	}
	
	/**
	 * Return the system class
	 * @return the system class
	 */
	public Class<? extends AbstractSystem<?>> getSystemClass() {
		return systemClass;
	}
}
