package tas.mape.planner;

public enum ratingType {

	NUMBER(Double.class),
	CLASS(String.class);
	
	private final Class<?> rateClass;
	
	private ratingType(Class<?> rateClass) {
		this.rateClass = rateClass;
	}
	
	public Class<?> getRateClass() {
		return rateClass;
	}
	
}
