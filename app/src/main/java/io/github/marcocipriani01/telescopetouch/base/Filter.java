package io.github.marcocipriani01.telescopetouch.base;

/**
 * An interface for determining whether or not an object should be included in a collection.
 *
 * @author Brent Bryan
 */
public interface Filter<E> {

    /**
     * Returns true if the given object should be included in the collection.
     */
    boolean accept(E object);
}
