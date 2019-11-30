package service.provider;

import service.auxiliary.AbstractMessage;

/**
 * This interface should be implemented by the classes that are interested to provide communication mechanism between different services.
 * 
 * @author M. Usman Iftikhar, Yifan Ruan
 */
public interface ServiceProvider {
 
    /**
     * This method enables a service to listen for messages on the given endpoint.
     * @param endPoint   which endpoint to be listened
     * @param messageReceiver  instance for dealing with incoming messages
     */
    public void startListening(String endPoint, MessageReceiver messageReceiver);
    
    /**
     * This method stops a service for listening to the incoming messages. 
     */
    public void stopListening();
    
    /**
     * With this method, a service can send a message to the other service.
     * @param msgText  the message content
     * @param destinationEndPoint  the destination to send the message
     */
    public void sendMessage(AbstractMessage msg, String destinationEndPoint);
}
