package tas.services.alarm;

import service.atomic.AtomicService;
import service.auxiliary.ServiceOperation;

public class AlarmService extends AtomicService {

	public AlarmService(String serviceName, String serviceEndpoint) {
		super(serviceName, serviceEndpoint);
	}
	
	@ServiceOperation
	public void triggerAutomaticAlarm(int patientId){
		//System.out.println(this.getServiceDescription().getServiceName() + " is triggered!");
	}
	
	@ServiceOperation
	public void triggerManualAlarm(int patientId){
		//System.out.println(this.getServiceDescription().getServiceName() + " is triggered!");
	}
}
