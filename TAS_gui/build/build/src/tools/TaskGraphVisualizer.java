package tools;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import taskgraph.TaskGraph;
import taskgraph.TaskGraph.START;


/**
 * Task Graph visualizer.
 * Suggested workflow:
 * - create an instance and invoke exportGML() method
 * - use external tool (yEd) to draw the GML file
 *  
 * @author "Kostiantyn Kucher"
 */
public class TaskGraphVisualizer extends GMLExporter {

	/**
	 * Map of method names to corresponding Task Graph edge names
	 */
	private final static Map<String, String> edgeNames;
	
	static {
		edgeNames = new LinkedHashMap<String, String>();
		edgeNames.put("getPrev", "prev");
		edgeNames.put("getNext", "next");
		edgeNames.put("getTrue", "true");
		edgeNames.put("getFalse", "false");
		edgeNames.put("getFirst", "first");
		edgeNames.put("getLeft", "left");
		edgeNames.put("getRight", "right");
	}
		
	/**
	 * Current GML group ID
	 */
	private int currentGroupID;
	
	/**
	 * Map of unique scope ID to Task Graphs
	 * (used for resolving function calls)
	 */
	private START allTaskGraphs;
		
	/**
	 * Symbol Table used to retrieve scope information
	 */
	
	/**
	 * Set of already visited nodes (to avoid loops)
	 */
	private Set<TaskGraph> visitedNodes;
	
	/**
	 * Special GML contents buffer for method call edges
	 * (to avoid addressing GML nodes prior to their declarations)
	 */
	private StringBuilder callBuffer;
	
	/**
	 * Special GML contents buffer for edges
	 * (to avoid addressing GML nodes prior to their declarations)
	 */
	private StringBuilder edgeBuffer;
		
	/**
	 * Exports the visualization of Task Graph to a GML file
	 * 
	 * @param baseFilename exported file name (without extension)
	 * @param allTaskGraphs map of unique scope ID to Task Graphs
	 * @throws IOException if writing to external file has failed
	 */
	public void exportGML(String baseFilename,
			START allTaskGraphs)
		throws IOException {
		// Initialize exporter
		startExporter();
		
		// Initialize members
		this.allTaskGraphs = allTaskGraphs;
		visitedNodes = new LinkedHashSet<TaskGraph>();
		callBuffer = new StringBuilder();
		edgeBuffer = new StringBuilder();
		
		
		exportTask(allTaskGraphs);
		
				
		// Append contents of edge buffer
		buffer.append(edgeBuffer.toString());
		
		// Append contents of call buffer
		buffer.append(callBuffer.toString());
						
		// Finalize exporter contents
		stopExporter(baseFilename);
	}
	
	
	/**
	 * Constructs a string common for GML edge definition 
	 * (convenience method).
	 * 
	 * @param sourceID ID of source GML node
	 * @param targetID ID of target GML node
	 * @param label optional edge label (may be null)
	 * @return string to be appended to resulting buffer
	 */
	private String constructEdge(int sourceID, int targetID, String label) {
		StringBuilder tempBuffer = new StringBuilder();
				
		tempBuffer.append(GMLHelper.EDGE_HEADER)
			.append(GMLHelper.SRC_ATTR).append(sourceID).append("\n")
			.append(GMLHelper.TGT_ATTR).append(targetID).append("\n");
		
		if (label != null) {
			tempBuffer.append(label);
		}
		
		tempBuffer.append(GMLHelper.FOOTER);
		
		return tempBuffer.toString();
	}
	
	/**
	 * Checks if task has already been visited and adds it
	 * to the visited set as a side effect (auxiliary method
	 * to avoid code duplication).
	 * 
	 * @param task Task Graph node to check
	 * @return result of check
	 */
	private boolean isVisited(TaskGraph node) {
		if (visitedNodes.contains(node)) {
			return true;
		} else {
			visitedNodes.add(node);
			return false;
		}
	}
	
	/**
	 * Constructs a string for GML node definition
	 * for task nodes. 
	 * 
	 * @param node Task Graph node
	 * @return string to be appended to resulting buffer
	 */
	private String constructNode(TaskGraph node) {
				
		// Get node ID
		int nodeID = getID(node);
		
		StringBuilder tempBuffer = new StringBuilder();
		
		// Append node description
		tempBuffer.append(GMLHelper.NODE_HEADER)
			.append(GMLHelper.ID_ATTR).append(nodeID).append("\n")
			.append(GMLHelper.composeLabelAttributes(
					node.getClass().getSimpleName(),
					prepareAttributes(node)))
			.append(GMLHelper.GID_ATTR).append(currentGroupID).append("\n")
			.append(GMLHelper.FOOTER);
				
		return tempBuffer.toString();
	}
	
	/**
	 * Prepares a map of attributes values common for a node of given AST subtype.
	 * The result is used for composing an HTML label.
	 * 
	 * @param node Task Graph node to prepare attributes for
	 * @return resulting map of node attributes to their values 
	 */
	private Map<String, String> prepareAttributes(TaskGraph node) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		
		// Check the class that node extends and 
		// use the corresponding attributes' values
		if (node instanceof TaskGraph.PUSH) {
			TaskGraph.PUSH castedNode = (TaskGraph.PUSH) node;
			result.put("name", castedNode.getName());
		}
		
		if (node instanceof TaskGraph.ALLOC) {
			TaskGraph.ALLOC castedNode = (TaskGraph.ALLOC) node;
			result.put("template", castedNode.getName());
			if (castedNode.getInstanceName() != null)
				result.put("instanceName", castedNode.getInstanceName());
		}
		
		if (node instanceof TaskGraph.CALL) {
			TaskGraph.CALL castedNode = (TaskGraph.CALL) node;
			result.put("ServiceName", castedNode.getServiceName());
			result.put("OperationName", castedNode.getOperationName());
		}
		
		if (node instanceof TaskGraph.Expression) {
			TaskGraph.Expression castedNode = (TaskGraph.Expression) node;
			result.put("value", (castedNode.getValue() == null)
					? "null" : castedNode.getValue().toString());
		}
		
		if (node instanceof TaskGraph.MemoryOperation) {
			TaskGraph.MemoryOperation castedNode = (TaskGraph.MemoryOperation) node;
			result.put("varName", castedNode.getVarName());
			result.put("isLocal", Boolean.toString(castedNode.isLocal()));
		}
		
		if (node instanceof TaskGraph.DECL) {
			TaskGraph.DECL castedNode = (TaskGraph.DECL) node;
			result.put("type", (castedNode.getType() == null)
					? "null" : castedNode.getType().toString());
			result.put("isArray", Boolean.toString(castedNode.isArray()));
			result.put("isReference", Boolean.toString(castedNode.isReference()));
		}
		
		if (node instanceof TaskGraph.Store) {
			TaskGraph.Store castedNode = (TaskGraph.Store) node;
			result.put("assignOp", (castedNode.getAssignOp() == null)
					? "null" : castedNode.getAssignOp().toString());
		}
		
		if (node instanceof TaskGraph.BinaryOp) {
			TaskGraph.BinaryOp castedNode = (TaskGraph.BinaryOp) node;
			result.put("binaryOp", (castedNode.getBinaryOp() == null) 
					? "null" : castedNode.getBinaryOp().toString());
		}
		
		if (node instanceof TaskGraph.UnaryOp) {
			TaskGraph.UnaryOp castedNode = (TaskGraph.UnaryOp) node;
			result.put("unaryOp", (castedNode.getUnaryOp() == null)
					? "null" : castedNode.getUnaryOp().toString());
		}
				
		return result;
	}
	
	/**
	 * Performs common task exporting operations 
	 * (auxiliary method to avoid code duplication).
	 * 
	 * @param task Task Graph node to export
	 * @return <ul><li>true if task has already been exported
	 * 			<li>false otherwise</ul>  
	 */
	private boolean exportTask(TaskGraph task) {
		if (task == null || isVisited(task))
			return true;
		
		// Export node
		buffer.append(constructNode(task));
		
		// Visit connected nodes
		// (uses reflection to retrieve edges)
		Class cl = task.getClass();
		for (Method m: cl.getMethods()) {
			// Check method name
			for (String methodName: edgeNames.keySet()) {
				if (m.getName().equals(methodName)) {
					// Invoke the method
					try {
						TaskGraph result = (TaskGraph) m.invoke(task, null);
						
						if (result != null) {
							// Visit Task Graph node
							exportTask(result);
														
							// Export edge
							edgeBuffer.append(constructEdge(getID(task), getID(result),
									String.format("label \"%s\"\n", edgeNames.get(methodName))));
						}
						
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		
		return false;
	}
	
}
