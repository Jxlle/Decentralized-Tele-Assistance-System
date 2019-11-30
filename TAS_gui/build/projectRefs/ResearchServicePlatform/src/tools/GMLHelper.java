package tools;

import java.util.Collection;
import java.util.Map;

/**
 * Auxiliary class for GML constants / helper method definitions
 * that are shared by other GML-related classes.
 * 
 * @author "Kostiantyn Kucher"
 */
public class GMLHelper {

	/**
	 * Default GML file header
	 */
	public final static String GML_HEADER = "graph [\n"
		+ "hierarchic 1\n"
		+ "directed 1\n";
	
	/**
	 * Default footer
	 */
	public final static String FOOTER = "]\n";
		
	/**
	 * Default node header
	 */
	public final static String NODE_HEADER = "node [\n";
	
	/**
	 * Default edge header
	 */
	public final static String EDGE_HEADER = "edge [\n";
	
	/**
	 * ID attribute
	 */
	public final static String ID_ATTR = "id ";
	
	/**
	 * GID attribute
	 */
	public final static String GID_ATTR = "gid ";
	
	/**
	 * Edge source attribute
	 */
	public final static String SRC_ATTR = "source ";
	
	/**
	 * Edge target attribute
	 */
	public final static String TGT_ATTR = "target ";
	
	/**
	 * isGroup attribute / value
	 */
	public final static String ISGROUP = "isGroup 1\n";
	
	/**
	 * HTML label header
	 */
	public final static String LABEL_HEADER = "label	\"<html>\n";
	
	/**
	 * HTML label footer
	 */
	public final static String LABEL_FOOTER = "</html>\"\n";
	
	/**
	 * HTML table row header
	 */
	public final static String ROW_HEADER = "<tr border=&quot;1&quot;>\n<td>\n";
	
	/**
	 * HTML table row footer
	 */
	public final static String ROW_FOOTER = "</td>\n</tr>\n";
	
	/**
	 * HTML table footer
	 */
	public final static String TABLE_FOOTER = "</table>";
	
	/**
	 * Aqua background attribute for GML nodes
	 */
	public final static String BG_AQUA = "graphics\n[fill \"#00FFFF\"]\n";
		
	/**
	 * Lime background attribute for GML nodes
	 */
	public final static String BG_LIME = "graphics\n[fill \"#00FF00\"]\n";
	
	/**
	 * Red background attribute for GML nodes
	 */
	public final static String BG_SALMON = "graphics\n[fill \"#FA8072\"]\n";
	
	/**
	 * Aqua color and dashed style attributes for GML edges
	 */
	public final static String EDGE_DASHED_AQUA =
		"graphics\n[style \"dashed\"\n fill \"#00FFFF\"\n arrow \"last\"]\n";
	
	/**
	 * Lime color and dashed style attributes for GML edges
	 */
	public final static String EDGE_DASHED_LIME =
		"graphics\n[style \"dashed\"\n fill \"#00FF00\"\n arrow \"last\"]\n";
	
	/**
	 * Gray color and dashed style attributes for GML edges
	 */
	public final static String EDGE_DASHED_GRAY =
		"graphics\n[style \"dashed\"\n fill \"#606060\"\n arrow \"last\"]\n";
		
	/**
	 * Performs escaping for some reserved HTML symbols
	 * 
	 * @param string String to perform escaping for
	 * @return valid HTML string to be used in GML file
	 */
	public static String escapeHtml(String string) {
		if (string == null) {
			return "";
		} else {
			return string.replace("&", "&amp;")
					.replace("<", "&lt;")
					.replace(">", "&gt;")
					.replace("\"", "&quot;");
		}
	}
	
	/**
	 * Composes a fancy-looking HTML label for given entity and its attributes
	 * 
	 * @param entityName entity name to be displayed with large font
	 * @param attributes map of entity attributes (and their values)
	 * @return label to be used in GML node definition
	 */
	public static String composeLabelAttributes(String entityName, Map<String, String> attributes) {
		StringBuilder sb = new StringBuilder(LABEL_HEADER);
		
		// Append entity name as a header
		sb.append("<h2 align=&quot;center&quot;>")
			.append(escapeHtml(entityName))
			.append("</h2>\n");
		
		// Append attributes in a table (if any given)
		sb.append(composeAttributes(attributes));
		
		sb.append(LABEL_FOOTER);
		
		return sb.toString();
	}
	
	/**
	 * Composes HTML representation for attributes key-values
	 * 
	 * @param attributes map of entity attributes (and their values)
	 * @return HTML code to be used in GML label definition
	 */
	public static String composeAttributes(Map<String, String> attributes) {
		StringBuilder sb = new StringBuilder();
				
		// Append attributes in a table (if any given)
		if (attributes != null && attributes.size() > 0) {
			sb.append("<table align=&quot;center&quot;>\n");
			
			for (String key: attributes.keySet()) {
				sb.append("<tr align=&quot;center&quot;>\n");
				sb.append("<td><b>").append(escapeHtml(key));
				sb.append("</b>: ").append(escapeHtml(attributes.get(key)));
				sb.append("</td>\n</tr>\n");
			}
			
			sb.append("</table>");
		}
		
		return sb.toString();
	}
	
	/**
	 * Composes a fancy-looking HTML label for given entity and its composite attributes
	 * 
	 * @param entityName entity name to be displayed with large font
	 * @param attributes map of entity attributes (and their values)
	 * @return label to be used in GML node definition
	 */
	public static String composeLabelComplexAttributes(String entityName,
			Map<String, Collection<String>> attributes) {
		StringBuilder sb = new StringBuilder(LABEL_HEADER);
		
		// Append entity name as a header
		sb.append("<h2 align=&quot;center&quot;>")
			.append(escapeHtml(entityName))
			.append("</h2>\n");
		
		// Append attribute values in a table (if any given)
		if (attributes != null && attributes.size() > 0) {
			sb.append("<table align=&quot;center&quot;>\n");
			
			// Append attribute name (in bold font)
			for (String key: attributes.keySet()) {
				sb.append("<tr align=&quot;center&quot;>\n");
				sb.append("<td><b>").append(escapeHtml(key));
				sb.append(":</b></td>\n</tr>\n");
				
				// Append attribute values
				for (String value: attributes.get(key)) {
					sb.append("<tr align=&quot;center&quot;>\n");
					sb.append("<td>").append(escapeHtml(value));
					sb.append("</b></td>\n</tr>\n");
				}
			}
			
			sb.append("</table>");
		}
		
		sb.append(LABEL_FOOTER);
		
		return sb.toString();
	}
	
	
	/**
	 * Composes a label and a top part of HTML table to use for nested structures visualization. 
	 * 
	 * @param label entity description to use on the top of the table
	 * @return the upper part of HTML table to use in GML Signature / Environment description
	 */
	public static String composeTableHeader(String label) {
		StringBuilder sb = new StringBuilder();
		
		// Append entity description as a header
		sb.append(escapeHtml(label)).append("\n");
		
		// Append table header
		sb.append("<table>\n");
		
		return sb.toString();
	}
	
}