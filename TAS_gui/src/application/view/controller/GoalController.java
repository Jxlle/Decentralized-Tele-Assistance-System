package application.view.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalRelation;
import tas.mape.knowledge.Goal.GoalType;

public class GoalController implements Initializable {

	@FXML
	private Label typeLabel;
	
	@FXML
	private Label relationLabel;
	
	@FXML
	private Label valueLabel;
	
	@FXML
	private ComboBox<GoalType> typeBox;
	
	@FXML
	private ComboBox<GoalRelation> relationBox;
	
	@FXML
	private TextField valueField;
	
	@FXML
	private Button addBtn;
	
	private Stage stage;
	private SystemEntityController parent;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setTooltips();
		addGoalTypeOptions();
		addGoalRelationOptions();
		initializeTextfield();
		initializeAddButton();
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void setParent(SystemEntityController parent) {
		this.parent = parent;
	}
	
	private void setTooltips() {
		
		Tooltip typeTooltip = new Tooltip();
		typeTooltip.setText(
		    "Select the type of the goal"
		);
		
		Tooltip relationTooltip = new Tooltip();
		relationTooltip.setText(
		    "Select the relation of the goal"
		);
		
		Tooltip valueTooltip = new Tooltip();
		valueTooltip.setText(
		    "Choose the value the\n"
		    + "relation acts on (DOUBLE)"
		);
		
		typeLabel.setTooltip(typeTooltip);
		relationLabel.setTooltip(relationTooltip);
		valueLabel.setTooltip(valueTooltip);
	}
	
	private void addGoalTypeOptions() {
		ObservableList<GoalType> goalTypes = FXCollections.observableArrayList();
		goalTypes.addAll(GoalType.values());
		typeBox.setItems(goalTypes);
	}
	
	private void addGoalRelationOptions() {
		ObservableList<GoalRelation> goalRelations = FXCollections.observableArrayList();
		goalRelations.addAll(GoalRelation.values());
		relationBox.setItems(goalRelations);
	}
	
	private void initializeTextfield() {
		valueField.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		    	
		    	try {
		    		Double.parseDouble(newValue);
		    	}
		    	catch(Exception e) {
		        	valueField.setText(newValue.replaceAll("[^\\d]", ""));
		    	}
		    }
		});
	}
	
	private void initializeAddButton() {
		
		addBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {
		    	
		    	if (typeBox.getValue() == null) {
		    		Alert fail = new Alert(AlertType.WARNING);
		            fail.setHeaderText("MISSING CONTENT");
		            fail.setContentText("No goal type selected.");
		            fail.showAndWait();
		    	}
		    	else if (relationBox.getValue() == null) {
		    		Alert fail = new Alert(AlertType.WARNING);
		            fail.setHeaderText("MISSING CONTENT");
		            fail.setContentText("No goal relation selected.");
		            fail.showAndWait();
		    	}
		    	else if (valueField.getText().trim().isEmpty()) {
		    		Alert fail = new Alert(AlertType.WARNING);
		            fail.setHeaderText("MISSING CONTENT");
		            fail.setContentText("The goal doesn't have a value assigned to it.");
		            fail.showAndWait();
		    	}
		    	else {
		    		Goal goal = new Goal(typeBox.getValue(), relationBox.getValue(), Double.parseDouble(valueField.getText()));
		    		parent.addGoalToList(goal);
		    		stage.close();
		    	}
		    }
		});
	}
}
