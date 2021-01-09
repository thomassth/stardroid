package io.github.marcocipriani01.telescopetouch.renderer.util;

/**
 * A base {@link UpdateClosure} that implements
 * {@link Comparable#compareTo(Object)} using hash codes so that they can
 * be used in TreeSets.
 *
 * @author Brent Bryan
 * @author John Taylor
 */
public abstract class AbstractUpdateClosure implements UpdateClosure {
    @Override
    public int compareTo(UpdateClosure that) {
        int thisHashCode = this.hashCode();
        int thatHashCode = that.hashCode();

        if (thisHashCode == thatHashCode) {
            return 0;
        }
        return (thisHashCode < thatHashCode) ? -1 : 1;
    }
}
