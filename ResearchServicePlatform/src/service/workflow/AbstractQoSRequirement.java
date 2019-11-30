package service.workflow;

import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import service.atomic.AtomicService;

/**
 * 
 * Interface to specify QoS requirements
 * 
 */
public interface AbstractQoSRequirement {
	/**
	 * Select a best service from list of services
	 * @param descriptions  list of service descriptions for selecting
	 * @param opName  the operation name
	 * @param params the parameters for the operation
	 * @return the best service
	 */
    ServiceDescription applyQoSRequirement(List<ServiceDescription> descriptions, String opName, Object[] params);
}
