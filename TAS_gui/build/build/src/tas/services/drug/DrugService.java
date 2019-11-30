package tas.services.drug;

import service.atomic.AtomicService;
import service.auxiliary.ServiceOperation;

public class DrugService extends AtomicService {

	public DrugService(String serviceName, String serviceEndpoint) {
		super(serviceName, serviceEndpoint);
	}

	@ServiceOperation()
	public void changeDoses(int patientId){
		System.out.println("Doses are changed.");
	}
	
	@ServiceOperation()
	public void changeDrug(int patientId){
		System.out.println("Drug is changed.");
	}
}
