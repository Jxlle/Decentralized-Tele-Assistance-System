package service.auxiliary;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

/**
 * Class providing a collection where each item in the collection has a weight.
 * This weight determines the chance that this item is randomly taken from the collection.
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
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
	 * Update the weight of the given item 
	 * @param item the given item
	 * @param newWeight the new weight value
	 * @throws IllegalArgumentException throw when the given weight is smaller than zero.
	 */
	public void updateWeight(E item, int newWeight) throws IllegalArgumentException {
		int pairIndex = items.indexOf(items.stream().filter(p -> p.getKey().equals(item)).findFirst().orElse(null));
		
		if (newWeight < 0) {
			throw new IllegalArgumentException("Item weight cannot be smaller than zero!");
		}
		
		totalWeight = totalWeight - items.get(pairIndex).getValue() + newWeight;
		Pair<E, Integer> newPair = new Pair<E, Integer>(item, newWeight);
		items.set(pairIndex, newPair);
	}
	
	/**
	 * Increase the weight of a given item with a given increasement value
	 * @param item the item that needs its weight to increasement
	 * @param increasement the weight increasement
	 * @throws IllegalArgumentException throw when increasement is actually an decreasement						
	 */
	public void increaseWeight(E item, int increasement) throws IllegalArgumentException {
		int pairIndex = items.indexOf(items.stream().filter(p -> p.getKey().equals(item)).findFirst().orElse(null));	
		int newWeight = items.get(pairIndex).getValue() + increasement;
		
		if (increasement < 0) {
			throw new IllegalArgumentException("Illegal item weight increasement! (<0)");
		}
		
		totalWeight += increasement;
		Pair<E, Integer> newPair = new Pair<E, Integer>(item, newWeight);
		items.set(pairIndex, newPair);
	}
	
	/**
	 * Decrease the weight of a given item with a given decreasement value
	 * @param item the item that needs its weight to decrease
	 * @param decreasement the weight decreasement
	 * @throws IllegalArgumentException throw when weight decreasement is too big
	 * @throws IllegalArgumentException	throw when decreasement is actually an increasement
	 */
	public void decreaseWeight(E item, int decreasement) throws IllegalArgumentException {
		int pairIndex = items.indexOf(items.stream().filter(p -> p.getKey().equals(item)).findFirst().orElse(null));	
		int newWeight = items.get(pairIndex).getValue() - decreasement;
		
		if (decreasement > 0) {
			throw new IllegalArgumentException("Illegal item weight increasement! (>0)");
		}
		
		if (newWeight < 0) {
			throw new IllegalArgumentException("Item weight decreasement is too big, new weight is smaller than zero!");
		}
		
		totalWeight -= decreasement;
		Pair<E, Integer> newPair = new Pair<E, Integer>(item, newWeight);
		items.set(pairIndex, newPair);
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
	 * @throws IllegalArgumentException throw when given weight is smaller than zero
	 */
	public void add(E item, int weight) throws IllegalArgumentException {
		
		if (weight < 0) {
			throw new IllegalArgumentException("Item weight cannot be smaller than zero!");
		}
		
		totalWeight += weight;
		items.add(new Pair<E, Integer>(item, weight));
	}
	
	/**
	 * Remove a given item from the collection 
	 * @param item the item to be removed
	 */
	public void remove(E item) {
		Pair<E, Integer> itemPair = items.stream().filter(p -> p.getKey().equals(item)).findFirst().orElse(null);
		totalWeight -= itemPair.getValue();
		items.remove(itemPair);
	}
	
	/**
	 * Return whether a given item exists in the collection
	 * @param item the given item
	 * @return if the given item exists in the collection
	 */
	public boolean itemExists(E item) {
		
		for (Pair<E, Integer> itemPair : items) {
			if (itemPair.getKey().equals(item)) {
				return true;
			}
		}
		
		return false;
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
