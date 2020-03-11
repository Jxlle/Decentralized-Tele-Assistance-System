package application.view.controller;

import java.util.HashMap;
import java.util.List;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.probes.SystemRunProbe;
import tas.mape.system.entity.SystemEntity;

public class SystemRunChartController {
	
	private AnchorPane systemRunChartPane;
	private SystemRunProbe probe = new SystemRunProbe();
	private int maximumDelta = 10;
	
	public void addEntityToProbe(SystemEntity entity) {
		probe.connect(entity);
	}
	
	public void resetProbe() {
		probe.reset();
	}
	
	public SystemRunChartController(AnchorPane systemRunChartPane) {
		this.systemRunChartPane = systemRunChartPane;
	}
	
	public void clear() {
		systemRunChartPane.getChildren().clear();
	}
	
	// TODO: colors, goals, tab (?)
	public void generateSystemRunChart() {
		
		// Define chart axis
		NumberAxis xAxis = new NumberAxis("Total Service Combination Failure Rate (approximated by test runs)", 0, 1, 1);
		NumberAxis yAxis = new NumberAxis("Total Service Combination Cost", 0, (probe.getMaxCost() + maximumDelta), 1);
		
		// Set chart position & size
		ScatterLineChart<Number, Number> systemRunChart = new ScatterLineChart<Number, Number>(xAxis, yAxis); 
		systemRunChartPane.getChildren().add(systemRunChart);
		systemRunChart.prefWidthProperty().bind(systemRunChartPane.widthProperty());
		systemRunChart.prefHeightProperty().bind(systemRunChartPane.heightProperty());
		
		// Set data points
		HashMap<String, List<Pair<Double, Double>>> dataPoints = probe.getDataPoints();
		
		for (String entity : dataPoints.keySet()) {
			
			XYChart.Series<Number, Number> series = new Series<Number, Number>();
			series.setName(entity);
			
			for (Pair<Double, Double> dataPoint : dataPoints.get(entity)) {
				System.err.print("CHART DATA POINT ADDED" + dataPoint.getKey() + " " + dataPoint.getValue() + " \n");
				series.getData().add(new XYChart.Data<Number, Number>(dataPoint.getKey(), dataPoint.getValue()));
			}
			
			systemRunChart.getData().add(series);
			
			List<Goal> goals = probe.getConnectedEntity(entity).getManagingSystem().getGoals();
			
			for (Goal goal : goals) {
				if (goal.getType().equals(GoalType.COST)) {
					systemRunChart.addHorizontalValueMarker(new Data<>(0, goal.getValue()));
				}
				else if (goal.getType().equals(GoalType.FAILURE_RATE)) {
					systemRunChart.addVerticalValueMarker(new Data<>(goal.getValue(), 0));
				}
			}
			
		}
	}
}
