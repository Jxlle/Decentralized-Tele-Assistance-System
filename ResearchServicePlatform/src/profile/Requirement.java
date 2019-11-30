package profile;

import java.util.TreeMap;

public class Requirement {	
	private String name;
	private String type;
	
	TreeMap<Integer,String> values=new TreeMap<>();

	public Requirement(String name, String type, String data){
		this.setName(name);
		this.setType(type);
		this.values.put(0, data);
	}
	
	public void removeValue(int invocations){
		this.values.remove(invocations);
	}
	
	public void addValue(int invocations, String data){
		this.values.put(invocations, data);
	}
	
	public String getName() {
		return name;
	}

	public TreeMap<Integer,String> getValues(){
		return this.values;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
