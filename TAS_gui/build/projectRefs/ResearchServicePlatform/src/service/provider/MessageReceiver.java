package service.provider;

import service.auxiliary.AbstractMessage;

/**
 * This interface enables a service to listen for messages from service provider.
 * 
 * @author M. Usman Iftikhar, Yifan Ruan
 */
public interface MessageReceiver {
    
    /**
     * The service provider will notify incoming messages through this method.
     * @param message the incoming message
     */
    public void onMessage(AbstractMessage msg);
}
