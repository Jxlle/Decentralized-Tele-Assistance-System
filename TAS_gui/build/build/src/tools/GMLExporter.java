package tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Abstract class for creating GML visualizations of graphs and trees.
 *   
 * @author "Kostiantyn Kucher"
 */
public abstract class GMLExporter {

	/**
	 * Map of objects to their integer IDs
	 */
	protected Map<Object, Integer> graphID;
	
	/**
	 * Current maximal used ID
	 */
	protected int maxID;
	
	/**
	 * Stack of group ID values (used for nesting)
	 */
	protected LinkedList<Integer> groupID;
	
	/**
	 * GML file contents buffer
	 */
	protected StringBuilder buffer;
	
	/**
	 * Initializes utility data structures for GML contents construction
	 */
	protected void startExporter() {
		// Initialize map and counter
		maxID = 0;
		graphID = new LinkedHashMap<Object, Integer>();
		groupID = new LinkedList<Integer>();
		
		// Initialize buffer
		buffer = new StringBuilder(GMLHelper.GML_HEADER);
	}
	
	/**
	 * Finalizes the GML contents for export
	 * 
	 * @param baseFilename exported file name (without extension)
	 */
	protected void stopExporter(String baseFilename)
			throws IOException {
		// Finalize file
		buffer.append(GMLHelper.FOOTER);
		
		// Write content to external file
		Writer writer = new OutputStreamWriter(
				new FileOutputStream(baseFilename + ".gml"));
		writer.write(buffer.toString());
		writer.close();
	}
	
	/**
	 * Returns unique ID for given object
	 * 
	 * @param object object to get ID for
	 * @return unique integer ID
	 */
	protected int getID(Object object) {
		if (object == null)
			return -1;
		
		// Check if object is already included to map
		if (graphID.containsKey(object)) {
			return graphID.get(object);
		} else {
			int result = maxID++;
			graphID.put(object, result);
			return result;
		}
	}
		
}
