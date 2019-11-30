package tas.services.medical;

import java.util.HashMap;
import java.util.Random;

import service.atomic.AtomicService;
import service.auxiliary.ServiceOperation;

public class MedicalAnalysisService extends AtomicService {

    public MedicalAnalysisService(String serviceName, String serviceEndpoint) {
	super(serviceName, serviceEndpoint);
    }

    @ServiceOperation()
    public int analyzeData(HashMap data) {
	System.out.println(this.getServiceDescription().getServiceName() + " is started to analyze the data.");
	return new Random().nextInt(3) + 1;
    }
}
