package tas.mape.planner;

/**
 * Enum that represents the type of the rating for a service combination chosen by the analyzer component
 * 
 * @author Jelle Van De Sijpe (jelle.vandesijpe@student.kuleuven.be)
 */
public enum RatingType {
	
	// Available ratings
	SCORE(Double.class),
	CLASS(Integer.class);
	
	// Fields
	private final Class<? extends Comparable<?>> typeClass;
	
	/**
	 * Create a new rating type with a given class representing the type of the rating
	 * @param typeClass class representing the given type of the rating
	 */
	private RatingType(Class<? extends Comparable<?>> typeClass) {
		this.typeClass = typeClass;
	}
	
	/**
	 * Return the type class of this rating type
	 * @return the type class
	 */
	public Class<? extends Comparable<?>> getTypeClass() {
		return typeClass;
	}
	
}
