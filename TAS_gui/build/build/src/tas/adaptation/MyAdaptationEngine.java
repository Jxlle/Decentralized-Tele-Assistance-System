/**
 * 
 */

package tas.adaptation;

import service.adaptation.effectors.WorkflowEffector;
import service.auxiliary.ServiceDescription;
import tas.adaptation.simple.MyProbe;
import tas.services.assistance.AssistanceService;

/**
 * @author M. Usman Iftikhar
 * @email muusaa@lnu.se
 *
 */
public class MyAdaptationEngine implements AdaptationEngine {

    MyProbe myProbe;
    WorkflowEffector myEffector;
    AssistanceService assistanceService;

    public MyAdaptationEngine(AssistanceService assistanceService) {
    	this.assistanceService = assistanceService;
    	myProbe = new MyProbe();
    	myProbe.connect(this);
    	myEffector = new WorkflowEffector(assistanceService);
    }
    
    
    public void handleServiceFailure(ServiceDescription service, String opName) {
    	this.myEffector.removeService(service);
    }

    public void handleServiceNotFound(String serviceType, String opName) {
    	myEffector.refreshAllServices(serviceType, opName);
    }

    @Override
    public void start() {
    	assistanceService.getWorkflowProbe().register(myProbe);
    	myEffector.refreshAllServices();
    }
    
    @Override
    public void stop() {
    	assistanceService.getWorkflowProbe().unRegister(myProbe);
    }
}
