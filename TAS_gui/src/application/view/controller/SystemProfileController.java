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
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import application.utility.Convert;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
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
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import profile.SystemProfileValue;
import profile.SystemProfileVariable;
import profile.SystemRequirementType;
import tas.data.systemprofile.SystemProfile;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.mape.planner.RatingType;
import tas.mape.system.entity.SystemEntity;
import tas.mape.system.structure.AbstractSystem;

public class SystemProfileController implements Initializable {

	@FXML
	TextArea profileTextArea;
	
	@FXML
	TextField executionCyclesTextField;
	
	@FXML
	TextField workflowCyclesTextField;
	
	@FXML
	TextField systemClassTextField;
	
	@FXML
	ComboBox<SystemRequirementType> requirementTypeComboBox;
	
	@FXML
	ComboBox<RatingType> ratingTypeComboBox;
	
	@FXML
	ListView<String> variableListView;
	
	@FXML
	TableView<ValueEntry> valueTableView;
	
	@FXML
	TextField dataTextField;
	
	@FXML
	TextField ratioTextField;
	
	@FXML
	Button addValueButton;
	
	@FXML
	Button saveButton1;
	
	@FXML
	Button saveButton2;
	
	private Stage stage;
	private String filePath;
	private ObservableList<ValueEntry> valueData = FXCollections.observableArrayList();
	private SystemProfileVariable currentVariable = null;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeValueTable();
	}

	public void setStage(Stage stage){
		this.stage = stage;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
		initializeXML();
		initializeOther();
	}
	
	@SuppressWarnings("unchecked")
	private void initializeValueTable(){
		
		valueTableView.setEditable(true);
           
		TableColumn<ValueEntry,String> nameColumn = new TableColumn<ValueEntry,String>("Value");
		nameColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("name"));
		nameColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));
			
		TableColumn<ValueEntry,String> typeColumn = new TableColumn<ValueEntry,String>("Type");
		typeColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("type"));
		typeColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));
	        
		TableColumn<ValueEntry,String> valueColumn = new TableColumn<ValueEntry,String>("Data");
		valueColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("data"));
		valueColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));
		valueColumn.setCellFactory(TextFieldTableCell.<ValueEntry>forTableColumn());			
		valueColumn.setOnEditCommit(
				new EventHandler<CellEditEvent<ValueEntry, String>>() {
	                    @Override
	                    public void handle(CellEditEvent<ValueEntry, String> t) {
	                    	ValueEntry value=(ValueEntry) t.getTableView().getItems().get(
	                                t.getTablePosition().getRow());
	                    	
	                    	Object data= value.getRealData(t.getNewValue());
	                    	if(data!=null){
		                    	value.getProfileValue().setData(data);
	                    	}
	                    }
	                }
	        );
		
		TableColumn<ValueEntry,String> ratioColumn = new TableColumn<ValueEntry,String>("Ratio");
		ratioColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("ratio"));
		ratioColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));
		ratioColumn.setCellFactory(TextFieldTableCell.<ValueEntry>forTableColumn());	
		ratioColumn.setOnEditCommit(
				new EventHandler<CellEditEvent<ValueEntry, String>>() {
	                    @Override
	                    public void handle(CellEditEvent<ValueEntry, String> t) {
	                    	ValueEntry value=(ValueEntry) t.getTableView().getItems().get(
	                                t.getTablePosition().getRow());
	                    	double ratio=Double.parseDouble(t.getNewValue());
	                    	value.getProfileValue().setRatio(ratio);
	                    }
	                }
	        );
			
		valueTableView.setItems(valueData);
		valueTableView.getColumns().addAll(nameColumn, typeColumn, valueColumn, ratioColumn);
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
		
		SystemProfile profile = SystemProfileDataHandler.readFromXml(filePath);
		
		UnaryOperator<Change> integerFilter = change -> {
		    String newText = change.getControlNewText();
		    if (newText.matches("-?([1-9][0-9]*)?")) { 
		        return change;
		    }
		    return null;
		};
		
		executionCyclesTextField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
		executionCyclesTextField.setText(profile.getExecutionCycles() + "");
		
		workflowCyclesTextField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
		workflowCyclesTextField.setText(profile.getWorkflowCycles() + "");
		
		systemClassTextField.setText(profile.getSystemClass().getName());
		
		ObservableList<SystemRequirementType> requirementTypes = FXCollections.observableArrayList();
		requirementTypes.addAll(SystemRequirementType.values());
		requirementTypeComboBox.setItems(requirementTypes);
		requirementTypeComboBox.setValue(profile.getRequirementType());
		
		ObservableList<RatingType> ratingTypes = FXCollections.observableArrayList();
		ratingTypes.addAll(RatingType.values());
		ratingTypeComboBox.setItems(ratingTypes);
		ratingTypeComboBox.setValue(profile.getRatingType());
	
		variableListView.getItems().addAll(profile.getVariableNames());
		variableListView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> val, 
                        String oldVal, String newVal) {
                    	
                    	valueData.clear();
                    	
                    	currentVariable=profile.getVariable(newVal);
                    	List<SystemProfileValue> values=currentVariable.getValues();
                    	for(int i=0;i<values.size();i++){
                    		SystemProfileValue value=values.get(i);
                    		valueData.add(new ValueEntry("value "+i,value.getData().getClass().getSimpleName(),
                    				value.getData().toString(),String.valueOf(value.getRatio()),value));
                    	}
                    	
                    	String type=values.get(0).getData().getClass().getSimpleName();
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
                        		SystemProfileValue newValue = new SystemProfileValue(object, value);
                        		currentVariable.getValues().add(newValue);
                        		valueData.add(new ValueEntry("value "+valueData.size(),newValue.getData().getClass().getSimpleName(),
                        				newValue.getData().toString(),String.valueOf(newValue.getRatio()),newValue));
                    		}
                    		
                    		dataTextField.clear();
                    		ratioTextField.clear();
                    	});
                }
            });
		
		saveButton2.setOnAction(event -> {
			
			try {
				@SuppressWarnings("unchecked")
				Class<? extends AbstractSystem<SystemEntity>> systemClass = (Class<? extends AbstractSystem<SystemEntity>>) Class.forName(systemClassTextField.getText());
				profile.setExecutionCycles(Integer.valueOf(executionCyclesTextField.getText()));
				profile.setWorkflowCycles(Integer.valueOf(workflowCyclesTextField.getText()));	
				profile.setSystemClass(systemClass);
				SystemProfileDataHandler.writeToXml(profile, filePath);	
				stage.close();
				
			} catch (ClassNotFoundException e) {
				
	    		Alert fail = new Alert(AlertType.WARNING);
	            fail.setHeaderText("INVALID INFO");
	            fail.setContentText("Invalid system class.");
	            fail.showAndWait();
				
			}
		});
	}
	
	public class ValueEntry {
		private  SimpleStringProperty invocations;
	    private  SimpleStringProperty name;  
	    private  SimpleStringProperty type;
	    private  SimpleStringProperty data;
	    private  SimpleStringProperty ratio;
	    private  SystemProfileValue profileValue;
	    
	    
	    public ValueEntry(String invocations, String name,String type,String data, String ratio, SystemProfileValue profileValue) {
	    	this.invocations = new SimpleStringProperty(invocations);
	    	this.name = new SimpleStringProperty(name);
	    	this.type = new SimpleStringProperty(type);
	    	this.data = new SimpleStringProperty(data);
	    	this.ratio = new SimpleStringProperty(ratio);
	    	this.profileValue = profileValue;
	    }
	    
	    
	    public ValueEntry(String name,String type,String data, String ratio,SystemProfileValue profileValue) {
	    	this.name = new SimpleStringProperty(name);
	    	this.type = new SimpleStringProperty(type);
	    	this.data = new SimpleStringProperty(data);
	    	this.ratio = new SimpleStringProperty(ratio);
	    	this.profileValue = profileValue;
	    }
	    	    
	    public SystemProfileValue getProfileValue() {
	    	return this.profileValue;
	    }
	    
	    public void setRatio(String ratio) {
	    	this.ratio=new SimpleStringProperty(ratio);
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
