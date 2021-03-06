package com.example.android.beacons;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BeaconManager beaconManager;
    private TextView textVerdiep;
    private LinearLayout textVerdiepInfo;
    private List<Verdiep> verdiepen = new ArrayList<>();
    private List<String> verdiepIds = new ArrayList<>();
    private int currentVerdiep;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);

        setTitle("Beacons in De Krook");

        //iBeacon layout nr
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        textVerdiep = (TextView) findViewById(R.id.text_verdiep);
        textVerdiepInfo = (LinearLayout) findViewById(R.id.text_verdiep_info);

        // Beacons die de verdiepen bepalen initialiseren
        initVerdiepen();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check?
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                snackbar = Snackbar.make(findViewById(android.R.id.content), "Bluetooth staat uit", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Zet op", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setBluetooth();
                            }
                        });
                snackbar.show();
            }


        }
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {

                    //Collection beacons omzetten naar List om te kunnen sorteren op distance
                    final List<Beacon> beaconsList = new ArrayList<>(beacons);

                    // Eigen comparator aanmaken om de Beacon objecten te kunnen sorteren
                    Comparator<Beacon> comparator = new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon left, Beacon right) {
                            int compare = 0;
                            if (compare == 0) {
                                if (left.getDistance() < right.getDistance()) {
                                    compare = -1;
                                } else if (left.getDistance() > right.getDistance()) {
                                    compare = 1;
                                }
                            }
                            return compare;
                        }
                    };

                    Collections.sort(beaconsList, comparator);

                    // checken met nabijheid van 2 beacons, de 2 dichtste
                    if (verdiepIds.contains((beaconsList.get(0).getId1().toString()))) {
                        setCurrentVerdiep(beaconsList.get(0).getId1().toString());
                    } else if (beaconsList.size() > 1 && verdiepIds.contains((beaconsList.get(1).getId1().toString()))) {
                        setCurrentVerdiep(beaconsList.get(1).getId1().toString());
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(MainActivity.this, "Geen beacons gevonden",
                            //        Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    private void initVerdiepen() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset("beaconsVerdiepen.json"));
            JSONArray beaconsArray = obj.getJSONArray("beaconsVerdiepen");
            for (int i = 0; i < beaconsArray.length(); i++) {
                JSONObject jsonObject = beaconsArray.getJSONObject(i);
                String uuid = jsonObject.getString("uuid");
                int verdiep = jsonObject.getInt("verdiep");
                verdiepen.add(new Verdiep(uuid, verdiep));
                verdiepIds.add(uuid);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentVerdiep(final String verdiep) {
        // thread van de beacons kan geen UI dingen aanpassen dus moet dit expliciet op de UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // currentVerdiep dat dichtste bij is info updaten in de textviews, enkel String ID wordt meegegeven in setCurrentVerdiep
                for (Verdiep v : verdiepen) {
                    if (v.getId().equals(verdiep)) {
                        updateTextViews(v.getVerdiep());
                        setVerdiepInt(v.getVerdiep());
                    }
                }
            }
        });
    }

    private void updateTextViews(int verdiep) {
        if (currentVerdiep != verdiep) {
            String[] infoVerdiep;
            textVerdiepInfo.removeAllViews();
            switch (verdiep) {
                case -2:
                    textVerdiep.setText(getResources().getString(R.string.verdiepMin2));
                    infoVerdiep = getResources().getStringArray(R.array.verdiepMin2);
                    for (String i : infoVerdiep) {
                        TextView t = new TextView(this);
                        t.setText(i);
                        t.setPadding(50, 10, 0, 10);
                        textVerdiepInfo.addView(t);
                    }
                    break;
                case -1:
                    textVerdiep.setText(getResources().getString(R.string.verdiepMin1));
                    infoVerdiep = getResources().getStringArray(R.array.verdiepMin1);
                    for (String i : infoVerdiep) {
                        TextView t = new TextView(this);
                        t.setText(i);
                        t.setPadding(50, 10, 0, 10);
                        textVerdiepInfo.addView(t);
                    }
                    break;
                case 0:
                    textVerdiep.setText(getResources().getString(R.string.verdiep0));
                    infoVerdiep = getResources().getStringArray(R.array.verdiep0);
                    for (String i : infoVerdiep) {
                        TextView t = new TextView(this);
                        t.setText(i);
                        t.setPadding(50, 10, 0, 10);
                        textVerdiepInfo.addView(t);
                    }
                    break;
                case 1:
                    textVerdiep.setText(getResources().getString(R.string.verdiep1));
                    infoVerdiep = getResources().getStringArray(R.array.verdiep1);
                    for (String i : infoVerdiep) {
                        TextView t = new TextView(this);
                        t.setText(i);
                        t.setPadding(50, 10, 0, 10);
                        textVerdiepInfo.addView(t);
                    }
                    break;
                case 2:
                    textVerdiep.setText(getResources().getString(R.string.verdiep2));
                    infoVerdiep = getResources().getStringArray(R.array.verdiep2);
                    for (String i : infoVerdiep) {
                        TextView t = new TextView(this);
                        t.setText(i);
                        t.setPadding(50, 10, 0, 10);
                        textVerdiepInfo.addView(t);
                    }
                    break;
                case 3:
                    textVerdiep.setText(getResources().getString(R.string.verdiep3));
                    infoVerdiep = getResources().getStringArray(R.array.verdiep3);
                    for (String i : infoVerdiep) {
                        TextView t = new TextView(this);
                        t.setText(i);
                        t.setPadding(50, 10, 0, 10);
                        textVerdiepInfo.addView(t);
                    }
                    break;
            }
        }
    }

    public void setVerdiepInt(int verdiepInt) {
        this.currentVerdiep = verdiepInt;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void setBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (!isEnabled) {
            bluetoothAdapter.enable();
        }
    }
}