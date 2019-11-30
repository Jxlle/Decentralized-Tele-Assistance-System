package service.atomic;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

/**
 * Annotation for setting attributes of service profile
 * @author Yifan Ruan
 * @email  ry222ad@student.lnu.se
 */
public @interface ServiceProfileAttribute {
}
