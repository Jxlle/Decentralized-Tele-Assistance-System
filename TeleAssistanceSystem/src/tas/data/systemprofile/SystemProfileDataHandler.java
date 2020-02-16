package tas.data.systemprofile;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import profile.SystemProfileValue;
import profile.SystemProfileVariable;

/**
 * Read and write data from a system profile to/from an xml file
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be), 
 * based on the old input profile code of Yifan Ruan (ry222ad@student.lnu.se)
 */
public class SystemProfileDataHandler {
	
	private static XStream xstream = new XStream(new DomDriver());
	public static SystemProfile activeProfile;
	
	/**
	 * XStream aliases
	 */
	static {
		xstream.alias("variable", SystemProfileVariable.class);
		xstream.alias("value", SystemProfileValue.class);
	}
	
	/**
	 * Read profile object from xml file
	 * @param xmlPath the xml file path
	 */
	public static SystemProfile readFromXml(String xmlPath) {
		
		try {
			InputStream input = new FileInputStream(xmlPath);
			return (SystemProfile) xstream.fromXML(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Write profile object to xml file
	 * @param xmlPath the xml file path
	 */
	public static void writeToXml(SystemProfile profile, String xmlPath) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(xmlPath));	
			writer.write(xstream.toXML(profile));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
