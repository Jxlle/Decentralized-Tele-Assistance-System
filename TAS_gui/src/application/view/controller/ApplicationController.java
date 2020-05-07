package application.view.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;

import service.atomic.AtomicService;
import service.atomic.ServiceProfile;
import service.auxiliary.Description;
import service.auxiliary.ExecutionThread;
import service.auxiliary.ServiceDescription;
import service.registry.ServiceRegistry;
import service.workflow.ast.rspLexer;
import service.workflow.ast.rspParser;
import tas.data.inputprofile.InputProfile;
import tas.data.inputprofile.InputProfileDataHandler;
import tas.data.inputprofile.InputProfileExecutor;
import tas.data.serviceinfo.GlobalServiceInfo;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalRelation;
import tas.mape.knowledge.Goal.GoalType;
import tas.system.entity.LocalServiceSystem;
import tas.system.entity.MAPEKFeedbackLoop;
import tas.system.entity.MAPEKSystemEntity;
import tas.system.entity.MAPEKSystemEntityLoader;
import tas.system.entity.MAPEKSystemEntityWriter;
import tas.system.entity.MAPEKFeedbackLoop.Builder;
import tas.workflowAnalyzer.WorkflowAnalyzer;
import application.MainGui;
import application.model.CostEntry;
import application.model.PerformanceEntry;
import application.model.ReliabilityEntry;
import application.utility.NodeVisitor;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

// TODO Service panes list with endpoints instead of names, names are not unique, endpoints usually are.
// TODO Reset registry IDs when changing service data
public class ApplicationController implements Initializable {

    Stage primaryStage;
    
    // for generating kinds of charts
    SystemRunResultController chartController;

    // the path of important files 
    //String workflowPath = "src"+File.separator+"resources" + File.separator + "workflow_test1.txt";
    
    static String baseDir="";
    
    String resourceDirPath = baseDir + "resources" + File.separator;
    static String resultDirPath = baseDir + "resources" + File.separator + "results" + File.separator;
    static String fileDirPath = baseDir + "resources" + File.separator + "files" + File.separator;
    
    String workflowPath;
    String workflowFilePath = fileDirPath + "workflow" + File.separator;
    static String profileFilePath = fileDirPath + "profiles" + File.separator;
    static String entityFilePath = fileDirPath + "entities" + File.separator;
    String serviceDataFilePath = fileDirPath + "serviceData" + File.separator;
    String resultFilePath = resultDirPath + "result.csv";
    String logFilePath = resultDirPath + "log.csv";
    String defaultServiceDataPath = serviceDataFilePath + "DefaultServiceData.xml";
	String latestProfilePath;

    ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(5);

    List<ServiceRegistry> serviceRegistries;
    
    MAPEKSystemEntity selectedEntity;
    List<MAPEKSystemEntity> entities = new ArrayList<>();
    Map<String, Boolean> workflowAnalyzed = new HashMap<>(); 
    Map<String, List<ServiceRegistry>> entityRegistries;
    Map<String, AnchorPane> servicePanes = new ConcurrentHashMap<>();
    Map<String, ListView<AnchorPane>> serviceRegistryPanes = new ConcurrentHashMap<>();
    Task<Void> progressTask;
    int maxSteps;
    String entityBeingAnalyzed;
    boolean analyzed, done;
    
    @FXML
    ListView<AnchorPane> profileListView;
    
    @FXML
    ListView<AnchorPane> entityListView;
    
    @FXML
    TextArea workflowTextArea;

    @FXML
    TableView<ReliabilityEntry> reliabilityTableView;

    @FXML
    TableView<CostEntry> costTableView;
    
    @FXML
    TableView<PerformanceEntry> performanceTableView;

    @FXML
    MenuItem openServicesMenuItem;
    
    @FXML
    MenuItem configureMenuItem;
    
    @FXML
    MenuItem openLogMenuItem;

    @FXML
    MenuItem openProfileMenuItem;

    @FXML
    MenuItem saveRunMenuItem;
    
    @FXML
    MenuButton saveMenuButton;
    
    @FXML
    MenuItem saveResultDataMenuItem;

    @FXML
    AnchorPane reliabilityChartPane;

    @FXML
    AnchorPane costChartPane;
    
    @FXML
    AnchorPane performanceChartPane;
    
    @FXML
    AnchorPane invCostChartPane;
    
    @FXML
    AnchorPane avgReliabilityChartPane;
    
    @FXML
    AnchorPane systemRunChartPane;
    
    @FXML
    AnchorPane avgCostChartPane;
    
    @FXML
    AnchorPane avgPerformanceChartPane;
    
    @FXML
    AnchorPane invRateChartPane;
    
    @FXML
    AnchorPane protocolMessageChartPane;
    
    @FXML
    AnchorPane protocolFlowAnchorPane;
    
    @FXML
    AnchorPane failureRateErrorChartPane;
    
    @FXML
    AnchorPane failureRateChartPane;
    
    @FXML
    AnchorPane ratingEvolutionChartPane;
    
    @FXML
    AnchorPane failureRateSystemChartPane;
    
    @FXML
    AnchorPane ratingEvolutionSystemChartPane;
    
    @FXML
    Accordion serviceRegistryAcc;

    @FXML
    ScrollPane serviceScrollPane;

    @FXML
    ScrollPane profileScrollPane;

    @FXML
    MenuItem openRunMenuItem;

    @FXML
    Button aboutButton;
    
    @FXML
    Button addSystemEntityBtn;
    
    @FXML
    Button saveWorkflowButton;
    
    @FXML
    Button AddInputProfileButton;

    @FXML
    Button helpButton;
    
    @FXML
    Button clearResultsBtn;
    
    @FXML 
    MenuItem exampleScenariosMenuItem;
    
    @FXML
    MenuItem changeServiceCollectionMenuItem;
    
    @FXML
    MenuItem OpenSystemEntityMenuItem;
    
    @FXML
    MenuItem saveSystemRunPerformanceMenuItem;
    
    @FXML
    MenuItem saveProtocolMessageMenuItem;
    
    @FXML
    MenuItem saveRatingEvolutionMenuItem;
    
    @FXML
    MenuItem saveFailureRateEvolutionMenuItem;
    
    @FXML
    MenuItem saveCostEvolutionMenuItem;
    
    @FXML
    ToolBar toolBar;
    
    @FXML
    Button configureButton;
    
    @FXML
    AnchorPane canvasPane;
    
    @FXML
    MenuItem saveInvMenuItem;
    
    @FXML
    TabPane entityTabPane;
    
    @FXML
    TitledPane serviceTitledPane;
    
    @FXML
    Accordion entityResultTableAccordion;
    
    @FXML
    Label protocolDetailsText;
    
    ProgressBar progressBar;
    Label invocationLabel;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	GlobalServiceInfo.loadData(new File(defaultServiceDataPath));
    	this.initChartController();
    	this.addServicesProfiles();
    	this.addItems();
    	this.fillSystemProfiles();
    	this.setButton();
    	this.fillSystemEntities();
    }
    
    
    private void generateSequenceDiagram(String workflowPath){
    	
    	canvasPane.getChildren().clear();
    	
		try {
			rspLexer lexer = new rspLexer(new ANTLRFileStream(workflowPath));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			rspParser parser = new rspParser(tokens);
			NodeVisitor visitor=new NodeVisitor();
			visitor.setCanvasPane(canvasPane);
			visitor.visit((CommonTree)parser.start().getTree());
		}
		catch(Exception e){
			e.printStackTrace();
		}

    }
    
    public void removeEntityFromList(MAPEKSystemEntity entity) {
		entities.remove(entity);
		workflowAnalyzed.remove(entity.getEntityName());
		entityListView.getItems().remove(entityListView.getItems().stream().filter(x -> x.getChildren().stream().filter(x2 -> x2 instanceof Button).map(Button.class::cast).anyMatch(x3 -> x3.getText().equals(entity.getEntityName()))).findAny().orElse(null));
    }
    
	public void addEntityToList(MAPEKSystemEntity entity, File entityFile) {
		
		if (entities.stream().anyMatch(x -> x.getEntityName().equals(entity.getEntityName()))) {
			
			Alert fail = new Alert(AlertType.WARNING);
            fail.setHeaderText("FAILURE");
            fail.setContentText("An entity with the same name already exists in the system.");
            fail.showAndWait();
			
			return;
		}
		
		entities.add(entity);
		workflowAnalyzed.put(entity.getEntityName(), false);
		
		AnchorPane entityPane = new AnchorPane();
		entityPane.setId(entity.getEntityName());

		Button selectButton = new Button();
		selectButton.setText(entity.getEntityName());
		selectButton.setId("profileButton");
		selectButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {   
		    	selectedEntity = entities.stream().filter(x -> x.getEntityName().equals(selectButton.getText())).findFirst().orElse(null);
		    	entityTabPane.setVisible(true);
		    	serviceTitledPane.setVisible(true);
		    	selectEntity();
		    }
		});
    	
    	Button deleteButton = new Button();
    	deleteButton.setText("Delete");
    	deleteButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {   	   			    	
		    	Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete " + entity.getEntityName() + "? This action can't be reverted.", ButtonType.YES, ButtonType.NO);
		    	alert.showAndWait();

		    	if (alert.getResult() == ButtonType.YES) {			    		    		
			    	if (!entityFile.exists()) {
						Alert fail = new Alert(AlertType.INFORMATION);
			            fail.setHeaderText("WARNING");
			            fail.setContentText("The entity file cannot be deleted. Please make sure that the file has the same name as the entity.");
			            fail.showAndWait();
			    	}
			    	else {
				    	entities.removeIf(x -> x.getEntityName().equals(entity.getEntityName()));
				    	entityListView.getItems().remove(deleteButton.getParent());
				    	workflowAnalyzed.remove(entity.getEntityName());
			    		entityFile.delete();
			    	}
		    	}
		    }
		});
    	
	    ApplicationController self = this;
	    
    	Button inspectButton = new Button();
    	inspectButton.setPrefWidth(32);
    	inspectButton.setPrefHeight(32);
    	inspectButton.setLayoutY(5);
    	inspectButton.setId("inspectButton");
    	inspectButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
        		try {	
	    		    FXMLLoader loader = new FXMLLoader();
	    		    loader.setLocation(MainGui.class.getResource("view/systemEntity.fxml"));
	    		    AnchorPane systemEntityPane = (AnchorPane) loader.load();
	
	    		    Stage dialogStage = new Stage();
	    		    dialogStage.setTitle("Add System Entity");
	    		    dialogStage.setResizable(false);
	    		    
	    		    SystemEntityController controller = (SystemEntityController) loader.getController();
	    		    controller.setStage(dialogStage);
					controller.setParent(self);
					controller.setEntity(entity);
	
	    		    Scene dialogScene = new Scene(systemEntityPane);
	        	    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());
	    		    dialogStage.initOwner(primaryStage);
	    		    dialogStage.setScene(dialogScene);
	        	    dialogStage.setResizable(false);
	    		    dialogStage.show();   
        		    
        		} catch (Exception e) {
        		    e.printStackTrace();
        		}
    	    }
    	});
    	
    	AnchorPane.setLeftAnchor(selectButton, 10.0);
    	AnchorPane.setTopAnchor(selectButton, 10.0);
    	AnchorPane.setBottomAnchor(selectButton, 10.0);
    	AnchorPane.setRightAnchor(inspectButton, 10.0);
    	AnchorPane.setTopAnchor(inspectButton, 10.0);
    	AnchorPane.setBottomAnchor(inspectButton, 10.0);
    	AnchorPane.setRightAnchor(inspectButton, 80.0);
    	AnchorPane.setTopAnchor(deleteButton, 10.0);
    	AnchorPane.setBottomAnchor(deleteButton, 10.0);
    	AnchorPane.setRightAnchor(deleteButton, 10.0);
    	
    	entityPane.getChildren().addAll(inspectButton, deleteButton, selectButton);
    	entityListView.getItems().add(entityPane);
	}
	
	public void analyzeEntity(MAPEKSystemEntity entity) {
		
		WorkflowAnalyzer.analyzeWorkflow(entity.getManagedSystem().getAssistanceService());
		Map<Description, Pair<List<ServiceDescription>, Double>> usableServices = WorkflowAnalyzer.getUsableServicesAndChance();
		
		// Reset all entity probes, not only the current entity
		for (MAPEKSystemEntity e : entities) {
			e.getManagingSystem().getMonitor().resetProbes();
		}
		
		entity.getManagingSystem().getKnowledge().setWorkflowServiceTree(WorkflowAnalyzer.getWorkflowServiceTree());
		entity.getManagingSystem().getKnowledge().setUsedServicesAndChances(usableServices);
		workflowAnalyzed.put(entity.getEntityName(), true);
	}

    public void setPrimaryStage(Stage primaryStage) {
    	this.primaryStage = primaryStage;
    }
    
    private void initChartController() {
    	chartController = new SystemRunResultController(systemRunChartPane, protocolMessageChartPane, protocolFlowAnchorPane, failureRateErrorChartPane, costChartPane, failureRateChartPane, 
    			failureRateSystemChartPane, ratingEvolutionChartPane, ratingEvolutionSystemChartPane, entityResultTableAccordion, protocolDetailsText);
    }
    
    private void fillSystemEntities() {
    	File folder = new File(entityFilePath);
    	File[] files = folder.listFiles();

    	try {
    	    for (File file : files) {
        		if (file.isFile()) {
        		    if (file.getName().lastIndexOf('.') > 0)
        		    	addEntityToList(MAPEKSystemEntityLoader.loadFromXml(file), file);
        		}
    	    }
    	    
    	    if (files.length == 0) {
    	    	addDefaultEntities();
    	    }
    	    
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }
    
    private void addDefaultEntities() {
    	
		LocalServiceSystem workflowExecutor = new LocalServiceSystem(
				Arrays.asList(GlobalServiceInfo.getServiceRegistry("service.shared.registry"), GlobalServiceInfo.getServiceRegistry("service.individual1.registry")));	
		workflowExecutor.setWorkflowPath(workflowFilePath + "TeleAssistanceWorkflow.txt");		
		MAPEKFeedbackLoop.Builder builder = new Builder();
		
		try {
			builder.initializeKnowledge(10, new ArrayList<String>(Arrays.asList("service.shared.registry", "service.individual1.registry")))
			 	   .initializePlanner()
				   .initializeAnalyzer(1000, 1)
				   .initializeMonitor(0.005, 0.005);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		MAPEKFeedbackLoop component = builder.build();
		MAPEKSystemEntity systemEntity = new MAPEKSystemEntity("Default System Entity", workflowExecutor, component);
		systemEntity.getManagingSystem().getKnowledge().addGoal(new Goal(GoalType.COST, GoalRelation.LOWER_THAN, 18));
		systemEntity.getManagingSystem().getKnowledge().addGoal(new Goal(GoalType.FAILURE_RATE, GoalRelation.LOWER_THAN, 0.19));
		
		File entityFile = new File(entityFilePath + systemEntity.getEntityName() + ".xml");
		MAPEKSystemEntityWriter.writeToXml(systemEntity, entityFile);
		addEntityToList(systemEntity, entityFile);
		
		workflowExecutor = new LocalServiceSystem(
				Arrays.asList(GlobalServiceInfo.getServiceRegistry("service.shared.registry"), 
						GlobalServiceInfo.getServiceRegistry("service.individual2.registry")));	
		workflowExecutor.setWorkflowPath(workflowFilePath + "TeleAssistanceWorkflow.txt");
		
		builder = new Builder();
		
		try {
			builder.initializeKnowledge(10, new ArrayList<String>(Arrays.asList("service.shared.registry", "service.individual2.registry")))
			 	   .initializePlanner()
				   .initializeAnalyzer(1000, 1)
				   .initializeMonitor(0.005, 0.005);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		component = builder.build();
		systemEntity = new MAPEKSystemEntity("Default System Entity 2", workflowExecutor, component);
		systemEntity.getManagingSystem().getKnowledge().addGoal(new Goal(GoalType.COST, GoalRelation.LOWER_THAN, 18));
		systemEntity.getManagingSystem().getKnowledge().addGoal(new Goal(GoalType.FAILURE_RATE, GoalRelation.LOWER_THAN, 0.19));	
		
		entityFile = new File(entityFilePath + systemEntity.getEntityName() + ".xml");
		MAPEKSystemEntityWriter.writeToXml(systemEntity, new File(entityFilePath + systemEntity.getEntityName() + ".xml"));
		addEntityToList(systemEntity, entityFile);
    }
    
    private void selectEntity() {
    	
    	// Set workflow data
    	workflowPath = selectedEntity.getManagedSystem().getWorkflowPath();
    	
    	try {
    	    String content = new String(Files.readAllBytes(Paths.get(workflowPath)));
    	    workflowTextArea.setText(content);
    	    
    	    this.generateSequenceDiagram(workflowPath);
    	    
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	
    	// Set service data
    	List<ServiceRegistry> serviceRegistries = new ArrayList<>();
    	
    	for (String registryEndpoint : selectedEntity.getManagingSystem().getKnowledge().getRegistryEndpoints()) {
    		serviceRegistries.add(GlobalServiceInfo.getServiceRegistry(registryEndpoint));
    	}
    	
    	this.serviceRegistries = serviceRegistries;
    	openServicesMenuItem.fire();
    }
    
    public boolean entityNameExists(String name) {
    	return entities.stream().filter(x -> x.getEntityName().equals(name)).findAny().isPresent() ? true : false;
    }

    private void addItems() {
    	progressBar = new ProgressBar(0);
    	invocationLabel = new Label();
    	toolBar.getItems().addAll(new Label("Progress "), progressBar, invocationLabel);
    }
    
    private void setButton() {

    	openServicesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    	    	
    	    	serviceRegistryAcc.getPanes().removeAll(serviceRegistryAcc.getPanes());
    	    	serviceRegistryPanes = new HashMap<>();
    	    	servicePanes = new HashMap<>();
        	    List<String> services = new ArrayList<String>();	
    	    	
        	    for (ServiceRegistry registry : serviceRegistries) {
        	    	serviceRegistryPanes.put(registry.getServiceDescription().getServiceEndpoint(), addServiceRegistry(registry));
        	    	services.addAll(registry.getAllServiceNames());
        	    }
        	    
        		for (String service : services) {
        			// Check might not be needed, this service is usually not added to the service list
        		    if (!service.equals("TeleAssistanceService"))
        		    	servicePanes.put(service, addService(service));
        		}
    	    }
    	});
    	
    	configureMenuItem.setOnAction(event->{	
    		try{
    	
    			FXMLLoader loader = new FXMLLoader();
    			loader.setLocation(MainGui.class.getResource("view/configureDialog.fxml"));
    			GridPane configurePane = (GridPane) loader.load();

    			Stage dialogStage = new Stage();
    			dialogStage.setTitle("ReSeP Configuration");
        
    			ConfigureController controller=(ConfigureController)loader.getController();
    			controller.setStage(dialogStage);

    			Scene dialogScene = new Scene(configurePane);
    			dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());

    			dialogStage.initOwner(primaryStage);
    			dialogStage.setScene(dialogScene);
    			dialogStage.setResizable(false);
    			dialogStage.show(); 
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    	});
    	
    	configureButton.setOnAction(event->{
    		try{
    			
    			FXMLLoader loader = new FXMLLoader();
    			loader.setLocation(MainGui.class.getResource("view/configureDialog.fxml"));
    			GridPane configurePane = (GridPane) loader.load();

    			Stage dialogStage = new Stage();
    			dialogStage.setTitle("ReSeP Configuration");
        
    			ConfigureController controller=(ConfigureController)loader.getController();
    			controller.setStage(dialogStage);

    			Scene dialogScene = new Scene(configurePane);
    			dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());

    			dialogStage.initOwner(primaryStage);
    			dialogStage.setScene(dialogScene);
    			dialogStage.setResizable(false);
    			dialogStage.show(); 
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    	});

    	openProfileMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		FileChooser fileChooser = new FileChooser();
    		fileChooser.setInitialDirectory(new File(profileFilePath));
    		fileChooser.setTitle("Select profile");
    		FileChooser.ExtensionFilter extension = new FileChooser.ExtensionFilter("Add Files(*.xml)", "*.xml");
    		fileChooser.getExtensionFilters().add(extension);
    		File file = fileChooser.showOpenDialog(primaryStage);
    		if (file != null) {
    		    addSystemProfile(file.getPath());
    		}
    	    }
    	});

    	saveRunMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    	    	DirectoryChooser chooser = new DirectoryChooser();
    	    	chooser.setTitle("Choose result directory");
    	    	File defaultDirectory = new File(resultDirPath);
    	    	chooser.setInitialDirectory(defaultDirectory);
    	    	File selectedDirectory = chooser.showDialog(primaryStage);
    	    	
    	    	try {
					chartController.saveAll(selectedDirectory, entities, latestProfilePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    	});

    	openRunMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		FileChooser fileChooser = new FileChooser();
    		fileChooser.setTitle("Select profile");
    		FileChooser.ExtensionFilter extension = new FileChooser.ExtensionFilter("Add Files(*.csv)", "*.csv");
    		fileChooser.getExtensionFilters().add(extension);
    		File file = fileChooser.showOpenDialog(primaryStage);
    		if (file != null) {
    		    try {
    			BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
    			String line;
    			//int invocationNum = 0;
    			
    			while ((line = br.readLine()) != null) {
    			    String[] str = line.split(",");
    			    if (str.length >= 3) {
    				//invocationNum = Integer.parseInt(str[0]);
    			    }
    			}
    			
    			br.close();

    			//TODO CHARTS
    			//chartController.generateCharts(resultFilePath, tasStart.getCurrentSteps());
    			//chartController.generateAvgCharts(resultFilePath, tasStart.getCurrentSteps(),Integer.parseInt(sliceTextField.getText()));
    			
    			/*tableViewController.fillReliabilityDate(file.getPath());
    			tableViewController.fillCostData(file.getPath());
    			tableViewController.fillPerformanceData(file.getPath());*/
    		    } catch (Exception e) {
    			e.printStackTrace();
    		    }
    		}
    	    }
    	});
    	
    	aboutButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		try {
    		    // System.out.println("about");

    		    FXMLLoader loader = new FXMLLoader();
    		    loader.setLocation(MainGui.class.getResource("view/aboutDialog.fxml"));
    		    AnchorPane aboutPane = (AnchorPane) loader.load();

    		    Stage dialogStage = new Stage();
    		    dialogStage.setTitle("About");
    		    dialogStage.setResizable(false);

    		    Scene dialogScene = new Scene(aboutPane);
    		    dialogStage.initOwner(primaryStage);
    		    dialogStage.setScene(dialogScene);
    		    dialogStage.show();
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	    }
    	});
    	
		ApplicationController self = this;
    	
    	addSystemEntityBtn.setOnAction(new EventHandler<ActionEvent>() {
    		
    	    @Override
    	    public void handle(ActionEvent event) {
	    		try {
	    			
	    		    FXMLLoader loader = new FXMLLoader();
	    		    loader.setLocation(MainGui.class.getResource("view/systemEntity.fxml"));
	    		    AnchorPane systemEntityPane = (AnchorPane) loader.load();
	
	    		    Stage dialogStage = new Stage();
	    		    dialogStage.setTitle("Add System Entity");
	    		    dialogStage.setResizable(false);
	    		    
	    		    SystemEntityController controller = (SystemEntityController) loader.getController();
	    		    controller.setStage(dialogStage);
	    		    controller.setParent(self);
					controller.setEntity(null);
	
	    		    Scene dialogScene = new Scene(systemEntityPane);
	        	    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());
	    		    dialogStage.initOwner(primaryStage);
	    		    dialogStage.setScene(dialogScene);
	        	    dialogStage.setResizable(false);
	    		    dialogStage.show();
	        	    
	    		} catch (Exception e) {
	    		    e.printStackTrace();
	    		}
    	    }
    	});

    	helpButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		try {
    		    FXMLLoader loader = new FXMLLoader();
    		    loader.setLocation(MainGui.class.getResource("view/helpDialog.fxml"));
    		    AnchorPane helpPane = (AnchorPane) loader.load();

    		    Stage dialogStage = new Stage();
    		    dialogStage.setTitle("Help");
    		    dialogStage.setResizable(false);

    		    Scene dialogScene = new Scene(helpPane);
    		    dialogStage.initOwner(primaryStage);
    		    dialogStage.setScene(dialogScene);
    		    dialogStage.show();
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	    }
    	});
    	
    	openLogMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		try {
    		    FXMLLoader loader = new FXMLLoader();
    		    loader.setLocation(MainGui.class.getResource("view/logDialog.fxml"));
    		    AnchorPane helpPane = (AnchorPane) loader.load();

    		    Stage dialogStage = new Stage();
    		    dialogStage.setTitle("Log");
    		    
    			LogController controller=(LogController)loader.getController();
    			controller.setStage(dialogStage);

    		    Scene dialogScene = new Scene(helpPane);
    		    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());

    		    dialogStage.initOwner(primaryStage);
    		    dialogStage.setScene(dialogScene);
    		    dialogStage.show();
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	    }
    	});
    	
    	saveWorkflowButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {   
    	    	
    	    	if (selectedEntity != null) {
    				Alert fail = new Alert(AlertType.INFORMATION);
    	            fail.setHeaderText("INFORMATION");
    	            fail.setContentText("Not every type of workflow is supported yet. Make sure each service operation is not used multiple times throughout the workflow.");
    	            fail.showAndWait();
        	    	
        	    	try (PrintWriter out = new PrintWriter(workflowPath)) {
        	    	    out.println(workflowTextArea.getText());
        	    	} catch (FileNotFoundException e) {
    					e.printStackTrace();
    				}
        	    		
        	    	workflowAnalyzed.put(selectedEntity.getEntityName(), false);
        	    	updateWorkflowVisual();
    	    	}
    	    }
    	});
    	
    	changeServiceCollectionMenuItem.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File(serviceDataFilePath));
				fileChooser.setTitle("Select service data");
				FileChooser.ExtensionFilter extension = new FileChooser.ExtensionFilter("Add Files(*.xml)", "*.xml");
				fileChooser.getExtensionFilters().add(extension);
				File file = fileChooser.showOpenDialog(primaryStage);
				
				if (file != null) {
					GlobalServiceInfo.loadData(file);
					
					//for 
					
					if (selectedEntity != null) {
						selectEntity();
					}
				}
		    }
    	});
    	
    	OpenSystemEntityMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
        		FileChooser fileChooser = new FileChooser();
        		fileChooser.setInitialDirectory(new File(entityFilePath));
        		fileChooser.setTitle("Select system entity");
        		FileChooser.ExtensionFilter extension = new FileChooser.ExtensionFilter("Add Files(*.xml)", "*.xml");
        		fileChooser.getExtensionFilters().add(extension);
        		File file = fileChooser.showOpenDialog(primaryStage);
        		
        		if (file != null) {
        			addEntityToList(MAPEKSystemEntityLoader.loadFromXml(file), file);
        		}
    	    }
    	});
    	
    	
    	AddInputProfileButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
        		try {	
        		    FXMLLoader loader = new FXMLLoader();
        		    loader.setLocation(MainGui.class.getResource("view/systemProfileDialog.fxml"));
        		    AnchorPane pane = (AnchorPane) loader.load();

        		    Stage dialogStage = new Stage();
        		    dialogStage.setTitle("System Profile");
        		    
        			SystemProfileController controller = (SystemProfileController) loader.getController();
        			controller.setStage(dialogStage);
        			controller.setEntityData(entities);
        			controller.setFilePath(null);
        			controller.setParent(self);

        		    Scene dialogScene = new Scene(pane);
        		    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());

        		    dialogStage.initOwner(primaryStage);
        		    dialogStage.setResizable(false);
        		    dialogStage.setScene(dialogScene);
        		    dialogStage.show();    		    
        		    
        		} catch (Exception e) {
        		    e.printStackTrace();
        		}
    	    }
    	});
    	
    	saveSystemRunPerformanceMenuItem.setOnAction(event->{
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save system run performance graph");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    				chartController.saveSystemRunPerformanceChart(file.getPath());

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	});
    	
    	saveProtocolMessageMenuItem.setOnAction(event->{
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save protocol message graph");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    				chartController.SaveProtocolMessageChart(file.getPath());

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	});
    	
    	saveRatingEvolutionMenuItem.setOnAction(event->{
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save rating evolution graphs");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    				chartController.saveRatingCharts(file.getPath());

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	});
    	
    	saveFailureRateEvolutionMenuItem.setOnAction(event->{
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save failure rate evolution graphs");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    				chartController.saveFailureRateEvolutionCharts(file.getPath());

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	});
    	
    	saveCostEvolutionMenuItem.setOnAction(event->{
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save cost evolution graph");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    				chartController.saveCostEvolutionChart(file.getPath());

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	});
    	
    	saveResultDataMenuItem.setOnAction(event->{
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save run data");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    				chartController.saveRunData(file.getPath(), entities, latestProfilePath);

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	});
    	
    	clearResultsBtn.setOnAction(event->{
			chartController.clear();	
    	});
    }
    
    private void updateWorkflowVisual() {
	    this.generateSequenceDiagram(workflowPath);   	
    }
    
    private void fillSystemProfiles() {
    	File folder = new File(profileFilePath);
    	File[] files = folder.listFiles();

    	try {
    	    for (File file : files) {
        		if (file.isFile()) {
        		    if (file.getName().lastIndexOf('.') > 0)
        			this.addSystemProfile(file.getAbsolutePath());
        		}
    	    }
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }
    
    private void resetProgressBar() {
		progressTask.cancel();
    	invocationLabel.setText("");
		progressBar.progressProperty().unbind();
		progressBar.setProgress(0.0f);
	    done = false;
	    analyzed = false;
    }
    
    public void addSystemProfile(String profilePath) {
    	
		if (profileListView.getItems().stream().anyMatch(x -> x.getChildren().stream().filter(x2 -> x2 instanceof Label).map(Label.class::cast).anyMatch(x3 -> x3.getText().equals(Paths.get(profilePath).getFileName().toString().split("\\.")[0])))) {
			
			Alert fail = new Alert(AlertType.WARNING);
            fail.setHeaderText("FAILURE");
            fail.setContentText("This profile is already present in the system.");
            fail.showAndWait();
			
			return;
		}
    	
    	AnchorPane itemPane = new AnchorPane();
    	
    	Button deleteButton = new Button();
    	deleteButton.setText("Delete");
    	deleteButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
			public void handle(ActionEvent event) {   	   	
		    	AnchorPane parent = (AnchorPane) deleteButton.getParent();
		    	
		    	Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete " + Paths.get(profilePath).getFileName().toString().split("\\.")[0] + "? This action can't be reverted.", ButtonType.YES, ButtonType.NO);
		    	alert.showAndWait();

		    	if (alert.getResult() == ButtonType.YES) {			    	
			    	File profileFile = new File(profilePath); 
		    		
			    	if (!profileFile.exists()) {
						Alert fail = new Alert(AlertType.INFORMATION);
			            fail.setHeaderText("WARNING");
			            fail.setContentText("The input profile file cannot be deleted.");
			            fail.showAndWait();
			    	}
			    	else {
    					profileListView.getItems().remove(parent);
    					profileFile.delete();
			    	}
		    	}
		    }
		});

    	Button inspectButton = new Button();
    	inspectButton.setPrefWidth(32);
    	inspectButton.setPrefHeight(32);
    	inspectButton.setLayoutY(5);
    	inspectButton.setId("inspectButton");
    	inspectButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
        		try {	
        		    FXMLLoader loader = new FXMLLoader();
        		    loader.setLocation(MainGui.class.getResource("view/systemProfileDialog.fxml"));
        		    AnchorPane pane = (AnchorPane) loader.load();

        		    Stage dialogStage = new Stage();
        		    dialogStage.setTitle("System Profile");
        		    
        			SystemProfileController controller = (SystemProfileController) loader.getController();
        			controller.setStage(dialogStage);
        			controller.setEntityData(entities);
        			controller.setFilePath(profilePath);

        		    Scene dialogScene = new Scene(pane);
        		    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());

        		    dialogStage.initOwner(primaryStage);
        		    dialogStage.setResizable(false);
        		    dialogStage.setScene(dialogScene);
        		    dialogStage.show();    		    
        		    
        		} catch (Exception e) {
        		    e.printStackTrace();
        		}
    	    }
    	});
    	
    	Button runButton = new Button();
    	runButton.setPrefWidth(32);
    	runButton.setPrefHeight(32);
    	runButton.setLayoutY(5);
    	runButton.setId("runButton");
    	
    	runButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    	    	
    		done = false;

    	    if (runButton.getId().equals("runButton")) {
    			
    			Task<Void> task = new Task<Void>() {
    			    @Override
    			    protected Void call() throws Exception {	
    			    	
    				    Platform.runLater(new Runnable() {
        					@Override
        					public void run() {
        					    runButton.setId("stopButton");
        						saveMenuButton.setDisable(true);
        					}
    				    });
    			    	
    	    			chartController.resetProbes();
    	    			InputProfile profile = InputProfileDataHandler.readFromXml(profilePath);
    	    			InputProfileDataHandler.activeProfile = profile;
    	    		    analyzed = false;
    	    		    
    	    		    // Check profile integrity
    	    		    for (int i = 0; i < profile.getSystemType().getMaxEntities(); i++) {
    	    		    	
    			    		final int index = i;
    			    		
    			    		MAPEKSystemEntity entity = entities.stream()
    			    				.filter(x -> x.getEntityName().equals(profile.getParticipatingEntity(index))).findFirst().orElse(null);
    			    		
    			    		if (entity == null) {
    			    			
            				    Platform.runLater(new Runnable() {
                					@Override
                					public void run() {	
            			    			resetProgressBar();
                						Alert fail = new Alert(AlertType.WARNING);
            				            fail.setHeaderText("INVALID CONTENT");
            				            fail.setContentText("System run stopped. Input profile contains unknown system entity.");
            				            fail.showAndWait();
                					}
            				    });
    				            
    			    			runButton.setId("runButton");
    			    			return null;
    			    		}
    			    		
    			    		if (profile.getEntityRequirementType(profile.getParticipatingEntity(index)) == null) {
            				    Platform.runLater(new Runnable() {
                					@Override
                					public void run() {	
            			    			resetProgressBar();
                						Alert fail = new Alert(AlertType.WARNING);
            				            fail.setHeaderText("INVALID CONTENT");
            				            fail.setContentText("System run stopped. Input profile misses requirement type for entity " + profile.getParticipatingEntity(index) + ".");
            				            fail.showAndWait();
                					}
            				    });
    				            
    			    			runButton.setId("runButton");
    			    			return null;
    			    		}
    	    		    }
    	    		    
    			    	// Analyze entity workflows if needed
    			    	for (int i = 0; i < profile.getSystemType().getMaxEntities(); i++) {
    			    		
    			    		final int index = i;
    			    		
    			    		MAPEKSystemEntity entity = entities.stream()
    			    				.filter(x -> x.getEntityName().equals(profile.getParticipatingEntity(index))).findFirst().orElse(null);
    			    		
    			    		chartController.addEntityToProbe(entity);
    			    		
    			    		if (!workflowAnalyzed.get(entity.getEntityName())) {
    	    			    	entityBeingAnalyzed = entity.getEntityName();
    			    			analyzeEntity(entity);
    			    		}
    			    	}
    			    	
    			    	analyzed = true;
    			    	entityBeingAnalyzed = "";
    			    	
    			    	// Set latest profile path
    			    	latestProfilePath = profilePath;
    			    	
    			    	// Set protocol 
    			    	InputProfileExecutor.setProtocol(profile);
    			    	
    			    	// Set chart protocol probe
    			    	chartController.setProtocolProbe();
    			    	
    			    	// Execute system
    			    	InputProfileExecutor.execute(entities);
    			    	done = true;
					    
    				    Platform.runLater(new Runnable() {
        					@Override
        					public void run() {
        						chartController.clear();			
        						chartController.generateSystemRunCharts();
        						chartController.generateSystemRunTables();
        						saveMenuButton.setDisable(false);
        						
        					    resetProgressBar();
        					    runButton.setId("runButton");
        					}
    				    });
    				
    				return null;
    				
    			    }
    			};
    			
    			ExecutionThread thread = new ExecutionThread("main", task);
    			thread.setDaemon(true);
    			thread.start();
    			
    			progressTask = new Task<Void>() {
    			    @Override
    			    protected Void call() throws Exception {
    			    	
    			    	while (WorkflowAnalyzer.getCurrentSteps() < WorkflowAnalyzer.analyzerCycles && !analyzed) {
        				    Platform.runLater(new Runnable() {
            					@Override
            					public void run() {	
        							invocationLabel.setText("[ANALYZING] Analyzing the workflow of \"" + entityBeingAnalyzed
        									+ "\", [PROGRESS: "+ WorkflowAnalyzer.getCurrentSteps() + "/" + WorkflowAnalyzer.analyzerCycles + "]");
            					}
        				    });
        				    
        				    updateProgress(WorkflowAnalyzer.getCurrentSteps(), WorkflowAnalyzer.analyzerCycles);
        				    Thread.sleep(100);
    			    	}
    			    	
    			    	while (analyzed && !done) {
        				    Platform.runLater(new Runnable() {
            					@Override
            					public void run() {	
        							invocationLabel.setText("Executing system run ["
            					+ InputProfileExecutor.getCurrentExecutionCycle() + "/" + InputProfileDataHandler.activeProfile.getExecutionCycles() 
            					+ "], [PROGRESS: " + InputProfileExecutor.getCurrentWorkflowCycle() + "/" + InputProfileDataHandler.activeProfile.getWorkflowCycles()
			    						* + InputProfileDataHandler.activeProfile.getAmountOfParticipatingEntities() + "]");
            					}
        				    });
        				    
        				    updateProgress(InputProfileExecutor.getCurrentExecutionCycle(), InputProfileDataHandler.activeProfile.getExecutionCycles());
        				    Thread.sleep(100);
    			    	}
    			    	
    			    	if (done) {
        				    Platform.runLater(new Runnable() {
            					@Override
            					public void run() {	
                			    	invocationLabel.setText("");
                				    updateProgress(0, 0);
                				    done = false;
            					}
        				    });
    			    	}
    			    	
        				return null;
    			    }
    			};
    			
    			progressBar.progressProperty().bind(progressTask.progressProperty());
    			new Thread(progressTask).start();
    	    }   
    	    else {    	
			    
			    Platform.runLater(new Runnable() {
					@Override
					public synchronized void run() {	
						if (analyzed) {
		    				// Stop system
		    				InputProfileExecutor.stopSystemExecution();	
		    				
		    				// Plot current graphs
							chartController.generateSystemRunCharts();
							chartController.generateSystemRunTables();
							saveMenuButton.setDisable(false);
						}
						else {
							// Stop analyzer
							WorkflowAnalyzer.stop();
						}
						
						chartController.clear();
					    runButton.setId("runButton");	
					    
					    // Reset progress bar
					    resetProgressBar();
					}
			    });
    	    }
    	    }
    	});
    	
    	Label label = new Label();
    	label.setLayoutY(15);
    	label.setText(Paths.get(profilePath).getFileName().toString().split("\\.")[0]);
    	
    	AnchorPane.setTopAnchor(label, 10.0);
    	AnchorPane.setBottomAnchor(label, 10.0);
    	AnchorPane.setLeftAnchor(label, 10.0);
    	AnchorPane.setRightAnchor(deleteButton, 10.0);
    	AnchorPane.setTopAnchor(deleteButton, 10.0);
    	AnchorPane.setBottomAnchor(deleteButton, 10.0);
    	AnchorPane.setRightAnchor(inspectButton, 130.0);
    	AnchorPane.setTopAnchor(inspectButton, 10.0);
    	AnchorPane.setBottomAnchor(inspectButton, 10.0);
    	AnchorPane.setRightAnchor(runButton, 80.0);
    	AnchorPane.setTopAnchor(runButton, 10.0);
    	AnchorPane.setBottomAnchor(runButton, 10.0);
    	
    	itemPane.getChildren().setAll(label, deleteButton, runButton, inspectButton);
    	profileListView.getItems().add(itemPane);
    }

    private ListView<AnchorPane> addServiceRegistry(ServiceRegistry registry) {
    	
    	TitledPane registryPane = new TitledPane();
    	registryPane.setText(registry.getServiceDescription().getServiceName());
    	
    	AnchorPane registryAnchorPane = new AnchorPane(); 	
    	ListView<AnchorPane> registryListView = new ListView<AnchorPane>();	
    	
    	registryPane.setContent(registryAnchorPane);
    	registryAnchorPane.getChildren().add(registryListView);
    	AnchorPane.setTopAnchor(registryListView, -10.0);
    	AnchorPane.setBottomAnchor(registryListView, -10.0);
    	AnchorPane.setLeftAnchor(registryListView, -10.0);
    	AnchorPane.setRightAnchor(registryListView, -10.0);
    	serviceRegistryAcc.getPanes().add(registryPane);
    	
    	return registryListView;
    }
    
    private void addServicesProfiles() {
    	for (AtomicService service : GlobalServiceInfo.getServices()) {
    		addServiceProfiles(service.getServiceDescription().getServiceName());
    	}
    }
    
    private void addServiceProfiles(String serviceName) {
    	
    	ServiceDescription Idescription = null;
    	
    	for (ServiceRegistry registry : GlobalServiceInfo.getServiceRegistries()) {
    		
    		Idescription = registry.getService(serviceName);
    		
    		if (Idescription != null) {
    			break;
    		}
    		
    	}
    	
    	ServiceDescription description = Idescription;
    	
    	List<Class<?>> allProfiles = GlobalServiceInfo.getServiceProfileClasses();
    	AtomicService service = GlobalServiceInfo.getService(serviceName);
    	List<ServiceProfile> serviceProfiles = service.getServiceProfiles(); 
		List<String> availableProfiles=new ArrayList<>();
		
		for(int i=0;i< serviceProfiles.size();i++){
			ServiceProfile profile = serviceProfiles.get(i);
			availableProfiles.add(profile.getClass().getName());
		}
		
		for(int i=0;i<allProfiles.size();i++){
			try{				
				if(!availableProfiles.contains(allProfiles.get(i).getName())){
				
					Constructor<?> [] constructors = allProfiles.get(i).getConstructors();
					ServiceProfile profile=null;
									
					for(int j = 0;j < constructors.length; j++){
						
						if(constructors[j].getParameterTypes().length == 0){
							profile = (ServiceProfile)constructors[j].newInstance();
							break;
						}
						else if (constructors[j].getParameterTypes().length == 1 && constructors[j].getParameterTypes()[0].equals(ServiceDescription.class)) {
							profile = (ServiceProfile)constructors[j].newInstance(description);
							break;
						}
					}
					
					if (profile.getDefaultEnabled()) {
						service.addServiceProfile(profile);
					}	
				
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
    }

    private AnchorPane addService(String serviceName) {
	
    	AnchorPane itemPane = new AnchorPane();

    	Button inspectButton = new Button();
    	inspectButton.setPrefWidth(32);
    	inspectButton.setPrefHeight(32);
    	inspectButton.setLayoutY(5);
    	inspectButton.setId("inspectButton");
    	inspectButton.setOnAction(event->{
    		 			
    		try{
        	    FXMLLoader loader = new FXMLLoader();
        	    loader.setLocation(MainGui.class.getResource("view/serviceProfileDialog.fxml"));
        	    AnchorPane pane = (AnchorPane) loader.load();

        	    Stage dialogStage = new Stage();
        	    dialogStage.setTitle(serviceName);
        	    
        		ServiceProfileController controller=(ServiceProfileController)loader.getController();
        		controller.setStage(dialogStage);
        		controller.setServiceProfileClasses(GlobalServiceInfo.getServiceProfileClasses());
        		controller.setService(GlobalServiceInfo.getService(serviceName));
        		
        	    Scene dialogScene = new Scene(pane);
        	    dialogScene.getStylesheets().add(MainGui.class.getResource("view/application.css").toExternalForm());

        	    dialogStage.initOwner(primaryStage);
        	    dialogStage.setScene(dialogScene);
        	    dialogStage.setResizable(false);
        	    dialogStage.show();
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    	});
    	
    	ServiceDescription Idescription = null;
    	ServiceRegistry Iregistry = null;
    	
    	for (ServiceRegistry registry : serviceRegistries) {
    		
    		Idescription = registry.getService(serviceName);
    		
    		if (Idescription != null) {
    			Iregistry = registry;
    			break;
    		}
    		
    	}
    	
    	ServiceDescription description = Idescription;

    	Label label = new Label();
    	label.setLayoutY(15);
    	label.setText(serviceName);
    	
    	Circle circle = new Circle();
    	circle.setOnMouseClicked(event -> {
    		if(circle.getFill().equals(Color.DARKRED)) {
    			selectedEntity.getManagingSystem().getKnowledge().removeServiceFromBlacklist(description);
    		    circle.setFill(Color.GREEN);
    		}
    		else {    		    
    			selectedEntity.getManagingSystem().getKnowledge().addServiceToBlacklist(description);
    		    circle.setFill(Color.DARKRED);
    		}
    	});
    	
    	circle.setLayoutY(20);
    	circle.setRadius(10);
    	
    	if (!selectedEntity.getManagingSystem().getKnowledge().isBlacklisted(description))
    	    circle.setFill(Color.GREEN);
    	else
    	    circle.setFill(Color.DARKRED);

    	AnchorPane.setLeftAnchor(circle, 10.0);
    	AnchorPane.setLeftAnchor(label, 40.0);
    	AnchorPane.setRightAnchor(inspectButton, 10.0);
    	itemPane.getChildren().setAll(circle, label, inspectButton);
    	serviceRegistryPanes.get(Iregistry.getServiceDescription().getServiceEndpoint()).getItems().add(itemPane);

    	return itemPane;
    }

}
