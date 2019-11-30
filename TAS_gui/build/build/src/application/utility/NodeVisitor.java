package application.utility;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.AnchorPane;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import application.Node.ArrowNode.RightArrowNode;
import application.Node.IfElseNode;
import application.Node.InstanceNode;
import service.workflow.ast.ASTNode.ArgumentList;
import service.workflow.ast.ASTNode.BinaryOperator;
import service.workflow.ast.ASTNode.If;
import service.workflow.ast.ASTNode.MethodRef;
import service.workflow.ast.rspParser;


public class NodeVisitor {
	
	private int currentScope=0;
	private AnchorPane canvasPane;
	private Map<Integer,IfElseNode> ifElseNodes=new HashMap<>();
	private InstanceNode assistanceServiceNode;
		
    private double initialX=150.0;    // the initial layout x
    private double initialY=50.0;   // the initial layout y
    private double instanceNodeLen=450;  // length of instance node
    
    double layoutX=5.0;     // 
    double layoutY=0.0;     // 
    double intervalX=200;  // horizontal interval between two nodes
    double intervalY=20;  //  vertical interval 
    
	
    Map<String, InstanceNode> instanceNodes=new HashMap<>();

	public void setCanvasPane(AnchorPane canvasPane){
		this.canvasPane=canvasPane;
	    this.assistanceServiceNode=new InstanceNode(initialX,initialY,"TeleAssistanceService",instanceNodeLen);
	    canvasPane.getChildren().add(assistanceServiceNode);
	    
	    layoutY=assistanceServiceNode.getCenterY();	    
	}

	public void visit(Tree initialNode) {
		
		CommonTree node=(CommonTree) initialNode;
		if(node!=null && node.getType()!=rspParser.RETURN){
			
			switch(node.getType()){
			
			case rspParser.IF:{
				
				If ifNode=(If)node;
				
				// Get the condition
				String condition="";
				if(ifNode.getExpression() instanceof BinaryOperator){
					BinaryOperator opNode=(BinaryOperator)ifNode.getExpression();		
					switch(opNode.getType()){
		
					case rspParser.EQUAL_OP:
						condition="if "+opNode.getLeftHandExpression()+"=="+opNode.getRightHandExpression();
						break;
					}
				}				
				
				IfElseNode ifElseNode;
				
				if(!ifElseNodes.containsKey(currentScope)){
					ifElseNode=new IfElseNode(0,0);
					AnchorPane.setLeftAnchor(ifElseNode, layoutX);
					AnchorPane.setTopAnchor(ifElseNode, layoutY+=intervalY);
					
					this.canvasPane.getChildren().add(ifElseNode);
				}
				else{
					ifElseNode=new IfElseNode(0,0);
					IfElseNode currentNode=ifElseNodes.get(currentScope);
					currentNode.addIfElseNode(ifElseNode,0,0);
				}
				
				ifElseNode.addSubPane(condition);
				
				currentScope++;
				
				//System.out.println("Enter Scope "+currentScope+" "+ifElseNode);
				
				ifElseNodes.put(currentScope, ifElseNode);
				visit(ifNode.getIfStatement());

				if(ifNode.getElseStatement()!=null){
					ifElseNode.addSubPane("else");
					visit(ifNode.getElseStatement());
				}
				//System.out.println(ifElseNode.getChildrenCount());				
				//System.out.println("Leave Scope "+currentScope+" "+ifElseNode);

				currentScope--;

				break;
			}
			
			
			case rspParser.METHOD_REF:{
	
					MethodRef methodRef=(MethodRef)node;
					
					String service=methodRef.getServiceName();
					
					if(!service.equals("this")){
						if(!instanceNodes.containsKey(service)){
							InstanceNode serviceNode=new InstanceNode(initialX+(layoutX+=intervalX),initialY,
									methodRef.getServiceName(),instanceNodeLen);
							canvasPane.getChildren().add(serviceNode);
							serviceNode.toBack();
							instanceNodes.put(service, serviceNode);
						}
						
						InstanceNode serviceNode=instanceNodes.get(service);					
		    			double length=serviceNode.getCenterX()-assistanceServiceNode.getCenterX();
		    			

						if(!ifElseNodes.containsKey(currentScope)){
			    			//RightArrowNode arrowNode=new RightArrowNode(assistanceServiceNode.getCenterX(),layoutY+=intervalY, 
			    				//	methodRef.getOperationName(), length);
							//this.canvasPane.getChildren().add(arrowNode);
						}
						else{
							
							IfElseNode preNode=ifElseNodes.get(currentScope);
							//Label label=new Label(methodRef.getServiceName()+"--->"+methodRef.getOperationName()+"("+
								//	methodRef.getArguments()+")");
							//System.out.println(preNode.getCurrentAlignY());
											        	
					        	String operation=methodRef.getOperationName();
					        	ArgumentList argumentList=methodRef.getArguments();
					        	String arguments="";
					        	for(int j=0;j<argumentList.size();j++){
					        		arguments=arguments+" "+argumentList.getChild(j);
					        	}
							
				    			RightArrowNode arrowNode=new RightArrowNode(0,0, 
				    					operation+"("+arguments+" )", length);
		    			
				    			preNode.addNode(arrowNode, assistanceServiceNode.getCenterX()-(currentScope+2)*5, 0);
				    			
				    			/*
				    			RightArrowNode arrowNode1=new RightArrowNode(0,0, 
				    					operation+"("+arguments+" )", length);
		    			
				    			preNode.addNode(arrowNode1, assistanceServiceNode.getCenterX()-(currentScope+2)*5, 0);
				    			
				    			RightArrowNode arrowNode2=new RightArrowNode(0,0, 
				    					operation+"("+arguments+" )", length);
		    			
				    			preNode.addNode(arrowNode2, assistanceServiceNode.getCenterX()-(currentScope+2)*5, 0);
				    				*/    			
						}
						
					}
					
					//System.out.println("Scope "+currentScope+" "+methodRef.getServiceName()+"--->"+methodRef.getOperationName()+"("+
						//	methodRef.getArguments()+")");
					
				break;
			}
			
			default:{
				//System.out.println(node.toStringTree());
				if(node.getChildCount()!=0)
					node.getChildren().forEach(child->{
						visit((CommonTree)child);
					});
				break;
			}
			
			}
		}
	}
	
}
