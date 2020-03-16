package application.view.controller;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import application.model.CostEntry;
import application.model.PerformanceEntry;
import application.model.ReliabilityEntry;

public class TableViewController {

	TableView<ReliabilityEntry> reliabilityTableView;
	TableView<CostEntry> costTableView;
	TableView<PerformanceEntry> performanceTableView;

	ObservableList<ReliabilityEntry> reliabilityData = FXCollections.observableArrayList();
	ObservableList<CostEntry> costData = FXCollections.observableArrayList();
	ObservableList<PerformanceEntry> performanceData = FXCollections.observableArrayList();

	
	public TableViewController(TableView<ReliabilityEntry> reliabilityTableView,
			TableView<CostEntry> costTableView,TableView<PerformanceEntry> performanceTableView){
		this.reliabilityTableView=reliabilityTableView;
		this.costTableView=costTableView;
		this.performanceTableView=performanceTableView;
		
		this.generateReliabilityTableView();
		this.generateCostTableView();
		this.generatePerformanceTableView();
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void generatePerformanceTableView(){	
		TableColumn<PerformanceEntry,String> serviceColumn = new TableColumn<PerformanceEntry,String>("Service");
		serviceColumn.setCellValueFactory(new PropertyValueFactory<PerformanceEntry, String>("service"));
		serviceColumn.prefWidthProperty().bind(performanceTableView.widthProperty().divide(6).multiply(2));
		
		TableColumn<PerformanceEntry,Integer> invocationColumn = new TableColumn<PerformanceEntry,Integer>("Invocations");
		invocationColumn.setCellValueFactory(new PropertyValueFactory<PerformanceEntry, Integer>("invocationNum"));
		invocationColumn.prefWidthProperty().bind(performanceTableView.widthProperty().divide(6));

		TableColumn<PerformanceEntry,Integer> failColumn = new TableColumn<PerformanceEntry,Integer>("Fail");
		failColumn.setCellValueFactory(new PropertyValueFactory<PerformanceEntry, Integer>("failNum"));
		failColumn.prefWidthProperty().bind(performanceTableView.widthProperty().divide(6));

		TableColumn<PerformanceEntry,Double> avgResponseTimeColumn = new TableColumn<PerformanceEntry,Double>("AvgResponseTime");
		avgResponseTimeColumn.setCellValueFactory(new PropertyValueFactory<PerformanceEntry, Double>("avgResponseTime"));
		avgResponseTimeColumn.prefWidthProperty().bind(performanceTableView.widthProperty().divide(6).multiply(2));
		
		performanceTableView.setItems(performanceData);
		performanceTableView.getColumns().addAll(serviceColumn,invocationColumn,failColumn,avgResponseTimeColumn);
	}
	
	@SuppressWarnings("unchecked")
	private void generateReliabilityTableView(){
		
		TableColumn<ReliabilityEntry,String> serviceColumn = new TableColumn<ReliabilityEntry,String>("Service");
		serviceColumn.setCellValueFactory(new PropertyValueFactory<ReliabilityEntry, String>("service"));
		serviceColumn.prefWidthProperty().bind(reliabilityTableView.widthProperty().divide(6).multiply(2));
		
		TableColumn<ReliabilityEntry,Integer> invocationColumn = new TableColumn<ReliabilityEntry,Integer>("Invocations");
		invocationColumn.setCellValueFactory(new PropertyValueFactory<ReliabilityEntry, Integer>("invocationNum"));
		invocationColumn.prefWidthProperty().bind(reliabilityTableView.widthProperty().divide(6));

		TableColumn<ReliabilityEntry,Integer> failColumn = new TableColumn<ReliabilityEntry,Integer>("Fail");
		failColumn.setCellValueFactory(new PropertyValueFactory<ReliabilityEntry, Integer>("failNum"));
		failColumn.prefWidthProperty().bind(reliabilityTableView.widthProperty().divide(6));

		TableColumn<ReliabilityEntry,Double> failRateColumn = new TableColumn<ReliabilityEntry,Double>("FailRate");
		failRateColumn.setCellValueFactory(new PropertyValueFactory<ReliabilityEntry, Double>("failRate"));
		failRateColumn.prefWidthProperty().bind(reliabilityTableView.widthProperty().divide(6));
		
		TableColumn<ReliabilityEntry,Double> successRateColumn = new TableColumn<ReliabilityEntry,Double>("SuccessRate");
		successRateColumn.setCellValueFactory(new PropertyValueFactory<ReliabilityEntry, Double>("successRate"));
		successRateColumn.prefWidthProperty().bind(reliabilityTableView.widthProperty().divide(6));

		reliabilityTableView.setItems(reliabilityData);
		reliabilityTableView.getColumns().addAll(serviceColumn,invocationColumn,failColumn,failRateColumn,successRateColumn);
	}
	
	/*
	public void fillMockReliabilityDate(){
		reliabilityData.addAll(new ReliabilityEntry("FASService0",100,98,2,0.02),
	    new ReliabilityEntry("FASService1",100,99,1,0.01),
	    new ReliabilityEntry("FASService2",100,90,10,0.1));
	}*/
	
	public void fillReliabilityDate(String resultFilePath){
		
		//reliabilityData.clear();
		
		Map<String,ReliabilityEntry> reliabilityEntries=new HashMap<>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(resultFilePath));
			String line;
			String service;
			boolean result;
			while ((line = br.readLine()) != null) {
				String[] str=line.split(",");
				if(str.length>=3){					
					service=str[1];
					result=Boolean.parseBoolean(str[2]);					
					
					if(!reliabilityEntries.containsKey(service)){
						reliabilityEntries.put(service, new ReliabilityEntry(service));
					}
					ReliabilityEntry reliabilityEntry=reliabilityEntries.get(service);

					reliabilityEntry.setInvocationNum(reliabilityEntry.getInvocationNum()+1);
					if(!result)
						reliabilityEntry.setFailNum(reliabilityEntry.getFailNum()+1);				
				}
			}
			br.close();	
			for (ReliabilityEntry entry : reliabilityEntries.values()) {
				entry.setRate();
				if(!entry.getService().equals("AssistanceService"))
					reliabilityData.add(entry);
			}
			//reliabilityData.add(new ReliabilityEntry());
			reliabilityData.add(reliabilityEntries.get("AssistanceService"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void generateCostTableView(){
		
		TableColumn<CostEntry,String> serviceColumn = new TableColumn<CostEntry,String>("Service");
		serviceColumn.setCellValueFactory(new PropertyValueFactory<CostEntry, String>("service"));
		serviceColumn.prefWidthProperty().bind(costTableView.widthProperty().divide(3));
		
		TableColumn<CostEntry,Integer> invocationColumn = new TableColumn<CostEntry,Integer>("Invocations");
		invocationColumn.setCellValueFactory(new PropertyValueFactory<CostEntry, Integer>("invocationNum"));
		invocationColumn.prefWidthProperty().bind(costTableView.widthProperty().divide(3));

		TableColumn<CostEntry,Integer> costColumn = new TableColumn<CostEntry,Integer>("TotalCost");
		costColumn.setCellValueFactory(new PropertyValueFactory<CostEntry, Integer>("totalCost"));
		costColumn.prefWidthProperty().bind(costTableView.widthProperty().divide(3));

		costTableView.setItems(costData);
		costTableView.getColumns().addAll(serviceColumn,invocationColumn,costColumn);
	}
	
	
	public void fillPerformanceData(String resultFilePath){
		
		Map<String,PerformanceEntry> performanceEntries=new HashMap<>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(resultFilePath));
			String line;
			String service;
			boolean result;
			while ((line = br.readLine()) != null) {
				
				String[] str=line.split(",");
				
				if(str.length>=3){					
					service=str[1];
					result=Boolean.parseBoolean(str[2]);					
					
					if(!service.equals("AssistanceService")){
						
						if(!performanceEntries.containsKey(service)){
							performanceEntries.put(service, new PerformanceEntry(service));
						}
						PerformanceEntry reliabilityEntry=performanceEntries.get(service);

						reliabilityEntry.setInvocationNum(reliabilityEntry.getInvocationNum()+1);
						
						if(result)
							reliabilityEntry.addResponseTime(Double.parseDouble(str[5]));
						else
							reliabilityEntry.setFailNum(reliabilityEntry.getFailNum()+1);
					}			
				}
			}
			br.close();	
			
			for (PerformanceEntry entry : performanceEntries.values()) {
				entry.setAvgResponseTime();
				performanceData.add(entry);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void fillCostData(String resultFilePath){
		
		//costData.clear();
		
		Map<String,CostEntry> costEntries=new HashMap<>();
		//costEntries.put("Total", new CostEntry("Total"));
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(resultFilePath));
			String line;
			
			//double totalCost=0;	
			//int invocationNum=0;
			//int currentInvocation=1;
			String service;
			boolean result;

	        while ((line = br.readLine()) != null) {
				String[] str=line.split(",");
				if(str.length>=3){
					//invocationNum=Integer.parseInt(str[0]);
					service=str[1];
					result=Boolean.parseBoolean(str[2]);
					
					if(result && !service.equals("AssistanceService")){
						if(!costEntries.containsKey(service)){
							costEntries.put(service, new CostEntry(service));
						}
						
						CostEntry entry=costEntries.get(service);
						entry.setInvocationNum(entry.getInvocationNum()+1);
						entry.setTotalCost(entry.getTotalCost()+Double.parseDouble(str[3]));
					}
				}
			}
			br.close();
	
			int totalInvocations=0;
			double totalCost=0;
			
			for (CostEntry entry : costEntries.values()) {
				totalInvocations=totalInvocations+entry.getInvocationNum();
				totalCost=totalCost+entry.getTotalCost();
				costData.add(entry);
			}

			costData.add(new CostEntry("Total",totalInvocations,totalCost));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void clear(){
	    reliabilityData.clear();
	    costData.clear();
	    performanceData.clear();
	}
}
