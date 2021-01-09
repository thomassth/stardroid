package io.github.marcocipriani01.telescopetouch.source;

/**
 * This interface represents the base class of objects which are to be displayed by
 * the UI, such as points, lines and labels.
 *
 * @author Brent Bryan
 */
public interface RendererSource {
    enum SourceType {POINT, LINE, TEXT, IMAGE}

}
