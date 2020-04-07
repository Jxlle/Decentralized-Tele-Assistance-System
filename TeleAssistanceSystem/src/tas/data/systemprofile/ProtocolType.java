package tas.data.systemprofile;

import tas.mape.communication.protocol.AbstractProtocol;

import java.util.ArrayList;
import java.util.List;

import tas.mape.communication.protocol.*;

/**
 * Enum representing the different protocol types
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public enum ProtocolType {

	// Available protocol types
	DO_NOTHING_PROTOCOL(TwoPlannerProtocolDoNothing.class, SystemType.DOUBLE_ENTITY),
	STANDARD_PROTOCOL_2ENTITIES(TwoPlannerProtocolStandard.class, SystemType.DOUBLE_ENTITY),
	STANDARD_PROTOCOL_3ENTITIES(ThreePlannerProtocolStandard.class, SystemType.TRIPLE_ENTITY);
	
	private Class<? extends AbstractProtocol<?, ?>> protocolClass;
	private SystemType systemType;
	
	/**
	 * Create a new protocol type with a given protocol class and system type
	 * @param protocolClass the given class representing the class that is used to create the protocol object
	 * @param systemType the type of the system
	 */
	private ProtocolType(Class<? extends AbstractProtocol<?, ?>> protocolClass, SystemType systemType) {
		this.protocolClass = protocolClass;
		this.systemType = systemType;
	}
	
	/**
	 * Find the protocol types compatible for the given system type 
	 * @param systemType the given system type
	 * @return the compatible protocol types
	 */
	public static List<ProtocolType> findProtocolTypes(SystemType systemType) {
		
		List<ProtocolType> protocolTypes = new ArrayList<>();
		
		for (ProtocolType type : ProtocolType.values()) {
			if (type.getSystemType().equals(systemType)) {
				protocolTypes.add(type);
			}
		}
		
		return protocolTypes;
	}
	
	/**
	 * Return the protocol class
	 * @return the protocol class
	 */
	public Class<? extends AbstractProtocol<?, ?>> getProtocolClass() {
		return protocolClass;
	}
	
	/**
	 * Return the protocol compatible system type
	 * @return the protocol compatible system type
	 */
	public SystemType getSystemType() {
		return systemType;
	}
}
