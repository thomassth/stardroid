/*
 * Copyright (C) 2020  Marco Cipriani (@marcocipriani01)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package io.github.marcocipriani01.telescopetouch.indi;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;

import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncBLOBLoader implements INDIPropertyListener {

    private final Handler handler;
    private final Set<BLOBListener> listeners = new HashSet<>();
    private volatile Thread loadingThread = null;
    private volatile INDIBLOBValue queuedValue = null;
    private volatile boolean stretch = false;
    private volatile Bitmap lastBitmap = null;
    private volatile INDIBLOBProperty prop = null;
    private INDIBLOBElement element = null;

    public AsyncBLOBLoader(Handler handler) {
        this.handler = handler;
    }

    private static int findFITSLineValue(String in) {
        if (in.contains("=")) in = in.split("=")[1];
        Matcher matcher = Pattern.compile("[0-9]+").matcher(in);
        if (matcher.find())
            return Integer.parseInt(matcher.group());
        return -1;
    }

    public INDIBLOBProperty getProp() {
        return prop;
    }

    public void attach(INDIBLOBProperty prop, INDIBLOBElement element) {
        if (!prop.getElementsAsList().contains(element))
            throw new IllegalArgumentException("Element not associated with property!");
        if ((prop == this.prop) && (element == this.element)) {
            if ((!listeners.isEmpty()) && (lastBitmap == null)) {
                queuedValue = element.getValue();
                if ((loadingThread == null) || (!loadingThread.isAlive())) startProcessing();
            }
            return;
        }
        if (this.prop != null) this.prop.removeINDIPropertyListener(this);
        this.prop = prop;
        this.element = element;
        this.prop.addINDIPropertyListener(this);
        if (!listeners.isEmpty()) {
            queuedValue = element.getValue();
            if ((loadingThread == null) || (!loadingThread.isAlive())) startProcessing();
        }
    }

    public void reload() {
        if (!listeners.isEmpty() && (element != null)) {
            queuedValue = element.getValue();
            if ((loadingThread == null) || (!loadingThread.isAlive())) startProcessing();
        }
    }

    public void detach() {
        if (this.prop != null) this.prop.removeINDIPropertyListener(this);
        this.prop = null;
        this.element = null;
    }

    public boolean hasBitmap() {
        return lastBitmap != null;
    }

    public synchronized Bitmap getLastBitmap() {
        return lastBitmap;
    }

    public void recycle() {
        handler.post(() -> {
            synchronized (listeners) {
                for (BLOBListener listener : listeners) {
                    listener.onBitmapDestroy();
                }
            }
            if (lastBitmap != null) {
                lastBitmap.recycle();
                lastBitmap = null;
            }
        });
    }

    public void addListener(BLOBListener listener) {
        synchronized (listeners) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(BLOBListener listener) {
        synchronized (listeners) {
            this.listeners.remove(listener);
        }
    }

    private void onException(Throwable throwable) {
        handler.post(() -> {
            synchronized (listeners) {
                for (BLOBListener listener : listeners) {
                    listener.onBitmapDestroy();
                    listener.onBLOBException(throwable);
                }
            }
            if (lastBitmap != null) {
                lastBitmap.recycle();
                lastBitmap = null;
            }
        });
    }

    public synchronized void setStretch(boolean stretch) {
        this.stretch = stretch;
    }

    private synchronized void onThreadFinish(Bitmap bitmap, String[] metadata) {
        if (listeners.isEmpty()) {
            if (lastBitmap != null) {
                lastBitmap.recycle();
                lastBitmap = null;
            }
            if (bitmap != null) bitmap.recycle();
            return;
        }
        handler.post(() -> {
            synchronized (listeners) {
                for (BLOBListener listener : listeners) {
                    listener.onBitmapLoaded(bitmap, metadata);
                }
            }
            if (lastBitmap != null) lastBitmap.recycle();
            lastBitmap = bitmap;
        });
        if (queuedValue != null) startProcessing();
    }

    private synchronized void startProcessing() {
        loadingThread = new LoadingThread(queuedValue);
        loadingThread.start();
        queuedValue = null;
        synchronized (listeners) {
            for (BLOBListener listener : listeners) {
                handler.post(listener::onBLOBLoading);
            }
        }
    }

    @Override
    public synchronized void propertyChanged(INDIProperty<?> indiProperty) {
        if (indiProperty == prop) {
            if (listeners.isEmpty()) return;
            queuedValue = element.getValue();
            if ((loadingThread == null) || (!loadingThread.isAlive())) startProcessing();
        }
    }

    public interface BLOBListener {
        void onBLOBLoading();

        void onBitmapLoaded(Bitmap bitmap, String[] metadata);

        void onBitmapDestroy();

        void onBLOBException(Throwable e);
    }

    private class LoadingThread extends Thread {

        private final INDIBLOBValue blobValue;

        private LoadingThread(INDIBLOBValue blobValue) {
            this.blobValue = blobValue;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            try {
                String format = blobValue.getFormat();
                int blobSize = blobValue.getSize();
                if (format.equals("") || (blobSize == 0))
                    throw new FileNotFoundException();
                String blobSizeString = String.format("%.2f MB", blobSize / 1000000.0);
                byte[] blobData = blobValue.getBlobData();
                if (format.equals(".fits") || format.equals(".fit") || format.equals(".fts")) {
                    try (InputStream stream = new ByteArrayInputStream(blobData)) {
                        int width = 0, height = 0;
                        byte bitPerPix = 0;
                        byte[] headerBuffer = new byte[80];
                        int extraByte = -1;
                        headerLoop:
                        while (stream.read(headerBuffer, 0, 80) != -1) {
                            String card = new String(headerBuffer);
                            if (card.contains("BITPIX")) {
                                bitPerPix = (byte) findFITSLineValue(card);
                            } else if (card.contains("NAXIS1")) {
                                width = findFITSLineValue(card);
                            } else if (card.contains("NAXIS2")) {
                                height = findFITSLineValue(card);
                            } else if (card.contains("NAXIS")) {
                                if (findFITSLineValue(card) != 2)
                                    throw new IndexOutOfBoundsException("Color FITS are not yet supported.");
                            } else if (card.startsWith("END ")) {
                                while (true) {
                                    extraByte = stream.read();
                                    if (((char) extraByte) != ' ') break headerLoop;
                                    if (stream.skip(79) != 79) throw new EOFException();
                                }
                            }
                        }
                        if ((bitPerPix == 0) || (width <= 0) || (height <= 0))
                            throw new IllegalStateException("Invalid FITS image");
                        if (bitPerPix == 32)
                            throw new UnsupportedOperationException("32 bit FITS are not yet supported.");
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        if (stretch) {
                            int[][] img = new int[width][height];
                            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                            if (bitPerPix == 8) {
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        int val;
                                        if (extraByte == -1) {
                                            val = stream.read();
                                        } else {
                                            val = extraByte;
                                            extraByte = -1;
                                        }
                                        img[w][h] = val;
                                        if (val > max) max = val;
                                        if (min > val) min = val;
                                    }
                                }
                            } else if (bitPerPix == 16) {
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        int val;
                                        if (extraByte == -1) {
                                            val = (stream.read() << 8) | stream.read();
                                        } else {
                                            val = (extraByte << 8) | stream.read();
                                            extraByte = -1;
                                        }
                                        img[w][h] = val;
                                        if (val > max) max = val;
                                        if (min > val) min = val;
                                    }
                                }
                            }
                            double logMin = Math.log10(min), multiplier = 255.0 / (Math.log10(max) - logMin);
                            for (int w = 0; w < width; w++) {
                                for (int h = 0; h < height; h++) {
                                    int interpolation = (int) ((Math.log10(img[w][h]) - logMin) * multiplier);
                                    bitmap.setPixel(w, h, Color.rgb(interpolation, interpolation, interpolation));
                                }
                            }
                        } else {
                            if (bitPerPix == 8) {
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        int val;
                                        if (extraByte == -1) {
                                            val = stream.read();
                                        } else {
                                            val = extraByte;
                                            extraByte = -1;
                                        }
                                        bitmap.setPixel(w, h, Color.rgb(val, val, val));
                                    }
                                }
                            } else if (bitPerPix == 16) {
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        int val;
                                        if (extraByte == -1) {
                                            val = (stream.read() << 8) | stream.read();
                                        } else {
                                            val = (extraByte << 8) | stream.read();
                                            extraByte = -1;
                                        }
                                        val /= 257;
                                        bitmap.setPixel(w, h, Color.rgb(val, val, val));
                                    }
                                }
                            }
                        }
                        onThreadFinish(bitmap, new String[]{
                                blobSizeString, width + "x" + height, format, String.valueOf(bitPerPix)});
                    }
                } else {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(blobData, 0, blobSize);
                    if (bitmap == null) {
                        onThreadFinish(null, new String[]{blobSizeString, null, format, null});
                    } else {
                        onThreadFinish(bitmap, new String[]{
                                blobSizeString, bitmap.getWidth() + "x" + bitmap.getHeight(), format,
                                (format.equals(".jpg") || format.equals(".jpeg")) ? "8" : null});
                    }
                }
            } catch (Throwable t) {
                onException(t);
            }
        }
    }
}