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

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncBlobLoader {

    private final Handler handler;
    private LoadListener listener;
    private Thread loadingThread = null;
    private INDIBLOBValue queuedValue = null;
    private boolean stretch;

    public AsyncBlobLoader(Handler handler) {
        this.handler = handler;
    }

    public void setListener(LoadListener listener) {
        this.listener = listener;
    }

    @SuppressLint("DefaultLocale")
    public void queue(final INDIBLOBElement element) {
        if (listener == null) throw new NullPointerException("Null listener!");
        queuedValue = element.getValue();
        if ((loadingThread == null) || (!loadingThread.isAlive())) startProcessing();
    }

    public void setStretch(boolean stretch) {
        this.stretch = stretch;
    }

    private void onThreadFinish(Bitmap bitmap, String[] metadata) {
        handler.post(() -> {
            if (listener != null)
                listener.onBitmapLoaded(bitmap, metadata);
        });
        if (queuedValue != null) startProcessing();
    }

    private void startProcessing() {
        loadingThread = new Thread(new LoadingRunnable(queuedValue, stretch));
        loadingThread.start();
        queuedValue = null;
    }

    public interface LoadListener {
        void onBitmapLoaded(Bitmap bitmap, String[] metadata);

        void onBlobException(Throwable e);
    }

    private class LoadingRunnable implements Runnable {

        private final INDIBLOBValue blobValue;
        private final boolean stretch;

        private LoadingRunnable(INDIBLOBValue blobValue, boolean stretch) {
            this.blobValue = blobValue;
            this.stretch = stretch;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            try {
                String format = blobValue.getFormat();
                int blobSize = blobValue.getSize();
                String blobSizeString = String.format("%.2f MB", blobSize / 1000000.0);
                byte[] blobData = blobValue.getBlobData();
                Bitmap bitmap;
                if (format.equals("") || (blobSize == 0)) {
                    throw new FileNotFoundException();
                } else if (format.equals(".fits")) {
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
                                if ((bitPerPix != 8) && (bitPerPix != 16))
                                    throw new UnsupportedOperationException("32 bit FITS are not yet supported.");
                            } else if (card.contains("NAXIS1")) {
                                width = findFITSLineValue(card);
                                if (width <= 0)
                                    throw new IllegalStateException("Invalid FITS image");
                            } else if (card.contains("NAXIS2")) {
                                height = findFITSLineValue(card);
                                if (height <= 0)
                                    throw new IllegalStateException("Invalid FITS image");
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
                        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
                    bitmap = BitmapFactory.decodeByteArray(blobData, 0, blobSize);
                    if (bitmap == null) {
                        onThreadFinish(null, new String[]{blobSizeString, null, format, null});
                    } else {
                        onThreadFinish(bitmap, new String[]{
                                blobSizeString, bitmap.getWidth() + "x" + bitmap.getHeight(), format, null});
                    }
                }
            } catch (Throwable e) {
                handler.post(() -> listener.onBlobException(e));
            }
        }

        private int findFITSLineValue(String in) {
            if (in.contains("=")) in = in.split("=")[1];
            Matcher matcher = Pattern.compile("[0-9]+").matcher(in);
            if (matcher.find())
                return Integer.parseInt(matcher.group());
            return -1;
        }
    }
}