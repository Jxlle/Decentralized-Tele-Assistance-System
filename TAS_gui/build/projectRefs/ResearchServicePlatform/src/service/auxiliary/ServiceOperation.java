package service.auxiliary;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

/**
 * 
 * Annotation for properties of operations
 */
public @interface ServiceOperation {
	
    public double OperationCost() default 0.0;
}
