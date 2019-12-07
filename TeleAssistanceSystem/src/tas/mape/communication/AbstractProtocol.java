package tas.mape.communication;

import java.util.List;

import service.auxiliary.AbstractMessage;

public abstract class AbstractProtocol {
	
	protected int messageID;
	
	public abstract void executeProtocol(List<CommunicationComponent> components); 
	
	public abstract void receiveAndHandleMessage(AbstractMessage message, CommunicationComponent receiver);
	
	public void increaseMessageID() {
		messageID++;
	}
	
	public void resetMessageID() {
		messageID = 0;
	}
}
