package io.github.marcocipriani01.telescopetouch.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.marcocipriani01.telescopetouch.R;

public class BLOBViewerFragment extends ActionFragment {

    private static int findFITSLineValue(String in) {
        if (in.contains("=")) in = in.split("=")[1];
        Matcher matcher = Pattern.compile("[0-9]+").matcher(in);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return -1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blob_viewer, container, false);
        return rootView;
    }

    private Bitmap loadBlob(INDIBLOBElement element, boolean stretch) throws IOException {
        INDIBLOBValue blobValue = ((INDIBLOBElement) element).getValue();
        String format = blobValue.getFormat();
        if (format.equals(".fits")) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(blobValue.getBlobData())) {
                int read, axis = 0, bitPerPix = 0, width = 0, height = 0;
                byte[] headerBuffer = new byte[80];
                while (inputStream.read(headerBuffer, 0, 80) != -1) {
                    String card = new String(headerBuffer);
                    if (card.contains("BITPIX")) {
                        bitPerPix = findFITSLineValue(card);
                    } else if (card.contains("NAXIS1")) {
                        width = findFITSLineValue(card);
                    } else if (card.contains("NAXIS2")) {
                        height = findFITSLineValue(card);
                    } else if (card.contains("NAXIS")) {
                        axis = findFITSLineValue(card);
                    } else if (card.startsWith("END ")) {
                        break;
                    }
                }
                if ((axis != 2) || (bitPerPix == 32)) throw new UnsupportedOperationException();
                if ((width <= 0) || (height <= 0) || ((bitPerPix != 8) && (bitPerPix != 16)))
                    throw new IllegalStateException();
                int bytesPerPix = bitPerPix / 8;
                byte[] imgBuffer = new byte[bytesPerPix];
                Bitmap bitmap;
                if (stretch) {
                    int[][] img = new int[width][height];
                    int min = -1, max = -1;
                    rowLoop:
                    for (int h = 0; h < height; h++) {
                        for (int w = 0; w < width; w++) {
                            read = inputStream.read(imgBuffer, 0, bytesPerPix);
                            if (read == -1) break rowLoop;
                            int val;
                            if (bytesPerPix == 2) {
                                val = ((imgBuffer[0] * 256) + imgBuffer[1]);
                                if (imgBuffer[1] < 0) val += 256;
                            } else {
                                val = imgBuffer[0] & 0xFF;
                            }
                            img[w][h] = val;
                            if ((max == -1) || (max < val)) max = val;
                            if ((min == -1) || (min > val)) min = val;
                        }
                    }
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    double logMin = Math.log10(min), multiplier = 255.0 / (Math.log10(max) - logMin);
                    for (int w = 0; w < width; w++) {
                        for (int h = 0; h < height; h++) {
                            int interpolation = (int) ((Math.log10(img[w][h]) - logMin) * multiplier);
                            bitmap.setPixel(w, h, Color.argb(255, interpolation, interpolation, interpolation));
                        }
                    }
                } else {
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    ByteBuffer.allocate(2);
                    rowLoop:
                    for (int h = 0; h < height; h++) {
                        for (int w = 0; w < width; w++) {
                            read = inputStream.read(imgBuffer, 0, bytesPerPix);
                            if (read == -1) break rowLoop;
                            short val;
                            if (bytesPerPix == 2) {
                                val = (short) (((imgBuffer[0] * 256) + imgBuffer[1]) / 65535.0 * 255.0);
                                if (imgBuffer[1] < 0) val += 256;
                            } else {
                                val = (short) (imgBuffer[0] & 0xFF);
                            }
                            bitmap.setPixel(w, h, Color.argb(255, val, val, val));
                        }
                    }
                }
                return bitmap;
            }
        } else {
            return BitmapFactory.decodeByteArray(blobValue.getBlobData(), 0, blobValue.getSize());
        }
    }

    @Override
    public int getActionDrawable() {
        return R.drawable.save;
    }

    @Override
    public void run() {

    }
}