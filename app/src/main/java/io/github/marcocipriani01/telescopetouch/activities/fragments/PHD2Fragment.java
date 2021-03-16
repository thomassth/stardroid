package io.github.marcocipriani01.telescopetouch.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;

import io.github.marcocipriani01.simplesocket.ConnectionException;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.phd2.PHD2Client;

public class PHD2Fragment extends ActionFragment implements PHD2Client.PHD2Listener {

    private PHD2Client phd2;
    private GraphView graph;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_phd2, container, false);
        graph = rootView.findViewById(R.id.phd_graph);
        phd2 = new PHD2Client("192.168.1.2", 4400);
        graph.addSeries(phd2.guidingDataRA);
        graph.addSeries(phd2.guidingDataDec);
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setLabelFormatter(new DefaultLabelFormatter());
        gridLabel.setNumHorizontalLabels(3);
        gridLabel.setNumVerticalLabels(6);
        gridLabel.setHorizontalLabelsAngle(45);
        Viewport viewport = graph.getViewport();
        //viewport.setMinX(0);
        //viewport.setMaxX(200);
        //viewport.setXAxisBoundsManual(true);
        //viewport.setYAxisBoundsManual(true);
        viewport.setScalable(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        phd2.addListener(this);
        try {
            phd2.connect();
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActionEnabled() {
        return false;
    }

    @Override
    public int getActionDrawable() {
        return R.drawable.navigation;
    }

    @Override
    public void onPHD2ParamUpdate(PHD2Client.PHD2Param param) {

    }

    @Override
    public void onPHD2CriticalError(Exception e) {

    }

    @Override
    public void onPHD2Close() {

    }

    @Override
    public void run() {

    }
}