package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.planner.Planner;

/**
 * Test class, to be deleted....
 */
public class PlannerProtocolTest extends PlannerTwoComponentProtocol {

	// Max response time
	int maxTime;
	
	public PlannerProtocolTest(int maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public void receiveAndHandleMessage(PlannerMessage message, Planner receiver) {
		
		/*PlannerMessage plannerMessage = (PlannerMessage) message;
		Planner planner = (Planner) receiver;
		
		String messageType = plannerMessage.getType();
		
		switch (messageType) {
			// TODO make test protocol, handle message and reply here.
		}*/
	}

	@Override
	protected void InitializeAndSendFirstMessage(List<Planner> components, int startIndex, int... receiverIndices) {
		
		
		// TODO Auto-generated method stub
		
	}
}
