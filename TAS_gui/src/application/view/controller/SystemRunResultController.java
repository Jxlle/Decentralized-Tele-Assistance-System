package application.view.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import application.model.ServiceCombinationEntry;
import application.utility.ScatterLineChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.probes.SystemRunProbe;
import tas.mape.system.entity.SystemEntity;

public class SystemRunResultController {
	
	private Accordion entityResultTableAccordion;
	private AnchorPane systemRunChartPane;
	private SystemRunProbe probe = new SystemRunProbe();
	private int maximumDelta = 10;
	
	public void addEntityToProbe(SystemEntity entity) {
		System.out.println("ADDED: " + entity.getEntityName());
		probe.connect(entity);
	}
	
	public void resetProbe() {
		probe.reset();
		//probe = new SystemRunProbe();
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
			cycleColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(4));
	    	
			TableColumn<ServiceCombinationEntry, String> servicesColumn = new TableColumn<ServiceCombinationEntry,String>("Services");
			servicesColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, String>("services"));
			servicesColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(4));
			
			TableColumn<ServiceCombinationEntry, Double> totalCostColumn = new TableColumn<ServiceCombinationEntry,Double>("Total Cost");
			totalCostColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("totalCost"));
			totalCostColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(4));

			TableColumn<ServiceCombinationEntry, Double> totalFailRateColumn = new TableColumn<ServiceCombinationEntry,Double>("Total FailRate");
			totalFailRateColumn.setCellValueFactory(new PropertyValueFactory<ServiceCombinationEntry, Double>("totalFailRate"));
			totalFailRateColumn.prefWidthProperty().bind(entityResultTable.widthProperty().divide(4));
			
			// Extract table data
			ObservableList<ServiceCombinationEntry> serviceCombinationData = FXCollections.observableArrayList();
			
			for (int i = 0; i < dataPoints.get(entity).size(); i++) {	
				Pair<Double, Double> dataPoint = dataPoints.get(entity).get(i);
				serviceCombinationData.add(new ServiceCombinationEntry(probe.getSystemCycles().get(entity).get(i), dataPoint.getValue(), dataPoint.getKey()));
			}
			
			// Set table data
			entityResultTable.setItems(serviceCombinationData);
			entityResultTable.getColumns().addAll(cycleColumn, servicesColumn, totalCostColumn, totalFailRateColumn);
			entityPane.setContent(entityResultTable);
		}
	}
	
	public void generateSystemRunChart() {
		
		// Define chart axis
		NumberAxis xAxis = new NumberAxis("Total Service Combination Failure Rate (approximated by test runs)", 0, 1, 0.1);
		NumberAxis yAxis = new NumberAxis("Total Service Combination Cost", 0, (probe.getMaxCost() + maximumDelta), 1);
		
		// Set chart position & size
		ScatterLineChart<Number, Number> systemRunChart = new ScatterLineChart<Number, Number>(xAxis, yAxis); 
		systemRunChartPane.getChildren().add(systemRunChart);
		systemRunChart.prefWidthProperty().bind(systemRunChartPane.widthProperty());
		systemRunChart.prefHeightProperty().bind(systemRunChartPane.heightProperty());
		
		// Set data points
		HashMap<String, List<Pair<Double, Double>>> dataPoints = probe.getDataPoints();
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
