package tas.adaptation.simple;

import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.ServiceDescription;
import tas.adaptation.MyAdaptationEngine;
import tas.services.log.Log;

/**
 * 
 * @author M. Usman Iftikhar
 *
 */
public class MyProbe implements WorkflowProbeInterface {

	MyAdaptationEngine myAdaptationEngine;
		
	public void connect(MyAdaptationEngine myAdaptationEngine) {
	    this.myAdaptationEngine = myAdaptationEngine;
	}

	@Override
	public void workflowStarted(String qosRequirement, Object[] params) {
	    System.err.println("Workflow Started monitoring");
	    //Log.addLog("WorkflowStarted", "Workflow Started monitoring");
	}

	@Override
	public void workflowEnded(Object result, String qosRequirement, Object[] params) {
	    System.err.println("Workflow Ended");
	}

	@Override
	public void serviceOperationInvoked(ServiceDescription service, String opName, Object[] params) {

	}

	@Override
	public void serviceOperationReturned(ServiceDescription service, Object result, String opName, Object[] params) {

	}

	@Override
	public void serviceOperationTimeout(ServiceDescription service, String opName, Object[] params) {
	    System.err.println("Service Failed: " + service.getServiceName());
	    
	    // Remove service from cache
	    myAdaptationEngine.handleServiceFailure(service, opName);
	}
	
	@Override
	public void serviceNotFound(String serviceType, String opName){
	    System.err.println(serviceType + opName + "Not found");
	    myAdaptationEngine.handleServiceNotFound(serviceType, opName);
	}
}
