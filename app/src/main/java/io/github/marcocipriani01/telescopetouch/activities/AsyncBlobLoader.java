package io.github.marcocipriani01.telescopetouch.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;

import androidx.core.util.Pair;

import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;

import java.io.EOFException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class AsyncBlobLoader {

    private final Handler handler;
    private LoadListener listener;
    private Thread loadingThread = null;

    public AsyncBlobLoader(Handler handler) {
        this.handler = handler;
    }

    private static int findFITSLineValue(String in) {
        if (in.contains("=")) in = in.split("=")[1];
        Matcher matcher = Pattern.compile("[0-9]+").matcher(in);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return -1;
    }

    public void setListener(LoadListener listener) {
        this.listener = listener;
    }

    @SuppressLint("DefaultLocale")
    public void loadBitmap(final INDIBLOBElement element, final boolean stretch) {
        if (listener == null) throw new NullPointerException();
        if ((loadingThread != null) && loadingThread.isAlive()) loadingThread.interrupt();
        loadingThread = new Thread(new Runnable() {
            private void checkInterrupted() throws InterruptedException {
                if (Thread.interrupted()) throw new InterruptedException();
            }

            @Override
            public void run() {
                try {
                    //INDIBLOBValue blobValue = element.getValue();
                    //String format = blobValue.getFormat();
                    //int blobSize = blobValue.getSize();
                    //String blobSizeString = String.format("%.2f MB", blobSize / 1000000.0);
                    String blobSizeString = "ciao", format = "ciaone";
                    Bitmap bitmap;
                    checkInterrupted();
                    if (/*format.equals(".fits")*/ true) {
                        try (InputStream inputStream = TelescopeTouchApp.getContext().getAssets().open("test2.fits")) {
                            int width = 0, height = 0;
                            byte bitPerPix = 0, axis = 0;
                            byte[] headerBuffer = new byte[80];
                            while (inputStream.read(headerBuffer, 0, 80) != -1) {
                                checkInterrupted();
                                String card = new String(headerBuffer);
                                if (card.contains("BITPIX")) {
                                    bitPerPix = (byte) findFITSLineValue(card);
                                } else if (card.contains("NAXIS1")) {
                                    width = findFITSLineValue(card);
                                } else if (card.contains("NAXIS2")) {
                                    height = findFITSLineValue(card);
                                } else if (card.contains("NAXIS")) {
                                    axis = (byte) findFITSLineValue(card);
                                } else if (card.startsWith("END ")) {
                                    break;
                                }
                            }
                            if (bitPerPix == 32)
                                throw new UnsupportedOperationException("32 bit FITS are not yet supported.");
                            if (axis != 2)
                                throw new UnsupportedOperationException("Color FITS are not yet supported.");
                            if ((width <= 0) || (height <= 0))
                                throw new IllegalStateException("Invalid FITS image");
                            //int bytesPerPix = bitPerPix / 8;
                            //byte[] imgBuffer = new byte[bytesPerPix];
                            //checkInterrupted();
                            /*if (stretch) {
                                int[][] img = new int[width][height];
                                int min = -1, max = -1;
                                rowLoop:
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        read = inputStream.read(imgBuffer, 0, bytesPerPix);
                                        if (read == -1) break rowLoop;
                                        int val;
                                        if (bytesPerPix == 2) {
                                            val = ((imgBuffer[0] * 256) + imgBuffer[1]) / 257;
                                            if (imgBuffer[1] < 0) val += 256;
                                        } else {
                                            val = imgBuffer[0] & 0xFF;
                                        }
                                        img[w][h] = val;
                                        if ((max == -1) || (max < val)) max = val;
                                        if ((min == -1) || (min > val)) min = val;
                                    }
                                    checkInterrupted();
                                }
                                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                double logMin = Math.log10(min), multiplier = 255.0 / (Math.log10(max) - logMin);
                                for (int w = 0; w < width; w++) {
                                    for (int h = 0; h < height; h++) {
                                        int interpolation = (int) ((Math.log10(img[w][h]) - logMin) * multiplier);
                                        bitmap.setPixel(w, h, Color.argb(255, interpolation, interpolation, interpolation));
                                    }
                                    checkInterrupted();
                                }
                            } else {*/
                            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            /*if (bitPerPix == 16) {
                                int bytesPerPix = bitPerPix / 8;
                                byte[] imgBuffer = new byte[bytesPerPix];
                                rowLoop:
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        read = inputStream.read(imgBuffer, 0, bytesPerPix);
                                        if (read == -1) break rowLoop;
                                        int val = ((imgBuffer[0] * 256) + imgBuffer[1]) / 257;
                                        if (imgBuffer[1] < 0) val += 256;
                                        bitmap.setPixel(w, h, Color.argb(255, val, val, val));
                                    }
                                    checkInterrupted();
                                }
                            } else */
                            if (bitPerPix == 8) {
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        int val = inputStream.read();
                                        if (val == -1) throw new EOFException();
                                        bitmap.setPixel(w, h, Color.argb(255, val, val, val));
                                    }
                                    //checkInterrupted();
                                }
                            } else if (bitPerPix == 16) {
                                for (int h = 0; h < height; h++) {
                                    for (int w = 0; w < width; w++) {
                                        byte[] tmp = new byte[2];
                                        int read = inputStream.read(tmp);
                                        if (read != 2) throw new EOFException();
                                        read = ((tmp[0] & 0xFF) << 8) | (tmp[1] & 0xFF);
                                        read /= 5;
                                        bitmap.setPixel(w, h, Color.argb(255, read, read, read));
                                    }
                                    checkInterrupted();
                                }
                            } else {
                                throw new IllegalStateException("Invalid FITS image");
                            }
                            //}
                            AsyncBlobLoader.this.callListener(new Pair<>(bitmap, new String[]{blobSizeString, width + "x" + height, format, String.valueOf(bitPerPix)}));
                        }
                    } /*else {
                        bitmap = BitmapFactory.decodeByteArray(blobValue.getBlobData(), 0, blobSize);
                        checkInterrupted();
                        if (bitmap == null) {
                            AsyncBlobLoader.this.callListener(new Pair<>(null, new String[]{blobSizeString, null, format, null}));
                        } else {
                            AsyncBlobLoader.this.callListener(new Pair<>(bitmap, new String[]{blobSizeString, bitmap.getWidth() + "x" + bitmap.getHeight(), format, null}));
                        }
                    }*/
                } catch (InterruptedException ignored) {

                } catch (Throwable e) {
                    handler.post(() -> listener.onBlobException(e));
                }
            }
        });
        loadingThread.start();
    }

    private void callListener(final Pair<Bitmap, String[]> result) {
        handler.post(() -> listener.onBitmapLoaded(result));
    }

    public interface LoadListener {
        void onBitmapLoaded(Pair<Bitmap, String[]> result);

        void onBlobException(Throwable e);
    }
}