package application.view.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.UnaryOperator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import application.utility.Convert;
import application.utility.Convert.DataType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;
import tas.data.inputprofile.InputProfileValue;
import tas.data.inputprofile.InputProfileVariable;
import tas.data.inputprofile.MaxLoadValue;
import tas.data.inputprofile.MaxLoadValue.MaxLoadType;
import tas.data.inputprofile.ProtocolType;
import tas.data.inputprofile.InputProfile;
import tas.data.inputprofile.InputProfileDataHandler;
import tas.data.inputprofile.SystemRequirementType;
import tas.data.inputprofile.SystemType;
import tas.mape.planner.RatingType;
import tas.mape.system.entity.MAPEKSystemEntity;

public class SystemProfileController implements Initializable {

	@FXML
	TextArea profileTextArea;
	
	@FXML
	TextField executionCyclesTextField;
	
	@FXML
	TextField workflowCyclesTextField;
	
	@FXML
	TextField maxProtocolIterations;
	
	@FXML
	ComboBox<RatingType> ratingTypeComboBox;
	
	@FXML
	ComboBox<SystemType> systemTypeComboBox;
	
	@FXML
	ComboBox<ProtocolType> protocolTypeComboBox;
	
	@FXML
	ComboBox<MaxLoadType> maxLoadTypeComboBox;
	
	@FXML
	ComboBox<DataType> dataTypeComboBox;
	
	@FXML
	ListView<String> variableListView;
	
	@FXML
	TableView<ValueEntry> valueTableView;
	
	@FXML
	TextField dataTextField;
	
	@FXML
	TextField ratioTextField;
	
	@FXML
	TextField protocolData;
	
	@FXML
	TextField VariableTextField;
	
	@FXML
	TextField maxLoadValueTextField;
	
	@FXML
	TextField profileNameTextField;
	
	@FXML
	Button addValueButton;
	
	@FXML
	Button saveButton1;
	
	@FXML
	Button saveButton2;
	
	@FXML
	Button AddVariableButton;
	
	@FXML
	Label protocolTypeLabel;
	
	@FXML
	Label protocolIterationsLabel;
	
	@FXML
	Label protocolDataLabel;
	
	@FXML
	Label profileNameLabel;
	
	@FXML
	ListView<BorderPane> entityListView;
	
	private Stage stage;
	private String filePath;
	private ObservableList<ValueEntry> valueData = FXCollections.observableArrayList();
	private InputProfileVariable currentVariable = null;
	private List<String> entityNameList;
	private List<Pair<InputProfileVariable, String>> inputVariables = new ArrayList<>();
	private ApplicationController parent;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeValueTable();
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void setParent(ApplicationController parent) {
		this.parent = parent;
	}
	
	public void setEntityData(List<MAPEKSystemEntity> entities) {
		
		entityNameList = new ArrayList<>();
		
		for (MAPEKSystemEntity entity : entities) {
			entityNameList.add(entity.getEntityName());
		}
	}
	
	public void setFilePath(String filePath) {
		
		if (filePath != null) {
			this.filePath = filePath;
			initializeXML();
		}
		
		initializeOther();
	}
	
	@SuppressWarnings("unchecked")
	private void initializeValueTable() {
		
		valueTableView.setEditable(true);
           
		TableColumn<ValueEntry,String> nameColumn = new TableColumn<ValueEntry,String>("Value");
		nameColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("name"));
		nameColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(5));
		nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");	
		
		TableColumn<ValueEntry,String> typeColumn = new TableColumn<ValueEntry,String>("Type");
		typeColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("type"));
		typeColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(5));
		typeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
	        
		TableColumn<ValueEntry,String> valueColumn = new TableColumn<ValueEntry,String>("Data");
		valueColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("data"));
		valueColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(5));
		valueColumn.setCellFactory(TextFieldTableCell.<ValueEntry>forTableColumn());	
		valueColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		valueColumn.setOnEditCommit(
				new EventHandler<CellEditEvent<ValueEntry, String>>() {
	                    @Override
	                    public void handle(CellEditEvent<ValueEntry, String> t) {
	                    	ValueEntry value = (ValueEntry) t.getTableView().getItems().get(
	                                t.getTablePosition().getRow());
	                    	
	                    	Object data = value.getRealData(t.getNewValue());
	                    	
	                    	if (data != null) {
		                    	value.getProfileValue().setData(data);
	                    	}
	                    	else {
		                    	// Force render update
		            			t.getTableView().getColumns().get(0).setVisible(false);
		                        t.getTableView().getColumns().get(0).setVisible(true);
	                    	}
	                    }
	                }
	        );
		
		TableColumn<ValueEntry,String> ratioColumn = new TableColumn<ValueEntry,String>("Ratio");
		ratioColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("ratio"));
		ratioColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(5));
		ratioColumn.setCellFactory(TextFieldTableCell.<ValueEntry>forTableColumn());
		ratioColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		ratioColumn.setOnEditCommit(
				new EventHandler<CellEditEvent<ValueEntry, String>>() {
	                    @Override
	                    public void handle(CellEditEvent<ValueEntry, String> t) {
	                    	ValueEntry value = (ValueEntry) t.getTableView().getItems().get(
	                                t.getTablePosition().getRow());
	                    	
	                    	double ratio = 0;
	                    	
	                    	try {
	                    		ratio = Double.parseDouble(t.getNewValue());
	                    	} catch (Exception e) {
		                    	// Force render update
		            			t.getTableView().getColumns().get(0).setVisible(false);
		                        t.getTableView().getColumns().get(0).setVisible(true);
	                    	}
	                    	
	                    	value.getProfileValue().setRatio(ratio);
	                    }
	                }
	        );
		
		TableColumn<ValueEntry, String> deleteColumn = new TableColumn<>("");
		deleteColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
		deleteColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(5));
		deleteColumn.setSortable(false);
		
		Callback<TableColumn<ValueEntry, String>, TableCell<ValueEntry, String>> cellFactoryDelete
        =
        new Callback<TableColumn<ValueEntry, String>, TableCell<ValueEntry, String>>() {
		    @Override
		    public TableCell<ValueEntry, String> call(final TableColumn<ValueEntry, String> param) {
		        final TableCell<ValueEntry, String> cell = new TableCell<ValueEntry, String>() {
		
		            final Button btn = new Button("Delete");
		
		            @Override
		            public void updateItem(String item, boolean empty) {
		            	
		            	ValueEntry attribute = (ValueEntry) this.getTableRow().getItem();
		                super.updateItem(item, empty);
		              
		                if (empty) {
		                    setGraphic(null);
		                    setText(null);
		                } 
		                else {
		                    btn.setOnAction(event -> {
		                    	InputProfileValue value = currentVariable.getValues().stream()
		                    			.filter(x -> x.getData().equals(attribute.getRealData(attribute.getData()))).findAny().orElse(null);
		                    	
		                    	currentVariable.getValues().remove(value);
		                    	valueData.remove(getTableRow().getItem());
		                    });
		                    
		                    setGraphic(btn);
		                    setText(null);
		                }
		            }
		        };
		        
		        cell.setAlignment(Pos.CENTER);
		        return cell;
		    }
		};
		
		deleteColumn.setCellFactory(cellFactoryDelete);
			
		valueTableView.setItems(valueData);
		valueTableView.getColumns().addAll(nameColumn, typeColumn, valueColumn, ratioColumn, deleteColumn);
	}
	
	private void initializeXML() {
		try {
			String content = new String(Files.readAllBytes(Paths.get(filePath)));
			
	        Source xmlInput = new StreamSource(new StringReader(content));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", 2);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);	        
			profileTextArea.setText(xmlOutput.getWriter().toString());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void initializeOther() {
			
		stage.setOnCloseRequest(event -> {
		});
		
		saveButton1.setOnAction(event->{
				String newContent = profileTextArea.getText();
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, false)));
					out.write(newContent);
					out.flush();
					out.close();
					stage.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		});
		
		InputProfile prof = null;
		
		if (filePath == null) {
			saveButton1.setDisable(true);
			profileNameLabel.setVisible(true);
			profileNameTextField.setVisible(true);
			saveButton2.setText("Create");
			prof = new InputProfile();
		}
		else {
			prof = InputProfileDataHandler.readFromXml(filePath);
		}
		
		InputProfile profile = prof;
		
		UnaryOperator<Change> integerFilter = change -> {
		    String newText = change.getControlNewText();
		    if (newText.matches("-?([1-9][0-9]*)?") || newText.matches("0")) { 
		        return change;
		    }
		    return null;
		};
		
		executionCyclesTextField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));		
		workflowCyclesTextField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));	
		maxProtocolIterations.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));	
		protocolData.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
		maxLoadValueTextField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
		
		ObservableList<RatingType> ratingTypes = FXCollections.observableArrayList();
		ratingTypes.addAll(RatingType.values());
		ratingTypeComboBox.setItems(ratingTypes);
		
		if (filePath != null) {
			ratingTypeComboBox.setValue(profile.getRatingType());
		}
		
		ratingTypeComboBox.valueProperty().addListener(new ChangeListener<RatingType>() {

			@Override
			public void changed(ObservableValue<? extends RatingType> observable, RatingType oldValue,
					RatingType newValue) {
				if (newValue.equals(RatingType.SCORE)) {
					Alert fail = new Alert(AlertType.INFORMATION);
		            fail.setHeaderText("INFORMATION");
		            fail.setContentText("Some features may not be fully functional when choosing the score rating type. "
		            		+ "It can be further worked out in the source code, it's currently here to show that more than one rating types can be supported."
		            		+ "\n\nThis rating type does not use threshold goals and rates a service combination based on its properties depending on the minimization goals. ");
		            fail.showAndWait();
				}
			}
			});
		
		ObservableList<MaxLoadType> maxLoadTypes = FXCollections.observableArrayList();
		maxLoadTypes.addAll(MaxLoadType.values());
		maxLoadTypeComboBox.setItems(maxLoadTypes);
		maxLoadTypeComboBox.valueProperty().addListener(new ChangeListener<MaxLoadType>() {

			@Override
			public void changed(ObservableValue<? extends MaxLoadType> observable, MaxLoadType oldValue,
					MaxLoadType newValue) {
				
				if (newValue.equals(MaxLoadType.STANDARD)) {
					maxLoadValueTextField.setVisible(false);
				}
				else {
					maxLoadValueTextField.setVisible(true);
				}
			}    
	    });
		
		ObservableList<SystemType> systemTypes = FXCollections.observableArrayList();
		systemTypes.addAll(SystemType.values());
		systemTypeComboBox.setItems(systemTypes);
		systemTypeComboBox.valueProperty().addListener(new ChangeListener<SystemType>() {

			@Override
			public void changed(ObservableValue<? extends SystemType> observable, SystemType oldValue,
					SystemType newValue) {
				
				int entityCount = newValue.getMaxEntities();
				
				if (entityCount > 1) {
					protocolTypeComboBox.setVisible(true);
					maxProtocolIterations.setVisible(true);
					protocolData.setVisible(true);
					protocolTypeLabel.setVisible(false);
					protocolIterationsLabel.setVisible(false);
					protocolDataLabel.setVisible(false);
				}
				else {
					protocolTypeComboBox.setVisible(false);
					maxProtocolIterations.setVisible(false);
					protocolData.setVisible(false);
					protocolTypeLabel.setVisible(true);
					protocolIterationsLabel.setVisible(true);
					protocolDataLabel.setVisible(true);
				}
				
				entityListView.getItems().removeAll(entityListView.getItems());
				
				ObservableList<ProtocolType> protocolTypes = FXCollections.observableArrayList();
				protocolTypes.addAll(ProtocolType.findProtocolTypes(newValue));
				protocolTypeComboBox.setItems(protocolTypes);
				
				for (int i = 0; i < entityCount; i++) {
			
					ComboBox<String> comboBoxEntity = new ComboBox<String>();
					ObservableList<String> entityNames = FXCollections.observableArrayList();
					entityNames.addAll(entityNameList);
					comboBoxEntity.setItems(entityNames);
					comboBoxEntity.setPrefWidth(200);
					
					ComboBox<SystemRequirementType> requirementTypeComboBox = new ComboBox<SystemRequirementType>();
					ObservableList<SystemRequirementType> requirementTypes = FXCollections.observableArrayList();
					requirementTypes.addAll(SystemRequirementType.values());
					requirementTypeComboBox.setItems(requirementTypes);
					
					if (filePath != null) {
						if (i < profile.getSystemType().getMaxEntities()) {
							comboBoxEntity.setValue(profile.getParticipatingEntity(i));
							requirementTypeComboBox.setValue(profile.getEntityRequirementType(profile.getParticipatingEntity(i)));
						}
					}
					
					BorderPane borderPane = new BorderPane();	
					borderPane.setRight(requirementTypeComboBox);
					borderPane.setLeft(comboBoxEntity);
					entityListView.getItems().add(borderPane);
				}
			}    
	    });
			
		if (filePath == null || profile.getSystemType().getMaxEntities() > 1) {
			
			if (filePath != null) {
				protocolTypeComboBox.setValue(profile.getProtocolType());
				maxProtocolIterations.setText(profile.getMaxProtocolIterations() + "");
				protocolData.setText(profile.getMessageContentPercentage() + "");
			}
			
			protocolTypeComboBox.setVisible(true);
			maxProtocolIterations.setVisible(true);
			protocolTypeLabel.setVisible(false);
			protocolIterationsLabel.setVisible(false);
			protocolDataLabel.setVisible(false);
		}
		else {
			protocolTypeComboBox.setVisible(false);
			maxProtocolIterations.setVisible(false);
			protocolData.setVisible(false);
			protocolTypeLabel.setVisible(true);
			protocolIterationsLabel.setVisible(true);
			protocolDataLabel.setVisible(true);
		}
		
		ObservableList<DataType> dataTypes = FXCollections.observableArrayList();
		dataTypes.addAll(DataType.values());
		dataTypeComboBox.setItems(dataTypes);
		dataTypeComboBox.valueProperty().addListener(new ChangeListener<DataType>() {

			@Override
			public void changed(ObservableValue<? extends DataType> observable, DataType oldValue,
					DataType newValue) {
				
            	AddVariableButton.setText("Add Variable");
				
				if (!VariableTextField.getText().trim().isEmpty() && !VariableTextField.getText().startsWith(" ") && dataTypeComboBox.getValue() != null) {
					AddVariableButton.setDisable(false);
				}
				else {
					AddVariableButton.setDisable(true);
				}
			}
	    });
		
		VariableTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			
        	AddVariableButton.setText("Add Variable");
			
			if (!VariableTextField.getText().trim().isEmpty() && !newValue.startsWith(" ") && dataTypeComboBox.getValue() != null) {
				AddVariableButton.setDisable(false);
			}
			else {
				AddVariableButton.setDisable(true);
			}
		});
		
		AddVariableButton.setOnAction(event -> {
			if (AddVariableButton.getText().equals("Add Variable")) {
				variableListView.getItems().add(VariableTextField.getText());
				inputVariables.add(new Pair<>(new InputProfileVariable(VariableTextField.getText()), dataTypeComboBox.getValue().toString()));
				VariableTextField.setText("");
			}
			else if (AddVariableButton.getText().equals("Delete Variable")) {
				String variableName = variableListView.getSelectionModel().getSelectedItem();
				inputVariables.remove(inputVariables.stream().filter(x -> x.getKey().getName().equals(variableName)).findAny().orElse(null));
				variableListView.getItems().remove(variableName);
			}
    	});
		
		
		ObservableList<ProtocolType> protocolTypes = FXCollections.observableArrayList();
		protocolTypeComboBox.setItems(protocolTypes);
	
		variableListView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> val, 
                        String oldVal, String newVal) {
                    	
                    	valueData.clear();
                    	
                    	AddVariableButton.setText("Delete Variable");
                    	AddVariableButton.setDisable(false);
                    	
                    	Pair<InputProfileVariable, String> currentEntry =  inputVariables.stream().filter(x-> x.getKey().getName().equals(newVal)).findAny().orElse(null);
                    	currentVariable = currentEntry.getKey();
                    	List<InputProfileValue> values = currentVariable.getValues();
                    	for(int i=0;i<values.size();i++){
                    		InputProfileValue value=values.get(i);
                    		valueData.add(new ValueEntry("value "+i,value.getData().getClass().getSimpleName(),
                    				value.getData().toString(),String.valueOf(value.getRatio()),value));
                    	}
                    	
                    	String type = currentEntry.getValue();
                    	addValueButton.setDisable(false);
                    	addValueButton.setOnAction(event -> {
                    		
                    		Object object;
                    		Double value;
                    		
                    		try {
                    			object = Convert.toObject(type, dataTextField.getText());
                    			value = Double.parseDouble(ratioTextField.getText());
                    		} catch (Exception e) {
                    			object = null;
                    			value = null;
                    		}	 
                    		
                    		if (object != null && value != null) {
                        		InputProfileValue newValue = new InputProfileValue(object, value);
                        		currentVariable.getValues().add(newValue);
                        		valueData.add(new ValueEntry("value "+valueData.size(),newValue.getData().getClass().getSimpleName(),
                        				newValue.getData().toString(),String.valueOf(newValue.getRatio()),newValue));
                    		}
                    		
                    		dataTextField.clear();
                    		ratioTextField.clear();
                    	});
                }
            });
		
		if (filePath != null) {
			executionCyclesTextField.setText(profile.getExecutionCycles() + "");
			workflowCyclesTextField.setText(profile.getWorkflowCycles() + "");
			maxLoadValueTextField.setText(profile.getMaxloadValue().getValue() + "");
			maxLoadTypeComboBox.setValue(profile.getMaxloadValue().getMaxLoadType());
			systemTypeComboBox.setValue(profile.getSystemType());
			variableListView.getItems().addAll(profile.getVariableNames());
			protocolTypes.addAll(ProtocolType.findProtocolTypes(profile.getSystemType()));
			
			for (InputProfileVariable var : profile.getVariables()) {
				inputVariables.add(new Pair<>(var, var.getValues().get(0).getData().getClass().getSimpleName()));
			}
			
			if (profile.getSystemType().getMaxEntities() != profile.getAmountOfParticipatingEntities()) {
				throw new IllegalStateException("The current selected profile has an illegal XML file. "
						+ "The system type and participating entity count don't match!");
			}
			
			if (profile.getProtocolType() != null && profile.getSystemType().getMaxEntities() > 1 && !ProtocolType.findProtocolTypes(profile.getSystemType()).contains(profile.getProtocolType())) {
				throw new IllegalStateException("The current selected profile has an illegal XML file. "
						+ "The system type and the protocol are not compatible! \n\tsystem type: " + profile.getSystemType() + ", protocol: " + profile.getProtocolType());
			}
		}
		
		saveButton2.setOnAction(event -> {
			
			
			if (saveButton2.getText().equals("Create") && (profileNameTextField.getText().trim().isEmpty() || profileNameTextField.getText().startsWith(" "))) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("The profile name is missing or starts with illegal characters.");
	            fail.showAndWait();
			}		
			else if (Integer.parseInt(executionCyclesTextField.getText()) <= 0) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("The amount of execution cycles must be greater than 0.");
	            fail.showAndWait();
			}
			else if (Integer.parseInt(workflowCyclesTextField.getText()) <= 0) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("The amount of workflow cycles must be greater than 0.");
	            fail.showAndWait();
			}
			else if (Integer.parseInt(maxLoadValueTextField.getText()) < 0) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("The maximum load must be greater or equal to 0.");
	            fail.showAndWait();
			}
			else if (Integer.parseInt(protocolData.getText()) < 0 || Integer.parseInt(protocolData.getText()) > 100) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("The protocol data percentage must be a value from 0 to 100.");
	            fail.showAndWait();
			}
			else if (maxProtocolIterations.isVisible() && Integer.parseInt(maxProtocolIterations.getText()) <= 0) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("The maximum amount of protocol iterations must be greater than 0.");
	            fail.showAndWait();
			}
			else if (!uniqueEntityList(entityListView.getItems())) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("Some participating entities are duplicates.");
	            fail.showAndWait();
			}
			else if (invalidEntities(entityListView.getItems())) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID CONTENT");
	            fail.setContentText("Some participating entity isn't present in the entity list. Choose a valid entity.");
	            fail.showAndWait();
			}
			else if (systemTypeComboBox.getValue() == null) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("MISSING CONTENT");
	            fail.setContentText("No system type selected.");
	            fail.showAndWait();
			}
			else if (ratingTypeComboBox.getValue() == null) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("MISSING CONTENT");
	            fail.setContentText("No rating type selected.");
	            fail.showAndWait();
			}
			else if (maxLoadTypeComboBox.getValue() == null) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("MISSING CONTENT");
	            fail.setContentText("No max load type selected.");
	            fail.showAndWait();
			}
			else if (protocolTypeComboBox.isVisible() && protocolTypeComboBox.getValue() == null) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("MISSING CONTENT");
	            fail.setContentText("No protocol type selected.");
	            fail.showAndWait();
			}
			else if (emptyEntities(entityListView.getItems())) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("MISSING CONTENT");
	            fail.setContentText("Not all participating entities are selected.");
	            fail.showAndWait();
			}	
			else if (emptyRequirements(entityListView.getItems())) {
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("MISSING CONTENT");
	            fail.setContentText("Not all requirements for the participating entities are selected.");
	            fail.showAndWait();
			}
			else {				
				
				profile.clearEntities();
				profile.setExecutionCycles(Integer.valueOf(executionCyclesTextField.getText()));
				profile.setWorkflowCycles(Integer.valueOf(workflowCyclesTextField.getText()));
				profile.setRatingType(ratingTypeComboBox.getValue());
				profile.setSystemType(systemTypeComboBox.getValue());
				profile.removeVariables();
				
				for (Pair<InputProfileVariable, String> pair : inputVariables) {
					profile.addVariable(pair.getKey());
				}
				
				if (maxLoadValueTextField.isVisible()) {
					profile.setmaxLoadValue(new MaxLoadValue(maxLoadTypeComboBox.getValue(), Integer.parseInt(maxLoadValueTextField.getText())));
				}
				else {
					profile.setmaxLoadValue(new MaxLoadValue(maxLoadTypeComboBox.getValue(), 0));
				}
				
				if (protocolTypeComboBox.isVisible()) {
					profile.setProtocolType(protocolTypeComboBox.getValue());
					profile.setMaxProtocolIterations(Integer.valueOf(maxProtocolIterations.getText()));
					profile.setMessageContentPercentage(Integer.valueOf(protocolData.getText()));
				}
				
				for (BorderPane borderPane : entityListView.getItems()) {
					ComboBox<String> entityBox = (ComboBox<String>) borderPane.getLeft();
					ComboBox<SystemRequirementType> requirementBox = (ComboBox<SystemRequirementType>) borderPane.getRight();
					profile.addEntity(entityBox.getValue(), requirementBox.getValue());
				}
				
				if (filePath == null) {
					filePath = ApplicationController.profileFilePath + profileNameTextField.getText() + ".xml";
					InputProfileDataHandler.writeToXml(profile, filePath);
					parent.addSystemProfile(filePath);
				}
				else {
					InputProfileDataHandler.writeToXml(profile, filePath);
				}
				
				stage.close();
			}
		});
	}
	
	private boolean uniqueEntityList(List<BorderPane> list) {
	    Set<String> set = new HashSet<>();

	    for (BorderPane pane: list) {
	    	
	    	@SuppressWarnings("unchecked")
			ComboBox<String> comboBox = (ComboBox<String>) pane.getLeft();
	    	
	        if (!set.add(comboBox.getValue()))
	            return false;
	    }

	    return true;
	}
	
	private boolean invalidEntities(List<BorderPane> list) {
	    for (BorderPane pane: list) {
	    	
	    	@SuppressWarnings("unchecked")
			ComboBox<String> comboBox = (ComboBox<String>) pane.getLeft();
	    	
	        if (!entityNameList.contains(comboBox.getValue()))
	            return true;
	    }

	    return false;
	}
	
	private boolean emptyEntities(List<BorderPane> list) {
	    for (BorderPane pane: list) {
	    	
	    	@SuppressWarnings("unchecked")
			ComboBox<String> comboBox = (ComboBox<String>) pane.getLeft();
	    	
	        if (comboBox.getValue() == null)
	            return true;
	    }

	    return false;
	}
	
	private boolean emptyRequirements(List<BorderPane> list) {
	    for (BorderPane pane: list) {
	    	
	    	@SuppressWarnings("unchecked")
			ComboBox<String> comboBox = (ComboBox<String>) pane.getRight();
	    	
	        if (comboBox.getValue() == null)
	            return true;
	    }

	    return false;
	}
	
	public class ValueEntry {
		private  SimpleStringProperty invocations;
	    private  SimpleStringProperty name;  
	    private  SimpleStringProperty type;
	    private  SimpleStringProperty data;
	    private  SimpleStringProperty ratio;
	    private  InputProfileValue profileValue;
	    
	    
	    public ValueEntry(String invocations, String name,String type,String data, String ratio, InputProfileValue profileValue) {
	    	this.invocations = new SimpleStringProperty(invocations);
	    	this.name = new SimpleStringProperty(name);
	    	this.type = new SimpleStringProperty(type);
	    	this.data = new SimpleStringProperty(data);
	    	this.ratio = new SimpleStringProperty(ratio);
	    	this.profileValue = profileValue;
	    }
	    
	    
	    public ValueEntry(String name,String type,String data, String ratio,InputProfileValue profileValue) {
	    	this.name = new SimpleStringProperty(name);
	    	this.type = new SimpleStringProperty(type);
	    	this.data = new SimpleStringProperty(data);
	    	this.ratio = new SimpleStringProperty(ratio);
	    	this.profileValue = profileValue;
	    }
	    	    
	    public InputProfileValue getProfileValue() {
	    	return this.profileValue;
	    }
	    
	    public void setRatio(String ratio) {
	    	this.ratio = new SimpleStringProperty(ratio);
	    }
	    
	    public Object getRealData(String data) {
	    	Object realValue = Convert.toObject(type.get(), data);
	    	
	    	if (realValue != null) {
	    		this.data = new SimpleStringProperty(data);
	    	}
	    	
	    	return realValue;
	    }
	    
	    public String getInvocations() {
	    	return this.invocations.get();
	    }
	    
	    public void setInvocations(String invocations) {
	    	this.invocations = new SimpleStringProperty(invocations);
	    }
	    
	    public String getName() {
	    	return this.name.get();
	    }
	    
	    public String getType() {
	    	return this.type.get();
	    }
	    
	    public String getData() {
	    	return this.data.get();
	    }
	    
	    public String getRatio() {
	    	return this.ratio.get();
	    }
	}
}
