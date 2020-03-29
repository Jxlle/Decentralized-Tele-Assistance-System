package tas.data.systemprofile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractTwoComponentProtocol;
import tas.mape.planner.Planner;
import tas.mape.probes.ProtocolProbe;
import tas.mape.system.entity.SystemEntity;
import tas.mape.system.structure.AbstractMultiLoopSystem;
import tas.mape.system.structure.AbstractSystem;

/**
 * Class used to execute system profiles.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class SystemProfileExecutor {
	
	/**
	 * Private constructor
	 */
	private SystemProfileExecutor() {}
	
	/**
	 * Current system instance (for gui data)
	 */
	private static AbstractSystem<?> systemInstance;
	
	/**
	 * Current system protocol
	 */
	private static AbstractTwoComponentProtocol<PlannerMessage, Planner> protocol;
	
	/**
	 * Find the protocol used described in the given system profile
	 * @param profile the given system profile
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static void setProtocol(SystemProfile profile) {
		
		Class<? extends AbstractTwoComponentProtocol<PlannerMessage, Planner>> protocolClass = null;
		Constructor<?> protocolConstructor = null;
		
		// Initialize protocol in a system with more than 2 participating entities
		if (profile != null && profile.getAmountOfParticipatingEntities() > 1) {
			protocolClass = (Class<? extends AbstractTwoComponentProtocol<PlannerMessage, Planner>>) profile.getProtocolType().getProtocolClass();
			
			try {
				protocolConstructor = protocolClass.getConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
			try {
				protocol = (AbstractTwoComponentProtocol<PlannerMessage, Planner>) protocolConstructor.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Execute the current active system profile
	 * @param entityList The given list of participating entities
	 */
	@SuppressWarnings("unchecked")
	public static void execute(List<SystemEntity> entityList) {
		
		SystemProfile profile = SystemProfileDataHandler.activeProfile;
		
		// Set the chosen data for all entities
		for (SystemEntity entity : entityList) {
			entity.getManagingSystem().setRatingType(profile.getRatingType());
			entity.getManagingSystem().setRequirementType(profile.getEntityRequirementType(entity.getEntityName()));
		}
		
		Class<? extends AbstractSystem<?>> systemClass = profile.getSystemType().getSystemClass();
		Constructor<?> systemConstructor = null;	
		
		// Run system with one participating entity
		if (profile.getAmountOfParticipatingEntities() == 1) {
			
			AbstractSystem<?> system = null;
			
			// Find system constructor
			try {
				systemConstructor = systemClass.getConstructor(SystemEntity.class);
				System.err.print(systemClass + " " + systemConstructor.getDeclaringClass() + "\n");
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
			// Find system entity
			SystemEntity entity = entityList.stream().filter(x -> x.getEntityName().equals(profile.getParticipatingEntity(0))).findFirst().orElse(null);
			
			if (entity == null) {
				throw new IllegalArgumentException("The given list of entities doesn't contain the participating system entities!");
			}
			
			// Create system instance
			try {					
				system = (AbstractSystem<?>) systemConstructor.newInstance(entity);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
			
			// Set current system instance
			systemInstance = system;
			
			// Execute system
			system.executeSystem(profile.getExecutionCycles());
		}
		// Run system with multiple participating entities
		else {
			AbstractMultiLoopSystem<?, PlannerMessage, Planner> system = null;
			
			// Find system constructor
			try {
				systemConstructor = systemClass.getConstructor(SystemEntity[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
			// Find system entities
			SystemEntity[] entities = new SystemEntity[profile.getAmountOfParticipatingEntities()];
			
			for (int i = 0; i < profile.getAmountOfParticipatingEntities(); i++) {
				int i2 = i;
				SystemEntity entity = entityList.stream().filter(x -> x.getEntityName().equals(profile.getParticipatingEntity(i2))).findFirst().orElse(null);
				
				if (entity == null) {
					throw new IllegalArgumentException("The given list of entities doesn't contain the participating system entities!");
				}
			
				entities[i] = entity;
			}
			
			// Create system instance
			try {
				system = (AbstractMultiLoopSystem<?, PlannerMessage, Planner>) systemConstructor.newInstance(new Object[] {entities});
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
			
			// Set current system instance
			systemInstance = system;
			
			// Execute system
			system.executeSystem(profile.getExecutionCycles(), protocol, profile.getMaxProtocolIterations(), profile.getMessageContentPercentage());
		}
	}
	
	/**
	 * Return the current system execution cycle
	 * @return the current system execution cycle
	 */
	public static int getCurrentExecutionCycle() {
		return systemInstance.getCurrentExecutionCycle();
	}
	
	/**
	 * Return the current workflow cycle
	 * @return the current workflow cycle
	 */
	public static int getCurrentWorkflowCycle() {
		return systemInstance.getTotalFinishedWorkflowCycles();
	}
	
	/**
	 * Subscribe the given protocol probe to the current system protocol
	 * @param probe the given protocol probe
	 */
	public static void subscribeToCurrentProtocol(ProtocolProbe probe) {
		System.err.println("SUBSCRIBE? " + protocol);
		if (protocol != null) {
			System.err.println("SUBSCRIBED");
			protocol.getObserver().register(probe);
		}
	}
	
	/**
	 * Unsubscribe the given protocol probe from the current system protocol
	 * @param probe the given protocol probe
	 */
	public static void unsubscribeFromCurrentProtocol(ProtocolProbe probe) {
		if (protocol != null) {
			protocol.getObserver().unRegister(probe);
		}
	}
	
	// Stop the current system run execution
	public static void stopSystemExecution() {
		systemInstance.stop();
	}
}
