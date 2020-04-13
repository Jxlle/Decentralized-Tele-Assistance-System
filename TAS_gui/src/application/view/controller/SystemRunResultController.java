package application.view.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import  org.apache.poi.hssf.usermodel.HSSFSheet;
import  org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;

import application.model.ServiceCombinationEntry;
import application.utility.Arrow;
import application.utility.ScatterLineChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Pair;
import service.atomic.AtomicService;
import service.registry.ServiceRegistry;
import tas.data.serviceinfo.GlobalServiceInfo;
import tas.mape.communication.message.ProtocolMessageInformation;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.planner.RatingType;
import tas.mape.planner.ServiceCombination;
import tas.mape.probes.ProtocolProbe;
import tas.mape.probes.SystemRunProbe;
import tas.mape.system.entity.MAPEKSystemEntity;

public class SystemRunResultController {
	
	private Accordion entityResultTableAccordion;
	private AnchorPane systemRunChartPane, protocolMessageChartPane, protocolFlowAnchorPane, failureRateErrorChartPane, 
	costChartPane, failureRateChartPane, ratingEvolutionChartPane, ratingEvolutionSystemChartPane, failureRateSystemChartPane;
	private Label protocolDetailsText;
	private SystemRunProbe systemRunProbe = new SystemRunProbe();
	private ProtocolProbe protocolProbe = new ProtocolProbe();
	private Map<String, List<Double>> failureRateErrors, entityFailureRate;
	private int maximumDelta = 10;
	private ScatterLineChart<Number, Number> systemRunChart;
	private LineChart<Number, Number> failureRateErrorChart, ratingChart, ratingSystemChart, failureRateSystemChart, failureRateChart, 
	costChart, protocolMessageChart;
	
	public void setProtocolProbe() {
		protocolProbe.connect();
	}
	
	public void addEntityToProbe(MAPEKSystemEntity entity) {
		systemRunProbe.connect(entity);
	}
	
	public void resetProbes() {
		systemRunProbe.reset();
		protocolProbe.reset();
	}
	
	public SystemRunResultController(AnchorPane systemRunChartPane, AnchorPane protocolMessageChartPane, AnchorPane protocolFlowAnchorPane, AnchorPane failureRateErrorChartPane, 
			AnchorPane costChartPane, AnchorPane failureRateChartPane, AnchorPane failureRateSystemChartPane, AnchorPane ratingEvolutionChartPane, AnchorPane ratingEvolutionSystemChartPane, Accordion entityResultTableAccordion, Label protocolDetailsText) {
		this.systemRunChartPane = systemRunChartPane;
		this.protocolMessageChartPane = protocolMessageChartPane;
		this.protocolFlowAnchorPane = protocolFlowAnchorPane;
		this.failureRateErrorChartPane = failureRateErrorChartPane;
		this.costChartPane = costChartPane;
		this.failureRateChartPane = failureRateChartPane;
		this.failureRateSystemChartPane = failureRateSystemChartPane;
		this.ratingEvolutionChartPane = ratingEvolutionChartPane;
		this.ratingEvolutionSystemChartPane = ratingEvolutionSystemChartPane;
		this.entityResultTableAccordion = entityResultTableAccordion;
		this.protocolDetailsText = protocolDetailsText;
	}
	
	public void clear() {
		systemRunChartPane.getChildren().clear();
		protocolMessageChartPane.getChildren().clear();
		protocolFlowAnchorPane.getChildren().clear();
		failureRateErrorChartPane.getChildren().clear();
		ratingEvolutionChartPane.getChildren().clear();
		ratingEvolutionSystemChartPane.getChildren().clear();
		failureRateChartPane.getChildren().clear();
		failureRateSystemChartPane.getChildren().clear();
		costChartPane.getChildren().clear();
		entityResultTableAccordion.getPanes().clear();
		
		protocolFlowAnchorPane.getChildren().add(protocolDetailsText);
		protocolDetailsText.setDisable(false);
	}
	
	public void saveAll(File directory, List<MAPEKSystemEntity> entities, String latestProfilePath) throws IOException {
		String defaultPath = directory.getPath() + File.separator;
		saveSystemRunPerformanceChart(defaultPath + "Performance Graph");
		SaveProtocolMessageChart(defaultPath +"Protocol Message Graph");
		saveCostEvolutionChart(defaultPath +"Cost Evolution Graph");
		saveChart(ratingChart, defaultPath + "Entity Rating Evolution Graph");
		saveChart(ratingSystemChart, defaultPath + "System Rating Graph");
		saveChart(failureRateChart, defaultPath + "Entity Failure Rate Graph");
		saveChart(failureRateSystemChart, defaultPath + "System Failure Rate Graph");	
		saveChart(failureRateErrorChart, defaultPath + "Failure Rate Error Graph");	
		saveRunData(defaultPath + "Run Results", entities, latestProfilePath);
	}
	
	public void saveSystemRunPerformanceChart(String filePath) throws IOException {
		// Save system run chart
		saveChart(systemRunChart, filePath);
	}
	
	public void SaveProtocolMessageChart(String filePath) throws IOException {
		// Saving protocol message chart
		saveChart(protocolMessageChart, filePath);
	}
	
	public void saveRatingCharts(String filePath) throws IOException {
		// Save entity rating chart
		saveChart(ratingChart, filePath + "1");
		
		// Save system rating chart
		saveChart(ratingSystemChart, filePath + "2");
	}
	
	public void saveFailureRateEvolutionCharts(String filePath) throws IOException {
		// Save entity failure rate chart
		saveChart(failureRateChart, filePath + "1");
		
		// Save system failure rate chart
		saveChart(failureRateSystemChart, filePath + "2");
		
		// Save failure rate error chart
		saveChart(failureRateErrorChart, filePath + "3");
	}
	
	public void saveCostEvolutionChart(String filePath) throws IOException {
		// Save cost evolution chart
		saveChart(costChart, filePath);
	}
	
	public void saveRunData(String filePath, List<MAPEKSystemEntity> entities, String latestProfilePath) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        
        // ENVIRONMENT DATA
        HSSFSheet envSheet = workbook.createSheet("Environment Data");  
        int rowIndex = 0;
        
        HSSFFont font = workbook.createFont();
        CellStyle bold = workbook.createCellStyle();
    	font.setBold(true);
    	bold.setFont(font);
    	
        HSSFRow mainProfileRow = envSheet.createRow((short)rowIndex);
        mainProfileRow.createCell(0).setCellValue("USED INPUT PROFILE");
        mainProfileRow.getCell(0).setCellStyle(bold);
    	
        rowIndex++;
        
        HSSFRow profileRow = envSheet.createRow((short)rowIndex);
        profileRow.createCell(0).setCellValue(latestProfilePath);
        
        rowIndex += 3;
        
    	// Service information
        HSSFRow mainServiceRow = envSheet.createRow((short)rowIndex);
        mainServiceRow.createCell(0).setCellValue("SERVICE INFORMATION");
        mainServiceRow.getCell(0).setCellStyle(bold);
        
        rowIndex++;
        
        for (ServiceRegistry registry : GlobalServiceInfo.getServiceRegistries()) {
        	
            HSSFRow registryRow = envSheet.createRow((short)rowIndex);
            registryRow.createCell(0).setCellValue(registry.getServiceDescription().getServiceEndpoint());
            registryRow.getCell(0).setCellStyle(bold);
            
            rowIndex ++;
            
            HSSFRow serviceRootRow = envSheet.createRow((short)rowIndex);
            serviceRootRow.createCell(0).setCellValue("Name");
            serviceRootRow.createCell(1).setCellValue("Endpoint");
            serviceRootRow.createCell(2).setCellValue("Cost");
            serviceRootRow.createCell(3).setCellValue("Failure Rate");
            // add column here if service gets new custom property
            
            for (int i = 0; i < 4; i++) {
            	serviceRootRow.getCell(i).setCellStyle(bold);
            }
            
            rowIndex ++;
            
            for (String serviceName : registry.getAllServiceNames()) {
            	AtomicService service = GlobalServiceInfo.getService(serviceName);
                HSSFRow serviceRow = envSheet.createRow((short)rowIndex);
                serviceRow.createCell(0).setCellValue(service.getServiceDescription().getServiceName());
                serviceRow.createCell(1).setCellValue(service.getServiceDescription().getServiceEndpoint());
                
                if (service.getServiceDescription().getCustomProperties().get("Cost") != null) {
                    serviceRow.createCell(2).setCellValue((double) service.getServiceDescription().getCustomProperties().get("Cost"));
                }
                else {
                    serviceRow.createCell(2).setCellValue(0);
                }
                
                if (service.getServiceDescription().getCustomProperties().get("FailureRate") != null) {
                    serviceRow.createCell(3).setCellValue((double) service.getServiceDescription().getCustomProperties().get("FailureRate"));
                }
                else {
                    serviceRow.createCell(3).setCellValue(0);
                }
                
                rowIndex++;
            }
            
            rowIndex ++;
        }
        
        rowIndex ++;
        
        
        // Entity information
        HSSFRow mainEntityRow = envSheet.createRow((short)rowIndex);
        mainEntityRow.createCell(0).setCellValue("ENTITY INFORMATION");
        mainEntityRow.getCell(0).setCellStyle(bold);
        
        rowIndex++;
        
        HSSFRow entityRootRow = envSheet.createRow((short)rowIndex);
        entityRootRow.createCell(0).setCellValue("Name");
        entityRootRow.createCell(1).setCellValue("Load Failure Delta");
        entityRootRow.createCell(2).setCellValue("Combination Limit");
        entityRootRow.createCell(3).setCellValue("Min Failure Delta");
        entityRootRow.createCell(4).setCellValue("Failure Change");
        entityRootRow.createCell(5).setCellValue("Analyzer Generation Strategy");
        entityRootRow.createCell(6).setCellValue("Goals");
        entityRootRow.createCell(7).setCellValue("Used Service Registries");
        entityRootRow.createCell(8).setCellValue("Workflow Path");
        // add column here if entity gets new custom property
        
        for (int i = 0; i < 9; i++) {
        	entityRootRow.getCell(i).setCellStyle(bold);
        }
        
        rowIndex++;
        
        for (MAPEKSystemEntity entity : entities) {
        	
        	String goalString = "{";
        	
        	for (Goal goal : entity.getManagingSystem().getKnowledge().getGoals()) {
        		goalString += goal.getType() + " " + goal.getRelation() + " " + goal.getValue() + ", ";
        	}
        	
        	goalString += "}";
        	
        	List<String> registries = entity.getManagingSystem().getKnowledge().getRegistryEndpoints();
        	String registryString = "{";
        	
        	for (int i = 0; i < registries.size(); i++) {
        		if (i == registries.size() - 1) {
            		registryString += registries.get(i) + "}";
        		}
        		else {
            		registryString += registries.get(i) + ", ";
        		}
        	}
        	
            HSSFRow entityRow = envSheet.createRow((short)rowIndex);
            entityRow.createCell(0).setCellValue(entity.getEntityName());
            entityRow.createCell(1).setCellValue(entity.getManagingSystem().getKnowledge().getLoadFailureDelta());
            entityRow.createCell(2).setCellValue(entity.getManagingSystem().getAnalyzer().getCombinationLimit());
            entityRow.createCell(3).setCellValue(entity.getManagingSystem().getMonitor().getMinFailureDelta());
            entityRow.createCell(4).setCellValue(entity.getManagingSystem().getMonitor().getFailureChange());
            entityRow.createCell(5).setCellValue(entity.getManagingSystem().getAnalyzer().getServiceGenerationStrategy());
            entityRow.createCell(6).setCellValue(goalString);
            entityRow.createCell(7).setCellValue(registryString);
            entityRow.createCell(8).setCellValue(entity.getManagedSystem().getWorkflowPath());
            
            rowIndex++;
        }
        
        
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
        HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
        HashMap<String, List<ServiceCombination>> chosenCombinationsAll = systemRunProbe.getChosenCombinations();
        
        
        
        // SERVICE COMBINATION CHOICES
        HSSFSheet sccSheet = workbook.createSheet("Service Combination Choices");  
        rowIndex = 0; 
        
        HSSFRow mainSccRow = sccSheet.createRow((short)rowIndex);
        mainSccRow.createCell(0).setCellValue("SERVICE COMBINATION CHOICES");
        mainSccRow.getCell(0).setCellStyle(bold);
        
        rowIndex += 2;
        
        // TODO
        
        
        // PROTOCOL COMMUNICATION RESULTS
        HSSFSheet procomSheet = workbook.createSheet("Protocol Communication Results"); 
        rowIndex = 0; 
        
        HSSFRow mainprocomRow = procomSheet.createRow((short)rowIndex);
        mainprocomRow.createCell(0).setCellValue("PROTOCOL COMMUNICATION RESULTS");
        mainprocomRow.getCell(0).setCellStyle(bold);
        
        rowIndex += 2;
        
        HSSFRow protocolMessageCountRootRow = procomSheet.createRow((short)rowIndex);
        protocolMessageCountRootRow.createCell(0).setCellValue("Cycle");
        protocolMessageCountRootRow.createCell(1).setCellValue("Protocol Messages");
        
        for (int i = 0; i < 2; i++) {
        	protocolMessageCountRootRow.getCell(i).setCellStyle(bold);
        }
        
        rowIndex++;
        
		for (int i = 0; i < protocolMessages.size(); i++) {
			HSSFRow protocolMessageCountRow = procomSheet.createRow((short)rowIndex);
			protocolMessageCountRow.createCell(0).setCellValue(i + 1);
			protocolMessageCountRow.createCell(1).setCellValue(protocolMessages.get(i));		
			
	        rowIndex++;
		}
        
        
        
        // RATING RESULTS
        HSSFSheet ratingSheet = workbook.createSheet("Rating Results"); 
        rowIndex = 0; 
        
        HSSFRow mainRatingRow = ratingSheet.createRow((short)rowIndex);
        mainRatingRow.createCell(0).setCellValue("RATING RESULTS");
        mainRatingRow.getCell(0).setCellStyle(bold);
        
        rowIndex += 2;
        
        // Set data points
		for (String entity : chosenCombinationsAll.keySet()) {
			
			HSSFRow entityRatingRootRootRow = ratingSheet.createRow((short)rowIndex);
			entityRatingRootRootRow.createCell(0).setCellValue(entity);
			entityRatingRootRootRow.getCell(0).setCellStyle(bold);
			
			rowIndex++;
			
	        HSSFRow entityRatingRootRow = ratingSheet.createRow((short)rowIndex);
	        entityRatingRootRow.createCell(0).setCellValue("Cycle");
	        entityRatingRootRow.createCell(1).setCellValue("Entity Rating");
	        entityRatingRootRow.createCell(2).setCellValue("System Rating");
	        
	        for (int i = 0; i < 3; i++) {
	        	entityRatingRootRow.getCell(i).setCellStyle(bold);
	        }
	        
	        rowIndex++;
			
			for (int i = 0; i < chosenCombinationsAll.get(entity).size(); i++) {		
				HSSFRow entityRatingRow = ratingSheet.createRow((short)rowIndex);
				entityRatingRow.createCell(0).setCellValue(i + 1);
				entityRatingRow.createCell(1).setCellValue(Double.valueOf(chosenCombinationsAll.get(entity).get(i).getRating().toString()));
				entityRatingRow.createCell(2).setCellValue(systemRunProbe.getRatings().get(entity).get(i));			
				
		        rowIndex++;
			}
			
			rowIndex++;
		}
        
        
        // FAILURE RATE RESULTS
        HSSFSheet failureRateSheet = workbook.createSheet("Failure Rate Results");  
        rowIndex = 0; 
        
        HSSFRow mainFailurerateRow = failureRateSheet.createRow((short)rowIndex);
        mainFailurerateRow.createCell(0).setCellValue("FAILURE RATE RESULTS");
        mainFailurerateRow.getCell(0).setCellStyle(bold);
        
        rowIndex += 2;
        
        
        // Set data points
		for (String entity : chosenCombinationsAll.keySet()) {
			
			HSSFRow entityFailureRateRootRootRow = failureRateSheet.createRow((short)rowIndex);
			entityFailureRateRootRootRow.createCell(0).setCellValue(entity);
			entityFailureRateRootRootRow.getCell(0).setCellStyle(bold);
			
			rowIndex++;
			
	        HSSFRow entityFailureRateRootRow = failureRateSheet.createRow((short)rowIndex);
	        entityFailureRateRootRow.createCell(0).setCellValue("Cycle");
	        entityFailureRateRootRow.createCell(1).setCellValue("Entity Failure Rate");
	        entityFailureRateRootRow.createCell(2).setCellValue("System Failure Rate");
	        entityFailureRateRootRow.createCell(3).setCellValue("Failure Rate Error");
	        
	        for (int i = 0; i < 4; i++) {
	        	entityFailureRateRootRow.getCell(i).setCellStyle(bold);
	        }
	        
	        rowIndex++;
			
			for (int i = 0; i < chosenCombinationsAll.get(entity).size(); i++) {		
				HSSFRow entityFailureRateRow = failureRateSheet.createRow((short)rowIndex);
				entityFailureRateRow.createCell(0).setCellValue(i + 1);
				entityFailureRateRow.createCell(1).setCellValue(entityFailureRate.get(entity).get(i));
				entityFailureRateRow.createCell(2).setCellValue(dataPoints.get(entity).get(i).getKey());			
				entityFailureRateRow.createCell(3).setCellValue(failureRateErrors.get(entity).get(i));	
				
		        rowIndex++;
			}
			
			rowIndex++;
		}
        
        
        
        
        // COST RESULTS
        HSSFSheet costSheet = workbook.createSheet("Cost Results");  
        rowIndex = 0; 
        
        HSSFRow mainCostRow = costSheet.createRow((short)rowIndex);
        mainCostRow.createCell(0).setCellValue("COST RESULTS");
        mainCostRow.getCell(0).setCellStyle(bold);
        
        rowIndex += 2;
        
		// Set data points
		for (String entity : dataPoints.keySet()) {
			
			HSSFRow entityCostRootRootRow = costSheet.createRow((short)rowIndex);
			entityCostRootRootRow.createCell(0).setCellValue(entity);
			entityCostRootRootRow.getCell(0).setCellStyle(bold);
			
			rowIndex++;
			
	        HSSFRow entityCostRootRow = costSheet.createRow((short)rowIndex);
	        entityCostRootRow.createCell(0).setCellValue("Cycle");
	        entityCostRootRow.createCell(1).setCellValue("Cost");
	        
	        for (int i = 0; i < 2; i++) {
	        	entityCostRootRow.getCell(i).setCellStyle(bold);
	        }
	        
	        rowIndex++;
			
			for (int i = 0; i < dataPoints.get(entity).size(); i++) {
				HSSFRow entityCostRow = costSheet.createRow((short)rowIndex);
				entityCostRow.createCell(0).setCellValue(i + 1);
				entityCostRow.createCell(1).setCellValue(dataPoints.get(entity).get(i).getValue());
		        rowIndex++;
			}
			
	        rowIndex++;
		}
        
        
        for (int i = 0; i < 20; i++) {
        	envSheet.autoSizeColumn(i);
        	procomSheet.autoSizeColumn(i);
        	ratingSheet.autoSizeColumn(i);
        	failureRateSheet.autoSizeColumn(i);
        	costSheet.autoSizeColumn(i);
        	sccSheet.autoSizeColumn(i);
        }
        
        
        
        FileOutputStream fileOut = new FileOutputStream(filePath + ".xls");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
	}
	
	public void generateSystemRunCharts() {
		generatePerformanceChart();
		generateProtocolMessageChart();
		generateFailureRateErrorChart();
		generateRatingChart();
		generateFailureRateSystemChart();
		generateRatingSystemChart();
		generateFailureRateChart();
		generateCostChart();
	}
	
	@SuppressWarnings("unchecked")
	public void generateSystemRunTables() {
		
		HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
		
		for (String entity : dataPoints.keySet()) {
			
			// Add entity tab
	    	TitledPane entityPane = new TitledPane();
	    	entityPane.setText(entity);  	
	    	entityResultTableAccordion.getPanes().add(entityPane);
	    	
	    	// Add entity table
	    	TableView<ServiceCombinationEntry> entityResultTable = new TableView<ServiceCombinationEntry>();
	    	
			// Table column data
			TableColumn<ServiceCombinationEntry, Integer> cycleColumn = new TableColumn<ServiceCombinationEntry,Integer>("Cycle");
			cycleColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Integer>("cycle"));
			cycleColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(5));
			cycleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
			
			TableColumn<ServiceCombinationEntry, Double> totalCostColumn = new TableColumn<ServiceCombinationEntry,Double>("Total Cost");
			totalCostColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("totalCost"));
			totalCostColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(5));
			totalCostColumn.setStyle("-fx-alignment: CENTER-LEFT;");

			TableColumn<ServiceCombinationEntry, Double> totalFailRateColumn = new TableColumn<ServiceCombinationEntry,Double>("Total FailRate");
			totalFailRateColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("totalFailRate"));
			totalFailRateColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(5));
			totalFailRateColumn.setStyle("-fx-alignment: CENTER-LEFT;");
			
			TableColumn<ServiceCombinationEntry, Double> protocolMessageCountColumn = new TableColumn<ServiceCombinationEntry,Double>("Messages sent during protocol");
			protocolMessageCountColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("protocolMessageCount"));
			protocolMessageCountColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(5));
			protocolMessageCountColumn.setStyle("-fx-alignment: CENTER-LEFT;");
			
			TableColumn<ServiceCombinationEntry, String> servicesColumn = new TableColumn<>("Services");
			servicesColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
			servicesColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(5));
			servicesColumn.setSortable(false);
			
			Callback<TableColumn<ServiceCombinationEntry, String>, TableCell<ServiceCombinationEntry, String>> cellFactoryShow
	        =
	        new Callback<TableColumn<ServiceCombinationEntry, String>, TableCell<ServiceCombinationEntry, String>>() {
			    @Override
			    public TableCell<ServiceCombinationEntry, String> call(final TableColumn<ServiceCombinationEntry, String> param) {
			        final TableCell<ServiceCombinationEntry, String> cell = new TableCell<ServiceCombinationEntry, String>() {
			
			            final Button btn = new Button("Show Info");
			
			            @Override
			            public void updateItem(String item, boolean empty) {
			            	
			            	ServiceCombinationEntry attribute = (ServiceCombinationEntry) this.getTableRow().getItem();
			                super.updateItem(item, empty);
			                
			                if (empty) {
			                    setGraphic(null);
			                    setText(null);
			                } 
			                else {
			                    btn.setOnAction(event -> {
			        	    		Alert fail = new Alert(AlertType.INFORMATION);
			        	            fail.setHeaderText("SERVICE COMBINATION INFORMATION");
			        	            fail.setContentText(attribute.getUsedServicesInfo());
			        	            fail.showAndWait();
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
			
			servicesColumn.setCellFactory(cellFactoryShow);
			
			// Extract table data
			ObservableList<ServiceCombinationEntry> serviceCombinationData = FXCollections.observableArrayList();
			
			for (int i = 0; i < dataPoints.get(entity).size(); i++) {	
				Pair<Double, Double> dataPoint = dataPoints.get(entity).get(i);
				int cycle = systemRunProbe.getSystemCycles().get(entity).get(i);
				int protocolMessageCount = systemRunProbe.getProtocolMessageCount().get(i);
				ServiceCombination combination = systemRunProbe.getChosenCombinations().get(entity).get(i);
				serviceCombinationData.add(new ServiceCombinationEntry(cycle, dataPoint.getValue(), dataPoint.getKey(), protocolMessageCount, combination));
			}
			
			// Set table data
			entityResultTable.setItems(serviceCombinationData);			
			entityResultTable.getColumns().addAll(cycleColumn, totalCostColumn, totalFailRateColumn, protocolMessageCountColumn, servicesColumn);
			entityPane.setContent(entityResultTable);
		}
	}
	
	private void saveChart(Chart chart, String filePath) throws IOException {
		WritableImage image = chart.snapshot(new SnapshotParameters(), null);
		BufferedImage awtImage = SwingFXUtils.fromFXImage(image, null);

	    File outputfile = new File(filePath + ".png");
	    ImageIO.write(awtImage, "png", outputfile);
	}
	
	private void generatePerformanceChart() {
		HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
		
		if (dataPoints.values().stream().anyMatch(x -> x.size() > 0)) {
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("Total service combination failure rate \n(approximated by workflow analyzer)", 0, 1, 0.1);
			NumberAxis yAxis = new NumberAxis("Total service combination cost", 0, (systemRunProbe.getMaxCost() + maximumDelta), 1);
			
			// Set chart position & size
			systemRunChart = new ScatterLineChart<Number, Number>(xAxis, yAxis); 
			systemRunChartPane.getChildren().add(systemRunChart);
			systemRunChart.prefWidthProperty().bind(systemRunChartPane.widthProperty());
			systemRunChart.prefHeightProperty().bind(systemRunChartPane.heightProperty());
			
			// Set data points
			ArrayList<Node> seriesNodes = new ArrayList<Node>();
			int seriesIndex = 0;
			
			for (String entity : dataPoints.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				// Dummy node
				series.getData().add(new XYChart.Data<Number, Number>(0, 0));
				
				for (Pair<Double, Double> dataPoint : dataPoints.get(entity)) {
					//System.err.print("CHART DATA POINT ADDED" + dataPoint.getKey() + " " + dataPoint.getValue() + " \n");
					series.getData().add(new XYChart.Data<Number, Number>(dataPoint.getKey(), dataPoint.getValue()));
				}
				
				systemRunChart.getData().add(series);
				
				Set<Node> nodes = systemRunChart.lookupAll(".series" + seriesIndex);
	            int flag=0;
	            for (Node n : nodes) {
				    n.setStyle("-fx-background-color:" + ScatterLineChart.colors.get(seriesIndex) + ", white;\n"
				            + "    -fx-background-insets: 0, 2;\n"
				            + "    -fx-background-radius: 5px;\n"
				            + "    -fx-padding: 5px;");
	                 if (flag==0) {
	                     seriesNodes.add(n);
	                 }
	                 flag++;
	            }
				
				List<Goal> goals = systemRunProbe.getConnectedEntity(entity).getManagingSystem().getKnowledge().getGoals();
				
				for (Goal goal : goals) {
					if (goal.getType().equals(GoalType.COST)) {
						systemRunChart.addHorizontalValueMarker(new Data<>(0, goal.getValue()), seriesIndex);
					}
					else if (goal.getType().equals(GoalType.FAILURE_RATE)) {
						systemRunChart.addVerticalValueMarker(new Data<>(goal.getValue(), 0), seriesIndex);
					}
				}
				
				seriesIndex++;
			}
			
			Set<Node> items = systemRunChart.lookupAll("Label.chart-legend-item");
	        int it=0;
	        for (Node item : items) {
	             Label label = (Label) item;
	             label.setGraphic(seriesNodes.get(it));
	             it++;
	        }
		}
	}
	
	private void generateFailureRateErrorChart() {
		
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
		HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
		
		if (protocolMessages.size() > 0) {
			
			double maxErrorValue = 0;
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat df = new DecimalFormat("#.#", otherSymbols);
			df.setRoundingMode(RoundingMode.CEILING);
			
			for (String entity : dataPoints.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				for (int i = 0; i < dataPoints.get(entity).size(); i++) {			
					if (systemRunProbe.getChosenCombinations().get(entity).get(i).getProperty("FailureRate") != null) {
						double entityFailRate = systemRunProbe.getChosenCombinations().get(entity).get(i).getProperty("FailureRate");
						double systemFailRate = dataPoints.get(entity).get(i).getKey();
						double errorValue = Math.abs(entityFailRate - systemFailRate);
						
						if (errorValue > maxErrorValue) {
							maxErrorValue = Double.valueOf(df.format(errorValue));
						}
					}
				}
			}
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = new NumberAxis("Chosen Service Combination Failure Rate error \n(system approximation <-> entity approximation)", 0, maxErrorValue, 0.1);
			
			// Set chart position & size
			failureRateErrorChart = new LineChart<Number, Number>(xAxis, yAxis); 
			failureRateErrorChartPane.getChildren().add(failureRateErrorChart);
			failureRateErrorChart.prefWidthProperty().bind(failureRateErrorChartPane.widthProperty());
			failureRateErrorChart.prefHeightProperty().bind(failureRateErrorChartPane.heightProperty());
			
			// Set data points
			failureRateErrors = new HashMap<>();
			for (String entity : dataPoints.keySet()) {
				
				failureRateErrors.put(entity, new ArrayList<>());
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				for (int i = 0; i < dataPoints.get(entity).size(); i++) {
					
					if (systemRunProbe.getChosenCombinations().get(entity).get(i).getProperty("FailureRate") != null) {
						double entityFailRate = systemRunProbe.getChosenCombinations().get(entity).get(i).getProperty("FailureRate");
						double systemFailRate = dataPoints.get(entity).get(i).getKey();
						
						series.getData().add(new XYChart.Data<Number, Number>(i + 1, Math.abs(entityFailRate - systemFailRate)));
						failureRateErrors.get(entity).add(Math.abs(entityFailRate - systemFailRate));
					}
					else {
						series.getData().add(new XYChart.Data<Number, Number>(i + 1, 0));
						failureRateErrors.get(entity).add(0.0);
					}
				}
				
				failureRateErrorChart.getData().add(series);
			}
		}
	}
	
	private void generateRatingChart() {
		
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
		HashMap<String, List<ServiceCombination>> chosenCombinationsAll = systemRunProbe.getChosenCombinations();
		
		if (protocolMessages.size() > 0) {
			
			List<ServiceCombination> chosenCombinations = new ArrayList<>(chosenCombinationsAll.values()).get(0);
			RatingType type = chosenCombinations.get(0).getRatingType();
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = null;
			
			switch (type) {
			
				case CLASS:
					
					int maxGoals = 0;
					
					for (String entity : chosenCombinationsAll.keySet()) {
						
						int goals = systemRunProbe.getConnectedEntity(entity).getManagingSystem().getKnowledge().getGoals().size();
						
						if (goals > maxGoals) {
							maxGoals = goals;
						}
					}
					
					yAxis = new NumberAxis(type + " rating value \n(what entity thinks)", 0, maxGoals + 1, 1);
					break;
				
				case SCORE:
					
					double maxScore = 0;
					
					for (String entity : chosenCombinationsAll.keySet()) {
						for (ServiceCombination serviceCombination : chosenCombinationsAll.get(entity)) {
							double score = Double.valueOf(serviceCombination.getRating().toString());
							
							if (score > maxScore) {
								maxScore = score;
							}
						}
					}
					
					yAxis = new NumberAxis(type + " rating value \n(what entity thinks)", 0, maxScore + 1, 1);
					break;
					
				default:
					throw new IllegalStateException("The system doesn't support the drawing of this rating type. Type: " + type);			
					
			}
			
			// Set chart position & size
			ratingChart = new LineChart<Number, Number>(xAxis, yAxis); 
			ratingEvolutionChartPane.getChildren().add(ratingChart);
			ratingChart.prefWidthProperty().bind(ratingEvolutionChartPane.widthProperty());
			ratingChart.prefHeightProperty().bind(ratingEvolutionChartPane.heightProperty());
			
			// Set data points
			for (String entity : chosenCombinationsAll.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				for (int i = 0; i < chosenCombinationsAll.get(entity).size(); i++) {		
					series.getData().add(new XYChart.Data<Number, Number>(i + 1, Double.valueOf(chosenCombinationsAll.get(entity).get(i).getRating().toString())));
				}
				
				ratingChart.getData().add(series);
			}
		}
	}
	
	private void generateRatingSystemChart() {
		
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
		HashMap<String, List<ServiceCombination>> chosenCombinationsAll = systemRunProbe.getChosenCombinations();
		
		if (protocolMessages.size() > 0) {
			
			List<ServiceCombination> chosenCombinations = new ArrayList<>(chosenCombinationsAll.values()).get(0);
			RatingType type = chosenCombinations.get(0).getRatingType();
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = null;
			
			switch (type) {
			
				case CLASS:
					
					int maxGoals = 0;
					
					for (String entity : chosenCombinationsAll.keySet()) {
						
						int goals = systemRunProbe.getConnectedEntity(entity).getManagingSystem().getKnowledge().getGoals().size();
						
						if (goals > maxGoals) {
							maxGoals = goals;
						}
					}
					
					yAxis = new NumberAxis("real " + type + " rating value", 0, maxGoals + 1, 1);
					break;
				
				case SCORE:
					
					// TODO score			
					yAxis = new NumberAxis(type + " rating type is work in progress", 0, 1, 1);
					break;
					
				default:
					throw new IllegalStateException("The system doesn't support the drawing of this rating type. Type: " + type);			
			}
			
			// Set chart position & size
			ratingSystemChart = new LineChart<Number, Number>(xAxis, yAxis);
			ratingEvolutionSystemChartPane.getChildren().add(ratingSystemChart);
			ratingSystemChart.prefWidthProperty().bind(ratingEvolutionSystemChartPane.widthProperty());
			ratingSystemChart.prefHeightProperty().bind(ratingEvolutionSystemChartPane.heightProperty());
			
			// Set data points
			for (String entity : chosenCombinationsAll.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				// Dummy node
				series.getData().add(new XYChart.Data<Number, Number>(0, 0));
				
				for (int i = 0; i < chosenCombinationsAll.get(entity).size(); i++) {		
					series.getData().add(new XYChart.Data<Number, Number>(i + 1, systemRunProbe.getRatings().get(entity).get(i)));
				}
				
				ratingSystemChart.getData().add(series);
			}
		}
	}
	
	private void generateFailureRateSystemChart() {
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
		HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
		
		if (protocolMessages.size() > 0) {
			
			double maxFailRate = 0;
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat df = new DecimalFormat("#.#", otherSymbols);
			df.setRoundingMode(RoundingMode.CEILING);
			
			for (String entity : dataPoints.keySet()) {	
				for (int i = 0; i < dataPoints.get(entity).size(); i++) {
					double systemFailRate = dataPoints.get(entity).get(i).getKey();
					if (systemFailRate > maxFailRate) {
						maxFailRate = Double.valueOf(df.format(systemFailRate));
					}
				}
			}
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = new NumberAxis("Total service combination failure rate \n(approximated by workflow analyzer)", 0, maxFailRate, 0.1);
			
			// Set chart position & size
			failureRateSystemChart = new LineChart<Number, Number>(xAxis, yAxis); 
			failureRateSystemChartPane.getChildren().add(failureRateSystemChart);
			failureRateSystemChart.prefWidthProperty().bind(failureRateSystemChartPane.widthProperty());
			failureRateSystemChart.prefHeightProperty().bind(failureRateSystemChartPane.heightProperty());
			
			// Set data points
			for (String entity : dataPoints.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				for (int i = 0; i < dataPoints.get(entity).size(); i++) {
			
					double systemFailRate = dataPoints.get(entity).get(i).getKey();
					
					series.getData().add(new XYChart.Data<Number, Number>(i + 1, systemFailRate));
				}
				
				failureRateSystemChart.getData().add(series);
			}
		}
	}
	
	private void generateFailureRateChart() {
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
		
		if (protocolMessages.size() > 0) {
			
			double maxFailRate = 0;
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat df = new DecimalFormat("#.#", otherSymbols);
			df.setRoundingMode(RoundingMode.CEILING);
			
			// Set data points
			for (String entity : systemRunProbe.getDataPoints().keySet()) {			
				for (int i = 0; i < systemRunProbe.getChosenCombinations().get(entity).size(); i++) {		
					if (systemRunProbe.getChosenCombinations().get(entity).get(i).getProperty("FailureRate") != null) {
						double combinationFailRate = systemRunProbe.getChosenCombinations().get(entity).get(i).getProperty("FailureRate");	
						
						if (combinationFailRate > maxFailRate) {
							maxFailRate = Double.valueOf(df.format(combinationFailRate));
						}
					}
				}
			}
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = new NumberAxis("Total service combination failure rate \n(what entity thinks)", 0, maxFailRate, 0.1);
				
			
			// Set chart position & size
			failureRateChart = new LineChart<Number, Number>(xAxis, yAxis); 
			failureRateChartPane.getChildren().add(failureRateChart);
			failureRateChart.prefWidthProperty().bind(failureRateChartPane.widthProperty());
			failureRateChart.prefHeightProperty().bind(failureRateChartPane.heightProperty());
			
			// Set data points
			entityFailureRate = new HashMap<>();
			for (String entity : systemRunProbe.getDataPoints().keySet()) {
				
				entityFailureRate.put(entity, new ArrayList<>());
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				for (int i = 0; i < systemRunProbe.getChosenCombinations().get(entity).size(); i++) {
			
					ServiceCombination serviceCombination = systemRunProbe.getChosenCombinations().get(entity).get(i);	
					
					if (serviceCombination.getProperty("FailureRate") != null) {
						series.getData().add(new XYChart.Data<Number, Number>(i + 1, serviceCombination.getProperty("FailureRate")));
						entityFailureRate.get(entity).add(serviceCombination.getProperty("FailureRate"));
					}	
					else {
						series.getData().add(new XYChart.Data<Number, Number>(i + 1, 0));
						entityFailureRate.get(entity).add(0.0);
					}
				}
				
				failureRateChart.getData().add(series);
			}
		}
	}
	
	private void generateCostChart() {
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
		HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
		
		if (protocolMessages.size() > 0) {
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = new NumberAxis("Total service combination cost", 0, (systemRunProbe.getMaxCost() + maximumDelta), 1);
			
			// Set chart position & size
			costChart = new LineChart<Number, Number>(xAxis, yAxis); 
			costChartPane.getChildren().add(costChart);
			costChart.prefWidthProperty().bind(costChartPane.widthProperty());
			costChart.prefHeightProperty().bind(costChartPane.heightProperty());
			
			// Set data points
			for (String entity : dataPoints.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				for (int i = 0; i < dataPoints.get(entity).size(); i++) {
			
					double systemCost = dataPoints.get(entity).get(i).getValue();
					
					series.getData().add(new XYChart.Data<Number, Number>(i + 1, systemCost));
				}
				
				costChart.getData().add(series);
			}
		}
	}
	
	private void generateProtocolMessageChart() {
		
		List<Integer> protocolMessages = systemRunProbe.getProtocolMessageCount();
		
		if (protocolMessages.size() > 0) {
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = new NumberAxis("Messages sent during protocol", 0, Collections.max(protocolMessages) + 5, 1);
			
			// Set chart position & size
			protocolMessageChart = new LineChart<Number, Number>(xAxis, yAxis); 
			protocolMessageChartPane.getChildren().add(protocolMessageChart);
			protocolMessageChart.prefWidthProperty().bind(protocolMessageChartPane.widthProperty());
			protocolMessageChart.prefHeightProperty().bind(protocolMessageChartPane.heightProperty());
			
			Series<Number, Number> series = new Series<>();
			series.setName("Protocol Messages");
			protocolMessageChart.getData().add(series);
			
			for (int i = 0; i < protocolMessages.size(); i++) {
				Data<Number, Number> data = new Data<>(i + 1, protocolMessages.get(i));
				series.getData().add(data);
				
	            // handler for clicking on data point:
				int index = i;
	            data.getNode().setOnMouseClicked(e -> generateProtocolFlow(index));
			}
		}
	}
	
	private void generateProtocolFlow(int executionCycle) {
		
		protocolDetailsText.setDisable(true);
		
		List<Double> lineXList = new ArrayList<>();
		List<String> entities = new ArrayList<>(systemRunProbe.getDataPoints().keySet());
		int widthDelta = 100;
		int heightDelta = 50;
		double lineWidthDelta = (protocolFlowAnchorPane.widthProperty().get() - widthDelta * 2) / (entities.size() - 1);	
		
		protocolFlowAnchorPane.getChildren().clear();
		
		for (int i = 0; i < entities.size(); i++) {
			
			Text text = new Text(entities.get(i));
			Line line = new Line(widthDelta + i * lineWidthDelta, heightDelta, widthDelta + i * lineWidthDelta, protocolFlowAnchorPane.heightProperty().get() - heightDelta);
			lineXList.add(widthDelta + i * lineWidthDelta);
			
			protocolFlowAnchorPane.getChildren().add(line);
			protocolFlowAnchorPane.getChildren().add(text);
			
			text.applyCss(); 
			final double textWidth = text.getLayoutBounds().getWidth();
			
			AnchorPane.setLeftAnchor(text, widthDelta + i * lineWidthDelta - textWidth / 2);
			AnchorPane.setTopAnchor(text, (double) heightDelta - 20);
		}
		
		if (entities.size() > 1) {
			List<ProtocolMessageInformation> protocolMessages = protocolProbe.getProtocolMessages(executionCycle);
			int lineHeightDeltaDelta = 30;
			double lineHeightDelta = (protocolFlowAnchorPane.heightProperty().get() - (heightDelta + lineHeightDeltaDelta) * 2) / (protocolMessages.size() - 1);
			List<String> entityCommEndpoints = new ArrayList<>();
			
			for (String entity : entities) {
				entityCommEndpoints.add(systemRunProbe.getConnectedEntity(entity).getManagingSystem().getPlanner().getEndpoint());
			}
			
			
			for (int i = 0; i < protocolMessages.size(); i++) {
				
				ProtocolMessageInformation message = protocolMessages.get(i);
				double senderX = lineXList.get(entityCommEndpoints.indexOf(message.sender));
				double receiverX = lineXList.get(entityCommEndpoints.indexOf(message.receiver));		
				Text arrowText = new Text(message.messageType);
				arrowText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, Math.min(15, lineHeightDelta)));  
				final double textWidth = arrowText.getLayoutBounds().getWidth();
				final double textHeight = arrowText.getLayoutBounds().getHeight();
				
				StackPane stp = new StackPane();
				Rectangle rect = new Rectangle();
				rect.setHeight(textHeight);
				rect.setWidth(textWidth);
				rect.setFill(Color.rgb(244, 244, 244));
				stp.getChildren().addAll(rect, arrowText);
				
				Arrow arrow = new Arrow();
				arrow.setStartX(senderX);
				arrow.setStartY(heightDelta + lineHeightDeltaDelta + i * lineHeightDelta);
				arrow.setEndX(receiverX);
				arrow.setEndY(heightDelta + lineHeightDeltaDelta + i * lineHeightDelta);
				
				AnchorPane.setLeftAnchor(stp, Math.min(senderX, receiverX) + (Math.abs(senderX - receiverX) - textWidth) / 2);
				AnchorPane.setTopAnchor(stp, heightDelta + lineHeightDeltaDelta + i * lineHeightDelta - (textHeight / 2));
				
				protocolFlowAnchorPane.getChildren().add(arrow);
				protocolFlowAnchorPane.getChildren().add(stp);
			}
		}
	}
}
