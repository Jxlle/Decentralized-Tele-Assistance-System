/**
 * 
 */
package profile;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Read and write input profile to xml file
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class ProfileExecutor {

	private static XStream xstream = new XStream(new StaxDriver());
	
	/**
	 * The constructed profile can be fetched outside
	 */
	public static Map<String, InputProfile> profiles = new HashMap<>();
	
	static{
		xstream.alias("inputProfile", InputProfile.class);
		xstream.alias("variable", InputProfileVariable.class);
		xstream.alias("value", InputProfileValue.class);
		xstream.alias("requirement", Requirement.class);
	}
	
	/**
	 * Read profile object from xml file
	 * @param xmlPath the xml file path
	 */
	public static void readFromXml(String xmlPath, String id) {
		try {
			InputStream input = new FileInputStream(xmlPath);
			ProfileExecutor.profiles.put(id, (InputProfile) xstream.fromXML(input));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write profile object to xml file
	 * @param xmlPath the xml file path
	 */
	public static void writeToXml(String xmlPath, String id){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(xmlPath));	
			writer.write(xstream.toXML(ProfileExecutor.profiles.get(id)));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	// TODO cleanup
	// Used for generating the XML files
	public static void main(String[] args){
		
		/*String xmlPath = "inputProfile.xml";
		
		ProfileExecutor.profile.maxSteps = 100;

		InputProfileValue value = new InputProfileValue(1,1);
		InputProfileVariable variable = new InputProfileVariable("patientId");
		variable.addValue(value);
		
		ProfileExecutor.profile.addVariable(variable);
		
		value = new InputProfileValue(1,0.75);
		variable = new InputProfileVariable("pick");
		variable.addValue(value);
		
		value=new InputProfileValue(2,0.25);
		variable.addValue(value);
		ProfileExecutor.profile.addVariable(variable);
		
		ProfileExecutor.profile.qosRequirement = "AlarmServiceQoSFailureRate";
		
		ProfileExecutor.writeToXml(xmlPath);	*/
	}
}
