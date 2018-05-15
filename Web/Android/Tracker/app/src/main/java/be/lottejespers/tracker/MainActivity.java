package be.lottejespers.tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private BeaconManager beaconManager;

    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private Snackbar snackbar;

    private ArrayList<Beacon> beaconLijst = new ArrayList<>();

    private String toestelnaam;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private RangeNotifier notifier;

    private Switch startStop;
    private TextView countdownTijd;

    private Handler handler;
    private Runnable runnable;

    private int minutes;
    private int hour;

    private LinearLayout trackTijd, trackTijdBtns;
    private TextView trackenTot;

    private boolean beaconsGevonden;
    private boolean totBuiten = false;
    private Handler checkBuiten;

    private EditText toestelEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();
        checkBluetooth();

        toestelEt = findViewById(R.id.toestelnaam);
        startStop = findViewById(R.id.startStop);
        startStop.setEnabled(false);
        startStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                toestelnaam = toestelEt.getText().toString();
                if (isChecked) {
                    if (!toestelnaam.equals("")) {
                        beaconManager = BeaconManager.getInstanceForApplication(MainActivity.this);

                        beaconManager.setForegroundScanPeriod(5000);
                        beaconManager.setForegroundBetweenScanPeriod(5000);
                        beaconManager.setBackgroundScanPeriod(5000);
                        beaconManager.setBackgroundBetweenScanPeriod(5000);

                        if (notifier != null)
                            beaconManager.addRangeNotifier(notifier);

                        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
                        beaconManager.bind(MainActivity.this);

                        resetBeacons();
                        if (totBuiten)
                            startTimer();
                        setDate();
                        toestelEt.setEnabled(false);
                    } else {
                        toestelEt.setError("Je moet een toestelnaam opgeven");
                        startStop.setChecked(false);
                    }
                } else {
                    stopTracking();
                    mRootRef.child(toestelnaam).removeValue();
                    toestelEt.setEnabled(true);
                }
            }
        });

        countdownTijd = findViewById(R.id.countdownTijd);

        Button selecteerTijd = findViewById(R.id.selecteerTijd);
        selecteerTijd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
                totBuiten = false;
            }
        });

        Button tijdAanwezig = findViewById(R.id.tijdAanwezig);
        tijdAanwezig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totBuiten = true;
                hour = 24;
                trackenTot.setText(R.string.krookVerlaat);
                trackTijdBtns.setVisibility(View.GONE);
                trackTijd.setVisibility(View.VISIBLE);
                startStop.setEnabled(true);
            }
        });

        trackTijd = findViewById(R.id.trackTijd);
        trackTijdBtns = findViewById(R.id.trackTijdBtns);
        trackenTot = findViewById(R.id.trackenTot);

        trackenTot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackTijdBtns.setVisibility(View.VISIBLE);
                trackTijd.setVisibility(View.GONE);
                mRootRef.child(toestelnaam).removeValue();
                toestelEt.setEnabled(true);
                startStop.setChecked(false);
            }
        });
    }

    private void stopTracking() {
        if (totBuiten)
            checkBuiten.removeMessages(0);
        handler.removeCallbacks(runnable);
        beaconManager.removeAllRangeNotifiers();
        trackTijd.setVisibility(View.GONE);
        trackTijdBtns.setVisibility(View.VISIBLE);
        countdownTijd.setText("");
        startStop.setEnabled(false);
    }

    private void setDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date d = cal.getTime();

        countDownStart(d);
    }

    public void countDownStart(final Date newDate) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                try {
                    Date currentDate = new Date();
                    if (!currentDate.after(newDate)) {
                        long diff = newDate.getTime()
                                - currentDate.getTime();
                        long days = diff / (24 * 60 * 60 * 1000);
                        diff -= days * (24 * 60 * 60 * 1000);
                        long hours = diff / (60 * 60 * 1000);
                        diff -= hours * (60 * 60 * 1000);
                        long minutes = diff / (60 * 1000);
                        diff -= minutes * (60 * 1000);
                        long seconds = diff / 1000;
                        if (hour != 24)
                            countdownTijd.setText(String.format(" %02d:%02d:%02d", hours, minutes, seconds));
                    } else {
                        countdownTijd.setText("00:00:00");
                        startStop.setChecked(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconManager != null)
            beaconManager.unbind(this);
        mRootRef.child(toestelnaam).removeValue();
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (notifier == null) {
                    Set<RangeNotifier> notifiers = beaconManager.getRangingNotifiers();
                    for (RangeNotifier n : notifiers) {
                        notifier = n;
                    }
                }
                if (beacons.size() > 0) {
                    if (beacons.iterator().next().getId1().toString().contains("e2c56db5-dffb-48d2-b060-d04f435441")) {
                        beaconsGevonden = true;
                        checkBuiten.removeMessages(0);
                        for (Beacon b : beacons) {
                            if (beaconLijst.contains(b)) {
                                int id = beaconLijst.indexOf(b);
                                beaconLijst.get(id).setRssi(b.getRssi());
                            } else
                                beaconLijst.add(b);
                        }
                        startTimer();
                        for (Beacon be : beaconLijst) {
                            String id = be.getId1().toString().substring(be.getId1().toString().length() - 2);
                            mRootRef.child(toestelnaam).child(id).child("RSSI").setValue(be.getRssi());
                        }
                        resetBeacons();
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "time picker");
    }

    private void resetBeacons() {
        beaconLijst.clear();
        beaconsGevonden = false;
    }

    private void startTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkBuiten = new Handler();
                checkBuiten.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!beaconsGevonden)
                            startStop.setChecked(false);
                    }
                }, 30000);
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

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
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCOUNTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }
                        });
                    }
                    builder.show();
                }
                break;
        }
    }

    public void checkBluetooth() {
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
    }

    public void setBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (!isEnabled) {
            bluetoothAdapter.enable();
        }
    }

    public void setTimeFromPicker(int hour, int minutes) {
        this.hour = hour;
        this.minutes = minutes;
        trackTijdBtns.setVisibility(View.GONE);
        trackTijd.setVisibility(View.VISIBLE);
        trackenTot.setText(String.format("%s:%s", (hour < 10 ? "0" : "") + Integer.toString(hour),
                (minutes < 10 ? "0" : "") + Integer.toString(minutes)));
    }

//    private void fillIdentifiers() {
//        identifiers = new ArrayList<>();
//
//        for (long i = 0; i <= 252; i++) {
//            String hex = Long.toHexString(i);
//            identifiers.add(Identifier.parse("e2c56db5-dffb-48d2-b060-d04f435441" + (hex.length() == 1 ? "0" : "") + String.valueOf(hex)));
//        }
//    }
}
