package application.view.controller;

import java.net.URL;
import java.util.ResourceBundle;

import service.auxiliary.ServiceDescription;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PropertyController implements Initializable{

	@FXML
	TextField nameTextField;
	
	@FXML
	ChoiceBox<String> typeChoiceBox;
	
	@FXML
	TextField valueTextField;
	
	@FXML
	Button okButton;
	
	Stage stage;
	
	ServiceDescription description;
	
	private boolean isClicked=false;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		typeChoiceBox.setItems(FXCollections.observableArrayList(
				"boolean","Boolean", "short","Short",
				"int", "Integer", "long", "Long", 
				"float","Float", "double", "Double"
			   )
		);
		typeChoiceBox.getSelectionModel().select("double");
		
		okButton.setOnAction(event->{
			if(!nameTextField.getText().isEmpty() && !valueTextField.getText().isEmpty()
					&& !typeChoiceBox.getValue().isEmpty()){
				
				Object realValue=null;
				String type=typeChoiceBox.getValue();
				String value=valueTextField.getText();
				String name=nameTextField.getText();
				
				try {
					switch (type) {
					case "boolean": 
					case "Boolean":
					{
						if (value.equals("true"))
							realValue= true;
						else
							realValue= false;
						break;
					}
					case "short":
					case "Short":
					{
						realValue= Short.parseShort(value);
						break;
					}
					case "int": 
					case "Integer": 
					{
						realValue= Integer.parseInt(value);
						break;
					}
					case "long": 
					case "Long": 
					{
						realValue= Long.parseLong(value);
						break;
					}
					case "float": 
					case "Float": 
					{
						realValue= Float.parseFloat(value);
						break;
					}
					case "double":
					case "Double": 
					{
						realValue= Double.parseDouble(value);
						break;
					}
					default: {
						System.out.println("Wrong attribute!!!!");
						break;
					}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(realValue!=null)
					description.getCustomProperties().put(name, realValue);
				
			}
			isClicked=true;
			stage.close();
		});
	}
	
	public boolean isClicked(){
		return this.isClicked;
	}
	
	public void setStage(Stage stage){
		this.stage=stage;
	}
	
	public void setServiceDescription(ServiceDescription description){
		this.description=description;
	}

}
