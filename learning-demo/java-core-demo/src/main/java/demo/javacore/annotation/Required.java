package demo.javacore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation: "this field must not be null or blank".
 *
 * <ul>
 *   <li>@Retention(RUNTIME): kept in the .class file AND readable with reflection at runtime.
 *       With SOURCE (like @Override) the compiler would drop it.</li>
 *   <li>@Target(FIELD): allowed only on fields. Record components become fields, so it works there too.</li>
 * </ul>
 * An annotation does nothing by itself; {@link SimpleValidator} reads it and applies the rule.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Required {

    String message() default "must not be blank";
}
