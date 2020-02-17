package tas.data.systemprofile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.communication.protocol.AbstractTwoComponentProtocol;
import tas.mape.planner.Planner;
import tas.mape.system.entity.SystemEntity;
import tas.mape.system.structure.AbstractMultiLoopSystem;
import tas.mape.system.structure.AbstractSystem;

public class SystemProfileExecutor {
	@SuppressWarnings("unchecked")
	public static void execute(List<SystemEntity> entityList) {
		
		SystemProfile profile = SystemProfileDataHandler.activeProfile;
		Class<? extends AbstractTwoComponentProtocol<PlannerMessage, Planner>> protocolClass = null;
		Constructor<?> protocolConstructor = null;
		AbstractTwoComponentProtocol<PlannerMessage, Planner> protocol = null;
		
		for (SystemEntity entity : entityList) {
			entity.getManagingSystem().setRatingType(profile.getRatingType());
		}
		
		if (profile.getAmountOfParticipatingEntities() > 1) {
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
		
		Class<? extends AbstractSystem<?>> systemClass = profile.getSystemType().getSystemClass();
		Constructor<?> systemConstructor = null;
		
		if (profile.getAmountOfParticipatingEntities() > 1) {
			
			AbstractSystem<?> system = null;
			
			try {
				systemConstructor = systemClass.getConstructor(SystemEntity.class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
			try {
				system = (AbstractSystem<?>) systemConstructor.newInstance(entityList.toArray());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
			
			system.executeSystem(profile.getExecutionCycles());
		}
		else {
			
			AbstractMultiLoopSystem<?, PlannerMessage, Planner> system = null;
			
			try {
				systemConstructor = systemClass.getConstructor(ArrayList.class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
			try {
				system = (AbstractMultiLoopSystem<?, PlannerMessage, Planner>) systemConstructor.newInstance(entityList.toArray());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
			
			
			system.executeSystem(profile.getExecutionCycles(), protocol, profile.getMaxProtocolIterations());
		}
	}
}
