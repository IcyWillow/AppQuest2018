package willow.imorseencoder;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    EditText txt;
    Button btn;
    String code = "";
    static public Context context;
    int i = 0;
    static List<Primitive> sandra = new ArrayList<>();
    static public ImageView imgMorse;
    String TAG = "Carne";
    static boolean isWhite = true;

    Handler mHandler;

    MorseEncoder morseEncoder = new MorseEncoder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        imgMorse = findViewById(R.id.imgMorse);
        context = this;
        txt = findViewById(R.id.txtMessage);
        btn = findViewById(R.id.btnAction);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = txt.getText().toString();

                try {

                    sandra = morseEncoder.textToCode(code.toUpperCase());


                } catch (Exception e) {

                }
                action();
            }
        });


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


    private void morse(List<Primitive> morsecode) {

        changeViewColor();

    }

    private void action() {

        txt.setVisibility(View.INVISIBLE);
        btn.setVisibility(View.INVISIBLE);
        int totalMs = 0;
        int charMs = 0;
        Handler h = new Handler();

        for (int i = 0; i < sandra.size(); i++) {
            String morsechar = sandra.get(i).getTextRepresentation();

            switch (morsechar) {
                case "·":
                    //Fica branco, espera 500ms
                    h.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            makeViewWhite();
                            Log.d(TAG, Calendar.getInstance().getTime().toString());
                        }
                    }, SystemClock.uptimeMillis() + totalMs);
                    break;
                case "−":
                    //Fica branco, espera 1500ms
                    h.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            makeViewWhite();
                            Log.d(TAG, Calendar.getInstance().getTime().toString());
                        }
                    }, SystemClock.uptimeMillis() + totalMs);
                    break;
                case " ":
                    //Fica preto, espera 500ms
                    h.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            makeViewBlack();
                            Log.d(TAG, Calendar.getInstance().getTime().toString());
                        }
                    }, SystemClock.uptimeMillis() + totalMs);
                    break;
                case "   ":
                    //Fica preto, espera 1500ms
                    h.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            makeViewBlack();
                            Log.d(TAG, Calendar.getInstance().getTime().toString());
                        }
                    }, SystemClock.uptimeMillis() + totalMs);
                    break;
                case " / ":
                    //Fica preto, espera 3500ms

                    h.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            makeViewBlack();
                            Log.d(TAG, Calendar.getInstance().getTime().toString());
                        }
                    }, SystemClock.uptimeMillis() + totalMs);

                    break;
            }

            if (i == sandra.size() - 1){
                h.postAtTime(new Runnable() {
                    @Override
                    public void run() {
                        makeViewBlack();
                        btn.setVisibility(View.VISIBLE);
                        txt.setVisibility(View.VISIBLE);
                    }
                }, SystemClock.uptimeMillis() + totalMs + charMs);
            }

            charMs = sandra.get(i).getSignalLengthInDits() * 500;
            totalMs += charMs;

        }

        }


    static public void makeViewWhite() {

        imgMorse.setBackgroundColor(Color.WHITE);

    }

    static public void makeViewBlue() {

        imgMorse.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));

    }

    static public void makeViewBlack() {
        imgMorse.setBackgroundColor(Color.BLACK);
    }

    private void changeViewColor() {

        if (isWhite) {
            makeViewWhite();
        } else {
            makeViewBlack();
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
                "  \"task\": \"Morseencoder\",\n" +
                "  \"solution\": " + '"' + Loesungswort + '"' + "\n" +
                "}";
        intent.putExtra("ch.appquest.logmessage", logmessage);

        startActivity(intent);
    }
}



