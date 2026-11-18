package demo.javacore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Custom annotation with a parameter: "this text field must not be longer than value()". */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MaxLength {

    /** An element named "value" can be written without its name: {@code @MaxLength(200)}. */
    int value();
}
