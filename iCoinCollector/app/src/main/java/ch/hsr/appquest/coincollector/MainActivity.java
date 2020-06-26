package ch.hsr.appquest.coincollector;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    public static final String TAG = "Beacon-Debug";
    private static final String appUuid = "52495334-5696-4DAE-BEC7-98D44A30FFDA";
    private static final String beaconLayout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final int REQUEST_COARSE_LOCATION = 1;
    public CoinManager coinManager = new CoinManager();
    private SharedPreferences localPrefs;
    private SharedPreferences.Editor editor;
    private LinearLayout ll;
    private List<Integer> foundBeaconsMajor = new ArrayList<>();
    private BeaconManager beaconManager;
    private NotificationUtil notificationUtil;


    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasLocationPermission() && hasFineLocationPermission()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_COARSE_LOCATION);
    }

    private boolean hasLocationPermission() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasFineLocationPermission() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll = findViewById(R.id.ll);
        localPrefs = this.getPreferences(MODE_PRIVATE);

        editor = localPrefs.edit();

        restoreData();
        setupBeaconManager();
        setupNotificationUtil();

    }

    private void setupBeaconManager() {

        //Permissions
        requestAppPermissions();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(beaconLayout));
        beaconManager.setBackgroundBetweenScanPeriod(100);
        beaconManager.setBackgroundScanPeriod(1100);
        beaconManager.setForegroundScanPeriod(100);
        beaconManager.setForegroundBetweenScanPeriod(500);

        //Region region = new Region("ch.hsr.appquest.coincollector", null, null, null);

        beaconManager.bind(this);

    }


    private void setupNotificationUtil() {
        notificationUtil = new NotificationUtil(this);
        notificationUtil.createNotificationChannel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Reset");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(item -> {

            final AlertDialog.Builder inputAlert = new AlertDialog.Builder(this);
            inputAlert.setTitle("Münzen löschen");
            inputAlert.setMessage("Bist du dir sicher, dass du die Daten löschen willst?");
            inputAlert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    wipeData();

                }
            });
            inputAlert.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = inputAlert.create();
            alertDialog.show();


            return true;
        });
        menuItem = menu.add("Log");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(item -> {
            try {
                onLogAction();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        });
        return true;
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addMonitorNotifier(new MonitorNotifier() {

            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "I just saw a beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see a beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region(appUuid, null, null, null));
        } catch (RemoteException e) {

        }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    int i = 1;
                    for (Iterator<Beacon> iter = beacons.iterator(); iter.hasNext(); ) {
                        Beacon b = iter.next();

                        //FOR DEBUG
                        Log.i(TAG, String.valueOf(b.getBluetoothName()));
                        Log.i(TAG, "UUID: " + b.getId1());
                        Log.i(TAG, "Major: " + b.getId2());
                        Log.i(TAG, "Minor: " + b.getId3());
                        Log.i(TAG, "TX Power: " + b.getTxPower());
                        Log.i(TAG, "Approx Distance: " + b.getDistance() + " meters");

                        if (Integer.parseInt(b.getId2().toString()) <= 15 && Integer.parseInt(b.getId2().toString()) >= 1
                                && b.getId1().toString().toUpperCase().equals(appUuid)) {
                            collectBeacon(b);

                        }
                        i++;
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region(appUuid, null, null, null));
        } catch (RemoteException e) {

        }

    }

    private void onLogAction() throws JSONException {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject json = coinManager.logJson();

        intent.putExtra("ch.appquest.logmessage", json.toString());

        startActivity(intent);

    }

    private void collectBeacon(Beacon beacon) {
        int major = beacon.getId2().toInt();
        int minor = beacon.getId3().toInt();
        updateCoin(major, minor);
    }

    private void updateCoin(int major, int minor) {

        if (!foundBeaconsMajor.contains(major)) {
            foundBeaconsMajor.add(major);
            coinManager.addToList(new Coin(major, minor));
            saveData();
            notificationUtil.sendNotificationToUser("Münz Nr. " + major + " gefunden!", "Region: " +
                    RegionIdentifier.getRegionName(major), major);
        }

        updateInterface();
    }

    private void updateInterface() {

        List<View> lv = getAllChildrenBFS(ll);

        for (View v : lv) {

            if (v instanceof ImageView) {

                int coinId = Integer.parseInt((String) v.getTag());

                for (int i = 0; i < foundBeaconsMajor.size(); i++) {

                    ImageView foundImg = (ImageView) v;
                    if (coinId == foundBeaconsMajor.get(i)) {

                        foundImg.setImageResource(RegionIdentifier.GetRegionPhotoFile(coinId));
                    }
                }


            }
        }

    }

    private List<View> getAllChildrenBFS(View v) {
        List<View> visited = new ArrayList<>();
        List<View> unvisited = new ArrayList<>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);
            if (!(child instanceof ViewGroup)) continue;
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) unvisited.add(group.getChildAt(i));
        }

        return visited;
    }

    public void saveData() {

        Gson gson = new Gson();

        String json = gson.toJson(coinManager);

        editor.clear();
        editor.putString("coinManager", json);
        editor.commit();

    }

    private void restoreData() {

        Gson gson = new Gson();
        String json = localPrefs.getString("coinManager", null);

        if (json != null && !json.isEmpty()) {

            coinManager = gson.fromJson(json, CoinManager.class);

            int[] foundCoinsId = coinManager.retrieveFoundIds();

            for (int i = 0; i < foundCoinsId.length; i++) {

                foundBeaconsMajor.add(foundCoinsId[i]);
            }

            updateInterface();
        }

    }

    private void wipeData() {

        coinManager = new CoinManager();
        foundBeaconsMajor.clear();

        List<View> allViews = getAllChildrenBFS(ll);

        for (View iv : allViews) {

            if (iv instanceof ImageView) {
                ((ImageView) iv).setImageResource(R.drawable.sample_coin);
            }

        }

        editor.clear();
        editor.putString("coinManager", "");
        editor.commit();

    }


}
