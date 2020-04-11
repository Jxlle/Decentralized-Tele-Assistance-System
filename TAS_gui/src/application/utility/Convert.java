package application.utility;

public class Convert {

	public static Object toObject(String type, String data){
    	Object realData = null;
    	
		try {
			switch (type) {
			
				case "boolean": 
				case "Boolean":
				case "BOOLEAN":
				{
					if (data.equals("true"))
						realData = true;
					else
						realData = false;
					break;
				}
				
				case "short":
				case "Short":
				case "SHORT":
				{
					realData = Short.parseShort(data);
					break;
				}
				
				case "int": 
				case "INT":
				case "Integer": 
				case "INTEGER":
				{
					realData = Integer.parseInt(data);
					break;
				}
				
				case "long": 
				case "Long": 
				case "LONG":
				{
					realData = Long.parseLong(data);
					break;
				}
				
				case "float": 
				case "Float": 
				case "FLOAT":
				{
					realData = Float.parseFloat(data);
					break;
				}
				
				case "double":
				case "Double": 
				case "DOUBLE":
				{
					realData = Double.parseDouble(data);
					break;
				}
				
				case "string":
				case "String":
				case "STRING":
				{
					realData = data;
					break;
				}
				
				default: {
					System.out.println("Wrong attribute!!!!");
					break;
				}
			}

		} catch (Exception e) {
			return null;
		}
		
		return realData;
	}
	
	public enum DataType {
		BOOLEAN,
		SHORT,
		INTEGER,
		LONG,
		FLOAT,
		DOUBLE,
		STRING;
	}
}
