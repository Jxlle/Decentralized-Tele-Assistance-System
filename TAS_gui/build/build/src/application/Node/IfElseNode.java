package application.Node;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class IfElseNode extends Parent{
	private double alignY=0;
	
	private VBox nodeBox=new VBox(2);
	private AnchorPane currentPane;
	
	
	public IfElseNode(double layoutX, double layoutY){
		//nodeBox.setLayoutX(layoutX);
		//nodeBox.setLayoutY(layoutY);
		nodeBox.setPadding(new Insets(0, 10, 0, 0));
		//nodeBox.setMinHeight(50.0);
		nodeBox.setStyle("-fx-border-color:black;-fx-border-style: dashed;");
        getChildren().add(nodeBox);
	}
	
	public void addSubPane(String condition){

		AnchorPane subPane=new AnchorPane();
		
		Label label=new Label(condition);
		AnchorPane.setLeftAnchor(label, 5.0);
		AnchorPane.setTopAnchor(label, 0.0);
		
		subPane.getChildren().add(label);
		
		nodeBox.getChildren().add(subPane);

		VBox.setVgrow(subPane, Priority.ALWAYS);
		subPane.setMaxHeight(Double.MAX_VALUE);
	    VBox.setMargin(subPane,new Insets(0,0,20,0));

        currentPane=subPane;
        alignY=0;
        
        
        //System.out.println(nodeBox.getChildren().size());
        //System.out.println(condition);
        
	}
	
	public AnchorPane getCurrentSubPane(){
		return this.currentPane;
	}
	
	public int getChildrenCount(){
		return nodeBox.getChildren().size();
	}
	
	public void addIfElseNode(Node node, double layoutX, double layoutY){	
		AnchorPane.setLeftAnchor(node, layoutX+5.0);
		AnchorPane.setTopAnchor(node, layoutY+(alignY+=10));
		//System.out.println(alignY);
		//AnchorPane.setBottomAnchor(node, 40.0);
		//AnchorPane.setRightAnchor(node, 20.0);
		currentPane.getChildren().add(node);
	}
	
	public void addArrowNode(){
		
	}
	
	public void addNode(Node node, double layoutX, double layoutY){	
		AnchorPane.setLeftAnchor(node, layoutX+5.0);
		AnchorPane.setTopAnchor(node, layoutY+(alignY+=15));
		//System.out.println(alignY);
		//AnchorPane.setBottomAnchor(node, -120.0);
		//AnchorPane.setRightAnchor(node, -200.0);
		currentPane.getChildren().add(node);
	}
	
}