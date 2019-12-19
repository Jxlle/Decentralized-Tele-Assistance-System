package tas.mape.communication.protocol;

import java.util.List;

import service.auxiliary.AbstractMessage;
import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.PlannerMessage;
import tas.mape.planner.Planner;

public class PlannerProtocolTest extends AbstractTwoComponentProtocol {

	// Fields
	int maxTime;
	
	public PlannerProtocolTest(int maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public void executeProtocol(List<CommunicationComponent> components) throws IllegalArgumentException {
		
		if (components.size() != 2) {
			throw new IllegalArgumentException("The used protocol doesn't support this amount of components!");
		}
		
		int senderIndex = (int) (Math.random() * (2 + 1));		
		int receiverIndex = Math.abs(senderIndex - 1);
		
		// TODO generate and send starting message
		//PlannerMessage startMessage = new PlannerMessage(messageID, components.get(receiverIndex).getEndpoint(), components.get(senderIndex).getEndpoint(), );
		//startComponent.sendMessage(startMessage);
	}

	@Override
	public void receiveAndHandleMessage(AbstractMessage message, CommunicationComponent receiver) {
		
		increaseMessageID();
		PlannerMessage plannerMessage = (PlannerMessage) message;
		Planner planner = (Planner) receiver;
		
		String messageType = plannerMessage.getType();
		
		switch (messageType) {
			// TODO make test protocol, handle message and reply here.
		}
	}

	@Override
	protected void sendFirstMessage(List<CommunicationComponent> components, int startIndex, int... receiverIndices) {
		// TODO Auto-generated method stub
		
	}

}
