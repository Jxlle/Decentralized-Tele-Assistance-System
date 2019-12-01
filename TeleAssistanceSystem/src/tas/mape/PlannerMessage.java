package tas.mape;

import java.util.List;
import java.util.Map;

import service.auxiliary.AbstractMessage;
import service.auxiliary.Description;
import service.auxiliary.WeightedCollection;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 * 
 * Class representing the type of message that planners use to communicate.
 */
public class PlannerMessage extends AbstractMessage {
	
	private String senderEndpoint;
	private Map<String, Integer> serviceLoads;
	private Map<Description, WeightedCollection<String>> chosenServices;
	
	/**
	 * Create a new planner message with a given id, receiver endpoint, sender endpoint, message type, map of chosen services per description and the chosen services service loads.
	 * @param id the id of the message (identifier)
	 * @param receiverEndpoint the receiver endpoint
	 * @param senderEndpoint the sender endpoint
	 * @param msgType the message type
	 * @param chosenServices the map of chosen services per description
	 * @param serviceLoads the the chosen services service loads
	 */
	public PlannerMessage(int id, String receiverEndpoint, String senderEndpoint, String msgType, Map<Description, WeightedCollection<String>> chosenServices, Map<String, Integer> serviceLoads) {
		super(id, receiverEndpoint, msgType);
		this.senderEndpoint = senderEndpoint;
		this.chosenServices = chosenServices;
		this.serviceLoads = serviceLoads; 
	}
	
	/**
	 * Return the sender endpoint
	 * @return the sender endpoint
	 */
	public String getSenderEndpoint() {
		return senderEndpoint;
	}
	
	/**
	 * Return the service loads
	 * @return the service loads
	 */
	public Map<String, Integer> getServiceLoads() {
		return serviceLoads;
	}
	
	/**
	 * Return the chosen services map
	 * @return the chosen services map
	 */
	public Map<Description, WeightedCollection<String>> getChosenServices() {
		return chosenServices;
	}

}
