package io.github.marcocipriani01.telescopetouch.base;

/**
 * This interface defines a function which transforms one object into another.
 *
 * @author Brent Bryan
 */
public interface Transform<E, F> {

    F transform(E e);
}
