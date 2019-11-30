/**
 * 
 */
package service.adaptation.effectors;

import service.composite.CompositeService;

/**
 * Abstract effector, inherited with generic functions
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class AbstractEffector {
	
	protected CompositeService compositeService=null;
	
	/**
	 * Constructor
	 * @param compositeService which composite service to be affected
	 */
	public AbstractEffector(CompositeService compositeService){
		this.compositeService=compositeService;
	}
	
	/**
	 * Get composite service
	 * @return the compositeService
	 */
	public CompositeService getCompositeService() {
		return compositeService;
	}

}
