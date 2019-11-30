package service.auxiliary;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Convert between XML and Object
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class XMLBuilder {
	
	private XStream xstream = new XStream(new DomDriver());
	
	/**
	 * Constructor
	 */
	public XMLBuilder(){
		xstream.alias("request", Request.class);
		xstream.alias("response", Response.class);
		xstream.alias("ServiceDescription", ServiceDescription.class);
		xstream.alias("param", Param.class);
		xstream.alias("operation", Operation.class);
	}
	
	/**
	 * Convert from object to xml string
	 * @param obj object 
	 * @return the converted xml string
	 */
	public String toXML(Object obj){
		return xstream.toXML(obj);
	}
	
	/**
	 * Convert from xml string to object
	 * @param str xml string
	 * @return the converted object
	 */
	public Object fromXML(String str){
		return xstream.fromXML(str);
	}

}
