package application.Node;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class InstanceNode extends Parent{
    private AnchorPane pane;
	private Label label;
	private Rectangle rect;
	private double centerX;
	private double centerY;
     
    public InstanceNode(double layoutX, double layoutY, String name, double length) {
  	
    	pane=new AnchorPane();
    	pane.setLayoutX(layoutX);
    	pane.setLayoutY(layoutY);
    	
        label = new Label(name);
        label.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setTopAnchor(label, 0.0);
        //label.setLayoutX(0);
        //label.setLayoutY(0);
    	double labelWidth=name.length()*(8.0);
    	//System.out.println(name.length());
    	double labelHeight=30;
        label.setPrefWidth(labelWidth);
        label.setPrefHeight(labelHeight);
        label.setStyle("-fx-border-color:black; -fx-padding:3px;");
        pane.getChildren().add(label);
        	
        rect = new Rectangle();
        double rectHeight=length;
        double rectWidth=10;
        AnchorPane.setLeftAnchor(rect, labelWidth/2-rectWidth/2);
        AnchorPane.setTopAnchor(rect, labelHeight);
        rect.setWidth(rectWidth);
        rect.setHeight(rectHeight);
        rect.setStroke(Color.BLACK);
        rect.setFill(Color.WHITESMOKE);
        pane.getChildren().add(rect);
        
        getChildren().addAll(pane);
        
        this.centerX=layoutX+labelWidth/2;
        this.centerY=layoutY+labelHeight;
    }
    
    public double getX(){
    	return pane.getLayoutX();
    }
    
    public double getY(){
    	return pane.getLayoutY();
    }
    
    public double getCenterX(){
    	return centerX;
    }
    
    public double getCenterY(){
    	return centerY;
    }
    
    public void extend(double extend){
    	rect.setHeight(rect.getHeight()+extend);
    }
}
