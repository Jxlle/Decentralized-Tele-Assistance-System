package tas.mape.probes;

import tas.communication.message.ProtocolMessageInformation;

/**
 * Interface used for probes that collect protocol message data
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public interface ProtocolProbeInterface {
	
	/**
	 * Probe the start of the protocol
	 */
	public void protocolStarted();
	
	/**
	 * Probe the end of the protocol
	 */
	public void protocolEnded();
	
	/**
	 * Probe the choice of a specific protocol message
	 * @param protocolMessageInformation given information about the sent protocol message
	 */
	public void protocolMessageSent(ProtocolMessageInformation protocolMessageInformation);
}
