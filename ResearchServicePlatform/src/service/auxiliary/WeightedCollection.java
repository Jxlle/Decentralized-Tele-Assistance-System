package service.auxiliary;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

/**
 * 
 * @author Jelle van de sijpe
 * @email jelle.vandesijpe@student.kuleuven.be
 *
 * Class providing a collection where each item in the collection has a weight.
 * This weight determines the chance that this item is randomly taken from the collection.
 * @param <E> Collection type
 * @note Items in the collection have to be unique
 */
public class WeightedCollection<E> {
	
	// Total weight of the collection
	private int totalWeight;
	
	// List of all items with a weight in the collection
	private List<Pair<E, Integer>> items;
	
	/**
	 * Constructor initializing the weighted collection
	 */
	public WeightedCollection() {
		totalWeight = 0;
		items = new ArrayList<Pair<E, Integer>>();
	}
	
	/**
	 * Return the weight of a given item
	 * @param item the given item
	 * @return the given item weight
	 */
	public int getWeight(E item) {
		Pair<E, Integer> itemPair =  items.stream().filter(p -> p.getKey().equals(item)).findFirst().orElse(null);
		return itemPair.getValue();
	}
	
	/**
	 * Return the chance that this item in the collection is chosen
	 * @param item the given item
	 * @return the item chosen chance
	 */
	public double getChance(E item) {
		return getWeight(item) / (double) totalWeight;
	}
	
	/**
	 * Add a given item with a certain weight to the collection
	 * @param item the item to be added
	 * @param weight the weight of the added item
	 */
	public void add(E item, int weight) {
		totalWeight += weight;
		items.add(new Pair<E, Integer>(item, weight));
	}
	
	/**
	 * Remove a given item from the collection 
	 * @param item the item to be removed
	 */
	public void remove(E item) {
		Pair<E, Integer> itemPair = items.stream().filter(p -> p.getKey().equals(item)).findFirst().orElse(null);
		items.remove(itemPair);
	}
	
	/**
	 * Return a list of the items in this collection
	 * @return the list of items
	 */
	public List<E> getItems() {
		
		List<E> itemList = new ArrayList<>();
		
		for (Pair<E, Integer> pair : items) {
			itemList.add(pair.getKey());
		}
		
		return itemList;
	}
	
	/**
	 * Return the list of weights in this collection
	 * @return the list of weights
	 */
	public List<Integer> getWeights() {
		
		List<Integer> itemList = new ArrayList<>();
		
		for (Pair<E, Integer> pair : items) {
			itemList.add(pair.getValue());
		}
		
		return itemList;
	}
	
	/**
	 * Get a random item based on the weight of all items in the collection.
	 * @return a random item
	 * @note Items with a higher weight are chosen more
	 * @note Total weight of the collection can be higher than 100
	 */
	public E next() {
		
		int randomIndex = -1;
		double random = Math.random() * totalWeight;
		
		for (int i = 0; i < items.size(); i++)
		{
		    random -= items.get(i).getValue();
		    
		    if (random <= 0.0d)
		    {
		        randomIndex = i;
		        break;
		    }
		}
		
		return items.get(randomIndex).getKey();	
	}
}
