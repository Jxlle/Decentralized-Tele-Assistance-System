package tas.services.qos;

import java.util.HashMap;
import java.util.List;

import service.auxiliary.ServiceDescription;
import service.workflow.AbstractQoSRequirement;


// Abstract QoS requirements are redundant in this version
public class MinCostQoS implements AbstractQoSRequirement {

    @Override
    public ServiceDescription applyQoSRequirement(List<ServiceDescription> descriptions, String opName, Object[] params) {
	
	double minCost = Double.MAX_VALUE;
	int index = 0;
	double cost;
	HashMap<?, ?> properties;
	for (int i = 0; i < descriptions.size(); i++) {
	    properties = descriptions.get(i).getCustomProperties();
	    if (properties.containsKey("Cost")){
		cost = (double) properties.get("Cost");
		if (cost < minCost){
		    minCost = cost;
		    index = i;
		}
	    }
	}
	
	return descriptions.get(index);
    }
}
