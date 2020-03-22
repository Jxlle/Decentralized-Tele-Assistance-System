package tas.workflowAnalyzer;

import java.util.ArrayList;
import java.util.List;

import service.adaptation.probes.interfaces.WorkflowProbeInterface;
import service.auxiliary.Description;
import service.auxiliary.ServiceDescription;
import service.auxiliary.StaticTree;
import service.auxiliary.WeightedCollection;

/**
 * Class representing the workflow probe for the workflow analyzer
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public class WorkflowAnalyzerProbe implements WorkflowProbeInterface {

	// Workflow description data
	private WeightedCollection<Description> usedDescriptions = new WeightedCollection<Description>();
	private List<Description> currentWorkflowServices = new ArrayList<>();
	private StaticTree<Description> workflowServiceTree = new StaticTree<>();
	
	/**
	 * Reset probe
	 */
	public void reset() {
		usedDescriptions = new WeightedCollection<Description>();
		currentWorkflowServices = new ArrayList<>();
		workflowServiceTree = new StaticTree<>();
	}
	
	/**
	 * Return the used description collection
	 * @return the used description collection
	 */
	public WeightedCollection<Description> getUsedDescriptions() {
		return usedDescriptions;
	}
	
	/**
	 * Return the workflow service tree
	 * @return the workflow service tree
	 */
	public StaticTree<Description> getServiceTree() {
		return workflowServiceTree;
	}

	/**
	 * Gets called when a workflow service is invoked.
	 * The description collection and workflow service tree is changed accordingly.
	 */
	@Override
	public void serviceOperationInvoked(ServiceDescription description, String opName, Object[] params) {
		
		Description typeAndOperation = new Description(description.getServiceType(), opName);
		currentWorkflowServices.add(typeAndOperation);
		
		if (usedDescriptions.itemExists(typeAndOperation)) {
			usedDescriptions.increaseWeight(typeAndOperation, 1);
		}
		else {
			usedDescriptions.add(typeAndOperation, 1);
		}
	}

	/**
	 * Gets called when the workflow ended.
	 * The workflow service tree is updated
	 */
	@Override
	public void workflowEnded(Object result, Object[] params) {
		workflowServiceTree.addNodes(currentWorkflowServices);
		currentWorkflowServices = new ArrayList<>();
	}
	
	/**
	 * Not used
	 */
	@Override
	public void serviceNotFound(String serviceType, String opName) throws IllegalStateException {
		// Should never be triggered here
		throw new IllegalStateException("Something went wrong! This probe function should never trigger!");
	}

	/**
	 * Not used
	 */
	@Override
	public void serviceOperationTimeout(ServiceDescription description, String opName, Object[] params) throws IllegalStateException {
		// Should never be triggered here
		throw new IllegalStateException("Something went wrong! This probe function should never trigger!");
	}
	
	/**
	 * Not used
	 */
	@Override
	public void serviceOperationReturned(ServiceDescription description, Object result, String opName,
			Object[] params) {
	}
	
	/**
	 * Not used
	 */
	@Override
	public void workflowStarted(Object[] params) {
	}
}
