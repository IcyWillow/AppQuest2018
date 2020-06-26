package com.example.willow.metaldetector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;




public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private TextView txt;
    private Button btnLog;
    private MediaPlayer mp1;
    private ProgressBar pb;
    private SensorManager sensorManager;
    private Sensor magneticSensor;
    private static final int SCAN_QR_CODE_REQUEST_CODE = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp1 = MediaPlayer.create(this, R.raw.gemidao);

        txt = (TextView)findViewById(R.id.txtMagnetic);
        pb = findViewById(R.id.progressBar);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);



        btnLog = findViewById(R.id.btnLog);
        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mp1.start();


            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String logMsg = intent.getStringExtra("SCAN_RESULT");
                log(logMsg);
            }
        }
    }



    private void log(String qrCode) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        // Achtung, je nach App wird etwas anderes eingetragen
        String logmessage = "{\n" +
                "  \"task\": \"Metalldetektor\",\n" +
                "  \"solution\": " + '"' + qrCode + '"' +"\n" +
                "}";
        intent.putExtra("ch.appquest.logmessage", logmessage);

        startActivity(intent);
    }




    protected void onResume() {

        super.onResume();
        sensorManager.registerListener(this, magneticSensor, sensorManager.SENSOR_DELAY_NORMAL);



    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mag = event.values;
        double betrag = Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1] + mag[2] * mag[2]);
       txt.setText(String.valueOf(betrag));
       pb.setProgress((int)betrag);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
