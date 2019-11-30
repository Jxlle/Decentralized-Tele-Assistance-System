package application.view.controller;

import java.net.URL;
import java.util.ResourceBundle;

import service.messagingservice.MessagingService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConfigureController implements Initializable{

	MessagingService service=MessagingService.getInstance();

	@FXML
	Button okButton;
	
	@FXML
	TextField msgMinDelayTextField;
	
	@FXML
	TextField msgMaxDelayTextField;
	
	@FXML
	TextField msgLossTextField;
	
	@FXML
	TextField timeScaleTextField;
	
	Stage stage;

	public void setStage(Stage stage){
		this.stage=stage;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		msgLossTextField.setText(service.getMessageLoss()+"");
		msgMinDelayTextField.setText(service.getMessageMinDelay()+"");
		msgMaxDelayTextField.setText(service.getMessageMaxDelay()+"");
		
		//timeScaleTextField.setText(Time.scale+"");
		
		okButton.setOnAction(event->{
			service.setMessageDelay(Integer.parseInt(msgMinDelayTextField.getText()), 
					Integer.parseInt(msgMaxDelayTextField.getText()));
			service.setMessageLoss(Integer.parseInt(msgLossTextField.getText()));
			//Time.scale=Integer.parseInt(timeScaleTextField.getText());
			
			stage.close();
		});
	}
	
	
	
}
