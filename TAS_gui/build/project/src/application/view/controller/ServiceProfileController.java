package application.view.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import com.sun.javafx.scene.control.skin.TableHeaderRow;

import application.MainGui;
import service.atomic.AtomicService;
import service.atomic.ServiceProfile;
import service.atomic.ServiceProfileAttribute;
import service.auxiliary.Operation;
import service.auxiliary.ServiceDescription;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ServiceProfileController implements Initializable {

    //@FXML
    //VBox serviceProfileVBox;
	
	@FXML
	ListView<AnchorPane> serviceProfileListView;
    
    @FXML
    TableView<AttributeEntry> serviceProfileTable;
    
    @FXML
    Label nameLabel;
    
    @FXML
    Label typeLabel;
    
    @FXML
    Label endpointLabel;
    
    @FXML
    Label idLabel;
    
    @FXML
    TableView<AttributeEntry> propertyTable;
    
    @FXML
    GridPane descriptionPane;
    
    @FXML
    ListView<String> operationListView;
    
    @FXML
    Accordion accordion;
    
    @FXML
    TitledPane descriptionTitledPane;
    
    @FXML
    TitledPane profileTitledPane;
    
	private Stage stage;
	private ServiceDescription description;
	private AtomicService service;
	private List<ServiceProfile> profiles;
	private List<Class<?>> profileClasses;
	
	ObservableList<AttributeEntry> attributeData = FXCollections.observableArrayList();
	ObservableList<AttributeEntry> propertyData = FXCollections.observableArrayList();
	
	
	private ServiceProfile currentProfile;
	private String currentProfileName;
	private String currentProfileType;

	
	public void setStage(Stage dialogStage){
		this.stage=dialogStage;
	}
	
	public void setServiceProfileClasses(List<Class<?>> serviceProfileClasses){
		this.profileClasses=serviceProfileClasses;
	}

	public void setService(AtomicService service){
		this.service=service;
		//this.serviceName=service.getServiceDescription().getServiceName();
		this.description=service.getServiceDescription();
		this.profiles=service.getServiceProfiles();
		this.setServiceProfiles();
		this.setServiceDescription();
	}
	
	private void setServiceDescription(){
		this.nameLabel.setText(description.getServiceName());
		this.typeLabel.setText(description.getServiceType());
		this.endpointLabel.setText(description.getServiceEndpoint());
		this.idLabel.setText(description.getRegisterID()+"");
		
		ObservableList<String> items =FXCollections.observableArrayList ();
		
		List<Operation> ops=description.getOperationList();
		
		if(ops.size()<6){
			descriptionPane.getRowConstraints().get(4).setMaxHeight(26*(ops.size()));
			//descriptionPane.getRowConstraints().get(4).setPrefHeight(25*(ops.size()));
			operationListView.prefHeightProperty().bind(descriptionPane.heightProperty());
			//operationListView.maxHeightProperty().bind(descriptionPane.heightProperty());
		}

		for(Operation op:ops){
			items.add(op.toString());
		}
		
		operationListView.setItems(items);			
		
		//descriptionPane.getRowConstraints().get(4).setPrefHeight(50);
		//descriptionPane.getRowConstraints().get(4).setMaxHeight(50);
		//operationScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		//operationScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		
		
		Map<String,Object> properties=description.getCustomProperties();
		Iterator<String> iter=properties.keySet().iterator();
		while(iter.hasNext()){
			String name=iter.next();
			Object value=properties.get(name);
			AttributeEntry attribute=new AttributeEntry(name,value.getClass().getSimpleName(),value.toString());
			propertyData.add(attribute);
		}
		
		//accordion.setExpandedPane(descriptionTitledPane);
		descriptionTitledPane.setExpanded(false);
		profileTitledPane.setExpanded(false);
				
		//descriptionTitledPane.expandedProperty().bind(profileTitledPane.expandedProperty().not());
		//profileTitledPane.expandedProperty().bind(descriptionTitledPane.expandedProperty().not());
	}

	public void setServiceProfiles(){
		//System.out.println(this.serviceName);
		
		List<String> availableProfiles=new ArrayList<>();

		//ObservableList<AnchorPane> items =FXCollections.observableArrayList ();
		//serviceProfileListView.setItems(items);

		for(int i=0;i<profiles.size();i++){
			ServiceProfile profile=profiles.get(i);
			//System.out.println(profile);
			addServiceProfile(profile,true);
			availableProfiles.add(profile.getClass().getName());
		}
		
		for(int i=0;i<profileClasses.size();i++){
			try{
				
				if(!availableProfiles.contains(profileClasses.get(i).getName())){
				
				Constructor<?> [] constructors = profileClasses.get(i).getConstructors();
				ServiceProfile profile=null;
								
				for(int j=0;j<constructors.length;j++){
					if(constructors[j].getParameterTypes().length==0){
						profile=(ServiceProfile)constructors[j].newInstance();
						break;
					}
				}
				
				addServiceProfile(profile,false);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	private void addServiceProfile(ServiceProfile profile, boolean enabled){
		
		AnchorPane itemPane = new AnchorPane();
		//itemPane.setPrefHeight(40);
		//itemPane.setMinHeight(40);
		
		Button profileButton=new Button();
		profileButton.setId("profileButton");
		//profileButton.setLayoutY(5);
		profileButton.setText(profile.getClass().getSimpleName());
		profileButton.setOnAction(event->{
			attributeData.clear();
			currentProfile=profile;
						
			for(Field field: profile.getClass().getFields()){			
				if(field.getAnnotation(ServiceProfileAttribute.class)!=null){
					//System.out.println(field.getType());
					//System.out.println(field.getName());
					try {						
						//System.out.println(field.getType().getSimpleName());
						if(field.getType().getSimpleName().equals("TreeMap")){
							//field.getDeclaringClass()
							//HashMap<Integer,Object> value=(HashMap<Integer,Object>)field.get(profile);
							
							
							ParameterizedType pt = (ParameterizedType)profile.getClass().getDeclaredField(field.getName()).getGenericType();
							String type=((Class<?>)pt.getActualTypeArguments()[1]).getSimpleName();
							
							TreeMap<Integer,Object> value=(TreeMap<Integer,Object>)field.get(profile);
							for(Integer key:value.keySet()){
								//System.out.println(key);
								AttributeEntry attribute=new AttributeEntry(String.valueOf(key),field.getName(),type,String.valueOf(value.get(key)));			
								attributeData.add(attribute);
								
								currentProfileName=field.getName();
								currentProfileType=type;
							}
							
						}
						else{
							
						}
						
						
						//System.out.println(field.getType().getName());
						
						
						/*
						if(field.getType().getSimpleName().equals("Attribute")){
							//field.getDeclaringClass()
						}
						else{
							AttributeEntry attribute=new AttributeEntry("null",field.getName(),field.getType().getName(),field.get(profile).toString());			
							attributeData.add(attribute);
						}*/
						
						/*
						AttributeEntry attribute=new AttributeEntry("10",field.getName(),field.getType().getName(),field.get(profile).toString());
						attributeData.add(attribute);					
						attribute=new AttributeEntry("20",field.getName(),field.getType().getName(),field.get(profile).toString());
						attributeData.add(attribute);*/
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		CheckBox checkBox=new CheckBox();
		checkBox.setLayoutY(5);
		checkBox.setSelected(enabled);
		
		if(!enabled)
			profileButton.setDisable(true);
		
		checkBox.selectedProperty().addListener(listener->{
			//System.out.println(index);
			//System.out.println(checkBox.selectedProperty().get());
			boolean selected=checkBox.selectedProperty().get();
			
			if(selected){
				service.addServiceProfile(profile);
				profileButton.setDisable(false);
			}
			else{
				service.removeServiceProfile(profile);
				profileButton.setDisable(true);
			}
		});
		
		
		AnchorPane.setLeftAnchor(checkBox, 10.0);
		AnchorPane.setLeftAnchor(profileButton, 40.0);
		itemPane.getChildren().setAll(checkBox, profileButton);

		serviceProfileListView.getItems().add(itemPane);
	}
	
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		//accordion.getPanes().get(0).setExpanded(true);
		//accordion.getPanes().get(1).setExpanded(false);

		//descriptionTitledPane.setExpanded(true);
		
		this.setServiceProfileTable();
		this.setPropertyTable();
	}
	
	
	private void setServiceProfileTable(){
		
	   serviceProfileTable.setEditable(true);
	     
       Callback<TableColumn<AttributeEntry, String>, TableCell<AttributeEntry, String>> cellFactory =
               new Callback<TableColumn<AttributeEntry, String>, TableCell<AttributeEntry, String>>() {
                   public TableCell<AttributeEntry, String> call(TableColumn<AttributeEntry, String> p) {
                      return new EditingCell();
                   }
       };
       
		TableColumn<AttributeEntry,String> invocationsColumn = new TableColumn<AttributeEntry,String>("Invocations");
		invocationsColumn.setCellValueFactory(new PropertyValueFactory<AttributeEntry, String>("invocations"));
		invocationsColumn.prefWidthProperty().bind(serviceProfileTable.widthProperty().divide(4));
		invocationsColumn.setCellFactory(cellFactory);	
		invocationsColumn.setSortable(false);
		invocationsColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AttributeEntry, String>>() {
                    @Override
                    public void handle(CellEditEvent<AttributeEntry, String> t) {
                    	
                    	AttributeEntry attribute=(AttributeEntry) t.getTableView().getItems().get(
                                t.getTablePosition().getRow());
                    	
                    	Object realValue= attribute.getRealValue(attribute.getValue());
                    	
                    	if(realValue!=null){
                    		            	    	
            	    	for(Field field: currentProfile.getClass().getFields()){			
            				if(field.getAnnotation(ServiceProfileAttribute.class)!=null){                    					
            					if(field.getName().equals(attribute.getName())){
            							try {
											((TreeMap<Integer,Object>)field.get(currentProfile)).remove(Integer.parseInt(t.getOldValue()));
											((TreeMap<Integer,Object>)field.get(currentProfile)).put(Integer.parseInt(t.getNewValue()),realValue);								    
										} catch (Exception e1) {
											e1.printStackTrace();
										}
                					break;
            					}
            				}
            	    	}
            	    	
            	    	
                    	}
                    }
                }
        );
		
		TableColumn<AttributeEntry,String> nameColumn = new TableColumn<AttributeEntry,String>("Name");
		nameColumn.setCellValueFactory(new PropertyValueFactory<AttributeEntry, String>("name"));
		nameColumn.prefWidthProperty().bind(serviceProfileTable.widthProperty().divide(4));
		nameColumn.setSortable(false);
		
		TableColumn<AttributeEntry,String> typeColumn = new TableColumn<AttributeEntry,String>("Type");
		typeColumn.setCellValueFactory(new PropertyValueFactory<AttributeEntry, String>("type"));
		typeColumn.prefWidthProperty().bind(serviceProfileTable.widthProperty().divide(4));
		typeColumn.setSortable(false);
        
		TableColumn<AttributeEntry,String> valueColumn = new TableColumn<AttributeEntry,String>("Value");
		valueColumn.setCellValueFactory(new PropertyValueFactory<AttributeEntry, String>("value"));
		valueColumn.prefWidthProperty().bind(serviceProfileTable.widthProperty().divide(4));
		valueColumn.setCellFactory(cellFactory);	
		valueColumn.setSortable(false);
            
		valueColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<AttributeEntry, String>>() {
                    @Override
                    public void handle(CellEditEvent<AttributeEntry, String> t) {
                    	AttributeEntry attribute=(AttributeEntry) t.getTableView().getItems().get(
                                t.getTablePosition().getRow());
                    	
                    	Object realValue= attribute.getRealValue(t.getNewValue());
                    	
                    	if(realValue!=null){
                    		            	    	
            	    	for(Field field: currentProfile.getClass().getFields()){			
            				if(field.getAnnotation(ServiceProfileAttribute.class)!=null){                    					
            					if(field.getName().equals(attribute.getName())){
            							try {
											((TreeMap<Integer,Object>)field.get(currentProfile)).put(Integer.parseInt(attribute.getInvocations()),realValue);
            							} catch (Exception e1) {
											e1.printStackTrace();
										}
                					break;
            					}
            				}
            	    	}
            	    	
            	    	
                    	}
                    }
                }
        );
		
		serviceProfileTable.setItems(attributeData);
		serviceProfileTable.getColumns().addAll(invocationsColumn,nameColumn,typeColumn,valueColumn);
		
			
		ContextMenu contextMenu = new ContextMenu();
		MenuItem item = new MenuItem("Add new profile");
		item.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent e) {
		    	
				Dialog<Void> dialog = new Dialog<>();
				dialog.setTitle("New Profile");
				
				ButtonType okButtonType = new ButtonType("OK",ButtonData.OK_DONE);
				dialog.getDialogPane().getButtonTypes().setAll(okButtonType);

				GridPane grid = new GridPane();
				grid.setHgap(10);
				grid.setVgap(10);
				grid.setPadding(new Insets(20, 40, 10, 20));

				TextField invocationsField = new TextField();
				invocationsField.setText("");
				invocationsField.textProperty().addListener(new ChangeListener<String>() {
				    @Override 
				    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				        if (!newValue.matches("\\d*")) {
				        	invocationsField.setText(oldValue);
				        }
				    }
				});

				TextField valueField = new TextField();
				valueField.setText("");
				
				grid.add(new Label("Invocations: "), 0, 0);
				grid.add(invocationsField, 1, 0);
				grid.add(new Label("Value: "), 0, 1);
				grid.add(valueField, 1, 1);
				


				dialog.getDialogPane().setContent(grid);

				dialog.setResultConverter(dialogButton -> {
				    if (dialogButton == okButtonType) {
				    	
				    	String invocations=invocationsField.getText();
				    	String value=valueField.getText();
				    	
						AttributeEntry attribute=new AttributeEntry(invocations,currentProfileName,currentProfileType,value);			
						attributeData.add(attribute);

                    	Object realValue= attribute.getRealValue(value);
                    	
                    	if(realValue!=null){
                    		
            	    	for(Field field: currentProfile.getClass().getFields()){			
            				if(field.getAnnotation(ServiceProfileAttribute.class)!=null){                    					
            					if(field.getName().equals(attribute.getName())){
            							try {
											((TreeMap<Integer,Object>)field.get(currentProfile)).put(Integer.parseInt(invocations),realValue);
										} catch (Exception e1) {
											e1.printStackTrace();
										}
                					break;
            					}
            				}
            	    	}
                    	}											
				    }
					return null;
				});
				
				dialog.showAndWait();
		    }
		});
		contextMenu.getItems().add(item);
		serviceProfileTable.setContextMenu(contextMenu);
	}
	
	public void setPropertyTable(){
		propertyTable.setEditable(true);
        
		TableColumn<AttributeEntry,String> nameColumn = new TableColumn<AttributeEntry,String>("Name");
		nameColumn.setCellValueFactory(new PropertyValueFactory<AttributeEntry, String>("name"));
		nameColumn.prefWidthProperty().bind(propertyTable.widthProperty().divide(3));
			
		TableColumn<AttributeEntry,String> typeColumn = new TableColumn<AttributeEntry,String>("Type");
		typeColumn.setCellValueFactory(new PropertyValueFactory<AttributeEntry, String>("type"));
		typeColumn.prefWidthProperty().bind(propertyTable.widthProperty().divide(3));

	    Callback<TableColumn<AttributeEntry, String>, TableCell<AttributeEntry, String>> cellFactory =
	    		new Callback<TableColumn<AttributeEntry, String>, TableCell<AttributeEntry, String>>() {
	            	public TableCell<AttributeEntry, String> call(TableColumn<AttributeEntry, String> p) {
	            		return new EditingCell();
	                }
	    };
	        
		TableColumn<AttributeEntry,String> valueColumn = new TableColumn<AttributeEntry,String>("Value");
		valueColumn.setCellValueFactory(new PropertyValueFactory<AttributeEntry, String>("value"));
		valueColumn.prefWidthProperty().bind(propertyTable.widthProperty().divide(3));
		valueColumn.setCellFactory(cellFactory);	
					            
		valueColumn.setOnEditCommit(
				new EventHandler<CellEditEvent<AttributeEntry, String>>() {
					@Override
	                public void handle(CellEditEvent<AttributeEntry, String> t) {
						AttributeEntry attribute=(AttributeEntry) t.getTableView().getItems().get(
	                                t.getTablePosition().getRow());
						
						Object realValue=attribute.getRealValue(t.getNewValue());
						description.getCustomProperties().put(attribute.getName(), realValue);
						
	                    }
	            }
	   );
		
		ContextMenu contextMenu = new ContextMenu();
		MenuItem item = new MenuItem("Add new property");
		item.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent e) {
		        //System.out.println("Add");
		    	
				try{
					
				    FXMLLoader loader = new FXMLLoader();
				    loader.setLocation(MainGui.class.getResource("view/propertyDialog.fxml"));
				    AnchorPane propertyPane = (AnchorPane) loader.load();

				    Stage dialogStage = new Stage();
				    dialogStage.setTitle("Add New Property");
				    
					PropertyController controller=(PropertyController)loader.getController();
					controller.setStage(dialogStage);
					controller.setServiceDescription(description);
					//controller.setServiceProfileClasses(tasStart.getServiceProfileClasses());
					//controller.setService(tasStart.getService(serviceName));

				    Scene dialogScene = new Scene(propertyPane);
				    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());

				    dialogStage.initOwner(stage);
				    dialogStage.setScene(dialogScene);
				    dialogStage.setResizable(false);
				    dialogStage.showAndWait();
				    
				    if(controller.isClicked()){
				    	propertyData.clear();
						Map<String,Object> properties=description.getCustomProperties();
						Iterator<String> iter=properties.keySet().iterator();
						while(iter.hasNext()){
							String name=iter.next();
							Object value=properties.get(name);
							AttributeEntry attribute=new AttributeEntry(name,value.getClass().getSimpleName(),value.toString());
							propertyData.add(attribute);
						}
				    }
				    
					}
				
					catch(Exception exception){
						exception.printStackTrace();
					}
		    }
		});
		contextMenu.getItems().add(item);
		propertyTable.setContextMenu(contextMenu);
			
		propertyTable.setItems(propertyData);
		propertyTable.getColumns().addAll(nameColumn,typeColumn,valueColumn);
	}
	
	
	public class AttributeEntry{
		private  SimpleStringProperty invocations;
	    private  SimpleStringProperty name;
	    private  SimpleStringProperty type;
	    private  SimpleStringProperty value;
	    
	    public AttributeEntry(String name,String type,String value){
	    	this.name=new SimpleStringProperty(name);
	    	this.type=new SimpleStringProperty(type);
	    	this.value=new SimpleStringProperty(value);
	    }
	    	
	    public AttributeEntry(String invocations,String name,String type,String value){
	    	this.invocations=new SimpleStringProperty(invocations);
	    	this.name=new SimpleStringProperty(name);
	    	this.type=new SimpleStringProperty(type);
	    	this.value=new SimpleStringProperty(value);
	    }
	    
	    public void setInvocations(String invocations){
	    	this.invocations=new SimpleStringProperty(invocations);
	    }
	    
	    public String getInvocations(){
	    	return this.invocations.get();
	    }
	    
	    public void setName(String name){
	    	this.name=new SimpleStringProperty(name);
	    }
	    
	    public void setType(String type){
	    	this.type=new SimpleStringProperty(type);
	    }
	    
	    public Object getRealValue(String value){
	    	this.value=new SimpleStringProperty(value);
	    	Object realValue=null;
			try {
				switch (type.get()) {
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
			return realValue;
	    }
	    
	    public String getName(){
	    	return this.name.get();
	    }
	    
	    public String getType(){
	    	return this.type.get();
	    }
	    
	    public String getValue(){
	    	return this.value.get();
	    }

	}
	
	
	  class EditingCell extends TableCell<AttributeEntry, String> {
		  
	        private TextField textField;
	 
	        public EditingCell() {
	        }
	 
	        @Override
	        public void startEdit() {
	            if (!isEmpty()) {
	                super.startEdit();
	                createTextField();
	                setText(null);
	                setGraphic(textField);
	                textField.selectAll();
	            }
	        }
	 
	        @Override
	        public void cancelEdit() {
	            super.cancelEdit();
	 
	            setText((String) getItem());
	            setGraphic(null);
	        }
	 
	        @Override
	        public void updateItem(String item, boolean empty) {
	            super.updateItem(item, empty);
	 
	            if (empty) {
	                setText(null);
	                setGraphic(null);
	            } else {
	                if (isEditing()) {
	                    if (textField != null) {
	                        textField.setText(getString());
	                    }
	                    setText(null);
	                    setGraphic(textField);
	                } else {
	                    setText(getString());
	                    setGraphic(null);
	                }
	            }
	        }
	 
	        /*
	        private void createTextField() {
	            textField = new TextField(getString());
	            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);

	            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
	                @Override
	                public void changed(ObservableValue<? extends Boolean> arg0, 
	                    Boolean arg1, Boolean arg2) {
	                        if (!arg2) {
	                            commitEdit(textField.getText());
	                        }
	                }
	            });
	            
	        }*/
	        
	        private void createTextField() {
	            textField = new TextField(getItem());
	            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

	            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
	                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	                    if(!newValue.booleanValue())
	                        commitEdit(textField.getText());
	                }
	            } );
	            textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
	                @Override public void handle(KeyEvent t) {
	                    if (t.getCode() == KeyCode.ENTER) {
	                        commitEdit(textField.getText());
	                    } else if (t.getCode() == KeyCode.ESCAPE) {
	                        cancelEdit();
	                    }
	                }
	            });
	        }
	 
	        private String getString() {
	            return getItem() == null ? "" : getItem().toString();
	        }
	    }
}

