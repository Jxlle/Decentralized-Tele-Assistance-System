package application.view.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import application.model.ServiceCombinationEntry;
import application.utility.Arrow;
import application.utility.ScatterLineChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
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
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Pair;
import tas.mape.communication.message.ProtocolMessageInformation;
import tas.mape.knowledge.Goal;
import tas.mape.knowledge.Goal.GoalType;
import tas.mape.planner.ServiceCombination;
import tas.mape.probes.ProtocolProbe;
import tas.mape.probes.SystemRunProbe;
import tas.mape.system.entity.SystemEntity;

public class SystemRunResultController {
	
	private Accordion entityResultTableAccordion;
	private AnchorPane systemRunChartPane, protocolMessageChartPane, protocolFlowAnchorPane, FailureRateErrorChartPane;
	private Label protocolDetailsText;
	private SystemRunProbe systemRunProbe = new SystemRunProbe();
	private ProtocolProbe protocolProbe = new ProtocolProbe();
	private int maximumDelta = 10;
	
	public void setProtocolProbe() {
		protocolProbe.connect();
	}
	
	public void addEntityToProbe(SystemEntity entity) {
		systemRunProbe.connect(entity);
	}
	
	public void resetProbes() {
		systemRunProbe.reset();
		protocolProbe.reset();
	}
	
	public SystemRunResultController(AnchorPane systemRunChartPane, AnchorPane protocolMessageChartPane, AnchorPane protocolFlowAnchorPane, AnchorPane FailureRateErrorChartPane, Accordion entityResultTableAccordion, Label protocolDetailsText) {
		this.systemRunChartPane = systemRunChartPane;
		this.protocolMessageChartPane = protocolMessageChartPane;
		this.protocolFlowAnchorPane = protocolFlowAnchorPane;
		this.FailureRateErrorChartPane = FailureRateErrorChartPane;
		this.entityResultTableAccordion = entityResultTableAccordion;
		this.protocolDetailsText = protocolDetailsText;
	}
	
	public void clear() {
		systemRunChartPane.getChildren().clear();
		protocolMessageChartPane.getChildren().clear();
		protocolFlowAnchorPane.getChildren().clear();
		FailureRateErrorChartPane.getChildren().clear();
		entityResultTableAccordion.getPanes().clear();
		
		protocolFlowAnchorPane.getChildren().add(protocolDetailsText);
		protocolDetailsText.setDisable(false);
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
				int cycle = systemRunProbe.getSystemCycles().get(entity).get(i);
				int protocolMessageCount = systemRunProbe.getProtocolMessageCount().get(entity).get(i);
				ServiceCombination combination = systemRunProbe.getChosenCombinations().get(entity).get(i);
				serviceCombinationData.add(new ServiceCombinationEntry(cycle, dataPoint.getValue(), dataPoint.getKey(), protocolMessageCount, combination));
			}
			
			// Set table data
			entityResultTable.setItems(serviceCombinationData);			
			entityResultTable.getColumns().addAll(cycleColumn, totalCostColumn, totalFailRateColumn, protocolMessageCountColumn, servicesColumn);
			entityPane.setContent(entityResultTable);
		}
	}
	
	public void generateSystemRunCharts() {
		generatePerformanceChart();
		generateProtocolMessageChart();
		generateFailureRateErrorChart();
	}
	
	private void generatePerformanceChart() {
		HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
		
		if (dataPoints.values().stream().anyMatch(x -> x.size() > 0)) {
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("Total service combination failure rate (approximated by workflow analyzer)", 0, 1, 0.1);
			NumberAxis yAxis = new NumberAxis("Total service combination cost", 0, (systemRunProbe.getMaxCost() + maximumDelta), 1);
			
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
				
				List<Goal> goals = systemRunProbe.getConnectedEntity(entity).getManagingSystem().getGoals();
				
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
		
		HashMap<String, List<Integer>> protocolMessagesAll = systemRunProbe.getProtocolMessageCount();
		HashMap<String, List<Pair<Double, Double>>> dataPoints = systemRunProbe.getDataPoints();
		
		if (protocolMessagesAll.values().stream().anyMatch(x -> x.size() > 0)) {
			
			List<Integer> protocolMessages = new ArrayList<>(protocolMessagesAll.values()).get(0);
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = new NumberAxis("Chosen Service Combination Failure Rate error (real <-> entity approximation)", 0, 1, 0.1);
			
			// Set chart position & size
			LineChart<Number, Number> failureRateErrorChart = new LineChart<Number, Number>(xAxis, yAxis); 
			FailureRateErrorChartPane.getChildren().add(failureRateErrorChart);
			failureRateErrorChart.prefWidthProperty().bind(FailureRateErrorChartPane.widthProperty());
			failureRateErrorChart.prefHeightProperty().bind(FailureRateErrorChartPane.heightProperty());
			
			// Set data points
			for (String entity : protocolMessagesAll.keySet()) {
				
				XYChart.Series<Number, Number> series = new Series<Number, Number>();
				series.setName(entity);
				
				for (int i = 0; i < dataPoints.get(entity).size(); i++) {
					
					double entityFailRate = systemRunProbe.getChosenCombinations().get(entity).get(i).getProperty("FailureRate");
					double systemFailRate = dataPoints.get(entity).get(i).getKey();
					
					series.getData().add(new XYChart.Data<Number, Number>(i + 1, Math.abs(entityFailRate - systemFailRate)));
				}
				
				for (Pair<Double, Double> dataPoint : dataPoints.get(entity)) {
					series.getData().add(new XYChart.Data<Number, Number>(dataPoint.getKey(), dataPoint.getValue()));
				}
				
				failureRateErrorChart.getData().add(series);
			}
		}
	}
	
	private void generateProtocolMessageChart() {
		
		HashMap<String, List<Integer>> protocolMessagesAll = systemRunProbe.getProtocolMessageCount();
		
		if (protocolMessagesAll.values().stream().anyMatch(x -> x.size() > 0)) {
			
			List<Integer> protocolMessages = new ArrayList<>(protocolMessagesAll.values()).get(0);
			
			// Define chart axis
			NumberAxis xAxis = new NumberAxis("System cycle", 1, protocolMessages.size(), 1);
			NumberAxis yAxis = new NumberAxis("Messages sent during protocol", 0, Collections.max(protocolMessages) + 5, 1);
			
			// Set chart position & size
			LineChart<Number, Number> protocolMessageChart = new LineChart<Number, Number>(xAxis, yAxis); 
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
				entityCommEndpoints.add(systemRunProbe.getConnectedEntity(entity).getManagingSystem().getPlannerEndpoint());
			}
			
			
			for (int i = 0; i < protocolMessages.size(); i++) {
				
				ProtocolMessageInformation message = protocolMessages.get(i);
				double senderX = lineXList.get(entityCommEndpoints.indexOf(message.sender));
				double receiverX = lineXList.get(entityCommEndpoints.indexOf(message.receiver));		
				Text arrowText = new Text(message.messageType);
				
				Arrow arrow = new Arrow();
				arrow.setStartX(senderX);
				arrow.setStartY(heightDelta + lineHeightDeltaDelta + i * lineHeightDelta);
				arrow.setEndX(receiverX);
				arrow.setEndY(heightDelta + lineHeightDeltaDelta + i * lineHeightDelta);
				
				arrowText.applyCss(); 
				final double textWidth = arrowText.getLayoutBounds().getWidth();
				
				AnchorPane.setLeftAnchor(arrowText, (Math.abs(senderX - receiverX) - textWidth) / 2);
				AnchorPane.setTopAnchor(arrowText, heightDelta + lineHeightDeltaDelta + i * lineHeightDelta - 20);
				
				protocolFlowAnchorPane.getChildren().add(arrow);
				protocolFlowAnchorPane.getChildren().add(arrowText);
			}
		}
	}
}
