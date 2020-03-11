package application.view.controller;

import java.awt.image.BufferedImage;
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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.imageio.ImageIO;

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
import tas.data.serviceinfo.GlobalServiceInfo;
import tas.data.systemprofile.SystemProfile;
import tas.data.systemprofile.SystemProfileDataHandler;
import tas.data.systemprofile.SystemProfileExecutor;
import tas.mape.system.entity.MAPEKComponent;
import tas.mape.system.entity.SystemEntity;
import tas.mape.system.entity.WorkflowExecutor;
import tas.mape.system.structure.AbstractSystem;
import tas.mape.system.structure.DoubleLoopSystem;
import tas.mape.system.structure.SoloLoopSystem;
import tas.workflowAnalyzer.WorkflowAnalyzer;
import tas.mape.system.entity.MAPEKComponent.Builder;
import application.MainGui;
import application.model.CostEntry;
import application.model.PerformanceEntry;
import application.model.ReliabilityEntry;
import application.utility.NodeVisitor;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

// TODO Service panes list with endpoints instead of names, names are not unique, endpoints usually are.
// TODO Reset registry IDs when changing service data
public class ApplicationController implements Initializable {

    Stage primaryStage;
    
    // for generating kinds of charts
    SystemRunChartController chartController;
    
    // for generating kinds of table views
    TableViewController tableViewController;

    // the path of important files 
    //String workflowPath = "src"+File.separator+"resources" + File.separator + "workflow_test1.txt";
    
    String baseDir="";
    
    String resourceDirPath = baseDir + "resources" + File.separator;
    String resultDirPath = baseDir + "results" + File.separator;
    String fileDirPath = baseDir + "resources" + File.separator + "files" + File.separator;
    
    String workflowPath;
    String workflowFilePath = fileDirPath + "workflow" + File.separator;
    String profileFilePath = fileDirPath + "profiles" + File.separator;
    String serviceDataFilePath = fileDirPath + "service data" + File.separator;
    String resultFilePath = resultDirPath + "result.csv";
    String logFilePath = resultDirPath + "log.csv";
    String defaultServiceDataPath = serviceDataFilePath + "DefaultServiceData.xml";

    ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(5);

    List<ServiceRegistry> serviceRegistries;
    
    SystemEntity selectedEntity;
    List<SystemEntity> entities = new ArrayList<>();
    Map<String, Boolean> workflowAnalyzed = new HashMap<>(); 
    Map<String, List<ServiceRegistry>> entityRegistries;
    List<Pair<String, Class<? extends AbstractSystem<SystemEntity>>>> systemLoops = 
    		Arrays.asList(new Pair<>("Solo Loop System", SoloLoopSystem.class), new Pair<>("Double Loop System", DoubleLoopSystem.class));
    Set<Button> profileRuns = new HashSet<>();
    Map<String, AnchorPane> servicePanes = new ConcurrentHashMap<>();
    Map<String, ListView<AnchorPane>> serviceRegistryPanes = new ConcurrentHashMap<>();
    int maxSteps;
    String entityBeingAnalyzed;
    boolean analyzed;
    
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
    MenuItem saveLogMenuItem;

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
    MenuItem saveReliabilityGraphMenuItem;

    @FXML
    MenuItem saveCostGraphMenuItem;
    
    @FXML
    MenuItem saveInvCostGraphMenuItem;
    
    @FXML
    MenuItem savePerformanceGraphMenuItem;

    @FXML
    MenuItem helpMenuItem;
    
    @FXML 
    MenuItem exampleScenariosMenuItem;
    
    @FXML
    MenuItem changeServiceCollectionMenuItem;
    
    @FXML
    ToolBar toolBar;
    
    @FXML
    Button configureButton;
    
    @FXML
    AnchorPane canvasPane;
    
    @FXML
    TextField sliceTextField;
    
    @FXML
    MenuItem saveInvMenuItem;
    
    @FXML
    TabPane entityTabPane;
    
    @FXML
    TitledPane serviceTitledPane;
    
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
    	this.addTestEntities();

    	/*scheduExec.scheduleAtFixedRate(new Runnable() {
    	    @Override
    		public void run() {
    	    	
    			Set<String> services = compositeService.getCache().getServices();
    			Set<String> registeredServices = servicePanes.keySet();

    			for (String service : registeredServices) {
    			    
    			    Platform.runLater(new Runnable() {
    				@Override
    				public void run() {
    					
    					Circle circle = (Circle) servicePanes.get(service).getChildren().get(0);
    					
    					// TODO check on endpoints, not names
    				    if(services != null && services.contains(service))
    					    circle.setFill(Color.GREEN);
    				    else
    					    circle.setFill(Color.DARKRED);
    				 }
    			    });
    			}

    	    }
    	}, 0, 1000, TimeUnit.MILLISECONDS);*/
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
    
	public void addEntityToList(SystemEntity entity) {
		
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
		    	AnchorPane parent = (AnchorPane) deleteButton.getParent();
		    	Button button = (Button) parent.getChildren().get(0);
		    	entities.removeIf(x -> x.getEntityName().equals(button.getText()));
		    	entityListView.getItems().remove(parent);
		    	workflowAnalyzed.remove(button.getText());
		    }
		});
    	
    	AnchorPane.setLeftAnchor(selectButton, 10.0);
    	AnchorPane.setTopAnchor(selectButton, 10.0);
    	AnchorPane.setBottomAnchor(selectButton, 10.0);
    	AnchorPane.setRightAnchor(deleteButton, 10.0);
    	AnchorPane.setTopAnchor(deleteButton, 10.0);
    	AnchorPane.setBottomAnchor(deleteButton, 10.0);
    	
    	entityPane.getChildren().addAll(deleteButton, selectButton);
    	entityListView.getItems().add(entityPane);
	}
	
	public void analyzeEntity(SystemEntity entity) {
		
		System.err.print("starting analyzing for " + entity.getEntityName() + "\n");
		Map<Description, Pair<List<ServiceDescription>, Double>> usableServices = 
				WorkflowAnalyzer.analyzeWorkflow(entity.getManagedSystem().getAssistanceService());
		
		entity.getManagingSystem().resetMonitorProbes();
		
		for (Description d : usableServices.keySet()) {
			System.err.print("------------------------------------------------------------ \n");
			
			for (ServiceDescription sd : usableServices.get(d).getKey()) {
				System.err.print("service name: " + sd.getServiceName() + ", " + d.toString() + " \n");
			}
			
			System.err.print("=== \n");
			System.err.print("description usage chance :" + usableServices.get(d).getValue() + " \n");
			System.err.print("------------------------------------------------------------ \n");
		}
		
		System.err.print("done analyzing\n");
		entity.getManagingSystem().setUsedServicesAndChances(usableServices);
		workflowAnalyzed.put(entity.getEntityName(), true);
	}

    public void setPrimaryStage(Stage primaryStage) {
    	this.primaryStage = primaryStage;
    }
    
    private void initChartController() {
    	chartController = new SystemRunChartController(systemRunChartPane);
    }
    
   /* public void setTasStart(TASStart tasStart) {
      	chartController = new ChartController(reliabilityChartPane, costChartPane,performanceChartPane,invCostChartPane,
    			avgReliabilityChartPane, avgCostChartPane, avgPerformanceChartPane, invRateChartPane, tasStart.getServiceTypes());
    	tableViewController = new TableViewController(reliabilityTableView, costTableView,performanceTableView);
    }*/
    
    private void addTestEntities() {
    	
		WorkflowExecutor workflowExecutor = new WorkflowExecutor(Arrays.asList(GlobalServiceInfo.getServiceRegistry("se.lnu.service.registry")));	
		workflowExecutor.setWorkflowPath(workflowFilePath + "TeleAssistanceWorkflow.txt");		
		MAPEKComponent.Builder builder = new Builder();
		
		try {
			builder.initializeKnowledge(10, new ArrayList<String>(Arrays.asList("se.lnu.service.registry")))
			 	   .initializePlanner()
				   .initializeAnalyzer(100, 1)
				   .initializeMonitor(0.05, 0.05);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		MAPEKComponent component = builder.build();
		SystemEntity systemEntity = new SystemEntity("Test Entity", workflowExecutor, component);
		addEntityToList(systemEntity);
		
		workflowExecutor = new WorkflowExecutor(Arrays.asList(GlobalServiceInfo.getServiceRegistry("se.lnu.service.registry"), 
				GlobalServiceInfo.getServiceRegistry("se.lnu.service.registry2")));	
		workflowExecutor.setWorkflowPath(workflowFilePath + "workflow_test1.txt");
		
		builder = new Builder();
		
		try {
			builder.initializeKnowledge(10, new ArrayList<String>(Arrays.asList("se.lnu.service.registry", "se.lnu.service.registry2")))
			 	   .initializePlanner()
				   .initializeAnalyzer(100, 1)
				   .initializeMonitor(0.05, 0.05);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		component = builder.build();
		systemEntity = new SystemEntity("Test Entity 2", workflowExecutor, component);
		addEntityToList(systemEntity);
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
    	
    	for (String registryEndpoint : selectedEntity.getManagingSystem().getRegistryEndpoints()) {
    		serviceRegistries.add(GlobalServiceInfo.getServiceRegistry(registryEndpoint));
    	}
    	
    	this.serviceRegistries = serviceRegistries;
    	openServicesMenuItem.fire();
    }

    private void addItems() {
    	progressBar = new ProgressBar(0);
    	invocationLabel = new Label();
    	toolBar.getItems().addAll(new Label("Progress "), progressBar, invocationLabel);
    }
    
    private void setButton() {
    	
    	sliceTextField.textProperty().addListener(new ChangeListener<String>() {
    	    @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
    	        if (newValue.matches("\\d*")) {
    	        	//System.out.println(newValue);
    	        } else {
    	        	sliceTextField.setText(oldValue);
    	        }
    	    }
    	});
    	
    	sliceTextField.setOnKeyPressed(new EventHandler<KeyEvent>(){
    		@Override
    		 public void handle(KeyEvent event){
    			if (event.getCode().equals(KeyCode.ENTER)) {
    				
    				// TODO CHART SLICES
					//chartController.generateAvgCharts(resultFilePath, tasStart.getCurrentSteps(),Integer.parseInt(sliceTextField.getText()));
    		    }
    		 }
    	});

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
    		fileChooser.setInitialDirectory(new File(resourceDirPath));
    		fileChooser.setTitle("Select profile");
    		FileChooser.ExtensionFilter extension = new FileChooser.ExtensionFilter("Add Files(*.xml)", "*.xml");
    		fileChooser.getExtensionFilters().add(extension);
    		File file = fileChooser.showOpenDialog(primaryStage);
    		if (file != null) {
    		    System.out.println(file.getPath());
    		    addProfile(file.getPath());
    		}
    	    }
    	});

    	saveRunMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		FileChooser fileChooser = new FileChooser();
    		fileChooser.setInitialDirectory(new File(resourceDirPath));
    		fileChooser.setTitle("Save Run");
    		File file = fileChooser.showSaveDialog(primaryStage);
    		if (file != null) {
    		    try {
    			Files.copy(Paths.get(resultFilePath), Paths.get(file.getPath() + ".csv"), StandardCopyOption.REPLACE_EXISTING);
    		    } catch (IOException e) {
    			e.printStackTrace();
    		    }
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

    	saveLogMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		FileChooser fileChooser = new FileChooser();
    		fileChooser.setInitialDirectory(new File(resultDirPath));
    		fileChooser.setTitle("Save Log");
    		File file = fileChooser.showSaveDialog(primaryStage);
    		if (file != null) {
    		    try {
    			Files.copy(Paths.get(logFilePath), Paths.get(file.getPath() + ".csv"), StandardCopyOption.REPLACE_EXISTING);
    		    } catch (IOException e) {
    			e.printStackTrace();
    		    }
    		}
    	    }
    	});

    	saveReliabilityGraphMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save Reliability Graph");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    			    SnapshotParameters param = new SnapshotParameters();
    			    param.setDepthBuffer(true);
    			    // TODO
    			    WritableImage snapshot = null;//chartController.reliabilityChart.snapshot(param, null);
    			    BufferedImage tempImg = SwingFXUtils.fromFXImage(snapshot, null);

    			    File outputfile = new File(file.getPath() + ".png");
    			    ImageIO.write(tempImg, "png", outputfile);

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	    }
    	});

    	saveCostGraphMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save Cost Graph");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    			    SnapshotParameters param = new SnapshotParameters();
    			    param.setDepthBuffer(true);
    			    // TODO
    			    WritableImage snapshot = null; //chartController.costChart.snapshot(param, null);
    			    BufferedImage tempImg = SwingFXUtils.fromFXImage(snapshot, null);

    			    File outputfile = new File(file.getPath() + ".png");
    			    ImageIO.write(tempImg, "png", outputfile);

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	    }
    	});
    	
    	
    	saveInvCostGraphMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save Invocation Cost Graph");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    			    SnapshotParameters param = new SnapshotParameters();
    			    param.setDepthBuffer(true);
    			    // TODO
    			    WritableImage snapshot = null;//chartController.invCostChart.snapshot(param, null);
    			    BufferedImage tempImg = SwingFXUtils.fromFXImage(snapshot, null);

    			    File outputfile = new File(file.getPath() + ".png");
    			    ImageIO.write(tempImg, "png", outputfile);

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	    }
    	});

    	savePerformanceGraphMenuItem.setOnAction(event->{
    		try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save Performance Graph");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    			try {
    			    SnapshotParameters param = new SnapshotParameters();
    			    param.setDepthBuffer(true);
    			    // TODO
    			    WritableImage snapshot = null;//chartController.performanceChart.snapshot(param, null);
    			    BufferedImage tempImg = SwingFXUtils.fromFXImage(snapshot, null);

    			    File outputfile = new File(file.getPath() + ".png");
    			    ImageIO.write(tempImg, "png", outputfile);

    			} catch (IOException e) {
    			    e.printStackTrace();
    			}
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    	});
    	
    	
    	saveInvMenuItem.setOnAction(event-> {
    		
    		// TODO saving
    		/*try {
    		    FileChooser fileChooser = new FileChooser();
    		    fileChooser.setInitialDirectory(new File(resultDirPath));
    		    fileChooser.setTitle("Save Invocations");
    		    File file = fileChooser.showSaveDialog(primaryStage);
    		    if (file != null) {
    		    	    		        		    	
    		    	List<String> services = new ArrayList<>();
    		    	for(String serviceName : tasStart.getServiceTypes().keySet())
    		    		services.add(serviceName);
    		    	
    				FileManager writer=new FileManager(file.getPath()+".csv");
    				writer.setMode(FileManager.WRITING);
    				writer.open();
    				
    				LinkedHashMap<Integer,Double> failureRates=chartController.failureRates;
    				LinkedHashMap<Integer,Double> responseTimes=chartController.responseTimes;
    				LinkedHashMap<Integer,Double> costs=chartController.costs;   
    				LinkedHashMap<Integer,Map<String,Double>> invocationRates=chartController.invocationRates;
    				
    				StringBuilder build;
    				InputProfile profile = ProfileExecutor.profiles.get("test");
    				
    				if (profile != null) {
    					
    					Requirement reliabilityReq=profile.getRequirement("reliability");
    					Requirement performanceReq=profile.getRequirement("performance");
    					
    					List<Integer> frInvocations=new ArrayList<>();
    					String curFailureRate="0";
    					int frIndex=1;
    					List<Integer> rtInvocations=new ArrayList<>();
    					String curResponseTime="0";
    					int rtIndex=1;
    					
    					if(reliabilityReq!=null){
    						for(int invocations: reliabilityReq.getValues().keySet()){
    							frInvocations.add(invocations);
    						}
    						curFailureRate=reliabilityReq.getValues().get(0);
    					}

    					if(performanceReq!=null){
    						for(int invocations:performanceReq.getValues().keySet()){
    							rtInvocations.add(invocations);
    						}
    						curResponseTime=performanceReq.getValues().get(0);
    					}
    					
        				for(int invocations:failureRates.keySet()){
        					build=new StringBuilder();
        					
        					if(reliabilityReq!=null){
        						if(frIndex<frInvocations.size()){
        							if(invocations>=frInvocations.get(frIndex)){
        								curFailureRate=reliabilityReq.getValues().get(frInvocations.get(frIndex));
        								frIndex++;
        							}
        						}
        					}
        					
        					if(performanceReq!=null){
        						if(rtIndex<rtInvocations.size()){
        							if(invocations>=rtInvocations.get(rtIndex)){
        								curResponseTime=performanceReq.getValues().get(rtInvocations.get(rtIndex));
        								rtIndex++;
        							}
        						}
        					}
        					
        					build.append(invocations+","+failureRates.get(invocations)+","+curFailureRate+","+
        							responseTimes.get(invocations)+","+curResponseTime+","+costs.get(invocations)+",");
        									
        					Map<String,Double> rates=invocationRates.get(invocations);
        					for(String service:services){ 						
        						Double rate=0.0;
        						if(rates.containsKey(service))
        							rate=rates.get(service);
        						build.append(rate+",");
        					}
        					//System.out.println(build.toString());
        					writer.write(build.toString());
        				}
    				}
    				writer.close();	    		    	
    		    }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}*/
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

    	helpMenuItem.setOnAction(new EventHandler<ActionEvent>() {
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
    	    	try (PrintWriter out = new PrintWriter(workflowPath)) {
    	    	    out.println(workflowTextArea.getText());
    	    	} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
    	    	
    	    	workflowAnalyzed.put(selectedEntity.getEntityName(), true);
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
					
					if (selectedEntity != null) {
						selectEntity();
					}
				}
		    }
    	});

    }
    
    private void fillSystemProfiles() {
    	File folder = new File(profileFilePath);
    	File[] files = folder.listFiles();

    	try {
    	    for (File file : files) {
        		if (file.isFile()) {
        		    if (file.getName().lastIndexOf('.') > 0)
        		    	System.err.print("test");
        			this.addSystemProfile(file.getAbsolutePath());
        		}
    	    }
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }
    
    private void addSystemProfile(String profilePath) {
    	
    	AnchorPane itemPane = new AnchorPane();

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
    	profileRuns.add(runButton);
    	
    	runButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {

    	    if (runButton.getId().equals("runButton")) {

    			chartController.resetProbe();
    			SystemProfile profile = SystemProfileDataHandler.readFromXml(profilePath);
    			SystemProfileDataHandler.activeProfile = profile;
    			
    			Task<Void> task = new Task<Void>() {
    			    @Override
    			    protected Void call() throws Exception {			    	

    			    	analyzed = false;
    			    	
    			    	// Analyze entity workflows if needed
    			    	for (int i = 0; i < profile.getSystemType().getMaxEntities(); i++) {
    			    		
    			    		final int index = i;
    			    		
    			    		SystemEntity entity = entities.stream()
    			    				.filter(x -> x.getEntityName().equals(profile.getParticipatingEntity(index))).findFirst().orElse(null);
    			    		
    			    		chartController.addEntityToProbe(entity);
    			    		
    			    		if (!workflowAnalyzed.get(entity.getEntityName())) {
    	    			    	System.err.print("analyzing \n");
    	    			    	entityBeingAnalyzed = entity.getEntityName();
    			    			analyzeEntity(entity);
    			    		}
    			    	}
    			    	
    			    	System.err.print("test2 \n");
    			    	
    			    	analyzed = true;
    			    	entityBeingAnalyzed = "";

    			    	// Execute system
    			    	SystemProfileExecutor.execute(entities);
					    
    				    Platform.runLater(new Runnable() {
        					@Override
        					public void run() {
        					    runButton.setId("runButton");
        						chartController.clear();
        						//tableViewController.clear();
        						
        						chartController.generateSystemRunChart();
        						//chartController.generateCharts(resultFilePath, tasStart.getCurrentSteps());
        						//chartController.generateAvgCharts(resultFilePath, tasStart.getCurrentSteps(),Integer.parseInt(sliceTextField.getText()));

        					    /*tableViewController.fillReliabilityDate(resultFilePath);
        					    tableViewController.fillCostData(resultFilePath);
        						tableViewController.fillPerformanceData(resultFilePath);*/
        					}
    				    });
    				
    				return null;
    				
    			    }
    			};
    			
    			ExecutionThread thread = new ExecutionThread("main", task);
    			thread.setDaemon(true);
    			thread.start();

    			System.out.println("Start task!!");
    			
    			Task<Void> progressTask = new Task<Void>() {
    			    @Override
    			    protected Void call() throws Exception {
    			    	while (WorkflowAnalyzer.getCurrentSteps() < WorkflowAnalyzer.analyzerCycles && !analyzed) {
        				    Platform.runLater(new Runnable() {
            					@Override
            					public void run() {	
        							invocationLabel.setText("Analyzing the workflows: Analyzing the workflow of " + entityBeingAnalyzed
        									+ " | PROGRESS: "+ WorkflowAnalyzer.getCurrentSteps() + " / " + WorkflowAnalyzer.analyzerCycles);
            					}
        				    });
        				    
        				    updateProgress(WorkflowAnalyzer.getCurrentSteps(), WorkflowAnalyzer.analyzerCycles);
        				    Thread.sleep(100);
    			    	}
    			    	
    			    	while (analyzed) {
        				    Platform.runLater(new Runnable() {
            					@Override
            					public void run() {	
        							invocationLabel.setText("Executing system run...");
            					}
        				    });
        				    
        				    //updateProgress(WorkflowAnalyzer.getCurrentSteps(), WorkflowAnalyzer.analyzerCycles);
        				    Thread.sleep(100);
    			    	}
    			    	
        				/*while (probe.workflowInvocationCount < maxSteps) {
        					
        				    Platform.runLater(new Runnable() {
            					@Override
            					public void run() {
            						
            						if (!analyzed) {
            							invocationLabel.setText("Analyzing the workflows...");
            						}
            						else {	
                					    invocationLabel.setText(" " + probe.workflowInvocationCount + " / " + maxSteps);
            						}
            					}
        				    });
        				    
        				    updateProgress(probe.workflowInvocationCount, maxSteps);
        				    Thread.sleep(1000);
        				}*/
        				
        				Platform.runLater(new Runnable() {
        				    @Override
        				    public void run() {
        				    	invocationLabel.setText("Analyzing the workflows...");
        				    }
        				});
        				
        				//updateProgress(probe.workflowInvocationCount, maxSteps);
        				System.out.println("stop task!!");
        				return null;
    			    }
    			};
    			
    			progressBar.progressProperty().bind(progressTask.progressProperty());
    			new Thread(progressTask).start();
    	    }
    	    
    	    else {
    	    	// TODO Stop needed?
    	    	//tasStart.stop();
    	    	
    	    	// TODO CHARTS
    		    /*Platform.runLater(new Runnable() {
    			@Override
    			public void run() {
    			    runButton.setId("runButton");
    				chartController.clear();
    				tableViewController.clear();
    				
    				
    				//chartController.generateCharts(resultFilePath, tasStart.getCurrentSteps());
    				//chartController.generateAvgCharts(resultFilePath, tasStart.getCurrentSteps(), Integer.parseInt(sliceTextField.getText()));

    			    tableViewController.fillReliabilityDate(resultFilePath);
    			    tableViewController.fillCostData(resultFilePath);
    				tableViewController.fillPerformanceData(resultFilePath);
    			}
    		    });*/
    	    }
    	    }
    	});
    	
    	Label label = new Label();
    	label.setLayoutY(15);
    	label.setText(Paths.get(profilePath).getFileName().toString().split("\\.")[0]);
    	
    	AnchorPane.setTopAnchor(label, 10.0);
    	AnchorPane.setBottomAnchor(label, 10.0);
    	AnchorPane.setLeftAnchor(label, 10.0);
    	AnchorPane.setRightAnchor(inspectButton, 60.0);
    	AnchorPane.setRightAnchor(runButton, 10.0);
    	
    	itemPane.getChildren().setAll(label, runButton, inspectButton);
    	profileListView.getItems().add(itemPane);
    }

    /*private void fillProfiles() {
	File folder = new File(profileFilePath);
	File[] files = folder.listFiles();

	try {
	    for (File file : files) {
		if (file.isFile()) {
		    // System.out.println(file.getName());
		    if (file.getName().lastIndexOf('.') > 0)
			this.addProfile(file.getAbsolutePath());
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	// this.addProfile("resources/files/inputProfile1.xml");
	// this.addProfile("/inputProfile2.xml");
    }*/

    private void addProfile(String profilePath) {

    	final String path = profilePath;

    	AnchorPane itemPane = new AnchorPane();
    	//itemPane.setPrefHeight(40);
    	//itemPane.setMinHeight(40);

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
    		    loader.setLocation(MainGui.class.getResource("view/inputProfileDialog.fxml"));
    		    AnchorPane pane = (AnchorPane) loader.load();

    		    Stage dialogStage = new Stage();
    		    dialogStage.setTitle("Input Profile");
    		    
    			InputProfileController controller=(InputProfileController)loader.getController();
    			controller.setStage(dialogStage);
    			controller.viewProfile(path);

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

    	// profileInspects.put(inspectButton, profilePath);

    	Button runButton = new Button();
    	runButton.setPrefWidth(32);
    	runButton.setPrefHeight(32);
    	runButton.setLayoutY(5);
    	runButton.setId("runButton");
    	profileRuns.add(runButton);
    	if (this.workflowPath == null)
    	    runButton.setDisable(true);
    	// profileInspects.put(runButton, profilePath);

    	Label label = new Label();
    	label.setLayoutY(15);
    	label.setText(Paths.get(profilePath).getFileName().toString().split("\\.")[0]);

    	final Circle circle = new Circle();
    	circle.setLayoutY(20);
    	circle.setFill(Color.GREEN);
    	circle.setRadius(10);

    	/*runButton.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {

    	    if(runButton.getId().equals("runButton")){

    			probe.reset();
    			
    			Task<Void> task = new Task<Void>() {
    			    @Override
    			    protected Void call() throws Exception {

    				//System.out.println("Task has been called!!");
    				//System.out.println(workflowPath);

    				if (workflowPath != null) {
    					
    				    //if(preAdaptation!=null)
    					//      adaptationEngines.get(preAdaptation).stop();
    					//    preAdaptation=currentAdaptation;
    					//    adaptationEngines.get(currentAdaptation).start();

    				    //System.out.println(workflowPath);

    				    Platform.runLater(new Runnable() {
        					@Override
        					public void run() {
        					    circle.setFill(Color.DARKRED);
        					    runButton.setId("stopButton");
        					}
    				    });

    				    //System.out.println("Before executing workflow!!");

    				    tasStart.executeWorkflow(workflowPath, path);

    				    //System.out.println("Finish executing workflow!!");

    				    //if(preAdaptation!=null)
    					//	adaptationEngines.get(preAdaptation).stop();
    					//    preAdaptation=null;
    					    
    				    Platform.runLater(new Runnable() {
        					@Override
        					public void run() {
        					    circle.setFill(Color.GREEN);
        					    runButton.setId("runButton");
        						chartController.clear();
        						tableViewController.clear();
        						
        						chartController.generateCharts(resultFilePath, tasStart.getCurrentSteps());
        						chartController.generateAvgCharts(resultFilePath, tasStart.getCurrentSteps(),Integer.parseInt(sliceTextField.getText()));

        					    tableViewController.fillReliabilityDate(resultFilePath);
        					    tableViewController.fillCostData(resultFilePath);
        						tableViewController.fillPerformanceData(resultFilePath);
        					}
    				    });
    				}
    				return null;
    			    }
    			};

    			// System.out.println("Bind progress bar with task!!");
    			
    			ExecutionThread thread=new ExecutionThread("main",task);
    			thread.setDaemon(true);
    			thread.start();
    			
    			//Thread thread = new Thread(task);
    			//thread.setDaemon(true);
    			//thread.start();

    			System.out.println("Start task!!");
    			ProfileExecutor.readFromXml(path, "test");
    			maxSteps = ProfileExecutor.profiles.get("test").getMaxSteps();
    			Task<Void> progressTask = new Task<Void>() {
    			    @Override
    			    protected Void call() throws Exception {
        				while (probe.workflowInvocationCount < maxSteps) {
        				    Platform.runLater(new Runnable() {
            					@Override
            					public void run() {
            					    invocationLabel.setText(" " + probe.workflowInvocationCount + " / " + maxSteps);
            					}
        				    });
        				    updateProgress(probe.workflowInvocationCount, maxSteps);
        				    Thread.sleep(1000);
        				}
        				Platform.runLater(new Runnable() {
        				    @Override
        				    public void run() {
        				    	invocationLabel.setText("" + maxSteps + " / " + maxSteps);
        				    }
        				});
        				updateProgress(probe.workflowInvocationCount, maxSteps);
        				return null;
    			    }
    			};
    			progressBar.progressProperty().bind(progressTask.progressProperty());
    			new Thread(progressTask).start();
    	    }
    	    
    	    else{ 	
    	    	//System.out.println("stop workflow");
    		    //if(preAdaptation!=null)
    			//	 adaptationEngines.get(preAdaptation).stop();
    			//    preAdaptation=null;
    			    
    	    	tasStart.stop();
    	    	//tasStart.pause();
    	    				
    		    Platform.runLater(new Runnable() {
    			@Override
    			public void run() {
    			    circle.setFill(Color.GREEN);
    			    runButton.setId("runButton");
    				chartController.clear();
    				tableViewController.clear();
    				
    				chartController.generateCharts(resultFilePath, tasStart.getCurrentSteps());
    				chartController.generateAvgCharts(resultFilePath, tasStart.getCurrentSteps(),Integer.parseInt(sliceTextField.getText()));

    			    tableViewController.fillReliabilityDate(resultFilePath);
    			    tableViewController.fillCostData(resultFilePath);
    				tableViewController.fillPerformanceData(resultFilePath);
    			}
    		    });
    	    }
    	    }
    	});*/

    	AnchorPane.setLeftAnchor(circle, 10.0);
    	AnchorPane.setLeftAnchor(label, 40.0);
    	AnchorPane.setRightAnchor(inspectButton, 60.0);
    	AnchorPane.setRightAnchor(runButton, 10.0);

    	itemPane.getChildren().setAll(circle, label, runButton, inspectButton);

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
    	
    	// TODO Check if this works with new system
    	Circle circle = new Circle();
    	circle.setOnMouseClicked(event -> {
    		if(circle.getFill().equals(Color.DARKRED)) {
    			selectedEntity.getManagingSystem().removeServiceFromBlacklist(description);
    		    circle.setFill(Color.GREEN);
    		}
    		else {    		    
    			selectedEntity.getManagingSystem().addServiceToBlacklist(description);
    		    circle.setFill(Color.DARKRED);
    		}
    	});
    	
    	circle.setLayoutY(20);
    	circle.setRadius(10);
    	
    	if (!selectedEntity.getManagingSystem().isBlacklisted(description))
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
