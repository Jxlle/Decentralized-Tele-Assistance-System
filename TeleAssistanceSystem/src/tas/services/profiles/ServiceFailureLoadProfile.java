package tas.services.profiles;
import java.util.TreeMap;

import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.auxiliary.ServiceDescription;


public class ServiceFailureLoadProfile extends ServiceProfile {
	
	public ServiceFailureLoadProfile() {
		type = "Failure";
	}
	
	@Override
	public boolean preInvokeOperation(ServiceDescription description, String operationName, Object... args) {
		// TODO tabel met fail rates
		return false;
	}

	@Override
	public Object postInvokeOperation(String operationName, Object result,
			Object... args) {
		return result;
	}
}
