package io.github.marcocipriani01.telescopetouch.source.impl;

import java.util.ArrayList;

import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.LineSource;
import io.github.marcocipriani01.telescopetouch.source.PointSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

/**
 * Simple class for implementing the AstronomicalSource interface. We may merge
 * the two in the future (but for now, this lets us do some parallel
 * development).
 *
 * @author Brent Bryan
 */
public class AstronomicalSourceImpl {
    private float level;
    private ArrayList<String> names;

    private ArrayList<ImageSource> imageSources;
    private ArrayList<LineSource> lineSources;
    private ArrayList<PointSource> pointSources;
    private ArrayList<TextSource> textSources;

    public ArrayList<String> getNames() {
        return names;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public ArrayList<ImageSource> getImageSources() {
        return imageSources;
    }

    public void setImageSources(ArrayList<ImageSource> imageSources) {
        this.imageSources = imageSources;
    }

    public ArrayList<LineSource> getLineSources() {
        return lineSources;
    }

    public void setLineSources(ArrayList<LineSource> lineSources) {
        this.lineSources = lineSources;
    }

    public ArrayList<PointSource> getPointSources() {
        return pointSources;
    }

    public void setPointSources(ArrayList<PointSource> pointSources) {
        this.pointSources = pointSources;
    }

    public ArrayList<TextSource> getTextSources() {
        return textSources;
    }

    public void setTextSources(ArrayList<TextSource> textSources) {
        this.textSources = textSources;
    }

    public void addPoint(PointSource point) {
        if (point == null) {
            pointSources = new ArrayList<PointSource>();
        }
        pointSources.add(point);
    }

    public void addLabel(TextSource label) {
        if (label == null) {
            textSources = new ArrayList<TextSource>();
        }
        textSources.add(label);
    }

    public void addImage(ImageSource image) {
        if (image == null) {
            imageSources = new ArrayList<ImageSource>();
        }
        imageSources.add(image);
    }

    public void addLine(LineSource line) {
        if (line == null) {
            lineSources = new ArrayList<LineSource>();
        }
        lineSources.add(line);
    }
}
