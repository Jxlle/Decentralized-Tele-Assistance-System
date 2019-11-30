package tas.services.qos;

import java.util.HashMap;
import java.util.List;

import service.auxiliary.ServiceDescription;
import service.workflow.AbstractQoSRequirement;

//Abstract QoS requirements are redundant in this version
public class ReliabilityQoS implements AbstractQoSRequirement{

    @Override
    public ServiceDescription applyQoSRequirement(List<ServiceDescription> descriptions, String opName, Object[] params) {
	Double minFailureRate = Double.MAX_VALUE;
	int index = 0;
	Double cost;
	HashMap<?, ?> properties;
	for (int i = 0; i < descriptions.size(); i++) {
	    properties = descriptions.get(i).getCustomProperties();
	    if (properties.containsKey("FailureRate")){
		cost = (Double) properties.get("FailureRate");
		if (cost < minFailureRate){
		    minFailureRate = cost;
		    index = i;
		}
	    }
	}
	
	return descriptions.get(index);
    }

}
