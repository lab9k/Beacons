package com.example.android.beacontrilateration;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.android.beacontrilateration.utils.CanvasView;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;

    private double[] centroid;
    private List<Beacon> verdiepBeacons = new ArrayList<>();
    private List<Double> verdiepX = new ArrayList<>();
    private List<Double> verdiepY = new ArrayList<>();
    private List<Beacon> bestaandeBeacons = new ArrayList<>();

    private BeaconManager beaconManager;
    private Snackbar snackbar;

    private BluetoothAdapter mBluetoothAdapter;

    private CanvasView canvasView;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if bluetooth is on
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            snackbar = Snackbar.make(findViewById(android.R.id.content), "Apparaat ondersteunt geen bluetooth", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                snackbar = Snackbar.make(findViewById(android.R.id.content), "Bluetooth staat uit", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Zet aan", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setBluetooth();
                            }
                        });
                snackbar.show();
            }
        }

        canvasView = (CanvasView) findViewById(R.id.canvas);
        //Beacons
        beaconManager = BeaconManager.getInstanceForApplication(this);

        //iBeacon layout nr
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        getInfoFromJson();

        // permissies checken
        checkAndRequestPermissions();

        beaconManager.bind(this);
    }

    public void getInfoFromJson() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset("beaconsKrook.json"));
            JSONArray beaconsArray = obj.getJSONArray("beaconsKrook");
            double x, y;
            for (int i = 0; i < beaconsArray.length(); i++) {
                JSONObject jsonObject = beaconsArray.getJSONObject(i);
                String beaconid = jsonObject.getString("beaconid");
                x = jsonObject.getDouble("x");
                y = jsonObject.getDouble("y");
                Beacon beacon = new Beacon.Builder()
                        .setId1(beaconid.toLowerCase())
                        .setId2("0")
                        .setId3("0")
                        .setTxPower(-61)
                        .build();
                verdiepBeacons.add(beacon);
                verdiepX.add(x);
                verdiepY.add(y);
                bestaandeBeacons.add(beacon);
            }
        } catch (JSONException e) {
            e.printStackTrace();
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

//                    for (Beacon b : beacons) {
//                        if (list.get(b.getId1().toString()) == null || list.get(b.getId1().toString()).size() > 4)
//                            list.put(b.getId1().toString(), new ArrayList<Double>());
//                        list.get(b.getId1()).add((double) b.getRssi());
//                        Double rssi = Kalman.kalman(list.get(b.getId1().toString()));
//                    }

                    List<Beacon> lijstBeacons = new ArrayList<>();
//
                    for (Beacon b : beacons) {
                        if (verdiepBeacons.contains(b)) {
                            lijstBeacons.add(b);
                        }
                    }

                    // Eigen comparator aanmaken om de Beacon objecten te kunnen sorteren
                    Comparator<Beacon> comparator = new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon left, Beacon right) {
                            int compare = 0;
                            if (left.getDistance() < right.getDistance()) {
                                compare = -1;
                            } else if (left.getDistance() > right.getDistance()) {
                                compare = 1;
                            }
                            return compare;
                        }
                    };

                    Collections.sort(lijstBeacons, comparator);

                    if (lijstBeacons.size() > 3) {
                        int index1 = verdiepBeacons.indexOf(lijstBeacons.get(0));
                        int index2 = verdiepBeacons.indexOf(lijstBeacons.get(1));
                        int index3 = verdiepBeacons.indexOf(lijstBeacons.get(2));
                        int index4 = verdiepBeacons.indexOf(lijstBeacons.get(3));

                        if (index1 != -1 && index2 != -1 && index3 != -1 && index4 != -1) {
                            double[][] positions = new double[][]{{verdiepX.get(index1), verdiepY.get(index1)},
                                    {verdiepX.get(index2), verdiepY.get(index2)},
                                    {verdiepX.get(index3), verdiepY.get(index3)},
                                    {verdiepX.get(index4), verdiepY.get(index4)}};
                            double[] distances = new double[]{(lijstBeacons.get(0).getDistance()) * 3.7795275590551,
                                    (lijstBeacons.get(1).getDistance()) * 3.7795275590551,
                                    (lijstBeacons.get(2).getDistance()) * 3.7795275590551,
                                    (lijstBeacons.get(3).getDistance()) * 3.7795275590551};

                            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
                            LeastSquaresOptimizer.Optimum optimum = solver.solve();

                            // the answer
                            centroid = optimum.getPoint().toArray();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    canvasView.drawLocation((float) centroid[0], (float) centroid[1]);
                                }
                            });
                        }
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }


    private boolean checkAndRequestPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int storagePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_ACCOUNTS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCOUNTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "coarse storage permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since storage access has not been granted, this app will not be able to save some generated documents.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }

                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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
                break;
        }
    }


    public void setBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (!isEnabled) {
            bluetoothAdapter.enable();
        }
    }
}
