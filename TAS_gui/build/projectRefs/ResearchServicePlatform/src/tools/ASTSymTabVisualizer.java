package tools;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;

import service.workflow.ast.ASTNode;
import service.workflow.ast.ASTNode.Start;



/**
 * AST and Symbol Tables visualizer.
 * Suggested workflow:
 * - create an instance and invoke exportGML() method
 * - use external tool (yEd) to draw the GML file
 *  
 * @author "Kostiantyn Kucher"
 */
public class ASTSymTabVisualizer extends GMLExporter {

	/**
	 * Root AST node
	 */
	Start rootNode;
	
	/**
	 * Exports the visualization of AST and Symbol Table to a GML file
	 * 
	 * @param baseFilename exported file name (without extension)
	 * @param rootNode root of AST
	 * @throws IOException if writing to external file has failed
	 */
	public void exportGML(String baseFilename,
			Start rootNode)
		throws IOException {
		// Initialize exporter
		startExporter();
		
		this.rootNode = rootNode;

		
		// Export content
		exportAST();
				
		// Finalize exporter contents
		stopExporter(baseFilename);
	}
	
	/**
	 * Exports the provided AST
	 */
	private void exportAST() {
		exportNode(rootNode, -1);
	}
	
	
	/**
	 * Exports the specified ASTNode with recursive calls
	 * to children nodes
	 * 
	 * @param node node to export
	 * @param parentNodeID parent node ID to use for edge visualization
	 */
	private void exportNode(CommonTree node, int parentNodeID) {
		if (node == null)
			return;
				
		// Get node ID
		int nodeID = getID(node);
						
		// Append node description
		buffer.append(GMLHelper.NODE_HEADER)
			.append(GMLHelper.ID_ATTR).append(nodeID).append("\n")
			.append(GMLHelper.composeLabelAttributes(
					node.getClass().getSimpleName(),
					prepareAttributes(node)))
			.append(GMLHelper.FOOTER);
	
		// Draw edge from parent to AST node, if necessary
		if (parentNodeID >= 0) {
			buffer.append(GMLHelper.EDGE_HEADER)
				.append(GMLHelper.SRC_ATTR).append(parentNodeID).append("\n")
				.append(GMLHelper.TGT_ATTR).append(nodeID).append("\n")
				.append(GMLHelper.FOOTER);
		}
				
		// Visit children nodes
		for (int i = 0; i < node.getChildCount(); i++) {
			if (node.getChild(i) != null)
				exportNode((CommonTree) node.getChild(i), nodeID);
		}
		
	}
	
	/**
	 * Prepares a map of attributes values common for a node of given AST subtype.
	 * The result is used for composing an HTML label.
	 * 
	 * @param node AST node to prepare attributes for
	 * @return resulting map of node attributes to their values 
	 */
	private Map<String, String> prepareAttributes(CommonTree node) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		
		// Check the class that node extends and 
		// use the corresponding attributes' values
		
		if (node instanceof ASTNode.VarRef) {
			ASTNode.VarRef castedNode = (ASTNode.VarRef) node;
			result.put("name", castedNode.getId());
		}
		
				
		if (node instanceof ASTNode.BooleanLiteral) {
			ASTNode.BooleanLiteral castedNode = (ASTNode.BooleanLiteral) node;
			result.put("value", Integer.toString(castedNode.getLiteralValue()));
		}
		
		if (node instanceof ASTNode.IntegerLiteral) {
			ASTNode.IntegerLiteral castedNode = (ASTNode.IntegerLiteral) node;
			result.put("value", Integer.toString(castedNode.getLiteralValue()));
		}
		
		if (node instanceof ASTNode.NullLiteral) {
			ASTNode.NullLiteral castedNode = (ASTNode.NullLiteral) node;
			result.put("value", null);
		}
		
		return result;
	}

}
