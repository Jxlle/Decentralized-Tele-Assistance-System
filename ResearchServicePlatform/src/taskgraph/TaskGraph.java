package taskgraph;

import java.util.LinkedList;

public abstract class TaskGraph {
	
	/**
	 * Override the default "toString" method
	 */
    @Override
    public String toString() {
    	return this.getClass().getSimpleName();
    }

    /**
     * 
     * @param stopGraph the stop graph
     */
    public void printHierarchy(TaskGraph stopGraph) {
		if (next == null || next == stopGraph) {
			return;
		}
		if (this instanceof IF) {
			IF ifTask = (IF) this;
			System.out.print(" --TrueSide-- ");
			ifTask.True.printHierarchy(ifTask.next);
			if (ifTask.False != ifTask.next) {
				System.out.print(" --FalseSide-- ");
				ifTask.True.printHierarchy(ifTask.next);
			}
			System.out.print(" --Next-- ");
		}
		System.out.print(this + "-->");
		next.printHierarchy(stopGraph);
    }

    protected TaskGraph next;
    private TaskGraph prev;
    protected ETaskType taskType;

    public void setNext(TaskGraph next) {
    	this.next = next;
    }

    public TaskGraph getNext() {
    	return next;
    }

    public void setPrev(TaskGraph prev) {
    	this.prev = prev;
    }

    public TaskGraph getPrev() {
    	return prev;
    }

    public ETaskType getTaskType() {
    	return taskType;
    }

    @Override
    public abstract TaskGraph clone();

    // XXX: does not work as expected
    // for operators or similar situations
    // (with circular references)
    protected void createClone(TaskGraph task) {
	if (next != null) {
	    task.next = next.clone();

	    if (next.prev != null)
		next.prev = task;
	}
	// task.taskType = taskType;
    }

    /*
     * Return the last element in list
     */
    public TaskGraph getLast() {
	if (this.next == null)
	    return this;

	return next.getLast();
    }

    public static class End extends TaskGraph {
	public End() {
	    this.taskType = ETaskType.END;
	}

	@Override
	public TaskGraph clone() {
	    return new End();
	}

    }

    public static class PARALLEL extends TaskGraph {
    	LinkedList<TaskGraph> statements = new LinkedList<TaskGraph>();
    	
    	public LinkedList<TaskGraph> getStatements(){
    		return statements;
    	}
    	
	public PARALLEL() {
	    this.taskType = ETaskType.PARALLEL;
	}

	@Override
	public TaskGraph clone() {
	    return new PARALLEL();
	}

    }

    public static class ALLOC extends Expression {
	private String name;

	/**
	 * Name of a template instance (from TemplateDef), if any
	 */
	private String instanceName;

	public ALLOC() {
	    this.taskType = ETaskType.ALLOC;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	public void setInstanceName(String instanceName) {
	    this.instanceName = instanceName;
	}

	public String getInstanceName() {
	    return instanceName;
	}

	@Override
	public TaskGraph clone() {
	    ALLOC alloc = new ALLOC();
	    alloc.name = name;
	    alloc.instanceName = instanceName;
	    createClone(alloc);
	    return alloc;
	}
    }

    /**
     * Special task indicating that local variables instantiation subgraph must be run for the latest template object created
     * 
     * @author Kostiantyn Kucher
     */
    public static class INSTANTIATE extends TaskGraph {

	/**
	 * The last task of original target subgraph
	 */
	private TaskGraph targetEnd;

	public INSTANTIATE() {
	    this.taskType = ETaskType.INSTANTIATE;
	}

	@Override
	public TaskGraph clone() {
	    INSTANTIATE instTask = new INSTANTIATE();
	    createClone(instTask);
	    return instTask;
	}

	public TaskGraph getTargetEnd() {
	    return targetEnd;
	}

	public void setTargetEnd(TaskGraph targetEnd) {
	    this.targetEnd = targetEnd;
	}

    }

    public static class RETURN extends Expression {
	public RETURN() {
	    this.taskType = ETaskType.RETURN;
	}

	@Override
	public TaskGraph clone() {
	    RETURN ret = new RETURN();
	    createClone(ret);
	    return ret;
	}
    }

    public static class PUSH extends TaskGraph {
	private String name;

	public PUSH() {
	    this.taskType = ETaskType.PUSH;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	@Override
	public TaskGraph clone() {
	    PUSH task = new PUSH();

	    if (name != null)
		task.name = name.trim();

	    createClone(task);
	    return task;
	}

    }

    public static class CALL extends Expression {
	private String serviceName;
	private String operationName;
	LinkedList<Expression> arguments = new LinkedList<TaskGraph.Expression>();
	public CALL(String serviceName, String operationName) {
	    this.taskType = ETaskType.CALL;
	    this.serviceName = serviceName;
	    this.operationName = operationName;
	}

	public String getOperationName() {
	    return operationName;
	}
	
	public String getServiceName() {
	    return serviceName;
	}
	
	public LinkedList<Expression> getArguments() {
	    return arguments;
	}
	
	public void addArgument(Expression argument) {
	    this.arguments.add(argument);
	}


	@Override
	public TaskGraph clone() {
	    CALL task = new CALL(serviceName, operationName);
	    createClone(task);
	    return task;
	}
    }

    public abstract static class MemoryOperation extends Expression {
	protected boolean isLocal;
	protected String varName;

	public void setLocal(boolean isLocal) {
	    this.isLocal = isLocal;
	}

	public boolean isLocal() {
	    return isLocal;
	}

	public void setVarName(String varName) {
	    this.varName = varName;
	}

	public String getVarName() {
	    return varName;
	}
    }

    public static class RANGE extends Expression {
	private TaskGraph min;
	private TaskGraph max;

	public RANGE() {
	    this.taskType = ETaskType.RANGE;
	}

	public TaskGraph getMin() {
	    return min;
	}

	public void setMin(TaskGraph min) {
	    this.min = min;
	}

	public TaskGraph getMax() {
	    return max;
	}

	public void setMax(TaskGraph max) {
	    this.max = max;
	}

	@Override
	public String toString() {
	    return "RANGE[" + min + "," + max + "]";
	}

	@Override
	public TaskGraph clone() {
	    // XXX: does not really work, leads
	    // to recursion and StackOverflowErrors
	    RANGE task = new RANGE();
	    task.min = min.clone();
	    task.max = max.clone();
	    createClone(task);
	    return task;
	}

    }

    public static class RATE extends Expression {
	private TaskGraph r;
	private TaskGraph q;

	public RATE() {
	    this.taskType = ETaskType.RATE;
	}

	public TaskGraph getR() {
	    return r;
	}

	public void setR(TaskGraph r) {
	    this.r = r;
	}

	public TaskGraph getQ() {
	    return q;
	}

	public void setQ(TaskGraph q) {
	    this.q = q;
	}

	@Override
	public String toString() {
	    if (q != null)
		return "RATE(" + r + "/" + q + ")";
	    else
		return "RATE(" + r + ")";
	}

	@Override
	public TaskGraph clone() {
	    // XXX: may not really work
	    RATE task = new RATE();
	    task.r = r.clone();
	    task.q = q.clone();
	    createClone(task);
	    return task;
	}

    }

    public static class DECL extends MemoryOperation {

	private Object type;
	private boolean isArray = false;
	private boolean isReference = false;

	public DECL() {
	    this.taskType = ETaskType.DECL;
	}

	public Object getType() {
	    return type;
	}

	public void setType(Object type) {
	    this.type = type;
	}

	public boolean isArray() {
	    return isArray;
	}

	public void setArray(boolean array) {
	    this.isArray = array;
	}

	public boolean isReference() {
	    return isReference;
	}

	public void setReference(boolean isReference) {
	    this.isReference = isReference;
	}

	@Override
	public TaskGraph clone() {
	    DECL task = new DECL();
	    task.isLocal = isLocal;
	    task.varName = varName.trim();
	    task.type = type;
	    task.isArray = isArray;
	    task.isReference = isReference;

	    createClone(task);
	    return task;
	}

	@Override
	public String toString() {
	    return "Declare [VarName=" + getVarName() + "]";
	}
    }

    public enum EAssignOp {
	ASSIGN,
	MULT_ASSIGN,
	DIVIDE_ASSIGN,
	REMAINDER_ASSIGN,
	PLUS_ASSIGN,
	MINUS_ASSIGN,
	LEFT_SHIFT_ASSIGN,
	RIGHT_SHIFT_ASSIGN,
	BITWISE_AND_ASSIGN,
	BITWISE_XOR_ASSIGN,
	BITWISE_OR_ASSIGN
    };

    public static class Store extends MemoryOperation {

	private EAssignOp assignOp;
	private TaskGraph rightExpression;
	

	public TaskGraph getRightExpression() {
	    return rightExpression;
	}
	public void setRightExpression(TaskGraph rightExpression) {
	    this.rightExpression = rightExpression;
	}
	
	
	public Store() {
	    this.taskType = ETaskType.STORE;
	}

	@Override
	public String toString() {
	    return "Store [VarName=" + getVarName() + "]";
	}

	public EAssignOp getAssignOp() {
	    return assignOp;
	}

	public void setAssignOp(EAssignOp assignOp) {
	    this.assignOp = assignOp;
	}

	@Override
	public TaskGraph clone() {
	    Store task = new Store();
	    task.isLocal = isLocal;
	    task.varName = varName.trim();
	    task.assignOp = assignOp;
	    createClone(task);
	    return task;
	}

    }

    public static class Load extends MemoryOperation {

	public Load() {
	    this.taskType = ETaskType.LOAD;
	}

	@Override
	public String toString() {
	    return "Load [VarName=" + getVarName() + "] [value=" + value + "]";
	}

	@Override
	public TaskGraph clone() {
	    Load task = new Load();
	    task.isLocal = isLocal;
	    task.varName = varName.trim();
	    createClone(task);
	    return task;
	}
    }

    public static abstract class Expression extends TaskGraph {

	// protected HashMap<Integer, Object> hashMap = new HashMap<Integer,
	// Object>();
	Object value;

	public void setValue(Object value) {
	    this.value = value;
	}

	public Object getValue() {
	    return value;
	}

    }

    public static class Literal extends Expression {

	public Literal() {
	    this.taskType = taskType.LITERAL;
	}

	@Override
	public Object getValue() {
	    return value;
	}

	@Override
	public TaskGraph clone() {
	    Literal task = new Literal();
	    task.value = value;
	    createClone(task);
	    return task;
	}
    }

    public static class BooleanLiteral extends Literal {

	@Override
	public String toString() {
	    return "BooleanLiteral [value=" + getValue() + "]";
	}

    }

    public static class IntLiteral extends Literal {

	@Override
	public String toString() {
	    return "IntLiteral [value=" + getValue() + "]";
	}

    }

    public static class IF extends Expression {
	protected TaskGraph True;
	protected TaskGraph False;
	protected TaskGraph First;

	public IF() {
	    this.taskType = ETaskType.IF;
	}

	public void setTrue(TaskGraph _true) {
	    True = _true;
	}

	public TaskGraph getTrue() {
	    return True;
	}

	public void setFalse(TaskGraph _false) {
	    False = _false;
	}

	public TaskGraph getFalse() {
	    return False;
	}

	// public void setFirst(TaskGraph first) {
	// First = first;
	// }

	public TaskGraph getFirst() {
	    return First;
	}

	@Override
	public TaskGraph clone() {
	    IF task = new IF();
	    task.True = True.clone();
	    task.False = False.clone();
	    task.First = First.clone();
	    createClone(task);
	    return task;
	}
    }

    public static class TernaryOp extends IF {
	public TernaryOp() {
	    this.taskType = ETaskType.TERNARY_OP;
	}

	@Override
	public TaskGraph clone() {
	    TernaryOp task = new TernaryOp();
	    task.True = True.clone();
	    task.False = False.clone();
	    task.First = First.clone();
	    createClone(task);
	    return task;
	}
    }

    // TODO: Prime equal operator
    public enum EBinaryOp {
	LOGIC_OR,
	IMPLY,
	LOGIC_AND,
	BITWISE_INCL_OR,
	BITWISE_EXCL_OR,
	BITWISE_AND,
	EQUAL,
	NOT_EQUAL,
	LT,
	GT,
	LT_EQUAL,
	GT_EQUAL,
	MIN,
	MAX,
	LEFT_SHIFT,
	RIGHT_SHIFT,
	PLUS,
	MINUS,
	MULT,
	DIVISION,
	REMAINDER
    }

    public static class BinaryOp extends Expression {
	private TaskGraph left;
	private TaskGraph right;
	private EBinaryOp binaryOp;

	public BinaryOp() {
	    this.taskType = ETaskType.BINARY_OP;
	}

	public void setLeft(TaskGraph left) {
	    this.left = left;
	}

	public TaskGraph getLeft() {
	    return left;
	}

	public void setRight(TaskGraph right) {
	    this.right = right;
	}

	public TaskGraph getRight() {
	    return right;
	}

	public EBinaryOp getBinaryOp() {
	    return binaryOp;
	}

	public void setBinaryOp(EBinaryOp binaryOp) {
	    this.binaryOp = binaryOp;
	}

	@Override
	public TaskGraph clone() {
	    BinaryOp task = new BinaryOp();
	    task.left = left.clone();
	    task.right = right.clone();
	    task.binaryOp = binaryOp;
	    task.value = value;
	    createClone(task);
	    return task;
	}

	@Override
	public String toString() {
	    return "BinaryOp " + binaryOp;
	}
    }

    // public static class AND extends BinaryOp {
    //
    // }
    //
    // public static class LT extends BinaryOp {
    //
    // }
    //
    // public static class PLUS extends BinaryOp {
    //
    // }
    //
    // public static class MINUS extends BinaryOp {
    //
    // }
    //
    // public static class MULT extends BinaryOp {
    //
    // }

    public enum EUnaryOp {
	PLUS, MINUS, NOT, PREFIX_INCREMENT, PREFIX_DECREMENT, POSTFIX_INCREMENT, POSTFIX_DECREMENT
    }

    public static class UnaryOp extends Expression {
	private EUnaryOp unaryOp;

	public UnaryOp() {
	    this.taskType = ETaskType.UNARY_OP;
	}

	public EUnaryOp getUnaryOp() {
	    return unaryOp;
	}

	public void setUnaryOp(EUnaryOp unaryOp) {
	    this.unaryOp = unaryOp;
	}

	@Override
	public TaskGraph clone() {
	    UnaryOp task = new UnaryOp();
	    task.unaryOp = unaryOp;
	    task.value = value;
	    createClone(task);
	    return task;
	}

    }

    // public static class NOT extends Expression {
    //
    // }

    public static class LIST_START extends TaskGraph {

	public LIST_START() {
	    this.taskType = ETaskType.LIST_START;
	}

	@Override
	public TaskGraph clone() {
	    LIST_START task = new LIST_START();
	    createClone(task);
	    return task;
	}

    }

    public static class LIST_ITEM extends TaskGraph {

	public LIST_ITEM() {
	    this.taskType = ETaskType.LIST_ITEM;
	}

	@Override
	public TaskGraph clone() {
	    LIST_ITEM task = new LIST_ITEM();
	    createClone(task);
	    return task;
	}

    }
    
    public static class START extends TaskGraph {

	public START() {
	    this.taskType = ETaskType.START;
	}

	@Override
	public TaskGraph clone() {
	    START task = new START();
	    createClone(task);
	    return task;
	}

    }

    public static class PARAM extends TaskGraph {

	String name;

	public PARAM(String name) {
	    this.taskType = ETaskType.PARAM;
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	@Override
	public TaskGraph clone() {
	    PARAM task = new PARAM(name);
	    createClone(task);
	    return task;
	}

    }

    public static class LIST_END extends Expression {

	public LIST_END() {
	    this.taskType = ETaskType.LIST_END;
	}

	@Override
	public TaskGraph clone() {
	    LIST_END task = new LIST_END();
	    task.value = value;
	    createClone(task);
	    return task;
	}

    }

    public static class ARRAY_ACCESS extends Load {

	private TaskGraph index;

	public ARRAY_ACCESS() {
	    this.taskType = ETaskType.ARRAY_ACCESS;
	}

	@Override
	public TaskGraph clone() {
	    ARRAY_ACCESS task = new ARRAY_ACCESS();
	    task.value = value;
	    task.index = index.clone();
	    createClone(task);
	    return task;
	}

	public TaskGraph getIndex() {
	    return index;
	}

	public void setIndex(TaskGraph index) {
	    this.index = index;
	}

	@Override
	public String toString() {
	    return "ArrayAccess [Index=" + getIndex() + "]";
	}

    }

    public static class QUALIFIED_ACCESS extends Load {

	public QUALIFIED_ACCESS() {
	    this.taskType = ETaskType.QUALIFIED_ACCESS;
	}

	@Override
	public TaskGraph clone() {
	    QUALIFIED_ACCESS task = new QUALIFIED_ACCESS();
	    task.value = value;
	    createClone(task);
	    return task;
	}

	@Override
	public String toString() {
	    return "QualifiedAccess [VarName=" + getVarName() + "]";
	}

    }

    public enum ETaskType {
	LIST_START,
	LIST_ITEM,
	LIST_END,
	DECL,
	LITERAL,
	IF,
	UNARY_OP,
	BINARY_OP,
	STORE,
	LOAD,
	ARRAY_ACCESS,
	TERNARY_OP,
	QUALIFIED_ACCESS,
	ALLOC,
	INSTANTIATE,
	RETURN,
	PUSH,
	CALL,
	END,
	SYNC,
	RATE,
	STRUCT_START,
	STRUCT_END,
	RANGE,
	PARALLEL,
	PARAM,
	START,
	PARALLEL_END;
	
    }

}