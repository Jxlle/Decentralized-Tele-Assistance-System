package tas.communication.message;

public class ProtocolMessageInformation {
	
	public String sender;
	public String receiver;
	public String messageType;
	
	public ProtocolMessageInformation(String sender, String receiver, String messageType) {
		this.sender = sender;
		this.receiver = receiver;
		this.messageType = messageType;
	}

}
