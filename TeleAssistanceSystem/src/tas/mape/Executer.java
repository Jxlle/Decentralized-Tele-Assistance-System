package tas.mape;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.adaptation.effectors.CacheEffector;
import service.adaptation.effectors.WorkflowEffector;
import service.composite.CompositeService;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class that represents the executer component in a MAPE-K component
 */
public class Executer {
	
	// Effectors used by the executer
	Map<String, Object> effectors;
	
	/**
	 * Create a new executer and initialize its effectors using a given composite service
	 * @param compositeService the given composite service that the effectors will effect
	 */
	public Executer(CompositeService compositeService) {
		
		WorkflowEffector workflowEffector = new WorkflowEffector(compositeService);
		
		effectors = new HashMap<>();
		effectors.put("workflowEffector", workflowEffector);
	}
	
	/**
	 * Execute a given plan
	 * @param plan the given plan composed of a list of plan components
	 */
	public void execute(List<PlanComponent> plan) {
		
		// Execute the plan by executing each plan component 
		for (PlanComponent planComponent : plan) {
			
			PlanComponentType planComponentType = planComponent.getPlanComponentType();
			Object effector = effectors.get(planComponentType.getEffectorIdentifier());
			Method effectorMethod = null;
			
			// Search effector method
			try {
				effectorMethod = effector.getClass().getMethod(planComponentType.getMethodName(), planComponentType.getMethodParameterTypes());
			} catch (NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}
			
			// Execute effector method
			try {
				effectorMethod.invoke(effector, planComponent.getParameterValues());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}
