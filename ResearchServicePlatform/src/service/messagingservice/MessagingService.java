package service.messagingservice;

import java.util.HashMap;
import java.util.Random;

import service.auxiliary.AbstractMessage;
import service.auxiliary.MessageFailed;
import service.auxiliary.Response;
import service.provider.MessageReceiver;
import service.utility.SimClock;

/**
 * 
 * Definition of a service for handling all messages
 */
public class MessagingService {
    
    private static MessagingService instance;
    
    static{
    	instance = new MessagingService();
    }
    
    /**
     * Get the single instance of RSPMessagingService
     * @return the single instance 
     */
    public static MessagingService getInstance() {
    	return instance;
    }

    private HashMap<String, MessageReceiver> queue = new HashMap<>();
    
   //private ExecutorService threadPool = Executors.newCachedThreadPool();
    
    private int messageLoss, messageCount;
    private int minDelay, maxDelay;
    private Random random = new Random();
    
    /**
     * Register new message receiver to the queue
     * @param endPoint the service endpoint
     * @param messageReceiver the message receiver for incoming messages
     */
    public void register(String endPoint, MessageReceiver messageReceiver) {
    	queue.put(endPoint, messageReceiver);
    }

    /**
     * Unregister from the queue with the endpoint
     * @param endPoint the service endpoint
     */
    public void unRegister(String endPoint) {
    	queue.remove(endPoint);
    }
    
    /**
     * Responsible for sending messages to the target endpoint
     * @param endPoint   the source 
     * @param destinationEndPoint  the destination
     * @param msgText    the message information
     */
    public void sendMessage(String endPoint, String destinationEndPoint, AbstractMessage msg) {
		//threadPool.submit(new Runnable() {
		//	@Override
		//	public void run() {

				if (!(endPoint.contains(".#CLIENT#.")
						|| destinationEndPoint.contains(".#CLIENT#.")
						|| endPoint.endsWith(".registry") || destinationEndPoint
						.endsWith(".registry"))) {
					
					if(msg.getType().equals("response")){
					
					if (messageLoss > 0) {
						
						if (100 / messageLoss == messageCount) {
							messageCount = 0;
							
							//if(msg.getType().equals("request")){
								//Request request=(Request)msg;
								//queue.get(endPoint).onMessage(new Response(-1,request.getId(),destinationEndPoint,new MessageFailed()));
							//}
							//else{
								Response response=(Response)msg;
								queue.get(destinationEndPoint).onMessage(new Response(-1,response.getRequestID(),endPoint,new MessageFailed()));
							//}	
												
							return;
						} else {
							messageCount++;
						}
						
					}

					if (minDelay + maxDelay != 0) {
							SimClock.increment(random.nextDouble()*(maxDelay - minDelay)+minDelay);
						/*
						try {
							Thread.sleep(random.nextInt((maxDelay - minDelay + 1)+ minDelay));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
					}
					}

				}
				queue.get(destinationEndPoint).onMessage(msg);
		//	}
		//});
    }
    
    /**
     * Set the message delay
     * @param minDelay the new minimum message delay
     * @param maxDelay the new maxmum message delay
     */
    public void setMessageDelay(int minDelay, int maxDelay) {
    	this.minDelay = minDelay;
    	this.maxDelay = maxDelay;
    }
    
    /**
     * Set the message loss
     * @param messageLoss the new message loss
     */
    public void setMessageLoss(int messageLoss) {
    	this.messageLoss = messageLoss;
    }
    
    /**
     * Return the message loss
     * @return the message loss
     */
    public int getMessageLoss(){
    	return this.messageLoss;
    }
    
    /**
     * Return the minimum message delay
     * @return the minimum message delay
     */
    public int getMessageMinDelay(){
    	return this.minDelay;
    }
    
    /**
     * Return the maxmum message delay
     * @return the maxmum message delay
     */
    public int getMessageMaxDelay(){
    	return this.maxDelay;
    }
}
