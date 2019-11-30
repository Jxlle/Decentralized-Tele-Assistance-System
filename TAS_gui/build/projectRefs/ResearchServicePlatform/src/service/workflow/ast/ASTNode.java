package service.workflow.ast;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import taskgraph.TaskGraph;
import taskgraph.TaskGraph.ARRAY_ACCESS;
import taskgraph.TaskGraph.BinaryOp;
import taskgraph.TaskGraph.CALL;
import taskgraph.TaskGraph.EAssignOp;
import taskgraph.TaskGraph.EBinaryOp;
import taskgraph.TaskGraph.EUnaryOp;
import taskgraph.TaskGraph.IF;
import taskgraph.TaskGraph.LIST_END;
import taskgraph.TaskGraph.LIST_ITEM;
import taskgraph.TaskGraph.LIST_START;
import taskgraph.TaskGraph.Load;
import taskgraph.TaskGraph.PARALLEL;
import taskgraph.TaskGraph.PARAM;
import taskgraph.TaskGraph.PUSH;
import taskgraph.TaskGraph.QUALIFIED_ACCESS;
import taskgraph.TaskGraph.RETURN;
import taskgraph.TaskGraph.Store;
import taskgraph.TaskGraph.UnaryOp;

public abstract class ASTNode extends CommonTree {

	public static HashMap<String, Object> symbolTable = null;
	private static int declarationID = 1;

	public void print() {
		if (token != null)
			System.out.println(token.getText());
		if (this.children != null)
			for (Object i : this.children) {
				if (i instanceof ASTNode)
					((ASTNode) i).print();
			}
	}

	public void setSymbolTable() {
		if (this.children != null)
			for (Object i : this.children) {
				if (i instanceof ASTNode)
					((ASTNode) i).setSymbolTable();
			}
	}

	public boolean isExists(String name) {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChild(i).getClass().getSimpleName().equals(name))
				return true;
		}
		return false;
	}

	public ASTNode getNode(String name) {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChild(i).getClass().getSimpleName().equals(name))
				return (ASTNode) getChild(i);
		}
		return null;
	}

	// TaskGraph Data and functions
	public TaskGraph last;
	public TaskGraph first;

	@Override
	public abstract ASTNode clone();

	public static class Start extends ASTNode {

		public Start(int ttype) {
			token = new CommonToken(ttype, "Start");
		}

		public TaskGraph getFirst() {
			if (first != null)
				return first;
			first = new TaskGraph.START();
			ParamList paramList = getParamList();
			if (paramList != null && paramList.size() > 0) {
				first.setNext(paramList.getFirst());
				StmntList stmntList = getStmntList();
				paramList.setNext(stmntList.getFirst());

			} else {
				StmntList stmntList = getStmntList();
				first.setNext(stmntList.getFirst());
			}

			return first;
		}

		public StmntList getStmntList() {
			return (StmntList) getNode(StmntList.class.getSimpleName());
		}

		public ParamList getParamList() {
			return (ParamList) getNode(ParamList.class.getSimpleName());
		}

		@Override
		public Start clone() {
			Start start = new Start(getType());
			ParamList paramList = getParamList();
			if (paramList != null) {
				start.addChild(paramList.clone());
			}
			StmntList result = new StmntList(getType());

			if (result != null) {
				result.addChild(result.clone());
			}

			return start;
		}
	}

	public static class TaskList extends ASTList<Expression> {

		public TaskList(int ttype, String type) {
			super(ttype, type);
		}

		// @Override
		public TaskGraph getFirst(boolean returnList) {
			if (first != null)
				return first;

			// Task List have only one element then no need to create array
			if (size() <= 1 && !returnList) {
				first = get(0).getFirst();
				last = get(0).getLast();
				return first;
			}

			LIST_START listStart = new LIST_START();
			first = listStart;

			// if (get(0) instanceof DeclType){
			// TypeId typeId = ((DeclType)get(0)).getTypeId();
			// if (typeId instanceof PrimitiveType){
			// PrimitiveType type = (PrimitiveType)typeId;
			// if (type.getPrimitiveType() == EPrimitiveType.INT){
			// // listStart.setNext(next)
			// }
			// }
			// }
			// else{
			listStart.setNext(get(0).getFirst());
			// }
			LIST_ITEM item = new LIST_ITEM();
			get(0).setNext(item);
			item.setPrev(get(0).getLast());
			for (int i = 0; i < size() - 1; i++) {
				item.setNext(get(i + 1).getFirst());
				item = new LIST_ITEM();
				get(i + 1).setNext(item);
				item.setPrev(get(i + 1).getLast());
			}
			LIST_END listEnd = new LIST_END();
			item.setNext(listEnd);
			last = listEnd;
			return first;
		}

		public TaskGraph getLast() {
			return last;
		}

		// @Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public TaskList clone() {
			TaskList result = new TaskList(getType(), getToken().getText());

			for (int i = 0; i < getChildCount(); i++) {
				Expression child = ((Expression) getChild(i)).clone();
				result.addChild(child);
			}

			return result;
		}

	}

	public static class Expression extends Stmnt {

		public Expression(int ttype, String name) {
			super(ttype, name);
		}

		@Override
		public TaskGraph getLast() {
			return last;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public TaskGraph getFirst() {
			return null;
		}

		@Override
		public Expression clone() {
			Expression result = new Expression(getType(), getToken().getText());

			return result;
		}

	}

	public static class VarRef extends Expression {

		String id;

		public VarRef(int ttype, String id) {
			super(ttype, "VarRef");
			this.id = id;
		}

		public String getId() {
			return id;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}

			Load load = new Load();

			load.setVarName(getId());
			first = last = load;

			return first;
		}

		// @Override
		public void setPrev(TaskGraph prev) {
			last.setPrev(prev);
		}

		@Override
		public TaskGraph getLast() {
			return last;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public String toString() {
			return id;
		}

		@Override
		public VarRef clone() {
			VarRef result = new VarRef(getType(), getId());

			return result;
		}

	}

	public static class ExprList extends ASTList<Expression> {

		public ExprList(int ttype) {
			super(ttype, "ExprList");
		}

		// @Override
		public TaskGraph getFirst() {
			if (first != null)
				return first;

			first = get(0).getFirst();
			for (int i = 0; i < size() - 1; i++) {
				get(i).setNext(get(i + 1).getFirst());
			}
			return first;
		}

		public TaskGraph getLast() {
			return get(size() - 1).getLast();
		}

		// @Override
		public void setNext(TaskGraph next) {
			get(size() - 1).setNext(next);
		}

		@Override
		public ExprList clone() {
			ExprList result = new ExprList(getType());

			for (int i = 0; i < getChildCount(); i++) {
				Expression child = ((Expression) getChild(i)).clone();
				result.addChild(child);
			}

			return result;
		}
	}

	public static class StmntList extends ASTList<Stmnt> {

		public StmntList(int ttype) {
			super(ttype, "StmntList");
		}

		public TaskGraph getFirst() {
			if (first != null)
				return first;

			first = get(0).getFirst();
			for (int i = 0; i < size() - 1; i++) {
				get(i).setNext(get(i + 1).getFirst());
			}
			last = get(this.getChildCount() - 1).getLast();
			return first;
		}

		@Override
		public StmntList clone() {
			StmntList result = new StmntList(getType());

			for (int i = 0; i < getChildCount(); i++) {
				Stmnt child = ((Stmnt) getChild(i)).clone();
				result.addChild(child);
			}

			return result;
		}

		public void setNext(TaskGraph next) {
			if (size() > 0) {
				get(this.getChildCount() - 1).setNext(next);
			} else {
				if (last != null)
					last.setNext(next);
			}
		}
	}

	public static abstract class Stmnt extends ASTNode {

		public Stmnt(int ttype, String stmnt) {
			token = new CommonToken(ttype, stmnt);
		}

		public abstract void setNext(TaskGraph first);

		public TaskGraph getFirst() {
			return null;
		}

		public TaskGraph getLast() {
			return last;
		}

		@Override
		public abstract Stmnt clone();

	}

	public static class ParamList extends ASTList<Param> {

		public ParamList(int ttype) {
			super(ttype, "ParamList");
		}

		// @Override
		public TaskGraph getFirst() {

			if (getChildCount() != 0 && first != null) {
				return first;
			}
			// if no element then graph will be set to next node
			if (getChildCount() == 0) {
				return last;
			} else {

				first = get(0).getFirst();
				for (int i = 0; i < size() - 1; i++) {
					get(i).setNext(get(i + 1).getFirst());
				}
			}
			return first;
		}

		// @Override
		public void setNext(TaskGraph next) {
			if (size() == 0) {
				last = next;
			} else {
				get(size() - 1).setNext(next);
			}
		}

		@Override
		public ParamList clone() {
			ParamList result = new ParamList(getType());

			for (int i = 0; i < getChildCount(); i++) {
				Param child = ((Param) getChild(i)).clone();
				result.addChild(child);
			}

			return result;
		}
	}

	public static class ArgumentList extends TaskList {

		public ArgumentList(int ttype) {
			super(ttype, "ArgumentList");
		}

		@Override
		public ArgumentList clone() {
			ArgumentList result = new ArgumentList(getType());

			for (int i = 0; i < getChildCount(); i++) {
				Expression child = ((Expression) getChild(i)).clone();
				result.addChild(child);
			}

			return result;
		}
	}

	public static class ForLoop extends Stmnt {

		public ForLoop(int ttype) {
			super(ttype, "ForLoop");
		}

		public ExprList getInitialization() {
			return (ExprList) getChild(0);
		}

		public ExprList getCondition() {
			return (ExprList) getChild(1);
		}

		public ExprList getUpdateExpr() {
			return (ExprList) getChild(2);
		}

		public StmntList getStatement() {
			return (StmntList) getChild(3);
		}

		@Override
		public ForLoop clone() {
			ForLoop result = new ForLoop(getType());

			result.addChild(((ExprList) getChild(0)).clone());
			result.addChild(((ExprList) getChild(1)).clone());
			result.addChild(((ExprList) getChild(2)).clone());
			result.addChild(((Stmnt) getChild(3)).clone());

			return result;
		}

		@Override
		public TaskGraph getFirst() {
			// TODO: check if it works with empty expressions / statement blocks
			if (first != null) {
				return first;
			}
			first = getInitialization().getFirst();

			// If multiple conditions with comma, only consider the right one
			Expression condition = getCondition()
					.get(getCondition().size() - 1);

			getInitialization().setNext(condition.getFirst());

			TaskGraph.IF If = new TaskGraph.IF();
			condition.setNext(If);

			If.setPrev(condition.getLast());

			StmntList stmntList = getStatement();
			if (stmntList.size() > 0) {
				If.setTrue(stmntList.getFirst());
				stmntList.setNext(getUpdateExpr().getFirst());
			} else {
				If.setTrue(getUpdateExpr().getFirst());
			}
			getUpdateExpr().setNext(condition.getFirst());
			last = If;
			return first;

		}

		@Override
		public void setNext(TaskGraph next) {
			((IF) last).setFalse(next);
			((IF) last).setNext(next);
		}

	}

	public static class While extends Stmnt {

		public While(int ttype) {
			super(ttype, "While");
		}

		public Expression getExpression() {
			return (Expression) getChild(0);
		}

		public StmntList getStatement() {
			return (StmntList) getChild(1);
		}

		@Override
		public While clone() {
			While result = new While(getType());

			result.addChild(((Expression) getChild(0)).clone());
			result.addChild(((Stmnt) getChild(1)).clone());

			return result;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}
			Expression exp = getExpression();
			first = exp.getFirst();
			TaskGraph.IF If = new TaskGraph.IF();
			// If.setFirst(first);
			If.setPrev(exp.getLast());
			exp.setNext(If);

			// Check if while statement is not empty

			StmntList stmntList = getStatement();
			if (stmntList.size() > 0) {
				If.setTrue(stmntList.getFirst());
				stmntList.setNext(first);
			} else {
				If.setTrue(first);
			}

			last = If;
			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			((IF) last).setFalse(next);
			((IF) last).setNext(next);
		}
	}

	public static class DoWhile extends Stmnt {

		public DoWhile(int ttype) {
			super(ttype, "DoWhile");
		}

		public Expression getExpression() {
			return (Expression) getChild(1);
		}

		public StmntList getStatementList() {
			return (StmntList) getChild(0);
		}

		@Override
		public DoWhile clone() {
			DoWhile result = new DoWhile(getType());

			result.addChild(((Stmnt) getChild(0)).clone());
			result.addChild(((Expression) getChild(1)).clone());

			return result;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}

			Expression exp = getExpression();

			TaskGraph.IF If = new TaskGraph.IF();
			// If.setFirst(first);
			exp.getFirst();
			If.setPrev(exp.getLast());
			exp.setNext(If);

			// Check if while statement is not empty
			StmntList stmntList = getStatementList();
			if (stmntList.size() > 0) {
				TaskGraph stmntGraph = stmntList.getFirst();
				first = stmntGraph;
				If.setTrue(stmntGraph);
				stmntList.setNext(exp.getFirst());
			} else {
				first = exp.getFirst();
				If.setTrue(exp.getFirst());
			}

			last = If;
			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			((IF) last).setFalse(next);
			((IF) last).setNext(next);
		}
	}

	public static class Parallel extends Stmnt {

		public Parallel(int ttype) {
			super(ttype, "Parallel");
		}

		public StmntList getStatementList() {
			return (StmntList) getChild(0);
		}

		@Override
		public Parallel clone() {
			Parallel result = new Parallel(getType());

			result.addChild(((Stmnt) getChild(0)).clone());

			return result;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}

			PARALLEL parallel = new TaskGraph.PARALLEL();
			first = parallel;
			last = first;

			StmntList stmntList = getStatementList();
			for (int i = 0; i < stmntList.size(); i++) {
				parallel.getStatements().add(stmntList.get(i).getFirst());
				stmntList.get(i).setNext(new TaskGraph.RETURN());
			}

			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}
	}

	public static class If extends Stmnt {

		public If(int ttype) {
			super(ttype, "If");
		}

		public Expression getExpression() {
			return (Expression) getChild(0);
		}

		public StmntList getIfStatement() {
			return (StmntList) getChild(1);
		}

		public StmntList getElseStatement() {
			return getChildCount() > 2 ? (StmntList) getChild(2) : null;
		}

		@Override
		public If clone() {
			If result = new If(getType());

			result.addChild(((Expression) getChild(0)).clone());
			result.addChild(((StmntList) getChild(1)).clone());

			if (getChildCount() > 2) {
				result.addChild(((StmntList) getChild(2)).clone());
			}

			return result;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}
			Expression exp = getExpression();
			first = exp.getFirst();
			TaskGraph.IF If = new TaskGraph.IF();
			last = If;
			If.setPrev(exp.getLast());
			exp.setNext(If);
			StmntList ifStatement = getIfStatement();
			if (ifStatement.size() > 0)
				If.setTrue(getIfStatement().getFirst());

			StmntList elseStmnt = getElseStatement();
			if (elseStmnt != null && elseStmnt.size() > 0)
				If.setFalse(elseStmnt.getFirst());

			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			getIfStatement().setNext(next);

			StmntList elseStmnt = getElseStatement();
			if (elseStmnt != null)
				elseStmnt.setNext(next);

			last.setNext(next);

		}
	}

	public static class Param extends ASTNode {

		private String varName;

		public Param(int ttype, String varName) {
			token = new CommonToken(ttype, "Param");
			this.varName = varName;
		}

		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		public TaskGraph getFirst() {
			if (first != null)
				return first;
			PARAM param = new PARAM(varName);
			first = param;
			last = param;
			return first;
		}

		public String getName() {
			return varName;
		}

		public TaskGraph getLast() {
			return last;
		}

		@Override
		public Param clone() {
			Param result = new Param(getType(), varName);

			return result;
		}

	}

	public static class Return extends Stmnt {

		public Return(int ttype) {
			super(ttype, "Return");
		}

		public Expression getExpression() {
			return getChildCount() > 0 ? (Expression) getChild(0) : null;
		}

		@Override
		public Return clone() {
			Return result = new Return(getType());

			if (getExpression() != null) {
				result.addChild(getExpression().clone());
			}

			return result;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}

			RETURN ret = new RETURN();
			last = ret;

			// If expression will be null in case of void function
			Expression exp = getExpression();
			if (exp != null) {
				first = exp.getFirst();
				exp.setNext(ret);
				ret.setPrev(exp.getLast());
			} else {
				first = ret;
			}
			return first;
		}

		@Override
		public void setNext(TaskGraph first) {
			// Nothing to do.. as return statement cannot have next
		}
	}

	public static class AssignmentStmt extends Expression {

		public AssignmentStmt(int token) {
			super(token, "AssignmentOp");
		}

		public int getOperatorType() {
			return getChild(0).getType();
		}

		public Expression getRightExpression() {
			return (Expression) getChild(2);
		}

		public Expression getLeftExpression() {
			return (Expression) getChild(1);
		}

		@Override
		public TaskGraph getFirst() {
			// if (first != null) {
			// return first;
			// }
			//
			// // First of all, right-hand side expression
			// // should be visited
			// Expression exp = getRightExpression();
			// first = exp.getFirst();
			//
			// // Prepare the store task node
			// Store store = new Store();
			// store.setAssignOp(((AssignOp) getChild(0)).getAssignmentOp());
			//
			// last = store;
			// if (getLeftExpression() instanceof VarRef) {
			// store.setVarName(((VarRef) getLeftExpression()).getId());
			// }
			//
			// // store.setLocal(!def.classVariable);
			//
			// // TODO: add support for arrays and structures
			//
			// if (store.getAssignOp() == EAssignOp.ASSIGN) {
			// // Easy case: just store the value
			// exp.setNext(store);
			// store.setPrev(exp.getLast());
			//
			// } else {
			// // Insert the left-hand side expression
			// // into task graph
			// Expression assignee = getLeftExpression();
			// exp.setNext(assignee.getFirst());
			//
			// // Insert an operator task node
			// // into task graph
			// BinaryOp binOp = new BinaryOp();
			// binOp.setBinaryOp(getBinaryOp(store.getAssignOp()));
			//
			// assignee.getLast().setNext(binOp);
			// binOp.setLeft(assignee.getLast());
			// binOp.setRight(exp.getLast());
			//
			// binOp.setNext(store);
			// store.setPrev(binOp);
			// }
			//
			// return first;
			if (first != null) {
				return first;
			}

			Store store = new Store();
			store.setAssignOp(((AssignOp) getChild(0)).getAssignmentOp());

			last = store;

			// Solve right expression first

			Expression rightExpression = getRightExpression();
			Expression leftExpression = getLeftExpression();

			first = rightExpression.getFirst();
			if (leftExpression instanceof VarRef) {
				rightExpression.setNext(store);
				store.setPrev(getRightExpression().getLast());
				store.setRightExpression(store.getPrev());

				store.setVarName(((VarRef) leftExpression).getId());
			} else {
				rightExpression.setNext(leftExpression.getFirst());
				leftExpression.setNext(store);
				store.setPrev(leftExpression.getLast());
				store.setRightExpression(rightExpression.getLast());
			}
			return first;

		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}
	}

	public static class TernaryOp extends Expression {

		public TernaryOp(Token token) {
			super(token.getType(), "TernaryOp");
		}

		public Expression getExpression() {
			return (Expression) getChild(0);
		}

		public Expression getIfExpression() {
			return (Expression) getChild(1);
		}

		public Expression getElseExpression() {
			return (Expression) getChild(2);
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}
			Expression exp = getExpression();
			first = exp.getFirst();
			TaskGraph.IF If = new TaskGraph.IF();
			If.setPrev(exp.getLast());
			exp.setNext(If);
			If.setTrue(getIfExpression().getFirst());
			If.setFalse(getElseExpression().getFirst());
			taskgraph.TaskGraph.TernaryOp ternaryOp = new TaskGraph.TernaryOp();
			ternaryOp.setPrev(If);
			If.setNext(ternaryOp);
			ternaryOp.setTrue(getIfExpression().getLast());
			ternaryOp.setFalse(getElseExpression().getLast());
			getIfExpression().setNext(ternaryOp);
			getElseExpression().setNext(ternaryOp);
			last = ternaryOp;
			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			// getIfExpression().setNext(next);
			// getElseExpression().setNext(next);
			last.setNext(next);
		}
	}

	public static abstract class BinaryOperator extends Expression {

		private EBinaryOp binaryOp;

		public BinaryOperator(Token token, String opName, EBinaryOp op) {
			super(token.getType(), opName);
			binaryOp = op;
		}

		public Expression getLeftHandExpression() {
			return (Expression) getChild(0);
		}

		public Expression getRightHandExpression() {
			return (Expression) getChild(1);
		}

		@Override
		public TaskGraph getLast() {
			return last;
		}

		@Override
		public void setNext(TaskGraph next) {
			if (last != null)
				last.setNext(next);
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}

			Expression exp1 = getLeftHandExpression();
			Expression exp2 = getRightHandExpression();
			first = exp1.getFirst();
			exp1.setNext(exp2.getFirst());
			BinaryOp task = new BinaryOp();
			task.setBinaryOp(binaryOp);
			exp2.setNext(task);
			last = task;
			task.setLeft(exp1.getLast());
			task.setRight(exp2.getLast());

			return first;
		}

		@Override
		public BinaryOperator clone() {
			BinaryOperator result = null;

			try {

				// Use reflection to acquire correct Constructor
				Constructor ctor = getClass().getConstructor(Token.class);

				result = (BinaryOperator) ctor.newInstance(getToken());

				for (int i = 0; i < getChildCount(); i++) {
					Expression child = ((Expression) getChild(i)).clone();
					result.addChild(child);
				}

			} catch (Exception e) {
				return null;
			}

			return result;
		}

	}

	public static class LogicOrOp extends BinaryOperator {

		public LogicOrOp(Token token) {
			super(token, "LogicOrOp", EBinaryOp.LOGIC_OR);
		}
	}

	public static class ImplyOp extends BinaryOperator {

		public ImplyOp(Token token) {
			super(token, "ImplyOp", EBinaryOp.IMPLY);
		}
	}

	public static class AndOp extends LogicalAndOp {

		public AndOp(Token token) {
			super(token);
		}
	}

	public static class OrOp extends BinaryOperator {

		public OrOp(Token token) {
			super(token, "OrOp", EBinaryOp.LOGIC_OR);
		}
	}

	public static class LogicalAndOp extends BinaryOperator {

		public LogicalAndOp(Token token) {
			super(token, "LogicalAndOp", EBinaryOp.LOGIC_AND);
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}

			Expression exp1 = getLeftHandExpression();
			Expression exp2 = getRightHandExpression();
			first = exp1.getFirst();
			IF ifTask = new IF();
			exp1.setNext(ifTask);
			ifTask.setPrev(exp1.getLast());
			ifTask.setTrue(exp2.getFirst());
			BinaryOp binaryOp = new BinaryOp();
			binaryOp.setBinaryOp(EBinaryOp.LOGIC_AND);
			ifTask.setNext(binaryOp);
			binaryOp.setLeft(ifTask);
			binaryOp.setRight(exp2.getLast());
			exp2.setNext(binaryOp);
			last = binaryOp;

			return first;
		}
	}

	public static class BitwiseInclOrOp extends BinaryOperator {

		public BitwiseInclOrOp(Token token) {
			super(token, "BitwiseInclOrOp", EBinaryOp.BITWISE_INCL_OR);
		}
	}

	public static class BitwiseExclOrOp extends BinaryOperator {

		public BitwiseExclOrOp(Token token) {
			super(token, "BitwiseExclOrOp", EBinaryOp.BITWISE_EXCL_OR);
		}
	}

	public static class BitwiseAndOp extends BinaryOperator {

		public BitwiseAndOp(Token token) {
			super(token, "BitwiseAndOp", EBinaryOp.BITWISE_AND);
		}
	}

	public static class NotEqualOp extends BinaryOperator {

		public NotEqualOp(Token token) {
			super(token, "NotEqualOp", EBinaryOp.NOT_EQUAL);
		}

	}

	public static class EqualOp extends BinaryOperator {

		public EqualOp(Token token) {
			super(token, "EqualOp", EBinaryOp.EQUAL);
		}
	}

	public static class Prime extends ASTNode {

		public Prime(Token token) {
			this.token = token;
		}

		@Override
		public Prime clone() {
			// TODO: copy instead of passing the reference?
			Prime result = new Prime(getToken());

			for (int i = 0; i < getChildCount(); i++) {
				Expression child = ((Expression) getChild(i)).clone();
				result.addChild(child);
			}

			return result;
		}

	}

	public static class LtOp extends BinaryOperator {

		public LtOp(Token token) {
			super(token, "LtOp", EBinaryOp.LT);
		}
	}

	public static class GtEqualOp extends BinaryOperator {

		public GtEqualOp(Token token) {
			super(token, "GtEqualOp", EBinaryOp.GT_EQUAL);
		}
	}

	public static class LtEqualOp extends BinaryOperator {

		public LtEqualOp(Token token) {
			super(token, "LtEqualOp", EBinaryOp.LT_EQUAL);
		}
	}

	public static class GtOp extends BinaryOperator {

		public GtOp(Token token) {
			super(token, "GtOp", EBinaryOp.GT);
		}
	}

	public static class MaxOp extends BinaryOperator {

		public MaxOp(Token token) {
			super(token, "MaxOp", EBinaryOp.MAX);
		}
	}

	public static class MinOp extends BinaryOperator {

		public MinOp(Token token) {
			super(token, "MinOp", EBinaryOp.MIN);
		}
	}

	public static class LeftShiftOp extends BinaryOperator {

		public LeftShiftOp(Token token) {
			super(token, "LeftShiftOp", EBinaryOp.LEFT_SHIFT);
		}
	}

	public static class RightShiftOp extends BinaryOperator {

		public RightShiftOp(Token token) {
			super(token, "RightShiftOp", EBinaryOp.RIGHT_SHIFT);
		}
	}

	public static class PlusOp extends BinaryOperator {

		public PlusOp(Token token) {
			super(token, "PlusOp", EBinaryOp.PLUS);
		}
	}

	public static class MinusOp extends BinaryOperator {

		public MinusOp(Token token) {
			super(token, "MinusOp", EBinaryOp.MINUS);
		}
	}

	public static class MultOp extends BinaryOperator {

		public MultOp(Token token) {
			super(token, "MultOp", EBinaryOp.MULT);
		}
	}

	public static class RemainderOp extends BinaryOperator {

		public RemainderOp(Token token) {
			super(token, "RemainderOp", EBinaryOp.REMAINDER);
		}
	}

	public static class DivisionOp extends BinaryOperator {

		public DivisionOp(Token token) {
			super(token, "DivisionOp", EBinaryOp.DIVISION);
		}
	}

	public static class PreDecrementOp extends UnaryOperator {

		public PreDecrementOp(int ttype) {
			super(ttype, "PreDecrement", EUnaryOp.PREFIX_DECREMENT);
		}
	}

	public static class PreIncrementOp extends UnaryOperator {

		public PreIncrementOp(int ttype) {
			super(ttype, "PreIncrement", EUnaryOp.PREFIX_INCREMENT);
		}
	}

	public static class MethodRef extends Expression {
		String operationName;

		public MethodRef(int ttype, String methodName) {
			super(ttype, "MethodRef");
			this.operationName = methodName;
		}

		public String getOperationName() {
			return operationName;
		}

		public String getServiceName() {
			return ((VarRef) getChild(0)).getId();
		}

		public ArgumentList getArguments() {
			return (ArgumentList) getChild(1);
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}

			CALL call = new CALL(getServiceName(), operationName);

			ArgumentList args = getArguments();

			if (args != null && args.size() > 0) {
				first = args.get(0).getFirst();
				last = args.get(0).getLast();
				call.addArgument((TaskGraph.Expression) last);

				Expression expression;
				for (int i = 1; i < args.size(); i++) {
					expression = args.get(i);
					last.setNext(expression.getFirst());
					last = expression.getLast();
					call.addArgument((TaskGraph.Expression) last);
				}

				last.setNext(call);
				last = call;
			} else {
				first = last = call;
			}
			return first;
		}

		// @Override
		public void setPrev(TaskGraph prev) {
			// getArguments().setPrev(prev);
		}

		@Override
		public TaskGraph getLast() {
			return last;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

	}

	public static class ArrayAccess extends Expression {

		public ArrayAccess(int ttype) {
			super(ttype, "ArrayAccess");
		}

		public Expression getLeftExpression() {
			return (Expression) getChild(0);
		}

		public Expression getIndex() {
			return (Expression) getChild(1);
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null)
				return first;

			ARRAY_ACCESS arraAccess = new ARRAY_ACCESS();
			first = getIndex().getFirst();
			getIndex().setNext(getLeftExpression().getFirst());

			arraAccess.setIndex(getIndex().getLast());
			getLeftExpression().setNext(arraAccess);
			arraAccess.setPrev(getLeftExpression().getLast());

			last = arraAccess;
			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}
	}

	public static class QualifiedAcess extends Expression {

		String memberId;

		public QualifiedAcess(int ttype, String memberId) {
			super(ttype, "QualifiedAcess");
			this.memberId = memberId;
		}

		public String getId() {
			return memberId;
		}

		public Expression getLeftExpression() {
			return (Expression) getChild(0);
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null)
				return first;

			QUALIFIED_ACCESS qualifiedAccess = new QUALIFIED_ACCESS();
			first = getLeftExpression().getFirst();
			getLeftExpression().setNext(qualifiedAccess);

			qualifiedAccess.setVarName(memberId);
			qualifiedAccess.setPrev(getLeftExpression().getLast());

			last = qualifiedAccess;
			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public QualifiedAcess clone() {
			QualifiedAcess result = new QualifiedAcess(getType(), memberId);

			for (int i = 0; i < getChildCount(); i++) {
				Expression child = ((Expression) getChild(i)).clone();
				result.addChild(child);
			}

			return result;
		}

	}

	public static class PostDecrement extends UnaryOperator {

		public PostDecrement(int ttype) {
			super(ttype, "PostDecrement", EUnaryOp.POSTFIX_DECREMENT);
		}
	}

	public static class PostIncrement extends UnaryOperator {

		public PostIncrement(int ttype) {
			super(ttype, "PostIncrement", EUnaryOp.POSTFIX_INCREMENT);
		}
	}

	public static class BooleanLiteral extends Expression {

		int value;

		public BooleanLiteral(int ttype, boolean value) {
			super(ttype, "BooleanLiteral");
			this.value = value ? 1 : 0;
		}

		public int getLiteralValue() {
			return value;
		}

		@Override
		public TaskGraph getFirst() {
			if (last != null) {
				return last;
			}
			taskgraph.TaskGraph.BooleanLiteral literal = new TaskGraph.BooleanLiteral();
			literal.setValue(value);

			last = literal;

			return last;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public BooleanLiteral clone() {
			BooleanLiteral result = new BooleanLiteral(getType(),
					getLiteralValue() != 0);

			return result;
		}
	}

	public static class IntegerLiteral extends Expression {

		Integer value;

		public IntegerLiteral(int ttype, int value) {
			super(ttype, "IntegerLiteral");
			this.value = value;
		}

		public Integer getLiteralValue() {
			return value;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}
			taskgraph.TaskGraph.IntLiteral literal = new TaskGraph.IntLiteral();
			literal.setValue(value);

			first = literal;
			last = literal;

			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public TaskGraph getLast() {
			return last;
		}

		@Override
		public String toString() {
			return value.toString();
		}

		@Override
		public IntegerLiteral clone() {
			IntegerLiteral result = new IntegerLiteral(getType(),
					getLiteralValue());

			return result;
		}

	}

	public static class NullLiteral extends Expression {

		Object value;

		public NullLiteral(int ttype, Object value) {
			super(ttype, "NullLiteral");
			this.value = value;
		}

		public Object getLiteralValue() {
			return value;
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}
			taskgraph.TaskGraph.Literal literal = new TaskGraph.Literal();
			literal.setValue(value);

			first = literal;
			last = literal;

			return first;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public TaskGraph getLast() {
			return last;
		}

		@Override
		public String toString() {
			return value.toString();
		}

		@Override
		public NullLiteral clone() {
			NullLiteral result = new NullLiteral(getType(), getLiteralValue());

			return result;
		}

	}

	// ///////////////////////////UNARY OPERATORS///////////////////////////////

	public static abstract class UnaryOperator extends Expression {

		protected EUnaryOp unaryOp;

		public UnaryOperator(int ttype, String opName, EUnaryOp op) {
			super(ttype, opName);
			unaryOp = op;
		}

		public UnaryOperator(Token token, String opName, EUnaryOp op) {
			super(token.getType(), opName);
			unaryOp = op;
		}

		public Expression getExpression() {
			return (Expression) getChild(0);
		}

		@Override
		public TaskGraph getLast() {
			return last;
		}

		@Override
		public void setNext(TaskGraph next) {
			last.setNext(next);
		}

		@Override
		public TaskGraph getFirst() {
			if (first != null) {
				return first;
			}
			Expression exp = getExpression();
			first = exp.getFirst();
			UnaryOp task = new UnaryOp();
			exp.setNext(task);
			task.setUnaryOp(unaryOp);
			task.setPrev(exp.getLast());
			last = task;
			return first;
		}

		@Override
		public UnaryOperator clone() {
			UnaryOperator result = null;

			try {

				// Use reflection to acquire correct Constructor
				Constructor ctor = getClass().getConstructor(Token.class);

				result = (UnaryOperator) ctor.newInstance(getToken());

				for (int i = 0; i < getChildCount(); i++) {
					Expression child = ((Expression) getChild(i)).clone();
					result.addChild(child);
				}

			} catch (Exception e) {
				return null;
			}

			return result;
		}

	}

	public static class UnaryNotOp extends UnaryOperator {

		public UnaryNotOp(Token token) {
			super(token, "UnaryNot", EUnaryOp.NOT);
		}
	}

	public static class UnaryPlusOp extends UnaryOperator {

		public UnaryPlusOp(Token token) {
			super(token, "UnaryPlus", EUnaryOp.PLUS);
		}
	}

	public static class UnaryMinusOp extends UnaryOperator {

		public UnaryMinusOp(Token token) {
			super(token, "UnaryMinus", EUnaryOp.MINUS);
		}
	}

	// ///////////////////////////ASSIGNMENT OPERATORS/////////////////////////

	// public static class Assign extends Expression {
	// public Assign(int ttype) {
	// super(ttype, "Assign");
	// }
	//
	// // public Variable def;
	//
	// public Expression getLeftExpression() {
	// return (Expression) getChild(1);
	// }
	//
	// public Expression getRightExpression() {
	// return (Expression) getChild(2);
	// }
	//
	// public EAssignOp getAssignOp() {
	// return ((AssignOp) getChild(0)).getAssignmentOp();
	// }
	//
	// @Override
	// public TaskGraph getFirst() {
	// if (first != null) {
	// return first;
	// }
	//
	// Store store = new Store();
	// last = store;
	//
	// // Solve right expression first
	// first = getRightExpression().getFirst();
	// getRightExpression().setNext(getLeftExpression().getFirst());
	//
	//
	// store.setValue(getRightExpression().getLast());
	// store.setAssignOp(getAssignOp());
	//
	// getLeftExpression().setNext(store);
	// store.setPrev(getLeftExpression().getLast());
	//
	// return first;
	// }
	//
	// @Override
	// public void setNext(TaskGraph next) {
	// last.setNext(next);
	// }
	// }

	public static class AssignOp extends ASTNode {

		public AssignOp(Token token) {
			this.token = token;
		}

		public EAssignOp getAssignmentOp() {
			return EAssignOp.ASSIGN;
		}

		@Override
		public AssignOp clone() {
			AssignOp result = null;

			try {

				// Use reflection to acquire correct Constructor
				Constructor ctor = getClass().getConstructor(Token.class);

				// TODO: copy instead of passing the reference?
				result = (AssignOp) ctor.newInstance(getToken());

			} catch (Exception e) {
				return null;
			}

			return result;
		}
	}

	public static class DivideAssignOp extends AssignOp {

		public DivideAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.DIVIDE_ASSIGN;
		}
	}

	public static class RemainderAssignOp extends AssignOp {

		public RemainderAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.REMAINDER_ASSIGN;
		}
	}

	public static class LeftShiftAssignOp extends AssignOp {

		public LeftShiftAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.LEFT_SHIFT_ASSIGN;
		}
	}

	public static class RightShiftAssignOp extends AssignOp {

		public RightShiftAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.RIGHT_SHIFT_ASSIGN;
		}
	}

	public static class PlusAssignOp extends AssignOp {

		public PlusAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.PLUS_ASSIGN;
		}
	}

	public static class MinusAssignOp extends AssignOp {

		public MinusAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.MINUS_ASSIGN;
		}
	}

	public static class MultAssignOp extends AssignOp {

		public MultAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.MULT_ASSIGN;
		}
	}

	public static class BitwiseXorAssignOp extends AssignOp {

		public BitwiseXorAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.BITWISE_XOR_ASSIGN;
		}
	}

	public static class BitwiseOrAssignOp extends AssignOp {

		public BitwiseOrAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.BITWISE_OR_ASSIGN;
		}
	}

	public static class BitwiseAndAssignOp extends AssignOp {

		public BitwiseAndAssignOp(Token token) {
			super(token);
		}

		@Override
		public EAssignOp getAssignmentOp() {
			return EAssignOp.BITWISE_AND_ASSIGN;
		}
	}

	public static abstract class ASTList<T> extends ASTNode implements
			Iterable<T> {

		public ASTList(int ttype, String nodeName) {
			token = new CommonToken(ttype, nodeName);
		}

		public T get(int i) {
			return (T) getChild(i);
		}

		public int size() {
			return getChildCount();
		}

		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				int pos = 0;

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public T next() {
					return get(pos++);
				}

				@Override
				public boolean hasNext() {
					return pos < size();
				}
			};
		}

		@Override
		public abstract ASTList<T> clone();
	}

}