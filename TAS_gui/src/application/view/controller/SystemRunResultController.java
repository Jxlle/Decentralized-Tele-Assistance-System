package application.view.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import application.model.ServiceCombinationEntry;
import application.utility.ScatterLineChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.Pair;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.planner.ServiceCombination;
import tas.mape.probes.SystemRunProbe;
import tas.mape.system.entity.SystemEntity;

public class SystemRunResultController {
	
	private Accordion entityResultTableAccordion;
	private AnchorPane systemRunChartPane;
	private SystemRunProbe probe = new SystemRunProbe();
	private int maximumDelta = 10;
	
	public void addEntityToProbe(SystemEntity entity) {
		probe.connect(entity);
	}
	
	public void resetProbe() {
		probe.reset();
	}
	
	public SystemRunResultController(AnchorPane systemRunChartPane, Accordion entityResultTableAccordion) {
		this.systemRunChartPane = systemRunChartPane;
		this.entityResultTableAccordion = entityResultTableAccordion;
	}
	
	public void clear() {
		systemRunChartPane.getChildren().clear();
		entityResultTableAccordion.getPanes().clear();
	}
	
	@SuppressWarnings("unchecked")
	public void generateSystemRunTables() {
		
		HashMap<String, List<Pair<Double, Double>>> dataPoints = probe.getDataPoints();
		
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
			cycleColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(6));
			cycleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
			
			TableColumn<ServiceCombinationEntry, Double> totalCostColumn = new TableColumn<ServiceCombinationEntry,Double>("Total Cost");
			totalCostColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("totalCost"));
			totalCostColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(6));
			totalCostColumn.setStyle("-fx-alignment: CENTER-LEFT;");

			TableColumn<ServiceCombinationEntry, Double> totalFailRateColumn = new TableColumn<ServiceCombinationEntry,Double>("Total FailRate");
			totalFailRateColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("totalFailRate"));
			totalFailRateColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(6));
			totalFailRateColumn.setStyle("-fx-alignment: CENTER-LEFT;");
			
			TableColumn<ServiceCombinationEntry, Double> protocolMessageCountColumn = new TableColumn<ServiceCombinationEntry,Double>("Messages sent during protocol");
			protocolMessageCountColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("protocolMessageCount"));
			protocolMessageCountColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(3));
			protocolMessageCountColumn.setStyle("-fx-alignment: CENTER-LEFT;");
			
			TableColumn<ServiceCombinationEntry, String> servicesColumn = new TableColumn<>("Services");
			servicesColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
			servicesColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(6));
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
				int cycle = probe.getSystemCycles().get(entity).get(i);
				int protocolMessageCount = probe.getProtocolMessageCount().get(entity).get(i);
				ServiceCombination combination = probe.getChosenCombinations().get(entity).get(i);
				serviceCombinationData.add(new ServiceCombinationEntry(cycle, dataPoint.getValue(), dataPoint.getKey(), protocolMessageCount, combination));
			}
			
			// Set table data
			entityResultTable.setItems(serviceCombinationData);			
			entityResultTable.getColumns().addAll(cycleColumn, totalCostColumn, totalFailRateColumn, protocolMessageCountColumn, servicesColumn);
			entityPane.setContent(entityResultTable);
		}
	}
	
	public void generateSystemRunChart() {
		
		HashMap<String, List<Pair<Double, Double>>> dataPoints = probe.getDataPoints();
		
		if (dataPoints.values().stream().anyMatch(x -> x.size() > 0)) {
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("Total Service Combination Failure Rate (approximated by workflow analyzer)", 0, 1, 0.1);
			NumberAxis yAxis = new NumberAxis("Total Service Combination Cost", 0, (probe.getMaxCost() + maximumDelta), 1);
			
			// Set chart position & size
			ScatterLineChart<Number, Number> systemRunChart = new ScatterLineChart<Number, Number>(xAxis, yAxis); 
			systemRunChartPane.getChildren().add(systemRunChart);
			systemRunChart.prefWidthProperty().bind(systemRunChartPane.widthProperty());
			systemRunChart.prefHeightProperty().bind(systemRunChartPane.heightProperty());
			
			// Set data points
			ArrayList<Node> seriesNodes = new ArrayList<Node>();
			int seriesIndex = 0;
			
			for (String entity : dataPoints.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
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
				
				List<Goal> goals = probe.getConnectedEntity(entity).getManagingSystem().getGoals();
				
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
}
