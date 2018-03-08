package com.example.android.beaconlist.fragments;

import android.content.Context;
import android.net.Uri;
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

import org.altbeacon.beacon.Beacon;

import java.util.List;

import butterknife.ButterKnife;


public class VerdiepMin1Fragment extends Fragment {

    private MainActivity hoofdActivity;
    private BeaconAdapter adapter;
    private RecyclerView lijst;
    private LinearLayoutManager mLayoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verdiep_min1, container, false);

        hoofdActivity = ((MainActivity) getActivity());

        lijst = v.findViewById(R.id.recycler_view_min_1);
        mLayoutManager = new LinearLayoutManager(getActivity());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(lijst.getContext(),
                DividerItemDecoration.VERTICAL);
        lijst.addItemDecoration(dividerItemDecoration);
        lijst.setLayoutManager(mLayoutManager);
        adapter = new BeaconAdapter(hoofdActivity.getBestaandeMin1Beacons());
        lijst.setAdapter(adapter);

        updateList(hoofdActivity.getGevondenBeacons());
        return v;
    }

    public void updateList(List<Beacon> gevondenBeacons) {
        adapter.updateList(gevondenBeacons);
        hoofdActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
