package com.example.android.beacontrilateration;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.beacontrilateration.model.Verdiep;
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
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
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
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;

    private double[] centroid;
    private HashMap<String, List<Beacon>> verdiepBeacons = new HashMap<>();
    private HashMap<String, List<Float>> verdiepX = new HashMap<>();
    private HashMap<String, List<Float>> verdiepY = new HashMap<>();
    //    private List<Float> xMin2 = new ArrayList<>();
//    private List<Float> yMin2 = new ArrayList<>();
    private List<Beacon> gevondenBeacons = new ArrayList<>();
    private List<Beacon> bestaandeBeacons = new ArrayList<>();

    private BeaconManager beaconManager;
    private List<Verdiep> verdiepen = new ArrayList<>();
    private List<String> verdiepIds = new ArrayList<>();
    private int currentVerdiep = -2;
    private Snackbar snackbar;

    private CanvasView canvasView;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);

        setTitle("Localisatie in De Krook");

        //iBeacon layout nr
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));


        for (int i = -2; i < 4; i++) {
            verdiepBeacons.put(String.valueOf(i), new ArrayList<Beacon>());
            verdiepX.put(String.valueOf(i), new ArrayList<Float>());
            verdiepY.put(String.valueOf(i), new ArrayList<Float>());
        }
        // Beacons die de verdiepen bepalen initialiseren
        initVerdiepen();
        getInfoFromJson();

        //check if bluetooth is on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(MainActivity.this, "Bluetooth wordt niet ondersteund door jouw toestel",
                    Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled
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

        checkAndRequestPermissions();

        canvasView = (CanvasView) findViewById(R.id.canvas);

        beaconManager.bind(this);
    }

    public void getInfoFromJson() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset("beaconsKrook.json"));
            JSONArray beaconsArray = obj.getJSONArray("beaconsKrook");
            List<Beacon> temp;
            List<Float> tempX, tempY;
            float x, y;
            for (int i = 0; i < beaconsArray.length(); i++) {
                JSONObject jsonObject = beaconsArray.getJSONObject(i);
                String beaconid = jsonObject.getString("beaconid");
                String verdiep = jsonObject.getString("verdieping");
                Beacon beacon = new Beacon.Builder()
                        .setId1(beaconid.toLowerCase())
                        .setId2("0")
                        .setId3("0")
                        .setTxPower(-61)
                        .build();
                switch (verdiep) {
                    case "-2":
                        temp = verdiepBeacons.get("-2");
                        temp.add(beacon);
                        verdiepBeacons.put("-2", temp);
                        x = jsonObject.getLong("x");
                        y = jsonObject.getLong("y");
                        tempX = verdiepX.get("-2");
                        tempX.add(x);
                        verdiepX.put("-2", tempX);
                        tempY = verdiepY.get("-2");
                        tempY.add(y);
                        verdiepY.put("-2", tempY);
                        break;
                    case "-1":
                        temp = verdiepBeacons.get("-1");
                        temp.add(beacon);
                        verdiepBeacons.put("-1", temp);
                        x = jsonObject.getLong("x");
                        y = jsonObject.getLong("y");
                        tempX = verdiepX.get("-1");
                        tempX.add(x);
                        verdiepX.put("-1", tempX);
                        tempY = verdiepY.get("-1");
                        tempY.add(y);
                        verdiepY.put("-1", tempY);
                        break;
//                    case "0":
//                        temp = verdiepBeacons.get("0");
//                        temp.add(beacon);
//                        verdiepBeacons.put("0", temp);
//                        break;
//                    case "1":
//                        temp = verdiepBeacons.get("1");
//                        temp.add(beacon);
//                        verdiepBeacons.put("1", temp);
//                        break;
//                    case "2":
//                        temp = verdiepBeacons.get("2");
//                        temp.add(beacon);
//                        verdiepBeacons.put("2", temp);
//                        break;
//                    case "3":
//                        temp = verdiepBeacons.get("3");
//                        temp.add(beacon);
//                        verdiepBeacons.put("3", temp);
//                        break;
                }
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

                    //Collection beacons omzetten naar List om te kunnen sorteren op distance
                    final List<Beacon> beaconsList = new ArrayList<>(beacons);

                    // Eigen comparator aanmaken om de Beacon objecten te kunnen sorteren
                    Comparator<Beacon> comparator = new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon left, Beacon right) {
                            int compare = 0;
                            if (left.getDistance() * 1000 < right.getDistance() * 1000) {
                                compare = -1;
                            } else if (left.getDistance() * 1000 > right.getDistance() * 1000) {
                                compare = 1;
                            }

                            return compare;
                        }
                    };

                    Collections.sort(beaconsList, comparator);

                    //  double distance = beacons.iterator().next().getDistance();

//                    // checken met nabijheid van 2 beacons, de 2 dichtste
//                    if (verdiepIds.contains((beaconsList.get(0).getId1().toString()))) {
//                        setCurrentVerdiep(beaconsList.get(0).getId1().toString());
//                    } else if (beaconsList.size() > 1 && verdiepIds.contains((beaconsList.get(1).getId1().toString()))) {
//                        setCurrentVerdiep(beaconsList.get(1).getId1().toString());
//                    }

//                    for (Beacon b : beaconsList) {
//                        if (locationBeacons.size() < 4)
//                            if (verdiepBeacons.get(String.valueOf(currentVerdiep)).contains(b)) {
//                                locationBeacons.add(b);
//                            }
//                    }
//
//                    if (currentVerdiep != 5 && locationBeacons.size() == 4) {
//                        int index1 = verdiepBeacons.get(String.valueOf(currentVerdiep)).indexOf(locationBeacons.get(0));
//                        int index2 = verdiepBeacons.get(String.valueOf(currentVerdiep)).indexOf(locationBeacons.get(1));
//                        int index3 = verdiepBeacons.get(String.valueOf(currentVerdiep)).indexOf(locationBeacons.get(2));
//                        int index4 = verdiepBeacons.get(String.valueOf(currentVerdiep)).indexOf(locationBeacons.get(3));

//                    if (beacon.getDistance() < 5.0) {
//                        Log.d(TAG, "I see a beacon that is less than 5 meters away.");
//                        // Perform distance-specific action here
//                    }

                    if (beaconsList.size() > 3) {
                        int index1 = verdiepBeacons.get("-1").indexOf(beaconsList.get(0));
                        int index2 = verdiepBeacons.get("-1").indexOf(beaconsList.get(1));
                        int index3 = verdiepBeacons.get("-1").indexOf(beaconsList.get(2));
                        int index4 = verdiepBeacons.get("-1").indexOf(beaconsList.get(3));
                        if (index1 != -1 && index2 != -1 && index3 != -1 && index4 != -1) {
                            double[][] positions = new double[][]{{verdiepX.get("-1").get(index1), verdiepY.get("-1").get(index1)},
                                    {verdiepX.get("-1").get(index2), verdiepY.get("-1").get(index2)},
                                    {verdiepX.get("-1").get(index3), verdiepY.get("-1").get(index3)},
                                    {verdiepX.get("-1").get(index4), verdiepY.get("-1").get(index4)}};
                            double[] distances = new double[]{(beaconsList.get(0).getDistance()) * 37.795275590551,
                                    (beaconsList.get(1).getDistance()) * 37.795275590551,
                                    (beaconsList.get(2).getDistance()) * 37.795275590551,
                                    (beaconsList.get(3).getDistance()) * 37.795275590551};

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
            e.printStackTrace();
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
//                    if (v.getId().equals(verdiep)) {
//                        setVerdiepInt(v.getVerdiep());
//                    }
                }
            }
        });
    }

    public void setVerdiepInt(int verdiepInt) {
        this.currentVerdiep = verdiepInt;
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
