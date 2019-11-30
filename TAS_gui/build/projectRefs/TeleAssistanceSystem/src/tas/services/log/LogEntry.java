package tas.services.log;

import javafx.beans.property.SimpleStringProperty;

public class LogEntry {

	 private SimpleStringProperty time;
	 private SimpleStringProperty title;
	 private SimpleStringProperty message;
	 
	 public LogEntry(String time,String title,String message){
	    	this.time=new SimpleStringProperty(time);
	    	this.title=new SimpleStringProperty(title);
	    	this.message=new SimpleStringProperty(message);
	 }

	public String getTime() {
		return time.get();
	}

	public void setTime(String time) {
		this.time = new SimpleStringProperty(time);
	}

	public String getTitle() {
		return title.get();
	}

	public void setTitle(String title) {
		this.title = new SimpleStringProperty(title);
	}

	public String getMessage() {
		return message.get();
	}

	public void setMessage(String message) {
		this.message = new SimpleStringProperty(message);
	}
}
