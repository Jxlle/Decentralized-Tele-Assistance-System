package tas.mape;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.WeightedCollection;

public abstract class AbstractWorkflowQoSRequirement {
	
	/**
	 * 
	 * @param strategy
	 * @param combinationLimit
	 * @param usableServices
	 * @return
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<Description, WeightedCollection<String>>> applyStrategy(int strategy, int combinationLimit, Map<Description, List<ServiceDescription>> usableServices) throws IllegalArgumentException {
		
		if (combinationLimit < 1) {
			throw new IllegalArgumentException("Combination limit must be at least 1.");
		}
		
		Method method = null;
		List<Map<Description, WeightedCollection<String>>> result = null;
		
		try {
			method = this.getClass().getMethod("applyStrategy" + strategy, int.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		

		try {
			result = (List<Map<Description, WeightedCollection<String>>>) method.invoke(this, combinationLimit, usableServices);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
