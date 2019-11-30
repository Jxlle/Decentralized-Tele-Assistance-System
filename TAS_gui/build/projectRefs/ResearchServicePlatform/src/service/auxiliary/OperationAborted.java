/**
 * 
 */
package service.auxiliary;

import javax.management.RuntimeErrorException;

/**
 * Responsible for the error that an operation can not be invoked
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class OperationAborted extends RuntimeErrorException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param e the error
	 */
	public OperationAborted(Error e) {
		super(e);
	}
}
