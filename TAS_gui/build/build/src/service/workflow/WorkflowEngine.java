package service.workflow;

import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import service.composite.CompositeService;
import service.composite.SDCache;
import service.workflow.ast.ASTNode.Start;
import service.workflow.ast.rspLexer;
import service.workflow.ast.rspParser;
import taskgraph.TaskGraph.START;
import taskgraph.TaskGraphInterpreter;

/**
 * Workflow execution of composite service
 * 
 */
public class WorkflowEngine {

    private CompositeService service;

    /**
     * Constructor to create workflow engine. Local cache is not supported.
     * @param service the composite service
     */
    public WorkflowEngine(CompositeService service) {
    	this.service = service;
    }

    /**
     * Execute the workflow with specific QoS requirement and initial parameters
     * @param workFlow the workflow to be executed
     * @param qosRequirement the QoS requirements to be satisfied
     * @param params   initial parameters for the workflow
     * @return the result after executing the workflow
     */
    public Object executeWorkflow(String workFlow, String qosRequirement, Object... params) {
		rspLexer lexer;
		try {
			lexer = new rspLexer(new ANTLRFileStream(workFlow));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			rspParser parser = new rspParser(tokens);

			// Get AST root
			Start start = (Start) parser.start().getTree();

			// ASTSymTabVisualizer astVisualizer = new ASTSymTabVisualizer();
			// astVisualizer.exportGML(workFlow + "_AST", start);

			START startGraph = (START) start.getFirst();
			// TaskGraphVisualizer tgVisualizer = new TaskGraphVisualizer();
			// tgVisualizer.exportGML(workFlow + "_TaskGraph", startGraph);

			TaskGraphInterpreter interpreter = new TaskGraphInterpreter();
			Object value = interpreter.interpret(startGraph, 
					qosRequirement, service, params);
			System.out.println("Result:" + value);
			return value;
		} catch (IOException | RecognitionException e) {
			e.printStackTrace();
			return null;
		}
    }

}
