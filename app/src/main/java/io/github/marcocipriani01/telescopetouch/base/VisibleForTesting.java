package io.github.marcocipriani01.telescopetouch.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a method is visible only for testing. Methods
 * in classes other than the declaring class should not call methods annotated
 * with this annotation, unless they are also annotated with @Test
 *
 * @author Brent Bryan
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface VisibleForTesting {
}
