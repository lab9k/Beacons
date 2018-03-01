package com.example.android.beacons;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);

        setTitle("Beacons in De Krook");

        //iBeacon layout nr
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));


        textVerdiep = (TextView) findViewById(R.id.text_verdiep);
        textVerdiepInfo = (LinearLayout) findViewById(R.id.text_verdiep_info);

        // Beacons die de verdiepen bepalen initialiseren
        initVerdiepen();

        // Android Permission check
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
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
                            Toast.makeText(MainActivity.this, "Geen beacons gevonden",
                                    Toast.LENGTH_LONG).show();
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
        // Verdiep -2
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354410e", -2));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354410d", -2));

        // Verdiep -1
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f43544126", -1));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f43544123", -1));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f43544128", -1));

        // Verdiep 0
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354414a", 0));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354416b", 0));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354415c", 0));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354415d", 0));

        // Verdiep 1
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354419e", 1));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354417f", 1));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f43544175", 1));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f43544176", 1));

        //Ingang
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f4354415f", 0));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f43544161", 0));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f43544160", 0));

        // Verdiep 2
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441cc", 2));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441b3", 2));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441ae", 2));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441b8", 2));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441b7", 2));

        // Verdiep 3
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441e5", 3));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441da", 3));
        verdiepen.add(new Verdiep("e2c56db5-dffb-48d2-b060-d04f435441d9", 3));

        for (Verdiep v : verdiepen) {
            verdiepIds.add(v.getId());
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
}