package application.view.controller;

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
import java.util.TreeMap;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import application.utility.Convert;
import profile.InputProfile;
import profile.SystemProfileValue;
import profile.SystemProfileVariable;
import profile.ProfileExecutor;
import profile.Requirement;


public class InputProfileController implements Initializable {

	private Stage stage;
	
	@FXML
	TextArea profileTextArea;
	
	@FXML
	TextField maxStepsTextField;
	
	@FXML
	Label qosRequirementLabel;
	
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
	
	@FXML
	TableView<ValueEntry> reqTableView;
	
	@FXML
	ListView<String> reqListView;
	
	@FXML
	TextField reqInvocationsTextField;
	
	@FXML
	TextField reqDataTextField;
	
	@FXML
	Button addReqValueButton;
	
	ObservableList<ValueEntry> valueData = FXCollections.observableArrayList();
	ObservableList<ValueEntry> reqData = FXCollections.observableArrayList();
	SystemProfileVariable currentVariable=null;
	Requirement currentReq=null;

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeValueTable();
		initializeReqTable();
	}

	public void setStage(Stage stage){
		this.stage=stage;
	}
	

	@SuppressWarnings("unchecked")
	private void initializeReqTable(){
		reqTableView.setEditable(true);
		
		TableColumn<ValueEntry,String> invocationsColumn = new TableColumn<ValueEntry,String>("Invocations");
		invocationsColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("invocations"));
		invocationsColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));
		invocationsColumn.setCellFactory(TextFieldTableCell.<ValueEntry>forTableColumn());		
		invocationsColumn.setOnEditCommit(
				new EventHandler<CellEditEvent<ValueEntry, String>>() {
	                    @Override
	                    public void handle(CellEditEvent<ValueEntry, String> t) {
	                    	ValueEntry value=(ValueEntry) t.getTableView().getItems().get(
	                                t.getTablePosition().getRow());
	                    	
	                    	String invocations= t.getNewValue();
	                    	String preInvocations=t.getOldValue();
	                    	String data=value.getData();
	                    	if(invocations!=null){
	                    		currentReq.removeValue(Integer.parseInt(preInvocations));
	                    		currentReq.addValue(Integer.parseInt(invocations), data);
	                    	}
	                    }
	                }
	        );
		
		TableColumn<ValueEntry,String> nameColumn = new TableColumn<ValueEntry,String>("Value");
		nameColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("name"));
		nameColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));
			
		TableColumn<ValueEntry,String> typeColumn = new TableColumn<ValueEntry,String>("Type");
		typeColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("type"));
		typeColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));

	        
		TableColumn<ValueEntry,String> dataColumn = new TableColumn<ValueEntry,String>("Data");
		dataColumn.setCellValueFactory(new PropertyValueFactory<ValueEntry, String>("data"));
		dataColumn.prefWidthProperty().bind(valueTableView.widthProperty().divide(4));
		dataColumn.setCellFactory(TextFieldTableCell.<ValueEntry>forTableColumn());		

		
		dataColumn.setOnEditCommit(
				new EventHandler<CellEditEvent<ValueEntry, String>>() {
	                    @Override
	                    public void handle(CellEditEvent<ValueEntry, String> t) {
	                    	ValueEntry value=(ValueEntry) t.getTableView().getItems().get(
	                                t.getTablePosition().getRow());
	                    	
	                    	String data= t.getNewValue();
	                    	int invocations=Integer.parseInt(value.getInvocations());
	                    	if(data!=null){
	                    		currentReq.addValue(invocations, data);
	                    	}
	                    }
	                }
	        );
			
		reqTableView.setItems(reqData);
		reqTableView.getColumns().addAll(invocationsColumn,nameColumn,typeColumn,dataColumn);
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
	                    	//attribute.setValue(t.getNewValue());
	                    	
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
		valueTableView.getColumns().addAll(nameColumn,typeColumn,valueColumn,ratioColumn);
	}

	public void viewProfile(String filePath){
		try{
			String content = new String(Files.readAllBytes(Paths.get(filePath)));
			
	        Source xmlInput = new StreamSource(new StringReader(content));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", 5);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);	        
			profileTextArea.setText(xmlOutput.getWriter().toString());
			
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
			
			ProfileExecutor.readFromXml(filePath, "test");
			
			InputProfile profile= ProfileExecutor.profiles.get("test");
			maxStepsTextField.setText(profile.getMaxSteps()+"");
			maxStepsTextField.textProperty().addListener(new ChangeListener<String>() {
			    @Override
			    public void changed(ObservableValue<? extends String> observable,
			            String oldValue, String newValue) {
			    	profile.setMaxSteps(Integer.parseInt(newValue));
			    }
			});
			
			qosRequirementLabel.setText(profile.getQosRequirement());

			
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
	                    	addValueButton.setOnAction(event->{
	                    		SystemProfileValue newValue=new SystemProfileValue(Convert.toObject(type, dataTextField.getText()),
	                    				Double.parseDouble(ratioTextField.getText()));
	                    		currentVariable.getValues().add(newValue);
	                    		valueData.add(new ValueEntry("value "+valueData.size(),newValue.getData().getClass().getSimpleName(),
	                    				newValue.getData().toString(),String.valueOf(newValue.getRatio()),newValue));
	                    		
	                    		dataTextField.clear();
	                    		ratioTextField.clear();
	                    		
	                    	});
	                    	
	                }
	            });
			
			
			reqListView.getItems().addAll(profile.getRequirementNames());
			reqListView.getSelectionModel().selectedItemProperty().addListener(
	                new ChangeListener<String>() {
	                    public void changed(ObservableValue<? extends String> val, 
	                        String oldVal, String newVal) {
	                    	reqData.clear();               	
	                    	currentReq=profile.getRequirement(newVal);
	                    	String name=currentReq.getName();
	                    	String type=currentReq.getType();
	                    	TreeMap<Integer,String> values=currentReq.getValues();
	                    	for(int invocations:values.keySet()){
		                    	reqData.add(new ValueEntry(String.valueOf(invocations),name,type, 
		                    				values.get(invocations), null, null));
	                    	}
	                    	
	                    	addReqValueButton.setDisable(false);
	                    	addReqValueButton.setOnAction(event->{
	                    		String invocations=reqInvocationsTextField.getText();
	                    		String data=reqDataTextField.getText();
		                    	reqData.add(new ValueEntry(invocations,currentReq.getName(),currentReq.getType(), 
		                    			data, null, null));
		                    	currentReq.addValue(Integer.parseInt(invocations), data);
	                    		reqInvocationsTextField.clear();
	                    		reqDataTextField.clear();
	                    	});
	                    	
	                }
	            });
			
			
			
			
			saveButton2.setOnAction(event->{
				ProfileExecutor.writeToXml(filePath, "test");
				stage.close();
			});
			
			
			ContextMenu contextMenu = new ContextMenu();
			MenuItem item = new MenuItem("Add new Requirement");
			
			item.setOnAction(new EventHandler<ActionEvent>() {
			    public void handle(ActionEvent e) {
			    	
					Dialog<Void> dialog = new Dialog<>();
					dialog.setTitle("New Requirement");
					
					ButtonType okButtonType = new ButtonType("OK",ButtonData.OK_DONE);
					dialog.getDialogPane().getButtonTypes().setAll(okButtonType);

					GridPane grid = new GridPane();
					grid.setHgap(10);
					grid.setVgap(10);
					grid.setPadding(new Insets(20, 40, 10, 20));

					TextField nameField = new TextField();
					nameField.setText("");
					
					ChoiceBox<String> typeBox=new ChoiceBox<>();
					typeBox.getItems().addAll("Threshold","Min/Max");
					typeBox.getSelectionModel().select(0);

					TextField dataField = new TextField();
					dataField.setText("");

					grid.add(new Label("Name: "), 0, 0);
					grid.add(nameField, 1, 0);
					grid.add(new Label("Type: "), 0, 1);
					grid.add(typeBox, 1, 1);
					grid.add(new Label("Data: "), 0, 2);
					grid.add(dataField, 1, 2);
					

					dialog.getDialogPane().setContent(grid);
					dialog.setResultConverter(dialogButton -> {
					    if (dialogButton == okButtonType) {
					    	
					    	String name=nameField.getText();
					    	String type=typeBox.getValue();
					    	String data=dataField.getText();
					    	
					    	//ValueEntry entry=new ValueEntry(name,type,data,null,null);			
							//reqData.add(entry);
					    	
					    	profile.addRequirement(new Requirement(name,type,data));
					    	reqListView.getItems().add(name);
							                	
					    }
						return null;
					});
					
					dialog.showAndWait();
			    }
			});
			contextMenu.getItems().add(item);
			reqListView.setContextMenu(contextMenu);
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
	}
	
	
	public class ValueEntry{
		private  SimpleStringProperty invocations;
	    private  SimpleStringProperty name;  
	    private  SimpleStringProperty type;
	    private  SimpleStringProperty data;
	    private  SimpleStringProperty ratio;
	    private  SystemProfileValue profileValue;
	    
	    
	    public ValueEntry(String invocations, String name,String type,String data, String ratio,SystemProfileValue profileValue){
	    	this.invocations=new SimpleStringProperty(invocations);
	    	this.name=new SimpleStringProperty(name);
	    	this.type=new SimpleStringProperty(type);
	    	this.data=new SimpleStringProperty(data);
	    	this.ratio=new SimpleStringProperty(ratio);
	    	this.profileValue=profileValue;
	    }
	    
	    
	    public ValueEntry(String name,String type,String data, String ratio,SystemProfileValue profileValue){
	    	this.name=new SimpleStringProperty(name);
	    	this.type=new SimpleStringProperty(type);
	    	this.data=new SimpleStringProperty(data);
	    	this.ratio=new SimpleStringProperty(ratio);
	    	this.profileValue=profileValue;
	    }
	    	    
	    public SystemProfileValue getProfileValue(){
	    	return this.profileValue;
	    }
	    
	    public void setRatio(String ratio){
	    	this.ratio=new SimpleStringProperty(ratio);
	    }
	    
	    public Object getRealData(String data){
	    	this.data=new SimpleStringProperty(data);
	    	Object realData=null;
			try {
				switch (type.get()) {
				case "boolean": 
				case "Boolean":
				{
					if (data.equals("true"))
						realData= true;
					else
						realData= false;
					break;
				}
				case "short":
				case "Short":
				{
					realData= Short.parseShort(data);
					break;
				}
				case "int": 
				case "Integer": 
				{
					realData= Integer.parseInt(data);
					break;
				}
				case "long": 
				case "Long": 
				{
					realData= Long.parseLong(data);
					break;
				}
				case "float": 
				case "Float": 
				{
					realData= Float.parseFloat(data);
					break;
				}
				case "double":
				case "Double": 
				{
					realData= Double.parseDouble(data);
					break;
				}
				default: {
					System.out.println("Wrong attribute!!!!");
					realData=data;
					break;
				}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}			
			return realData;
	    }
	    
	    public String getInvocations(){
	    	return this.invocations.get();
	    }
	    
	    public void setInvocations(String invocations){
	    	this.invocations=new SimpleStringProperty(invocations);
	    }
	    
	    public String getName(){
	    	return this.name.get();
	    }
	    
	    public String getType(){
	    	return this.type.get();
	    }
	    
	    public String getData(){
	    	return this.data.get();
	    }
	    
	    public String getRatio(){
	    	return this.ratio.get();
	    }
	}
}
