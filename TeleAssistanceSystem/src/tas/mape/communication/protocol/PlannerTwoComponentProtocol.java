package tas.mape.communication.protocol;

import java.util.List;

import tas.mape.communication.message.PlannerMessage;
import tas.mape.planner.Planner;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing the structure of a two-component planner protocol.
 */
public abstract class PlannerTwoComponentProtocol extends AbstractTwoComponentProtocol<PlannerMessage, Planner> {

	
	
	@Override
	protected void InitializeAndSendFirstMessage(List<Planner> components, int startIndex, int... receiverIndices) {
		
		// TODO store service registry information
		
		// TODO generate first planner message and send it to the receiver 
		//PlannerMessage firstMessage = new PlannerMessage(messageID, );
		//messageID++;
	}
}
