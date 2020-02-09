package tas.mape.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.adaptation.effectors.WorkflowEffector;
import service.composite.CompositeService;
import tas.mape.planner.PlanComponent;
import tas.mape.planner.PlanComponentType;

/**
 * Class that represents the executor component in a MAPE-K component.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class Executor {
	
	// Effectors used by the executor
	Map<String, Object> effectors;
	
	/**
	 * Create a new executor and initialize its effectors list using a given composite service
	 */
	public Executor() {		
		effectors = new HashMap<>();
	}
	
	/**
	 * Initialize executor effectors
	 * @param compositeService the given composite service that the effectors will affect
	 * @note Should be executed before using executor
	 */
	public void initializeEffectors(CompositeService compositeService) {
		WorkflowEffector workflowEffector = new WorkflowEffector(compositeService);
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
