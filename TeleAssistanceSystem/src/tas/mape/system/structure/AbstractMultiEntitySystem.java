package tas.mape.system.structure;

import tas.mape.communication.CommunicationComponent;
import tas.mape.communication.message.ComponentMessage;
import tas.mape.communication.protocol.AbstractProtocol;
import tas.mape.system.entity.AbstractSystemEntity;

/**
 * Abstract class representing a multi entity system containing system entities.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 *
 * @param <T> the system entity type
 * @param <P1> protocol message type parameter
 * @param <P2> protocol communication component type parameter
 */
public abstract class AbstractMultiEntitySystem<T extends AbstractSystemEntity<?, ?>, P1 extends ComponentMessage<?>, 
                      P2 extends CommunicationComponent<P1, ?>> extends AbstractSystem<T> {
	/**
	 * Create a new abstract multi-loop system with given system entities
	 * @param systemEntities the given system entities
	 * @param systemEntityCount the amount of entities that the system supports
	 * @throws IllegalArgumentException throw when the given 
	 *         amount of entities is not supported by the system
	 */
	protected AbstractMultiEntitySystem(int systemEntityCount, T[] systemEntities) throws IllegalArgumentException {
		super(systemEntityCount, systemEntities);
	}
	
	/**
	 * Execute the system with a given amount of execution cycles following a certain protocol
	 * @param executionCycles the given amount of execution cycles
	 * @param protocol the given protocol
	 * @param maxProtocolterations the given maximum amount of iterations
	 * @param usedMessageContentPercentage percentage indicating how much of the maximum amount of information the protocol uses in its messages
	 */
	public abstract void executeSystem(int executionCycles, AbstractProtocol<P1, P2> protocol, int maxProtocolterations, int usedMessageContentPercentage);
}
