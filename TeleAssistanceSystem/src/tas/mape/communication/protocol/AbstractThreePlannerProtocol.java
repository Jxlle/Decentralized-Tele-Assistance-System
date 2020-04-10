package tas.mape.communication.protocol;

import java.util.HashMap;
import java.util.List;

/**
 * Abstract class representing the structure of a three-component planner protocol.
 * This could be generalized to an n-component planner protocol
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public abstract class AbstractThreePlannerProtocol extends AbstractPlannerProtocol {

	protected AbstractThreePlannerProtocol() {
		super(3);
	}
	
	// Shared registry endpoints between both planners
	// This is data that should be exchanged at the start of the system, before the protocol
	protected HashMap<String, List<String>> sharedRegistryEndpoints;

	/**
	 * Initialize local protocol properties
	 * @param startIndex the given index of the starting component
	 * @param receiverIndices the given indices of the receivers of the first message
	 */
	@Override
	protected void InitializeProtocol(int startIndex, List<Integer> receiverIndices) {
		sharedRegistryEndpoints = new HashMap<>();
		
		// Set shared registry endpoints
		updateSharedRegistryEndpoints(0, 1);
		updateSharedRegistryEndpoints(0, 2);
		updateSharedRegistryEndpoints(1, 2);
	}
	
	/**
	 * Find the shared registry endpoints of two components given the component names
	 * @param c1 given component name 1
	 * @param c2 given component name 2
	 * @throws IllegalStateException when the given component names are not found in the shared registry map
	 * @return the shared registries
	 */
	protected List<String> findSharedRegistryEndpoints(String c1, String c2) {
		
		if (sharedRegistryEndpoints.get(c1 + c2) != null) {
			return sharedRegistryEndpoints.get(c1 + c2);
		}
		
		if (sharedRegistryEndpoints.get(c2 + c1) != null) {
			return sharedRegistryEndpoints.get(c2 + c1);
		}
		
		throw new IllegalStateException("The given component names are not found in the shared registry map!");
	}
	
	/**
	 * Update the shared registry endpoints of two components given the component indices
	 * @param c1 given component index 1
	 * @param c2 given component index 2
	 */
	private void updateSharedRegistryEndpoints(int c1, int c2) {
		List<String> sharedRegistryEndpointsLocal = components.get(c1).getRegistryEndpoints();
		List<String> registryEndpointsOther = components.get(c2).getRegistryEndpoints();
		
		// Calculate shared registry endpoints
		sharedRegistryEndpointsLocal.retainAll(registryEndpointsOther);
		
		// Update shared registry map
		sharedRegistryEndpoints.put(components.get(c1).getEndpoint() + components.get(c2).getEndpoint(), sharedRegistryEndpointsLocal);	
		System.out.println("c1: " + components.get(c1).getEndpoint() + " " + "c2: " + components.get(c2).getEndpoint()  + " " + sharedRegistryEndpointsLocal+ " " + sharedRegistryEndpointsLocal + " " + registryEndpointsOther);
	}
}
