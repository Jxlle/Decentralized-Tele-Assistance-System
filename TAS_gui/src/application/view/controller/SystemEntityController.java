package application.view.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SystemEntityController implements Initializable {

	@FXML
	Button workflowBtn;
	
	@FXML
	TableView propertyTable;
	
	@FXML
	ListView registryList;
	
	private Stage stage;
	private String baseDir = "";
	private String workflowPath;
	private String resourceDirPath = baseDir + "resources" + File.separator;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		InitializeData();
	}
	
	public void setStage(Stage dialogStage) {
		this.stage = dialogStage;
	}
	
	// TODO 
	private void InitializeData() {
		
		workflowBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File(resourceDirPath));
				fileChooser.setTitle("Select workflow");
				FileChooser.ExtensionFilter extension = new FileChooser.ExtensionFilter("Add Files(*.txt)", "*.txt");
				fileChooser.getExtensionFilters().add(extension);
				File file = fileChooser.showOpenDialog(stage);
				
				if (file != null) {
					workflowPath = file.getPath();
					workflowBtn.setText(workflowPath);
				}
		    }
		});
	}
}
