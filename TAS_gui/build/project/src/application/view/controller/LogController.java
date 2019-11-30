package application.view.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import tas.services.log.*;
import application.log.Report;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.awt.Desktop;


public class LogController implements Initializable{	
	@FXML
	TableView<LogEntry> logTableView;
	
	@FXML
	TextField filterTextField;
	
	@FXML
	DatePicker fromDatePicker;
	
	@FXML
	DatePicker toDatePicker;
	
	@FXML
	Button clearButton;
	
	@FXML
	Button reportButton;
	
	Stage stage;
	
	FilteredList<LogEntry> filteredData = new FilteredList<>(Log.logData,p->true);
	
    private final String pattern = "yyyy-MM-dd";


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {			
		this.generateTableView();
		this.setButtons();
		this.initializeDatePicker();
		this.initializeFilter();
	}
	
	public void setStage(Stage stage){
		this.stage=stage;
	}
	
	private void setButtons(){
		clearButton.setOnAction(event->{
			Log.clear();
		});
		
		
		reportButton.setOnAction(event->{
			
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File("results" + File.separator));
			fileChooser.setTitle("Save Report");
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				String filePath=file.getPath()+".pdf";
		    	Report report=new Report(filePath);
		    	report.open();
		    	report.addTitle("Tele Assistance System");
		    	report.addSubTitle("Diagnosis Report");
		    	
		    	report.addEmptyLine(1);

		    	StringBuilder date=new StringBuilder();
		    	
		    	if(fromDatePicker.getValue()!=null)
		    		date.append("From: "+fromDatePicker.getValue().toString());
		    	
		    	if(toDatePicker.getValue()!=null)
		    		date.append("             To: "+toDatePicker.getValue().toString());
		    		
		    	report.addSentence(date.toString());
		    	
		    	if(!filterTextField.getText().isEmpty())
		    		report.addSentence("Filter: "+filterTextField.getText());
		    	
		    	report.addEmptyLine(1);
		    	String[] columns={"Time","Title","Message"};

		    	List<String> values=new ArrayList<>();
		    	for(LogEntry entry:filteredData){
		    		values.add(entry.getTime());
		    		values.add(entry.getTitle());
		    		values.add(entry.getMessage());
		    	}
		    	
			    float[] columnWidths = {1f, 1f, 2f};
			    
		    	report.addTable(columns,columnWidths,values);
		    	report.close();	
		    	
		    	if (Desktop.isDesktopSupported()) {
		    	    try {
		    	        File pdf = new File(filePath);
		    	        Desktop.getDesktop().open(pdf);
		    	    } catch (IOException ex) {
		    	    	return;
		    	    }
		    	}
		    	
		    }
		});
	}
	
	private void initializeDatePicker(){
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = 
                DateTimeFormatter.ofPattern(pattern);
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };             
        fromDatePicker.setConverter(converter);
        fromDatePicker.setPromptText(pattern.toLowerCase());
        //fromDatePicker.requestFocus();
        
        toDatePicker.setConverter(converter);
        toDatePicker.setPromptText(pattern.toLowerCase());
        //toDatePicker.requestFocus();
        
        
        final Callback<DatePicker, DateCell> dayCellFactory = 
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                               
                                if (item.isBefore(
                                		fromDatePicker.getValue())
                                    ) {
                                        setDisable(true);
                                        setStyle("-fx-background-color: #ffc0cb;");
                                }   
                        }
                    };
                }
            };
            toDatePicker.setDayCellFactory(dayCellFactory);
	}
	
	private void initializeFilter(){
		
		fromDatePicker.valueProperty().addListener((observable, oldValue, newValue)->{
            filteredData.setPredicate(log -> {
                if (newValue == null) {
                    return true;
                }

                
                String date=log.getTime().split(" ")[0];
                String fromDate=newValue.toString();
          
                /*
                String values[]=date.split("-");
                String compareValues[]=fromDate.split("-");
                
                if(Integer.parseInt(values[0])>=Integer.parseInt(compareValues[0])){
                	if(Integer.parseInt(values[1])>=Integer.parseInt(compareValues[1])){
                		if(Integer.parseInt(values[2])>=Integer.parseInt(compareValues[2]))
                			return true;
                	}
                }*/
                
                return date.compareTo(fromDate)>=0 ;
                	
                
                //return false;             
             });
		});
		
		toDatePicker.valueProperty().addListener((observable, oldValue, newValue)->{
            filteredData.setPredicate(log -> {
                if (newValue == null) {
                    return true;
                }

                String date=log.getTime().split(" ")[0];
                String fromDate=newValue.toString();
          
                /*
                String values[]=date.split("-");
                String compareValues[]=fromDate.split("-");
                
                if(Integer.parseInt(values[0])<=Integer.parseInt(compareValues[0])){
                	if(Integer.parseInt(values[1])<=Integer.parseInt(compareValues[1])){
                		if(Integer.parseInt(values[2])<=Integer.parseInt(compareValues[2]))
                			return true;
                	}
                }
                
                return false;  */
                
                return date.compareTo(fromDate)<=0 ;

             });
		});
		
		
		filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			
            filteredData.setPredicate(log -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (log.getTitle().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; 
                } else if (log.getMessage().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; 
                }
                return false; 
            });
        });

        SortedList<LogEntry> sortedData = new SortedList<>(filteredData);
                
        sortedData.comparatorProperty().bind(logTableView.comparatorProperty());

        logTableView.setItems(sortedData);
	}
	
	private void generateTableView(){
		TableColumn<LogEntry,String> timeColumn = new TableColumn<LogEntry,String>("Time");
		timeColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("time"));
		timeColumn.prefWidthProperty().bind(logTableView.widthProperty().divide(6).multiply(1));

		TableColumn<LogEntry,String> titleColumn = new TableColumn<LogEntry,String>("Title");
		titleColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("title"));
		titleColumn.prefWidthProperty().bind(logTableView.widthProperty().divide(6));

		TableColumn<LogEntry,String> messageColumn = new TableColumn<LogEntry,String>("Message");
		messageColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));
		messageColumn.prefWidthProperty().bind(logTableView.widthProperty().divide(6).multiply(4));

		logTableView.getColumns().addAll(timeColumn,titleColumn,messageColumn);	
		timeColumn.setSortType(TableColumn.SortType.DESCENDING);
		logTableView.getSortOrder().add(timeColumn);
		
		/*
		new Thread(new Runnable() { public void run() { 
			while(true)
				System.out.println(filteredData.size());
		}}).start();*/
		
		//logTableView.requestFocus();
		//logTableView.getSelectionModel().select(0);
		//logTableView.getFocusModel().focus(0);		
	}
	

}
