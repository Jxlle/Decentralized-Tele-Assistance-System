package tas.services.qos;

import java.util.HashMap;
import java.util.List;

import service.auxiliary.ServiceDescription;
import service.workflow.AbstractQoSRequirement;


public class MinCostQoS implements AbstractQoSRequirement {

    @Override
    public ServiceDescription applyQoSRequirement(List<ServiceDescription> serviceDescriptions,String opName,Object[] params) {
	
	double minCost = Double.MAX_VALUE;
	int index = 0;
	double cost;
	HashMap properties;
	for (int i = 0; i < serviceDescriptions.size(); i++) {
	    properties = serviceDescriptions.get(i).getCustomProperties();
	    if (properties.containsKey("Cost")){
		cost = (double) properties.get("Cost");
		if (cost < minCost){
		    minCost = cost;
		    index = i;
		}
	    }
	}
	
	return serviceDescriptions.get(index);
    }
}
