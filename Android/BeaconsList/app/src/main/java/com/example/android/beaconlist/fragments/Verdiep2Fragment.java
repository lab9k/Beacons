package com.example.android.beaconlist.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.beaconlist.MainActivity;
import com.example.android.beaconlist.R;
import com.example.android.beaconlist.adapters.BeaconAdapter;
import com.example.android.beaconlist.utils.CanvasView;

import org.altbeacon.beacon.Beacon;

import java.util.List;


public class Verdiep2Fragment extends Fragment {

    private MainActivity hoofdActivity;
    private BeaconAdapter adapter;
    private RecyclerView lijst;
    private LinearLayoutManager mLayoutManager;
    private CanvasView canvasView;
    private float[][] grid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verdiep2, container, false);

        canvasView = (CanvasView) v.findViewById(R.id.canvas);

        hoofdActivity = ((MainActivity) getActivity());


        lijst = v.findViewById(R.id.recycler_view_2);
        mLayoutManager = new LinearLayoutManager(getActivity());
        lijst.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lijst.getContext(),
                DividerItemDecoration.VERTICAL);
        lijst.addItemDecoration(dividerItemDecoration);
        adapter = new BeaconAdapter(hoofdActivity.getVerdiepBeacons("2"));
        lijst.setAdapter(adapter);

        grid = new float[hoofdActivity.getVerdiepBeacons("2").size()][3];

        updateList(hoofdActivity.getGevondenBeacons());
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public void updateList(List<Beacon> gevondenBeacons) {
        adapter.updateList(gevondenBeacons);
        hoofdActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        grid = hoofdActivity.updateMapBeacons(grid, "2");
        canvasView.drawBeacons(grid);
    }
}