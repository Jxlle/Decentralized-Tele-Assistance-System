package tas.mape.probes;

import java.util.ArrayList;
import java.util.List;

import tas.data.systemprofile.SystemProfileExecutor;
import tas.mape.communication.message.ProtocolMessageInformation;

public class ProtocolProbe implements ProtocolProbeInterface {

	private List<List<ProtocolMessageInformation>> protocolMessages = new ArrayList<>();
	
	public void connect() {
		SystemProfileExecutor.subscribeToCurrentProtocol(this);
	}
	
	public void reset() {	
		SystemProfileExecutor.unsubscribeFromCurrentProtocol(this);
		protocolMessages.clear();
	}
	
	public List<ProtocolMessageInformation> getProtocolMessages(int cycle) {
		return protocolMessages.get(cycle);
	}
	
	@Override
	public void protocolStarted() {
		protocolMessages.add(new ArrayList<>());
	}

	@Override
	public void protocolMessageSent(ProtocolMessageInformation protocolMessageInformation) {
		List<ProtocolMessageInformation> currentCycleMessages = protocolMessages.get(protocolMessages.size() - 1);
		currentCycleMessages.add(protocolMessageInformation);
	}
	
	@Override
	public void protocolEnded() {
		// Not used	
	}
}