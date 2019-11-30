/**
 * 
 */
package service.adaptation.probes;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract probe class with lists of defined interfaces
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 *
 */
public class AbstractProbe<E> {
    protected List<E> subscribers = new LinkedList<E>();

    /**
     * Register a probe 
     * @param e subscriber object
     */
    public void register(E e){
    	subscribers.add(e);
    }
    
    /**
     * Unregister a probe
     * @param e subscriber object
     */
    public void unRegister(E e){
    	subscribers.remove(e);
    }

}
