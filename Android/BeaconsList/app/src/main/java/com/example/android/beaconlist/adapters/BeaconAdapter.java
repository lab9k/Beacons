package com.example.android.beaconlist.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.beaconlist.R;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lottejespers.
 */

public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ViewHolder> {


    private List<Beacon> bestaandeBeacons = new ArrayList<>();
    private List<Beacon> gevondenBeacons = new ArrayList<>();
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView hex_id;
        private TextView octa_id;
        private ImageView icon_beacon;

        public ViewHolder(View view) {
            super(view);
            hex_id = view.findViewById(R.id.hex_id);
            octa_id = view.findViewById(R.id.octa_id);
            icon_beacon = view.findViewById(R.id.icon_beacon);
        }
    }

    public BeaconAdapter(List<Beacon> beacons) {
        this.bestaandeBeacons = beacons;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Beacon beacon = bestaandeBeacons.get(position);
        holder.hex_id.setText(beacon.getId1().toString().toLowerCase());
        String hexString = beacon.getId1().toString().substring(Math.max(beacon.getId1().toString().length() - 2, 0));
        int octaId = hexToDec(hexString);
        holder.octa_id.setText("OCTA: " + String.valueOf(octaId));
        holder.icon_beacon.setImageResource(R.drawable.ic_radio_button);
        if (!gevondenBeacons.isEmpty()) {
            if (gevondenBeacons.contains(beacon)) {
                holder.icon_beacon.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
            } else
                holder.icon_beacon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        } else holder.icon_beacon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    public int getItemCount() {
        return bestaandeBeacons.size();
    }

    public void updateList(List<Beacon> beacons) {
        this.gevondenBeacons = beacons;
    }

    private static int hexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }
}