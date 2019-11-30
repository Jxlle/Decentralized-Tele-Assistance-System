package profile;

public class Requirement {
	
	private String name;
	private String type;
	private String data;
	
	public Requirement(String name, String type, String data){
		this.setName(name);
		this.setType(type);
		this.setData(data);
	}

	public String getName() {
		return name;
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
