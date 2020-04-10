package tas.data.inputprofile;

public class MaxLoadValue {

	private MaxLoadType maxLoadType;
	private int value;
	
	public MaxLoadValue() {
		maxLoadType = MaxLoadType.STANDARD;
		value = 0;
	}
	
	public MaxLoadValue(MaxLoadType maxLoadType, int value) {
		this.maxLoadType = maxLoadType;
		this.value = value;
	}
	
	public int getMaxLoad(int amountOfEntities, int workflowCycles) {
		
		switch (maxLoadType) {
		
			case STANDARD:
				return amountOfEntities * workflowCycles;
				
			case CUSTOM_AMOUNT_OF_ENTITIES:
				return value * workflowCycles;
				
			case CUSTOM_MAX_VALUE:
				return value;
			
			default:
				throw new IllegalArgumentException("Unknown max load type used: " + maxLoadType);
		}
	}
	
	public int getValue() {
		return value;
	}
	
	public MaxLoadType getMaxLoadType() {
		return maxLoadType;
	}
	
	public enum MaxLoadType {
		STANDARD,
		CUSTOM_AMOUNT_OF_ENTITIES,
		CUSTOM_MAX_VALUE;
	}
	
}