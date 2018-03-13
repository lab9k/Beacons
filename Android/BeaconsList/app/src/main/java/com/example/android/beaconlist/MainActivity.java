package com.example.android.beaconlist;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.beaconlist.fragments.Verdiep0Fragment;
import com.example.android.beaconlist.fragments.Verdiep1Fragment;
import com.example.android.beaconlist.fragments.Verdiep2Fragment;
import com.example.android.beaconlist.fragments.Verdiep3Fragment;
import com.example.android.beaconlist.fragments.VerdiepMin1Fragment;
import com.example.android.beaconlist.fragments.VerdiepMin2Fragment;
import com.example.android.beaconlist.interfaces.BeaconInterface;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


public class MainActivity extends AppCompatActivity implements BeaconConsumer, BeaconInterface {

    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;


    private BeaconManager beaconManager;

    private HashMap<String, List<Beacon>> verdiepBeacons = new HashMap<>();
    private List<Beacon> gevondenBeacons = new ArrayList<>();
    private List<Beacon> bestaandeBeacons = new ArrayList<>();
    private List<Float> xMin2 = new ArrayList<>();
    private List<Float> yMin2 = new ArrayList<>();
    private VerdiepMin2Fragment verdiepMin2Fragment;
    private VerdiepMin1Fragment verdiepMin1Fragment;
    private Verdiep0Fragment verdiep0Fragment;
    private Verdiep1Fragment verdiep1Fragment;
    private Verdiep2Fragment verdiep2Fragment;
    private Verdiep3Fragment verdiep3Fragment;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Beacons in De Krook");

        //Tablayout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //check if bluetooth is on
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
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

        //Beacons
        beaconManager = BeaconManager.getInstanceForApplication(this);

        //iBeacon layout nr
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        for (int i = -2; i < 4; i++)
            verdiepBeacons.put(String.valueOf(i), new ArrayList<Beacon>());

        getInfoFromJson();

        // beide permissies checken
        checkAndRequestPermissions();

        beaconManager.bind(this);

        // ignore FileUriExposedException
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
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
                    for (Beacon beacon : beacons) {
                        if (!gevondenBeacons.contains(beacon)) {
                            gevondenBeacons.add(beacon);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateFragmenten();
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


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        verdiepMin2Fragment = new VerdiepMin2Fragment();
        verdiepMin1Fragment = new VerdiepMin1Fragment();
        verdiep0Fragment = new Verdiep0Fragment();
        verdiep1Fragment = new Verdiep1Fragment();
        verdiep2Fragment = new Verdiep2Fragment();
        verdiep3Fragment = new Verdiep3Fragment();

        adapter.addFrag(verdiepMin2Fragment, "Verdiep -2");
        adapter.addFrag(verdiepMin1Fragment, "Verdiep -1");
        adapter.addFrag(verdiep0Fragment, "Verdiep 0");
        adapter.addFrag(verdiep1Fragment, "Verdiep 1");
        adapter.addFrag(verdiep2Fragment, "Verdiep 2");
        adapter.addFrag(verdiep3Fragment, "Verdiep 3");

        viewPager.setAdapter(adapter);
    }

    public List<Beacon> getGevondenBeacons() {
        return gevondenBeacons;
    }

    public List<Beacon> getVerdiepBeacons(String verdiep) {
        return verdiepBeacons.get(verdiep);
    }

    @Override
    public void updateFragmenten() {

        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());

        if (page instanceof VerdiepMin2Fragment)
            ((VerdiepMin2Fragment) page).updateList(gevondenBeacons);
        else if (page instanceof VerdiepMin1Fragment)
            ((VerdiepMin1Fragment) page).updateList(gevondenBeacons);
        else if (page instanceof Verdiep0Fragment)
            ((Verdiep0Fragment) page).updateList(gevondenBeacons);
        else if (page instanceof Verdiep1Fragment)
            ((Verdiep1Fragment) page).updateList(gevondenBeacons);
        else if (page instanceof Verdiep2Fragment)
            ((Verdiep2Fragment) page).updateList(gevondenBeacons);
        else
            ((Verdiep3Fragment) page).updateList(gevondenBeacons);
    }

    public void getInfoFromJson() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset("beaconsKrook.json"));
            JSONArray beaconsArray = obj.getJSONArray("beaconsKrook");
            List<Beacon> temp;
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
                        float x = jsonObject.getLong("x");
                        float y = jsonObject.getLong("y");
                        xMin2.add(x);
                        yMin2.add(y);
                        break;
                    case "-1":
                        temp = verdiepBeacons.get("-1");
                        temp.add(beacon);
                        verdiepBeacons.put("-1", temp);
                        break;
                    case "0":
                        temp = verdiepBeacons.get("0");
                        temp.add(beacon);
                        verdiepBeacons.put("0", temp);
                        break;
                    case "1":
                        temp = verdiepBeacons.get("1");
                        temp.add(beacon);
                        verdiepBeacons.put("1", temp);
                        break;
                    case "2":
                        temp = verdiepBeacons.get("2");
                        temp.add(beacon);
                        verdiepBeacons.put("2", temp);
                        break;
                    case "3":
                        temp = verdiepBeacons.get("3");
                        temp.add(beacon);
                        verdiepBeacons.put("3", temp);
                        break;
                }
                bestaandeBeacons.add(beacon);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Float> getYMin2() {
        return yMin2;
    }

    public List<Float> getXMin2() {
        return xMin2;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
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


    private void makeExcel(List<Beacon> beacons, final String filename, int verdieping) {
        final File sd = Environment.getExternalStorageDirectory();
        String csvFile = filename + ".xls";

        File directory = new File(sd.getAbsolutePath());
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {
            //file path
            final File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("nl", "NL"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet(filename, 0);
            // column and row
            sheet.addCell(new Label(0, 0, "Verdiep"));
            sheet.addCell(new Label(1, 0, "UUID"));
            sheet.addCell(new Label(2, 0, "OCTA-ID"));
            sheet.addCell(new Label(3, 0, "Werkend"));


            for (int i = 0; i < beacons.size(); i++) {
                String verdiep = "";

                Beacon b = beacons.get(i);
                if (verdieping == 7) {
                    if (i < 17)
                        verdiep = "-2";
                    else if (i < 63)
                        verdiep = "-1";
                    else if (i < 116)
                        verdiep = "0";
                    else if (i < 163)
                        verdiep = "1";
                    else if (i < 207)
                        verdiep = "2";
                    else
                        verdiep = "3";
                } else
                    verdiep = String.valueOf(verdieping);

                String uuid = b.getId1().toString();

                String hexString = b.getId1().toString().substring(Math.max(b.getId1().toString().length() - 2, 0));
                int octaId = Integer.parseInt(hexString, 16);
                String octa = String.valueOf(octaId);

                String werkend;
                if (gevondenBeacons.contains(b)) {
                    werkend = "werkend";
                } else {
                    werkend = "niet werkend";
                }

                sheet.addCell(new Label(0, i + 1, verdiep));
                sheet.addCell(new Label(1, i + 1, uuid));
                sheet.addCell(new Label(2, i + 1, octa));
                sheet.addCell(new Label(3, i + 1, werkend));
            }
            workbook.write();
            workbook.close();

            snackbar = Snackbar.make(findViewById(android.R.id.content), filename + " Exported in a Excel Sheet", Snackbar.LENGTH_INDEFINITE)
                    .setAction("open", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (file.exists()) {
                                Uri path = Uri.fromFile(file);
                                Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                                pdfIntent.setDataAndType(path, "application/vnd.ms-excel");
                                pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                try {
                                    startActivity(pdfIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(MainActivity.this, "Please install MS-Excel app to view the file.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                    });
            snackbar.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // builder.setIcon(R.drawable.ic_warning);
                builder.setTitle(R.string.choose)
                        .setItems(R.array.choose_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int chooseItem) {
                                switch (chooseItem) {
                                    case 0:
                                        makeExcel(verdiepBeacons.get("-2"), "Beacons Verdiep -2", -2);
                                        break;
                                    case 1:
                                        makeExcel(verdiepBeacons.get("-1"), "Beacons Verdiep -1", -1);
                                        break;
                                    case 2:
                                        makeExcel(verdiepBeacons.get("0"), "Beacons Verdiep 0", 0);
                                        break;
                                    case 3:
                                        makeExcel(verdiepBeacons.get("1"), "Beacons Verdiep 1", 1);
                                        break;
                                    case 4:
                                        makeExcel(verdiepBeacons.get("2"), "Beacons Verdiep 2", 2);
                                        break;
                                    case 5:
                                        makeExcel(verdiepBeacons.get("3"), "Beacons Verdiep 3", 3);
                                        break;
                                    case 6:
                                        makeExcel(bestaandeBeacons, "Alle Beacons", 7);
                                        break;
                                }
                            }
                        });
                builder.create();
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
}
