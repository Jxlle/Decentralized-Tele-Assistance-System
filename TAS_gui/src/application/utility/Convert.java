package application.utility;

public class Convert {

	public static Object toObject(String type, String data){
    	Object realData = null;
    	
		try {
			switch (type) {
			
				case "boolean": 
				case "Boolean":
				{
					if (realData.equals("true"))
						realData = true;
					else
						realData = false;
					break;
				}
				
				case "short":
				case "Short":
				{
					realData = Short.parseShort(data);
					break;
				}
				
				case "int": 
				case "Integer": 
				{
					realData = Integer.parseInt(data);
					break;
				}
				
				case "long": 
				case "Long": 
				{
					realData = Long.parseLong(data);
					break;
				}
				
				case "float": 
				case "Float": 
				{
					realData = Float.parseFloat(data);
					break;
				}
				
				case "double":
				case "Double": 
				{
					realData = Double.parseDouble(data);
					break;
				}
				
				case "string":
				case "String":
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
}
