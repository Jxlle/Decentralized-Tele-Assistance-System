package tas.mape;

import java.util.List;

public class PlannerProtocolTest extends AbstractProtocol {

	int maxTime;
	
	public PlannerProtocolTest(int maxTime) {
		this.maxTime = maxTime;
	}
	
	@Override
	public void startProtocol(CommunicationComponent startComponent) {
		
		//PlannerMessage message = new PlannerMessage();
		//startComponent.sendMessage(message);
	}

	@Override
	public void setResult(CommunicationComponent component) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeProtocol(List<CommunicationComponent> components) {
		// TODO Auto-generated method stub
		
	}

}
