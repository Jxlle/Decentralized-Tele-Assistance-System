package tas.data.systemprofile;

import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.communication.protocol.PlannerProtocolDoNothing;
import tas.mape.communication.protocol.PlannerTwoProtocolStandard;

/**
 * Enum representing the different protocol types
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public enum ProtocolType {

	// Available protocol types
	DO_NOTHING_PROTOCOL(PlannerProtocolDoNothing.class),
	STANDARD_PROTOCOL(PlannerTwoProtocolStandard.class);
	
	private Class<? extends AbstractProtocol<?, ?>> protocolClass;
	
	/**
	 * Create a new protocol type with a given protocol class
	 * @param protocolClass the given class representing the class that is used to create the protocol object
	 */
	private ProtocolType(Class<? extends AbstractProtocol<?, ?>> protocolClass) {
		this.protocolClass = protocolClass;
	}
	
	/**
	 * Return the protocol class
	 * @return the protocol class
	 */
	public Class<? extends AbstractProtocol<?, ?>> getProtocolClass() {
		return protocolClass;
	}
}
