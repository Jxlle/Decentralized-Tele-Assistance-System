package tas.mape.system.entity;

import service.composite.CompositeService;

// TODO
public class WorkflowExecuter {

	// Fields
	private CompositeService compositeService;
	
	// The amount of workflow execution cycles that are executed before the possibility of analyzer execution
	public static int workflowCycles = 100;
	
	public void executeWorkflow() {
		
	}
	
	public CompositeService getCompositeService() {
		return compositeService;
	}
}
