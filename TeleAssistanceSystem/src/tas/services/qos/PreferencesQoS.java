package tas.services.qos;

import java.util.List;
import java.util.Random;

import service.auxiliary.ServiceDescription;
import service.workflow.AbstractQoSRequirement;

//Abstract QoS requirements are redundant in this version
public class PreferencesQoS implements AbstractQoSRequirement {

    Random rand = new Random();

    @Override
    public ServiceDescription applyQoSRequirement(List<ServiceDescription> descriptions, String opName, Object[] params) {

	// Search Preferred Service
	for (ServiceDescription description : descriptions) {
	    if (description.getCustomProperties().containsKey("preferred")) {
		boolean value = (boolean) description.getCustomProperties().get("preferred");
		
		if (value)
		    return description;
	    }
	}

	// If there is no preferred service found then select a service randomly
	int index = rand.nextInt(descriptions.size());
	return descriptions.get(index);

    }
}
