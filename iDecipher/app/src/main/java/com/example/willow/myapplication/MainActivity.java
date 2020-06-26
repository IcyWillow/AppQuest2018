package com.example.willow.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnCamera;
    private ImageView pctImage;
    private Context context = this;
    private Intent viewPicture;
    private String channel = "red";
    private Matrix m = new Matrix();
    private File decipheredFile;
    private String mCurrentPhotoPath;
    private Bitmap pictureToDecipher;

    private static final int REQUEST_CAPTURE_IMAGE = 100;
    static final int ALL_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Objekte Instanzieren
        btnCamera = findViewById(R.id.btnCamera);
        pctImage = findViewById(R.id.pctHidden);

        //Matrix vorbereiten, um Bild zu drehen
        m.postRotate(90);

        //Berechtigungen verlangen
        checkAndRequestPermissions(true);

        //Auf "Foto aufnehmen" drücken -> überprüfen, ob Berechtigungen erteilt wurden und wenn ja Kamera starten
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!checkAndRequestPermissions(false)){
                    displayErrorMessage("Eine oder mehrere Berechtigung(en) fehlen. Die Applikation wird neu gestartet. Bitte geben Sie die nötigen Berechtigungen.", context);
                } else {

                    openCameraIntent();
                }
            }
        });

        //Auf Bild drücken -> Bild in einer neuer Activity zeigen
        pctImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPicture != null){
                    startActivity(viewPicture);
                } else {
                    Toast toast = Toast.makeText(context, "Fotografieren Sie irgendwas zuerst.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    //Radion Button, um Farbkanal auszuwählen
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radio_red:
                if (checked)
              channel = "red";
                    break;
            case R.id.radio_green:
                if (checked)
                    channel = "green";
                    break;
            case R.id.radio_blue:
                if (checked)
                    channel = "blue";
                    break;
        }
    }

    //Berechtigungen erteilen
    private  boolean checkAndRequestPermissions(boolean RequestPermissionsToo) {
        int permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            if (RequestPermissionsToo == true){
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),ALL_PERMISSIONS);
            }

            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);
                inputAlert.setTitle("Code eingeben");
                inputAlert.setMessage("Geben Sie den Geheimcode ein.");
                final EditText userInput = new EditText(context);
                inputAlert.setView(userInput);
                inputAlert.setPositiveButton("Eingeben", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String secretCode = userInput.getText().toString();
                        log(secretCode);

                    }
                });
                inputAlert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = inputAlert.create();
                alertDialog.show();

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //Bild mit Kamera-API aufnehmen
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile(false);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.willow.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    //Verzeichnis fürs aufgenomme Bild definieren und Name fürs Bilddatei vergeben
    private File createImageFile(boolean isDeciphered) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName;
        if(isDeciphered){
            imageFileName = "JPEG_DECIPHERED" + timeStamp + "_";
        } else {
            imageFileName = "JPEG_" + timeStamp + "_";
        }

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Bitmap lesen
    private Bitmap readImageFile(Uri imageUri) {
        File file = new File(imageUri.getPath());
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e("DECODER", "Could not find image file", e);
            return null;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    //Bitmap bearbeiten
    private Bitmap applyFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];
        int colorChannel = 0;

        bitmap.getPixels(data, 0, width, 0, 0, width, height);

        switch (channel){
            case "red":
                colorChannel = 16;
                break;
            case "green":
                colorChannel = 8;
                break;
            case "blue":
                colorChannel = 0;
                break;
        }

        // Hier werden die Pixel im data-array bearbeitet und
        // anschliessend damit ein neues Bitmap erstellt werden
        for (int i = 0; i < data.length; i++){
            int a = (data[i]>>24)&0xff;
            int channel = (data[i]>>colorChannel)&0xff;
            data[i] = (a<<24) | (channel<<colorChannel) | 0;

        }

        return Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {

            File imageFile = null;
            OutputStream os;
            pictureToDecipher = readImageFile(Uri.parse(mCurrentPhotoPath));

            //TEST Bild aus der Webseite der Aufgabe
            //pictureToDecipher = readImageFile(Uri.parse("/storage/emulated/0/Android/data/com.example.willow.myapplication/files/Pictures/beispielbild.jpg"));

            //Bild mithilfe der Matrix drehen
            pictureToDecipher = Bitmap.createBitmap(pictureToDecipher, 0, 0, pictureToDecipher.getWidth(), pictureToDecipher.getHeight(), m, true);

            //Farbkanal extrahieren
            Bitmap decipheredPicture = applyFilter(pictureToDecipher);

            //Extrahiertes Bilddatei schreiben
            try {
                imageFile = createImageFile(true);
                os = new FileOutputStream(imageFile);
                decipheredPicture.compress(Bitmap.CompressFormat.JPEG, 100, os);
                decipheredFile = imageFile;
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }

            //View, um das extrahiertes Bild anzuzeigen, vorbereiten
            viewPicture = new Intent(Intent.ACTION_VIEW)//
                    .setDataAndType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                                    android.support.v4.content.FileProvider.getUriForFile(this,"com.example.willow.provider", decipheredFile) : Uri.fromFile(decipheredFile),
                            "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //Image View mit kleiner Preview des extrahierten Bild zuweisen
            pctImage.setImageBitmap(decipheredPicture);
        }
    }

    private void log(String Loesungswort) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        // Achtung, je nach App wird etwas anderes eingetragen
        String logmessage = "{\n" +
                "  \"task\": \"Dechiffrierer\",\n" +
                "  \"solution\": " + '"' + Loesungswort + '"' +"\n" +
                "}";
        intent.putExtra("ch.appquest.logmessage", logmessage);

        startActivity(intent);
    }

    //Fehler zeigen, falls Berechtigungen nicht erteilt wurden
    public static void displayErrorMessage(String message, final Context context) {
        // display error message
        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);
        inputAlert.setTitle("Fehler");
        inputAlert.setMessage(message);

        inputAlert.setNeutralButton("Neu starten", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doRestart(context);
            }
        });

        AlertDialog alertDialog = inputAlert.create();
        alertDialog.show();
    }


    //App neustart, wenn Berechtigungen nicht erteilt sind.
    public static void doRestart(Context c) {
        String TAG = "Restart";
        String newStartMessage = "Die Applikation konnte nicht neugestartet werden. Bitte starten Sie manuell neu.";
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e(TAG, newStartMessage + " (mStartActivity NULL)");
                    }
                } else {
                    Log.e(TAG, newStartMessage + " (PM NULL)");
                }
            } else {
                Log.e(TAG, newStartMessage + " (CONTEXT NULL)");
            }
        } catch (Exception ex) {
            Log.e(TAG, newStartMessage);
        }
    }
}
