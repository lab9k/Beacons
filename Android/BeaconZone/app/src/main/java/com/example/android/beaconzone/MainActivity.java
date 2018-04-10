package com.example.android.beaconzone;

import android.app.Activity;
import android.os.RemoteException;
import android.os.Bundle;
import android.widget.Toast;

import com.example.android.beaconzone.utils.CanvasView;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements BeaconConsumer {
    private BeaconManager beaconManager;
    private HashMap<String, String[][]> zonesGrid = new HashMap<>();
    private HashMap<String, List<String>> triangulationZones = new HashMap<>();
    private HashMap<String, List<Double>> beaconsCoordinates = new HashMap<>();
    private HashMap<String, List<Double>> drawZoneGrid = new HashMap<>();
    private String zone = "0";

    private Comparator<Beacon> comparator;

    private CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        comparator = new Comparator<Beacon>() {
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

        beaconManager.bind(this);

        canvasView = findViewById(R.id.canvas);

        for (int i = 1; i < 7; i++) {
            zonesGrid.put(String.valueOf(i), new String[12][3]);
        }

        getInfoFromJson();
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

                    List<Beacon> lijstBeacons = new ArrayList<>(beacons);

                    Collections.sort(lijstBeacons, comparator);
                    if (beacons.size() > 3) {
                        String b1 = lijstBeacons.get(0).getId1().toString().toLowerCase();
                        String b2 = lijstBeacons.get(1).getId1().toString().toLowerCase();
                        String b3 = lijstBeacons.get(2).getId1().toString().toLowerCase();
                        String b4 = lijstBeacons.get(3).getId1().toString().toLowerCase();

                        if (!b1.isEmpty() && !b2.isEmpty() && !b3.isEmpty()) {
                            String tempZone = checkZone(b1, b2, b3, b4);
                            if (!tempZone.equals("0")) {
                                drawZone(tempZone);
                                calculateLocation(tempZone, lijstBeacons);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Geen zones gevonden", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Geen beacons gevonden", Toast.LENGTH_LONG).show();
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

    private String checkZone(String b1, String b2, String b3, String b4) {
        for (Map.Entry<String, String[][]> entry : zonesGrid.entrySet()) {
            String key = entry.getKey();
            String[][] zoneGrid = entry.getValue();
            for (String[] t : zoneGrid) {
                if ((b1.equals(t[0]) && b2.equals(t[1]) && b3.equals(t[2])) || (b1.equals(t[0]) && b2.equals(t[1]) && b4.equals(t[2]))) {
                    return key;
                }
            }
        }
        return "0";
    }

    private void drawZone(final String tempZone) {
        if (!zone.equals(tempZone)) {
            zone = tempZone;
            final List<Double> coordinates = drawZoneGrid.get(tempZone);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double left = coordinates.get(0);
                    double top = coordinates.get(1);
                    double right = coordinates.get(2);
                    double bottom = coordinates.get(3);

                    canvasView.drawZone(left, top, right, bottom);
                }
            });
        }
    }

    private void calculateLocation(String zone, List<Beacon> lijstBeacons) {

        List<String> triangulation = triangulationZones.get(zone);

        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();
        List<Double> distance = new ArrayList<>();

        for (int i = 0; i < triangulation.size(); i++) {
            for (Beacon b : lijstBeacons) {
                String beaconName = b.getId1().toString().toLowerCase();
                if (triangulation.contains(beaconName)) {
                    distance.add(b.getDistance());
                    x.add(beaconsCoordinates.get(beaconName).get(0));
                    y.add(beaconsCoordinates.get(beaconName).get(1));
                }
            }
        }

        double[][] positions;
        double[] distances;
        if (triangulation.size() == 4) {
            positions = new double[][]{{x.get(0), y.get(0)}, {x.get(1), y.get(1)}, {x.get(2), y.get(2)}, {x.get(3), y.get(3)}};
            distances = new double[]{distance.get(0) * 3.779528,
                    (distance.get(1) * 3.779528),
                    (distance.get(2) * 3.779528),
                    (distance.get(3) * 3.779528)};
        } else {
            positions = new double[][]{{x.get(0), y.get(0)}, {x.get(1), y.get(1)}, {x.get(2), y.get(2)}};
            distances = new double[]{distance.get(0) * 3.779528,
                    (distance.get(1) * 3.779528),
                    (distance.get(2) * 3.779528)};
        }
        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        final double[] centroid = optimum.getPoint().toArray();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                canvasView.drawLocation((float) centroid[0], (float) centroid[1]);
            }
        });
    }

    public void getInfoFromJson() {
        try {
            //zones
            JSONObject obj = new JSONObject(loadJSONFromAsset("beaconZones.json"));
            JSONArray beaconsArray = obj.getJSONArray("beaconZones");
            for (int i = 0; i < beaconsArray.length(); i++) {
                JSONObject jsonObject = beaconsArray.getJSONObject(i);
                String zone = jsonObject.getString("zone");
                JSONArray zones = jsonObject.getJSONArray("zones");
                JSONArray drawZone = jsonObject.getJSONArray("drawZone");
                JSONArray triangulation = jsonObject.getJSONArray("triangulation");
                for (int j = 0; j < zones.length(); j++) {
                    JSONArray ob = zones.getJSONObject(j).getJSONArray("beacons");
                    String[][] temp = zonesGrid.get(zone);
                    temp[j][0] = ob.getString(0).isEmpty() ? "0" : "e2c56db5-dffb-48d2-b060-d04f435441" + ob.getString(0).toLowerCase();
                    temp[j][1] = ob.getString(1).isEmpty() ? "0" : "e2c56db5-dffb-48d2-b060-d04f435441" + ob.getString(1).toLowerCase();
                    temp[j][2] = ob.getString(2).isEmpty() ? "0" : "e2c56db5-dffb-48d2-b060-d04f435441" + ob.getString(2).toLowerCase();

                    zonesGrid.put(zone, temp);
                }
                List<Double> tempDraw = new ArrayList<>();
                for (int k = 0; k < drawZone.length(); k++) {
                    tempDraw.add(drawZone.getDouble(k));
                }
                drawZoneGrid.put(zone, tempDraw);

                List<String> temp = new ArrayList<>();
                for (int l = 0; l < triangulation.length(); l++) {
                    temp.add("e2c56db5-dffb-48d2-b060-d04f435441" + triangulation.getString(l).toLowerCase());
                }
                triangulationZones.put(zone, temp);
            }

            //x en y
            JSONObject object = new JSONObject(loadJSONFromAsset("beaconsMin1.json"));
            JSONArray beaconsArrayXY = object.getJSONArray("beaconsKrook");
            double x, y;
            for (int i = 0; i < beaconsArrayXY.length(); i++) {
                JSONObject jsonobject = beaconsArrayXY.getJSONObject(i);
                String beaconid = jsonobject.getString("beaconid");
                x = jsonobject.getDouble("x");
                y = jsonobject.getDouble("y");
                beaconsCoordinates.put(beaconid.toLowerCase(), Arrays.asList(x, y));
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public String loadJSONFromAsset(String filename) {
        String json;
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
}