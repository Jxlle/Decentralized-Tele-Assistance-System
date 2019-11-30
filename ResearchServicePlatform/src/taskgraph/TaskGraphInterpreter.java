package taskgraph;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import service.auxiliary.ExecutionThread;
import service.auxiliary.TimeOutError;
import service.client.AbstractServiceClient;
import service.composite.CompositeService;
import service.utility.SimClock;
import taskgraph.TaskGraph.ARRAY_ACCESS;
import taskgraph.TaskGraph.BinaryOp;
import taskgraph.TaskGraph.CALL;
import taskgraph.TaskGraph.EAssignOp;
import taskgraph.TaskGraph.ETaskType;
import taskgraph.TaskGraph.Expression;
import taskgraph.TaskGraph.IF;
import taskgraph.TaskGraph.Load;
import taskgraph.TaskGraph.PARALLEL;
import taskgraph.TaskGraph.PARAM;
import taskgraph.TaskGraph.QUALIFIED_ACCESS;
import taskgraph.TaskGraph.Store;
import taskgraph.TaskGraph.TernaryOp;
import taskgraph.TaskGraph.UnaryOp;

public class TaskGraphInterpreter {

    private HashMap<String, Object> heap = new HashMap<String, Object>();

    AbstractServiceClient serviceClient;
    // AbstractServiceClient registryServiceClient = new
    // AbstractServiceClient(ServiceRegistry.ADDRESS);
    String qosRequirement;
    CompositeService compositeService;
    //SDCache sdCache;

    /**
     * Return the whole heap
     * 
     * @return heap map
     */
    public HashMap<String, Object> getHeap() {
	return heap;
    }

    public void setHeap(HashMap<String, Object> heap) {
	this.heap = heap;
    }

    /**
     * Returns the heap object specified by pointer
     * 
     * @param name
     *            heap object template instance identifier
     * @return resulting object
     */
    public Object getHeapObject(String name) {
	return heap.get(name);
    }

    /*
     * Each template object have its own ID, which can be used to set/get data variables For global section 0 will be used
     */
    public Object interpret(TaskGraph first, String qosRequirement, final CompositeService compositeService, final Object... args) {

	if (first == null) {
	    System.err.println("Interpreter received null taskgraph");
	}

	if (qosRequirement == null) {
	    System.err.println("Interpreter received null qosRequirement");
	}

	if (compositeService == null) {
	    System.err.println("Interpreter received null compositeService");
	}

	this.qosRequirement = qosRequirement;
	this.compositeService = compositeService;
	//this.sdCache = cache;
	// Start from global declaration which has id = 0 in taskGraphs

	TaskGraph CT = first;
	int HC = 1;

	List<Object> list = null;
	int paramId = 0;
	while (CT.getTaskType() != ETaskType.RETURN) {
	    // System.out.println(CT);
	    switch (CT.getTaskType()) {
	    case START:
		CT = CT.getNext();
		break;
	    case PARAM:
		PARAM param = (PARAM) CT;
		heap.put(param.getName(), args[paramId++]);
		CT = CT.getNext();
		break;
	    /*
	     * case LIST_START: list = new LinkedList(); CT = CT.getNext(); break; case LIST_ITEM: Expression exp = (Expression) CT.getPrev(); list.add(exp.getValue()); CT =
	     * CT.getNext(); break; case LIST_END: ((LIST_END) CT).setValue(list); list = null; CT = CT.getNext(); break; case DECL:
	     * 
	     * DECL decl = (DECL) CT; Object declType = decl.getType(); Object type = null;
	     * 
	     * // If is reference
	     * 
	     * 
	     * CT = CT.getNext(); break;
	     */
	    case LITERAL:
		CT = CT.getNext();
		break;
	    case IF:
		boolean expResult = getBooleanValue(CT.getPrev());
		IF If = (IF) CT;
		If.setValue(expResult);
		if (expResult) {
		    CT = If.getTrue() != null ? If.getTrue() : If.getNext();
		} else if (!expResult) {
		    CT = If.getFalse() != null ? If.getFalse() : If.getNext();
		}
		break;
	    case TERNARY_OP:
		boolean ternaryResult = getBooleanValue(CT.getPrev());
		TernaryOp ternaryOp = (TernaryOp) CT;
		if (ternaryResult) {
		    ternaryOp.setValue(((Expression) ternaryOp.getTrue()).getValue());
		} else {
		    ternaryOp.setValue(((Expression) ternaryOp.getFalse()).getValue());
		}
		CT = CT.getNext();
		break;
	    case UNARY_OP:

		UnaryOp op = (UnaryOp) CT;
		Object unaryResult = null;

		switch (op.getUnaryOp()) {
		case NOT:
		    unaryResult = !getBooleanValue(CT.getPrev());
		    break;
		case PLUS:
		    unaryResult = +getIntValue(CT.getPrev());
		    break;
		case MINUS:
		    unaryResult = -getIntValue(CT.getPrev());
		    break;
		case POSTFIX_DECREMENT:
		    if ((CT.getPrev()) instanceof Load) {
			String varName = ((Load) CT.getPrev()).getVarName();
			int value = (int) ((Load) CT.getPrev()).getValue();
			unaryResult = value;
			value--;
			heap.put(varName, value);
		    }
		    break;
		case POSTFIX_INCREMENT:
		    if ((CT.getPrev()) instanceof Load) {
			String varName = ((Load) CT.getPrev()).getVarName();
			int value = (int) ((Load) CT.getPrev()).getValue();
			unaryResult = value;
			value++;
			heap.put(varName, value);
		    }
		    break;
		case PREFIX_INCREMENT:
		    if ((CT.getPrev()) instanceof Load) {
			String varName = ((Load) CT.getPrev()).getVarName();
			int value = (int) ((Load) CT.getPrev()).getValue();
			value++;
			unaryResult = value;
			heap.put(varName, value);
		    }
		    break;
		case PREFIX_DECREMENT:
		    if ((CT.getPrev()) instanceof Load) {
			String varName = ((Load) CT.getPrev()).getVarName();
			int value = (int) ((Load) CT.getPrev()).getValue();
			value--;
			unaryResult = value;
			heap.put(varName, value);
		    }
		    break;

		default:
		    break;
		}
		op.setValue(unaryResult);
		CT = CT.getNext();
		break;
	    case BINARY_OP:
		BinaryOp binaryOp = (BinaryOp) CT;
		Object bExpResult = null;

		switch (binaryOp.getBinaryOp()) {
		case LOGIC_OR:
		    bExpResult = getBooleanValue(binaryOp.getLeft()) || getBooleanValue(binaryOp.getRight());
		    break;
		// case OR:
		// expResult = getBooleanValue(op.getLeft()) &&
		// getBooleanValue(op.getRight());
		// break;
		case IMPLY:
		    bExpResult = !getBooleanValue(binaryOp.getLeft()) || getBooleanValue(binaryOp.getRight());
		    break;
		case LOGIC_AND:
		    bExpResult = getBooleanValue(binaryOp.getLeft()) && getBooleanValue(binaryOp.getRight());
		    break;
		// case AND:
		// expResult = getBooleanValue(op.getLeft()) &&
		// getBooleanValue(op.getRight());
		// break;
		case BITWISE_INCL_OR:
		    bExpResult = getIntValue(binaryOp.getLeft()) | getIntValue(binaryOp.getRight());
		    break;
		case BITWISE_EXCL_OR:
		    bExpResult = getBooleanValue(binaryOp.getLeft()) ^ getBooleanValue(binaryOp.getRight());
		    break;
		case BITWISE_AND:
		    bExpResult = getIntValue(binaryOp.getLeft()) & getIntValue(binaryOp.getRight());
		    break;
		case EQUAL:
		    Object leftValue = ((Expression) binaryOp.getLeft()).getValue();
		    Object rightValue = ((Expression) binaryOp.getRight()).getValue();
		    if (leftValue instanceof TimeOutError && rightValue instanceof TimeOutError)
			bExpResult = true;
		    else if (leftValue instanceof TimeOutError || rightValue instanceof TimeOutError)
			bExpResult = false;
		    else
			bExpResult = getIntValue(binaryOp.getLeft()) == (getIntValue(binaryOp.getRight()));
		    break;
		case NOT_EQUAL:
		    bExpResult = getIntValue(binaryOp.getLeft()) != (getIntValue(binaryOp.getRight()));
		    break;
		case LT:
		    bExpResult = getIntValue(binaryOp.getLeft()) < getIntValue(binaryOp.getRight());
		    break;
		case GT:
		    bExpResult = getIntValue(binaryOp.getLeft()) > getIntValue(binaryOp.getRight());
		    break;
		case LT_EQUAL:
		    bExpResult = getIntValue(binaryOp.getLeft()) <= getIntValue(binaryOp.getRight());
		    break;
		case GT_EQUAL:
		    bExpResult = getIntValue(binaryOp.getLeft()) >= getIntValue(binaryOp.getRight());
		    break;
		case MIN:
		    bExpResult = Math.min(getIntValue(binaryOp.getLeft()), getIntValue(binaryOp.getRight()));
		    break;
		case MAX:
		    bExpResult = Math.max(getIntValue(binaryOp.getLeft()), getIntValue(binaryOp.getRight()));
		    break;
		case LEFT_SHIFT:
		    bExpResult = getIntValue(binaryOp.getLeft()) << getIntValue(binaryOp.getRight());
		    break;
		case RIGHT_SHIFT:
		    bExpResult = getIntValue(binaryOp.getLeft()) >> getIntValue(binaryOp.getRight());
		    break;
		case PLUS:
		    bExpResult = getIntValue(binaryOp.getLeft()) + getIntValue(binaryOp.getRight());
		    break;
		case MINUS:
		    bExpResult = getIntValue(binaryOp.getLeft()) - getIntValue(binaryOp.getRight());
		    break;
		case MULT:
		    bExpResult = getIntValue(binaryOp.getLeft()) * getIntValue(binaryOp.getRight());
		    break;
		case DIVISION:
		    bExpResult = getIntValue(binaryOp.getLeft()) / getIntValue(binaryOp.getRight());
		    break;
		case REMAINDER:
		    bExpResult = getIntValue(binaryOp.getLeft()) % getIntValue(binaryOp.getRight());
		    break;

		default:
		    break;
		}

		binaryOp.setValue(bExpResult);
		CT = CT.getNext();

		break;
	    case STORE:

		Store store = (Store) CT;
		String varName = store.getVarName();
		Object rightValue = ((Expression) store.getRightExpression()).getValue();
		Object result = null;
		if (varName != null) {
		    Object value = ((Expression) store.getPrev()).getValue();

		    Object leftValue = heap.get(varName);
		    result = handleAssignOperator(store.getAssignOp(), leftValue, rightValue);
		    heap.put(store.getVarName(), result);
		} else if (store.getPrev() instanceof ARRAY_ACCESS) {
		    ARRAY_ACCESS arrAccess = (ARRAY_ACCESS) store.getPrev();
		    int index = getIntValue(arrAccess.getIndex());
		    Object prevObject = ((Expression) arrAccess.getPrev()).getValue();
		    result = handleAssignOperator(store.getAssignOp(), arrAccess.getValue(), rightValue);
		    Array.set(prevObject, index, result);
		} else if (store.getPrev() instanceof QUALIFIED_ACCESS) {
		    QUALIFIED_ACCESS qualifiedAccess = (QUALIFIED_ACCESS) store.getPrev();
		    String memberId = qualifiedAccess.getVarName();
		    Object prevObject = ((Expression) qualifiedAccess.getPrev()).getValue();
		    result = handleAssignOperator(store.getAssignOp(), qualifiedAccess.getValue(), rightValue);
		    try {
			prevObject.getClass().getField(memberId).set(prevObject, result);
		    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		    }
		}
		store.setValue(result);

		CT = CT.getNext();
		break;
	    case LOAD:
		Load load = (Load) CT;
		Object valueLoaded = null;
		if (!heap.containsKey(load.getVarName())) {
		    if (load.getVarName().equalsIgnoreCase("TimeoutError")) {
			valueLoaded = new TimeOutError();
		    } else {
			throw new RuntimeException("Variable " + load.getVarName() + " not declared.");
		    }
		} else {
		    valueLoaded = heap.get(load.getVarName());
		}
		((Expression) CT).setValue(valueLoaded);
		CT = CT.getNext();
		break;
	    case ARRAY_ACCESS:
		ARRAY_ACCESS arrayAccess = (ARRAY_ACCESS) CT;
		int index = getIntValue(arrayAccess.getIndex());
		Object prevObject = ((Expression) CT.getPrev()).getValue();
		Object object2 = null;
		if (prevObject.getClass().isArray()) {
		    object2 = Array.get(prevObject, index);
		}
		// SAVE in ARRAY_ACCESS
		if (object2 == null) {
		    // throw new
		    // RuntimeException("Index out of range exception! Variable:"
		    // + object).getName() + " index:" + index);
		}
		arrayAccess.setValue(object2);
		// setTemporary(temporary, object2);
		CT = CT.getNext();
		break;
	    case QUALIFIED_ACCESS:
		QUALIFIED_ACCESS qualifiedAccess = (QUALIFIED_ACCESS) CT;
		Object object = ((Expression) CT.getPrev()).getValue();
		Object memberValue = null;
		if (object.getClass().isArray() && qualifiedAccess.getVarName().equals("length")) {
		    memberValue = Array.getLength(object);
		} else {
		    try {
			memberValue = object.getClass().getDeclaredField(qualifiedAccess.getVarName()).get(object);
		    } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		    }
		}

		// SAVE in QUALIFIED_ACCESS
		qualifiedAccess.setValue(memberValue);
		CT = CT.getNext();
		break;
	    case CALL:
		// TODO: CODE to INVOKE
		CALL call = (CALL) CT;
		// 1. Create list of parameters
		// 2. call method of invoke
		Object[] params = new Object[call.getArguments().size()];
		for (int k = 0; k < params.length; k++)
		    params[k] = call.getArguments().get(k).getValue();

		Object resultInvoke;
		if (call.getServiceName().equalsIgnoreCase("this")) {
		    resultInvoke = compositeService.invokeLocalOperation(call.getOperationName(), params);
		} else {
		    resultInvoke = compositeService.invokeServiceOperation(qosRequirement, call.getServiceName(), call.getOperationName(), params);
		}

		if (!compositeService.getConfiguration().ignoreTimeoutError && resultInvoke instanceof TimeOutError)
		    return resultInvoke;
		call.setValue(resultInvoke);
		CT = CT.getNext();
		break;

	    case PARALLEL:
		PARALLEL parallel = (PARALLEL) CT;
		List<TaskGraph> tasks = parallel.getStatements();
		//ExecutorService threadpool = Executors.newCachedThreadPool();
		FutureTask[] futureList = new FutureTask[tasks.size()];
		ExecutionThread[] threads=new ExecutionThread[tasks.size()];

		SimClock.beginParallel();
		
		for (int i = 0; i < tasks.size(); i++) {
		    final TaskGraph task = tasks.get(i);

		    Callable<Double> callable=new Callable<Double>(){
	            public Double call() throws Exception {  
				    TaskGraphInterpreter interpreter = new TaskGraphInterpreter();
				    interpreter.heap = heap;
				    interpreter.compositeService = compositeService;
				    interpreter.interpret(task, qosRequirement, compositeService, args);
				    return ((ExecutionThread)Thread.currentThread()).getCurrentTime();
				}  	
		    };
		    
	        FutureTask<Double> future = new FutureTask<Double>(callable);
	        futureList[i]=future;	        
	        new ExecutionThread("parallel"+i,future).start();		    
		}

		//Double maxTime=0.0;
		for (Future future : futureList) {
		    try {
		    	future.get();
		    	//System.out.println("Parallel time cost:"+future.get());
		    	//Double time=(Double)future.get();
		    	//System.out.println("Parallel time cost:"+time);
		    	//if(maxTime<time)
		    		//maxTime=time;
		    } catch (InterruptedException | ExecutionException e) {
		    	e.printStackTrace();
		    }
		}
		
		
		SimClock.endParallel();
		
		//threadpool.shutdown();

		CT = CT.getNext();
		break;
	    default:
		throw new RuntimeException("Task type is not handled:" + CT.getTaskType().toString());
	    }

	    if (CT == null) {
		System.out.println("Null CT");
	    }
	}

	TaskGraph last = CT.getPrev();
	if (last != null && last instanceof Expression) {
	    return ((Expression) last).getValue();
	}

	return null;

    }

    private Object handleAssignOperator(EAssignOp op, Object leftValue, Object rightValue) {
	Object result;
	switch (op) {
	case MULT_ASSIGN:
	    result = getIntValue(leftValue) * getIntValue(rightValue);
	    break;
	case DIVIDE_ASSIGN:
	    result = getIntValue(leftValue) / getIntValue(rightValue);
	    break;
	case REMAINDER_ASSIGN:
	    result = getIntValue(leftValue) % getIntValue(rightValue);
	    break;
	case PLUS_ASSIGN:
	    result = getIntValue(leftValue) + getIntValue(rightValue);
	    break;
	case MINUS_ASSIGN:
	    result = getIntValue(leftValue) - getIntValue(rightValue);
	    break;
	case LEFT_SHIFT_ASSIGN:
	    result = getIntValue(leftValue) << getIntValue(rightValue);
	    break;
	case RIGHT_SHIFT_ASSIGN:
	    result = getIntValue(leftValue) >> getIntValue(rightValue);
	    break;
	case BITWISE_AND_ASSIGN:
	    result = getIntValue(leftValue) & getIntValue(rightValue);
	    break;
	case BITWISE_XOR_ASSIGN:
	    result = getIntValue(leftValue) ^ getIntValue(rightValue);
	    break;
	case BITWISE_OR_ASSIGN:
	    result = getIntValue(leftValue) | getIntValue(rightValue);
	    break;
	case ASSIGN:
	    result = rightValue;
	    break;
	default:
	    throw new RuntimeException("Assignment operator is not handled:" + op.toString());
	}
	return result;
    }

    // private void setTemporary(boolean temporary, Object valueLoaded) {
    // // not needed for local variables to put in temporaryVars
    // if (valueLoaded instanceof UppaalType) {
    // UppaalType uppaalType = (UppaalType) valueLoaded;
    // if (temporary) {
    // if (!uppaalType.isTemporary())
    // uppaalType.setTemporary(temporary);
    // if (!temporaryVars.contains(uppaalType))
    // temporaryVars.add(uppaalType);
    // }
    // }
    // }

    public static int getIntValue(TaskGraph task) {
	Expression exp = (Expression) task;
	Object value = exp.getValue();
	// if value not initialized
	if (value == null) {
	    return 0;
	}

	if (value instanceof Integer) {
	    return (Integer) value;
	}
	if (value instanceof Boolean) {
	    return ((Boolean) value) == true ? 1 : 0;
	}
	return Integer.parseInt((String) value);
    }

    public static int getIntValue(Object value) {

	// if value not initialized
	if (value == null) {
	    return 0;
	}

	if (value instanceof Integer) {
	    return (Integer) value;
	}
	if (value instanceof Boolean) {
	    return ((Boolean) value) == true ? 1 : 0;
	}
	return Integer.parseInt((String) value);
    }

    public static boolean getBooleanValue(TaskGraph task) {
	Expression exp = (Expression) task;
	Object value = exp.getValue();

	// if value not initialized
	// if (value == null) {
	// return false;
	// }

	int result = 0;

	if (value instanceof Integer) {
	    result = (int) exp.getValue();
	}
	if (value instanceof Boolean) {
	    return (boolean) exp.getValue();
	}
	return result == 0 ? false : true;
    }

    /*
     * Search through service registry to get the list of service descriptions
     */
/*    public List<ServiceDescription> lookupService(String serviceType, String opName) {

	List<ServiceDescription> list = compositeService.lookupService(serviceType, opName);
	if (list == null || list.size() == 0) {
	    compositeService.getWorkflowProbe().serviceNotFound(serviceType, opName);
	    list = compositeService.lookupService(serviceType, opName);
	}

	return list;
    }

    protected ServiceDescription applyQoSRequirement(AbstractQoSRequirement qosRequirement, List<ServiceDescription> serviceDescriptions, String opName, Object... params) {
	if (qosRequirement == null) {
	    System.err.println("QoS requirement is null. To select among multiple services, a QoS requirement must have been provided.");
	    System.err.println("Selecting a service randomly...");
	    return serviceDescriptions.get(new Random().nextInt(serviceDescriptions.size()));
	}
	return qosRequirement.applyQoSRequirement(serviceDescriptions, opName, params);
    }

    public Object invokeServiceOperation(String serviceName, String operationName, Object[] params) {

	int timeout = compositeService.getConfiguration().timeout;
	Object resultVal;
	int retryAttempts = 0;
	do {
	    List<ServiceDescription> services = lookupService(serviceName, operationName);
	    if (services == null || services.size() == 0) {
		System.out.println("ServiceName: " + serviceName + "." + operationName + "not found!");
		return new TimeOutError();
	    }

	    // Apply strategy
	    ServiceDescription service = applyQoSRequirement(qosRequirement, services, operationName, params);

	    System.out.println("Operation " + service.getServiceType() + "." + operationName + " has been selected with following custom properties:"
		    + service.getCustomProperties());

	    compositeService.getWorkflowProbe().serviceOperationInvoked(service, operationName, params);

	    int maxResponseTime = timeout != 0 ? timeout : service.getResponseTime() * 3;
	    resultVal = compositeService.sendRequest(service.getServiceType(), service.getServiceEndpoint(), true, maxResponseTime, operationName, params);

	    if (resultVal instanceof TimeOutError) {
		compositeService.getWorkflowProbe().serviceOperationTimeout(service, operationName, params);
	    } else {
		compositeService.getWorkflowProbe().serviceOperationReturned(service, resultVal, operationName, params);
		compositeService.getCostProbe().costOperation(service, operationName);
	    }

	    retryAttempts++;
	} while (resultVal instanceof TimeOutError && retryAttempts < compositeService.getConfiguration().maxRetryAttempts);

	return resultVal;
    }

    public Object invokeLocalOperation(String operationName, Object[] params) {
	for (Method operation : compositeService.getClass().getMethods()) {
	    if (operation.getAnnotation(LocalOperation.class) != null) {
		if (operation.getName().equals(operationName)) {
		    try {
			return operation.invoke(compositeService, params);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	throw new RuntimeException("Local operation " + operationName + " is not found.");
    }
    */
}