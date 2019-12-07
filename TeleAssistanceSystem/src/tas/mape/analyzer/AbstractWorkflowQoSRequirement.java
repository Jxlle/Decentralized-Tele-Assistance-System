package tas.mape.analyzer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;
import tas.mape.planner.ServiceCombination;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * An abstract class for choosing service combinations in the analyzer step of the mape-k loop based on a QoS requirement and strategy.
 */
public abstract class AbstractWorkflowQoSRequirement {
	
	/**
	 * Apply a QoS requirement strategy with a given strategy number, combination limit and a map of usable services.
	 * @param strategy the number of the used strategy
	 * @param combinationLimit the limit of returned service combinations
	 * @param usableServices a map of usable services where each key is a service type & operation name combination (description) 
	 *        and the value is a list of the usable services for that description 
	 * @return a list of the chosen service combinations
	 * @throws IllegalArgumentException throw when the combination limit is illegal
	 */
	@SuppressWarnings("unchecked")
	public List<ServiceCombination> applyStrategy(int strategy, int combinationLimit, Map<Description, List<ServiceDescription>> usableServices) throws IllegalArgumentException {
		
		// Check if given combination limit is legal
		if (combinationLimit < 1) {
			throw new IllegalArgumentException("Combination limit must be at least 1.");
		}
		
		Method method = null;
		List<ServiceCombination> result = null;
		
		// Search strategy method
		try {
			method = this.getClass().getMethod("applyStrategy" + strategy, int.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		// Invoke method
		try {
			result = (List<ServiceCombination>) method.invoke(this, combinationLimit, usableServices);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
