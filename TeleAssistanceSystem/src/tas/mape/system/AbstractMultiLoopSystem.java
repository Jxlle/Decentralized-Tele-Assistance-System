package tas.mape.system;

import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.ComponentMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.system.entity.AbstractSystemEntity;

/**
 * @author Jelle Van De Sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Abstract class representing a multi-loop system containing system entities.
 * @param <T> the system entity type
 * @param <P1> protocol message type parameter
 * @param <P2> protocol communication component type parameter
 */
public abstract class AbstractMultiLoopSystem<T extends AbstractSystemEntity<?, ?>, P1 extends ComponentMessage<?>, 
                      P2 extends CommunicationComponent<P1>> extends AbstractSystem<T> {
	/**
	 * Create a new abstract multi-loop system with given system entities
	 * @param systemEntities the given system entities
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	@SafeVarargs
	protected AbstractMultiLoopSystem(T... systemEntities) throws IllegalArgumentException {
		super(systemEntities);
	}
	
	/**
	 * Execute the system with a given amount of execution cycles following a certain protocol
	 * @param executionCycles the given amount of execution cycles
	 * @param protocol the given protocol
	 */
	public abstract void executeSystem(int executionCycles, AbstractProtocol<P1, P2> protocol);

}
