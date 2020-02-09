package application.view.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.MainGui;
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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import service.registry.ServiceRegistry;
import tas.mape.knowledge.Goal;
import tas.mape.planner.RatingType;
import tas.mape.system.entity.SystemServiceInfo;

public class SystemEntityController implements Initializable {

	@FXML
	private Button workflowBtn;
	
	@FXML
	private Button goalBtn;
	
	@FXML
	private Button addBtn;
	
	@FXML
	private TableView<SystemEntityPropertyEntry> propertyTable;
	
	@FXML
	private ListView<AnchorPane> registryList;
	
	@FXML
	private TableView<GoalEntry> goalList;
	
	@FXML
	private TableColumn<SystemEntityPropertyEntry, String> nameColumn;
	
	@FXML
	private TableColumn<SystemEntityPropertyEntry, String> typeColumn;
	
	@FXML
	private TableColumn<SystemEntityPropertyEntry, String> valueColumn;
	
	@FXML
	private TableColumn<GoalEntry, String> goalTypeColumn;
	
	@FXML
	private TableColumn<GoalEntry, String> goalRelationColumn;
	
	@FXML
	private TableColumn<GoalEntry, String> goalValueColumn;
	
	@FXML
	private TableColumn<GoalEntry, String> deleteColumn;
	
	@FXML
	private ComboBox<RatingType> ratingTypeBox;
	
	@FXML
	private Label usedServiceRegistriesLabel;
	
	@FXML
	private Label workflowLabel;
	
	@FXML
	private Label goalsLabel;
	
	@FXML
	private Label propertiesLabel;
	
	@FXML
	private Label ratingTypeLabel;
	
	@FXML
	private Label reqStratLabel;
	
	private String baseDir = "";
	private String workflowPath;
	private String resourceDirPath = baseDir + "resources" + File.separator;
	private Stage stage;
	private GoalController controller;
	private SystemEntityPropertyEntry entityName;
	private SystemEntityController self;
	private List<ServiceRegistry> entityRegistries;
	private List<Goal> goals = new ArrayList<>();
	
	private ObservableList<SystemEntityPropertyEntry> entityData = FXCollections.observableArrayList();
	private ObservableList<GoalEntry> goalData = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setTooltips();
		addRatingTypeOptions();
		addPropertyTableEntries();
		initializeWorkflowButton();
		initializeGoalStuff();
		initializeAddButton();
		
		self = this;
	}
	
	public void setStage(Stage dialogStage) {
		this.stage = dialogStage;
	}
	
	public void addRegistryChoices(SystemServiceInfo serviceInfo) {
		
		entityRegistries = new ArrayList<>();
		List<ServiceRegistry> registries = serviceInfo.getRegistries();
		AnchorPane registryPane;
		Label registryLabel;
		
		for (ServiceRegistry registry : registries) {
			registryPane = new AnchorPane();
			
			registryLabel = new Label();
			registryLabel.setText(registry.getServiceDescription().getServiceName() 
					+ " (" + registry.getServiceDescription().getServiceEndpoint() + ")");
			AnchorPane.setLeftAnchor(registryLabel, 10.0);
			
			CheckBox registryBox = new CheckBox();
			AnchorPane.setRightAnchor(registryBox, 10.0);
			
			registryBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			        if (registryBox.isSelected()) {
			        	entityRegistries.add(registry);
			        }
			        else {
			        	entityRegistries.remove(registry);
			        }
			    }
			});
			
			registryPane.getChildren().addAll(registryLabel, registryBox);
			registryList.getItems().add(registryPane);
		}
	}
	
	public void addGoalToList(Goal goal) {
		goals.add(goal);
		goalData.add(new GoalEntry(goal));
		goalList.setItems(goalData);
	}
	
	private void setTooltips() {
		
		Tooltip workflowTooltip = new Tooltip();
		workflowTooltip.setText(
		    "Select which workflow (.txt)\n" +
		    "this system entity uses"
		);
		
		Tooltip ratingTypeTooltip = new Tooltip();
		ratingTypeTooltip.setText(
		    "Select which rating type\n" +
		    "this system entity uses"
		);
		
		Tooltip registryTooltip = new Tooltip();
		registryTooltip.setText(
		    "Select which service registries\n" +
		    "this system entity uses\n"
		);
		
		Tooltip goalTooltip = new Tooltip();
		goalTooltip.setText(
		    "(OPTIONAL)\n" +
		    "Add system goals"
		);
		
		Tooltip propertyTooltip = new Tooltip();
		propertyTooltip.setText(
		    "Choose the system entity properties by filling in the table with the right type.\n\n"
		    + "A system entity currently consists of a workflow executor and a MAPEK-component.\n"
		    + "The MAPEK-component is the managing system that chooses which services from the \n"
		    + "available service registries are available to the workflow executor.\n"
		    + "The default service profile increases the failure rate if the service has a bigger load,\n"
		    + "but the managing system only knows the default failure rate, not the increased rates if the load gets bigger.\n\n"
		    + "The managing system looks at the best service combinations depending on the current requirement.\n"
		    + "It does this by holding an approximated failure rate table where it approximates what the failure rate of a\n"
		    + "service will be at a certain load."
		);
		
		workflowLabel.setTooltip(workflowTooltip);
		ratingTypeLabel.setTooltip(ratingTypeTooltip);
		usedServiceRegistriesLabel.setTooltip(registryTooltip);
		goalsLabel.setTooltip(goalTooltip);
		propertiesLabel.setTooltip(propertyTooltip);
	}
	
	private void addPropertyTableEntries() {
				
		nameColumn.setCellValueFactory(new PropertyValueFactory<SystemEntityPropertyEntry, String>("name"));
		typeColumn.setCellValueFactory(new PropertyValueFactory<SystemEntityPropertyEntry, String>("type"));
		
		valueColumn.setCellValueFactory(new PropertyValueFactory<SystemEntityPropertyEntry, String>("value"));
		valueColumn.setCellFactory(TextFieldTableCell.<SystemEntityPropertyEntry>forTableColumn());
		valueColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<SystemEntityPropertyEntry, String>>() {
                    @Override
                    public void handle(CellEditEvent<SystemEntityPropertyEntry, String> t) {
                    	SystemEntityPropertyEntry attribute = (SystemEntityPropertyEntry) t.getTableView().getItems().get(
                                t.getTablePosition().getRow());
                    	
                    		Object realValue = attribute.getRealValue(t.getNewValue());
                    		
                    		if (realValue == null) {                  			
                    			// Force render update
                    			t.getTableView().getColumns().get(0).setVisible(false);
                                t.getTableView().getColumns().get(0).setVisible(true);
                    		}
                    }
                }
        );
		
		entityData.add(new SystemEntityPropertyEntry("Planner Endpoint", "String", ""));
		entityName = new SystemEntityPropertyEntry("Entity Name", "String", "");
		entityData.add(new SystemEntityPropertyEntry("Load Failure Delta", "Integer", ""));
		entityData.add(new SystemEntityPropertyEntry("Combination Limit", "Integer", ""));
		entityData.add(new SystemEntityPropertyEntry("Min Failure Delta", "Double", ""));
		entityData.add(new SystemEntityPropertyEntry("Failure Change", "Double", ""));
		entityData.add(entityName);
		propertyTable.setItems(entityData);
		propertyTable.setEditable(true);
		propertyTable.setRowFactory(r -> new TableRow<SystemEntityPropertyEntry>() {	
            private Tooltip tooltip = new Tooltip();
            
            @Override
            public void updateItem(SystemEntityPropertyEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                
                if (entry == null) {
                    setTooltip(null);
                } 
                else {
                	switch(entry.getName()) {
                		case "Planner Endpoint":
                			tooltip.setText("The unique planner component identifier\n"
                					+ "used for communication");
                			break;
                			
                		case "Entity Name":
                			tooltip.setText("The unique system entity name");
                			break;
                			
                		case "Load Failure Delta":
                			tooltip.setText("[A parameter of the knowledge component]\n"
                					+ "The difference between two load values (keys) in the approximated failure table.\n"
                					+ "A higher value means less information about approximated failure rates.\n\n"
                					+ "Choose a value that is around the same difference between two load values\n"
                					+ "in the service failure load profile table for the services.");
                			break;
                			
                		case "Combination Limit":
                			tooltip.setText("[A parameter of the analyzer component]\n"
                					+ "This limit dictates how many service combinations\n"
                					+ "are chosen in the analyzing stage.");
                			break;
                			
                		case "Min Failure Delta":
                			tooltip.setText("[A parameter of the monitor component]\n"
                					+ "The minimum amount of difference needed in failure rate\n"
                					+ "between the current approximated failure rate and\n"
                					+ "the real failure rate before the approximated failure rate is updated.");
                			break;
                			
                		case "Failure Change":
                			tooltip.setText("[A parameter of the monitor component]\n"
                					+ "The value increment used for updating approximated failure rates.");
                			break;
                			
                		default:
                			tooltip.setText("WARNING\nMISSING TOOLTIP");
                			break;
                	}
                	
                	setTooltip(tooltip);
                }
            }
		});
		
		/*propertyTable.setRowFactory(tv -> new TableRow<SystemEntityPropertyEntry> {
            private Tooltip tooltip = new Tooltip();
            @Override
            public void updateItem(SystemEntityPropertyEntry entry, boolean empty) {
                super.updateItem(person, empty);
                
                if (person == null) {
                    setTooltip(null);
                } else {
                    //tooltip.setText(person.getFirstName()+" "+person.getLastName());
                    //setTooltip(tooltip);
                }
            }
        });*/
	}
	
	private void addRatingTypeOptions() {
		ObservableList<RatingType> ratingTypes = FXCollections.observableArrayList();
		ratingTypes.addAll(RatingType.values());
		ratingTypeBox.setItems(ratingTypes);
	}	
	
	private void initializeWorkflowButton() {
		
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
	
	private void initializeGoalStuff() {
		
		goalBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {
	    		try {
	    			
	    		    FXMLLoader loader = new FXMLLoader();
	    		    loader.setLocation(MainGui.class.getResource("view/goalDialog.fxml"));
	    		    AnchorPane systemEntityPane = (AnchorPane) loader.load();
	
	    		    Stage dialogStage = new Stage();
	    		    dialogStage.setTitle("Add System Entity");
	    		    dialogStage.setResizable(false);
	
	    		    controller = (GoalController) loader.getController();
	    		    controller.setStage(dialogStage);
	    		    controller.setParent(self);
	    		    
	    		    Scene dialogScene = new Scene(systemEntityPane);
	        	    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());
	    		    dialogStage.initOwner(stage);
	    		    dialogStage.setScene(dialogScene);
	        	    dialogStage.setResizable(false);
	    		    dialogStage.show();
	        	    
	    		} catch (Exception e) {
	    		    e.printStackTrace();
	    		}
		    }
		});
		
		goalTypeColumn.setCellValueFactory(new PropertyValueFactory<GoalEntry, String>("type"));
		goalTypeColumn.setStyle("-fx-alignment: CENTER-LEFT;");

		goalRelationColumn.setCellValueFactory(new PropertyValueFactory<GoalEntry, String>("relation"));
		goalRelationColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		
		goalValueColumn.setCellValueFactory(new PropertyValueFactory<GoalEntry, String>("value"));
		goalValueColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		
		deleteColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
		
		Callback<TableColumn<GoalEntry, String>, TableCell<GoalEntry, String>> cellFactoryDelete
        =
        new Callback<TableColumn<GoalEntry, String>, TableCell<GoalEntry, String>>() {
		    @Override
		    public TableCell<GoalEntry, String> call(final TableColumn<GoalEntry, String> param) {
		        final TableCell<GoalEntry, String> cell = new TableCell<GoalEntry, String>() {
		
		            final Button btn = new Button("Delete");
		
		            @Override
		            public void updateItem(String item, boolean empty) {
		            	
		            	GoalEntry goalEntry = (GoalEntry) this.getTableRow().getItem();
		            	
		                super.updateItem(item, empty);
		                
		                if (empty) {
		                    setGraphic(null);
		                    setText(null);
		                }
		                else {
		                    btn.setOnAction(event -> {
		                    	goals.remove(goalEntry.getGoal());
		                    	goalData.remove(getTableRow().getItem());
		                    });
		                    
		                    btn.setPrefHeight(10);
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
	}
	
	private void initializeAddButton() {
		
		addBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {
		    	
		    	if (workflowPath == null || workflowPath == "") {
		    		Alert fail = new Alert(AlertType.WARNING);
		            fail.setHeaderText("MISSING CONTENT");
		            fail.setContentText("No workflow selected.");
		            fail.showAndWait();
		    	}
		    	else if (ratingTypeBox.getValue() == null) {
		    		Alert fail = new Alert(AlertType.WARNING);
		            fail.setHeaderText("MISSING CONTENT");
		            fail.setContentText("No rating type selected.");
		            fail.showAndWait();
		    	}
		    	else if (entityRegistries.size() == 0) {
		    		Alert fail = new Alert(AlertType.WARNING);
		            fail.setHeaderText("MISSING CONTENT");
		            fail.setContentText("No Service registry selected.");
		            fail.showAndWait();	
		    	}
		    	else if (propertyTable.getItems().stream().anyMatch(x -> x.getValue() == "")) {
		    		Alert fail = new Alert(AlertType.WARNING);
		            fail.setHeaderText("MISSING CONTENT");
		            fail.setContentText("Not all properties in the property table are filled.");
		            fail.showAndWait();	
		    	}
		    	else {
		    		// TODO
		    		/*Goal goal = new Goal(typeBox.getValue(), relationBox.getValue(), Double.parseDouble(valueField.getText()));
		    		parent.addGoalToList(goal);
		    		stage.close();*/
		    	}
		    }
		});
	}
	
	public class SystemEntityPropertyEntry {
		
	    private SimpleStringProperty name;
	    private SimpleStringProperty type;
	    private SimpleStringProperty value;
	    
	    public SystemEntityPropertyEntry(String name, String type, String value) {
	    	this.name = new SimpleStringProperty(name);
	    	this.type = new SimpleStringProperty(type);
	    	this.value = new SimpleStringProperty(value);
	    }
	    
	    public SystemEntityPropertyEntry(String name, String type) {
	    	this(name, type, "");
	    }
	    
	    public String getName() {
	    	return this.name.get();
	    }
	    
	    public String getType() {
	    	return this.type.get();
	    }
	    
	    public String getValue() {
	    	return this.value.get();
	    }
	    
	    public void setValue(String value) {
	    	this.value = new SimpleStringProperty(value); 
	    }
	    
	    public Object getRealValue(String value){
	    	Object realValue = null;
	    	
			try {
				switch (type.get()) {
				
					case "boolean": 
					case "Boolean":
					{
						if (value.equals("true"))
							realValue = true;
						else
							realValue = false;
						break;
					}
					
					case "short":
					case "Short":
					{
						realValue = Short.parseShort(value);
						break;
					}
					
					case "int": 
					case "Integer": 
					{
						realValue = Integer.parseInt(value);
						break;
					}
					
					case "long": 
					case "Long": 
					{
						realValue = Long.parseLong(value);
						break;
					}
					
					case "float": 
					case "Float": 
					{
						realValue = Float.parseFloat(value);
						break;
					}
					
					case "double":
					case "Double": 
					{
						realValue = Double.parseDouble(value);
						break;
					}
					
					case "string":
					case "String":
					{
						break;
					}
					
					default: {
						System.out.println("Wrong attribute!!!!");
						break;
					}
				}

			} catch (Exception e) {
				return null;
			}			
			
	    	this.value=new SimpleStringProperty(value);
			return realValue;
	    }
	}
	
	public class GoalEntry {
		
		private Goal goal;
	    private SimpleStringProperty type;
	    private SimpleStringProperty relation;
	    private SimpleStringProperty value;
	    
	    public GoalEntry(Goal goal) {
	    	this.goal = goal;
	    	this.type = new SimpleStringProperty(goal.getType().toString());
	    	this.relation = new SimpleStringProperty(goal.getRelation().toString());
	    	this.value = new SimpleStringProperty(Double.toString(goal.getValue()));
	    }
	    
	    public Goal getGoal() {
	    	return goal;
	    }
	    
	    public String getType() {
	    	return this.type.get();
	    }
	    
	    public String getRelation() {
	    	return this.relation.get();
	    }
	    
	    public String getValue() {
	    	return this.value.get();
	    }
	}
}
