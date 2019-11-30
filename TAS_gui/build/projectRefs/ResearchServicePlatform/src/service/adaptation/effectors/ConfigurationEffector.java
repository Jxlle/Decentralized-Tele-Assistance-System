package service.adaptation.effectors;

import service.composite.CompositeService;

/**
 * 
 * Responsible for changing configuration
 * 
 */
public class ConfigurationEffector extends AbstractEffector {

	/**
	 * Constructor
	 * @param compositeService which composite service to be affected
	 */
    public ConfigurationEffector(CompositeService compositeService) {
    	super(compositeService);
    }

    /**
     * Change timeout factor
     * 
     * @param timeout new timeout factor
     */
    public void changeTimeout(int timeout) {
    	compositeService.getConfiguration().timeout = timeout;
    }

    /**
     * Set the maximum retry attempts for sending messages
     * 
     * @param maxRetryAttempts the value of max retry attempts 
     */
    public void setMaxRetryAttempts(int maxRetryAttempts) {
    	compositeService.getConfiguration().maxRetryAttempts = maxRetryAttempts;
    }
}
