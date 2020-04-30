package tas.mape.probes;

import service.adaptation.probes.AbstractProbe;
import tas.communication.message.ProtocolMessageInformation;

/**
 * Class that monitors sent protocol messages during the communication step inside the planner
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class ProtocolObserver extends AbstractProbe<ProtocolProbeInterface> {
	
	/**
	 * Notify subscribed probes that the protocol has started
	 */
	public void protocolStarted() {
    	for (ProtocolProbeInterface plannerProbeInterface : subscribers) {
    		plannerProbeInterface.protocolStarted();
    	}
	}
	
	/**
	 * Notify subscribed probes that the protocol has ended
	 */
	public void protocolEnded() {
    	for (ProtocolProbeInterface plannerProbeInterface : subscribers) {
    		plannerProbeInterface.protocolEnded();
    	}
	}
	
	/**
	 * Notify subscribed probes that a protocol message had been sent
	 * @param protocolMessageInformation given information about the sent protocol message
	 */
	public void protocolMessageSent(ProtocolMessageInformation protocolMessageInformation) {
    	for (ProtocolProbeInterface plannerProbeInterface : subscribers) {
    		plannerProbeInterface.protocolMessageSent(protocolMessageInformation);
    	}
	}
}
