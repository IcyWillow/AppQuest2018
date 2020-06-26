package willow.imemory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import willow.memory.R;

public class MainActivity extends Activity {

    static final int REQUEST_WRITE_STORAGE_AND_CAMERA = 2;

    private SharedPreferences localPrefs;
    private SharedPreferences.Editor editor;
    private Context context;
    private int rowsCount;

    private Bitmap defaultBmp;
    private ImageView img1;
    private ImageView img2;
    private TextView txt1;
    private TextView txt2;
    private RelativeLayout rl;
    private LinearLayout ll;



    private Bitmap copyBitmap;
    private String copyTxt;
    CardView cdvTwins;


    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private ImageView toUpdateImg;
    private TextView toUpdateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        requestAppPermissions();
        context = this;
        localPrefs = this.getPreferences(MODE_PRIVATE);


        new JSONTask().execute();


        editor = localPrefs.edit();
        Button btnNewTwins = findViewById(R.id.btnNewTwins);
        cdvTwins = findViewById(R.id.cdvTwins);
        txt1 = findViewById(R.id.word1);
        txt2 = findViewById(R.id.word2);
        img1 = findViewById(R.id.picture1);
        img2 = findViewById(R.id.picture2);

        Drawable drawable = this.getResources().getDrawable(R.drawable.placeholder);
        defaultBmp = ((BitmapDrawable) drawable).getBitmap();
        img1.setImageBitmap(defaultBmp);
        img2.setImageBitmap(defaultBmp);
        ll = findViewById(R.id.ll);
        rl = findViewById(R.id.rl);

        txt1.setTextColor(Color.WHITE);
        txt2.setTextColor(Color.WHITE);


        btnNewTwins.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newTwins(null);
                rowsCount++;
                showRowsCount();


            }
        });

        img1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takeQrCodePicture(img1, txt1);

            }
        });


        img2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takeQrCodePicture(img2, txt2);
            }
        });

        recoverCards();

        List<View> views = getAllChildrenBFS(ll);

        for(int i = 0; i < views.size(); i++){

            if(views.get(i) instanceof RelativeLayout){
                rowsCount++;
            }
        }

        super.onCreate(savedInstanceState);
    }

    private void showRowsCount(){
        Context context = getApplicationContext();
        CharSequence text;

        switch (rowsCount){
            case 0:
                text = "Du hast keine einzige Reihe.";
                break;
            case 1:
                text = "Du hast eine Reihe.";
                break;
            default:
                text = "Du hast ingesamt " + rowsCount + " Reihen.";
                break;
        }


        int duration = Toast.LENGTH_LONG;


        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }



    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasCameraPermission() && hasWritePermissions() && hasNetworkStatePermission() && hasInternetPermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                }, REQUEST_WRITE_STORAGE_AND_CAMERA); // your request code
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasInternetPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasNetworkStatePermission() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED);
    }


    private boolean hasCameraPermission() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

              List<String> qrCodes = extractMemoryWords();
              String[] memory = new String[qrCodes.size()];
              memory = qrCodes.toArray(memory);

               log(memory);

                return false;
            }
        });




        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE
                && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            String path = extras.getString(
                    Intents.Scan.RESULT_BARCODE_IMAGE_PATH);

            Bitmap bmp = BitmapFactory.decodeFile(path);

            String code = extras.getString(
                    Intents.Scan.RESULT);

            toUpdateImg.setImageBitmap(bmp);
            toUpdateText.setText(code);
            toUpdateImg.setOnClickListener(null);
            setMethodOnPicture(toUpdateImg, toUpdateText);


            saveCards();



        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.setHeaderTitle("Wähle die Aktion aus:");
    }



      public void saveCards(){

          Gson gson = new Gson();

          Set<String> gsonList = new HashSet<>();
          List<String> encodedBitmaps = new ArrayList<>();
          List<Bitmap> bitmaps = new ArrayList<>();
          List<String> qrCodes = new ArrayList<>();

          for(int i = 0; i < ll.getChildCount(); ++i) {
              View nextChild = ll.getChildAt(i);

              if (nextChild instanceof CardView) {
                  List<View> cardViewChildren = getAllChildrenBFS(nextChild);

                  for (int j = 0; j < cardViewChildren.size(); j++) {
                      nextChild = cardViewChildren.get(j);


                      if (nextChild instanceof ImageView) {
                          ImageView foundImage = (ImageView) nextChild;
                          Bitmap pic = ((BitmapDrawable) foundImage.getDrawable()).getBitmap();
                          bitmaps.add(pic);

                      }

                      if (nextChild instanceof TextView) {
                          TextView foundTextView = (TextView) nextChild;
                          String text = foundTextView.getText().toString();
                          qrCodes.add(text);

                      }

                      if(qrCodes.size() >= 2 && bitmaps.size() >= 2){

                          if(qrCodes.get(0).length() > 0 || qrCodes.get(1).length() > 0){

                              ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                              ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

                              bitmaps.get(0).compress(Bitmap.CompressFormat.PNG, 100, baos1);
                              byte[] b1 = baos1.toByteArray();
                              encodedBitmaps.add(Base64.encodeToString(b1, Base64.DEFAULT));

                              bitmaps.get(1).compress(Bitmap.CompressFormat.PNG, 100, baos2);
                              byte[] b2 = baos2.toByteArray();
                              encodedBitmaps.add(Base64.encodeToString(b2, Base64.DEFAULT));
                              qrCodes.add(qrCodes.get(0));
                              qrCodes.add(qrCodes.get(1));

                              MemoryCard card = new MemoryCard(qrCodes.get(0), qrCodes.get(1)
                                      , Base64.encodeToString(b1, Base64.DEFAULT),
                                      Base64.encodeToString(b2, Base64.DEFAULT));

                              String json = gson.toJson(card);
                              gsonList.add(json);


                          }

                          qrCodes.clear();
                          bitmaps.clear();

                      }

                  }

              }

          }

          editor.clear();
          editor.putStringSet("memoryCards", gsonList);
          editor.commit();
      }


      public void takeQrCodePicture(ImageView img, TextView txt) {
          IntentIntegrator integrator = new IntentIntegrator(this);
          integrator.setCaptureActivity(MyCaptureActivity.class);
          integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
          integrator.setOrientationLocked(false);
          integrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
          integrator.initiateScan();

          toUpdateImg = img;
          toUpdateText = txt;

      }

      private List<View> getAllChildrenBFS(View v) {
          List<View> visited = new ArrayList<View>();
          List<View> unvisited = new ArrayList<View>();
          unvisited.add(v);

          while (!unvisited.isEmpty()) {
              View child = unvisited.remove(0);
              visited.add(child);
              if (!(child instanceof ViewGroup)) continue;
              ViewGroup group = (ViewGroup) child;
              final int childCount = group.getChildCount();
              for (int i=0; i<childCount; i++) unvisited.add(group.getChildAt(i));
          }

          return visited;
      }

      private void log(String[] memoryWords) {
          Intent intent = new Intent("ch.appquest.intent.LOG");

          if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
              Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
              return;
          }

          String arr = "[";
          String komma = ", ";

          for(int i = 0; i < memoryWords.length; i++){

              if(i == memoryWords.length - 1){
                  komma = "";
              }

              arr += memoryWords[i] + komma;

          }

          arr += "]";

          // Achtung, je nach App wird etwas anderes eingetragen
          String logmessage = "{" +
                  "  \"task\": \"Memory\"," +
                  "  \"solution\": " +  arr + "" +
                  "}";

          intent.putExtra("ch.appquest.logmessage", logmessage);

          startActivity(intent);
      }

      public void recoverCards(){

          Gson gson = new Gson();
          Set<String> gsonSet = localPrefs.getStringSet("memoryCards", null);
          List<String> gsonSetString = new ArrayList<>();

          try{
              MemoryCard[] cards = new MemoryCard[gsonSet.size()];
              gsonSetString.addAll(gsonSet);


              for(int i = 0; i < gsonSet.size(); i++){
                  cards[i] = gson.fromJson(gsonSetString.get(i), MemoryCard.class);
              }

              for(int i = 0; i < cards.length; i++){

                  if(cards[i].cardText1.length() < 1 & cards[i].cardText2.length() < 1 ){

                  } else {
                      newTwins(cards[i]);
                  }


              }
          }catch (Exception e){

          } finally {

          }
      }

      private void newTwins(@Nullable MemoryCard mc){


          LinearLayout ll = findViewById(R.id.ll);
          int firstId = generateViewId();
          int secondId = generateViewId();

          RelativeLayout rl = new RelativeLayout(this);
          rl.setLayoutParams(this.rl.getLayoutParams());

          final ImageView picture1 = new ImageView(this);
          final ImageView picture2 = new ImageView(this);
          picture1.setLayoutParams(img1.getLayoutParams());
          picture2.setLayoutParams(img2.getLayoutParams());
          picture1.setId(firstId);
          picture2.setId(secondId);

          picture1.setImageBitmap(defaultBmp);
          picture2.setImageBitmap(defaultBmp);

          final TextView text1 = new TextView(this);
          final TextView text2 = new TextView(this);

          text1.setBackground(txt1.getBackground());
          text2.setBackground(txt2.getBackground());

          text1.setGravity(Gravity.CENTER);
          text2.setGravity(Gravity.CENTER);

          text1.setTextColor(Color.WHITE);
          text2.setTextColor(Color.WHITE);

          RelativeLayout.LayoutParams paramsTxt1 = new RelativeLayout.LayoutParams(txt1.getLayoutParams());
          RelativeLayout.LayoutParams paramsTxt2 = new RelativeLayout.LayoutParams(txt2.getLayoutParams());
          paramsTxt1.addRule(RelativeLayout.BELOW, firstId);
          paramsTxt2.addRule(RelativeLayout.BELOW, secondId);
          paramsTxt2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

          text1.setLayoutParams(paramsTxt1);
          text2.setLayoutParams(paramsTxt2);

          text1.setText("");
          text2.setText("");

          //CardView

          CardView crd = new CardView(this);
          crd.setLayoutParams(cdvTwins.getLayoutParams());

          if(mc != null){

              text1.setText(mc.cardText1);
              text2.setText(mc.cardText2);
              byte[] image1AsBytes = Base64.decode(mc.cardBitmap1.getBytes(), Base64.DEFAULT);
              byte[] image2AsBytes = Base64.decode(mc.cardBitmap2.getBytes(), Base64.DEFAULT);

              Bitmap b1 = BitmapFactory.decodeByteArray(image1AsBytes, 0, image1AsBytes.length);
              Bitmap b2 = BitmapFactory.decodeByteArray(image2AsBytes, 0, image2AsBytes.length);

              picture1.setImageBitmap(b1);
              picture2.setImageBitmap(b2);

          }

          setMethodOnPicture(picture1, text1);

          setMethodOnPicture(picture2, text2);


          //ImageView
          rl.addView(picture1);
          rl.addView(picture2);
          rl.addView(text1);
          rl.addView(text2);
          crd.addView(rl);
          ll.addView(crd);

      }

      public void setMethodOnPicture(ImageView iv, final TextView txtView){

          final ImageView imageview = iv;



          if(txtView.getText().length() < 1){
              iv.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      takeQrCodePicture(imageview, txtView);


                  }
              });
          }


              registerForContextMenu(iv);
              iv.setOnLongClickListener(new View.OnLongClickListener() {
                  public boolean onLongClick(View v) {
                      toUpdateImg = (ImageView)v;
                      toUpdateText = txtView;
                      openContextMenu(v);

                      return true;
                  }
              });



      }

      public List<String> extractMemoryWords()
      {
          List<String> qrCodes = new ArrayList<>();
          List<String> twinsChecker = new ArrayList<>();

          for(int i = 0; i < ll.getChildCount(); ++i) {
              View nextChild = ll.getChildAt(i);

              if (nextChild instanceof CardView) {
                  CardView cardContent = (CardView)nextChild;

                  for (int j = 0; j < cardContent.getChildCount(); j++) {
                      nextChild = (cardContent).getChildAt(j);

                      if (nextChild instanceof RelativeLayout) {
                          twinsChecker.clear();
                          RelativeLayout relativeLayoutContent = (RelativeLayout) nextChild;

                          for (int l = 0; l < relativeLayoutContent.getChildCount(); l++) {
                              nextChild = relativeLayoutContent.getChildAt(l);

                              if (nextChild instanceof TextView) {
                                  TextView foundTextView = (TextView) nextChild;
                                  String text = foundTextView.getText().toString();

                                  if(text != ""){
                                      twinsChecker.add(text);
                                  }

                              }

                          }

                          if(twinsChecker.size() == 2 && twinsChecker.get(0).length() > 0 && twinsChecker.get(1).length() > 0){
                              qrCodes.add("[\"" + twinsChecker.get(0) + "\",\"" + twinsChecker.get(1) + "\"]");
                          }
                      }
                  }
              }
          }

          return qrCodes;

      }

      public boolean onContextItemSelected(MenuItem item){

          switch (item.getItemId()){
              case R.id.picture:
                  takeQrCodePicture(toUpdateImg, toUpdateText);
                   return true;
              case R.id.pick:
                  showPicture(toUpdateImg);
                  return true;
              case R.id.copy:
                  copyBitmap = ((BitmapDrawable) toUpdateImg.getDrawable()).getBitmap();
                  copyTxt = toUpdateText.getText().toString();
                  return true;
              case R.id.paste:
                  setPicture();
                  return true;
              case R.id.remove:
                deleteRow();
                  return true;
                  default:
                      return false;
          }
        }


        public void setPicture(){

        toUpdateImg.setImageBitmap(copyBitmap);
        toUpdateText.setText(copyTxt);


        }

      public void deleteRow(){

          final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);
          inputAlert.setTitle("Reihe löschen?");
          inputAlert.setMessage("Bist du dir sicher, dass du diese Reihe löschen willst?");
          inputAlert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                  ViewParent box = toUpdateImg.getParent();
                  box = box.getParent();
                  ll.removeView((View)box);
                  rowsCount--;
                  showRowsCount();

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
      }

      public static int generateViewId() {
          for (; ; ) {
              final int result = sNextGeneratedId.get();
              int newValue = result + 1;
              if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
              if (sNextGeneratedId.compareAndSet(result, newValue)) {
                  return result;
              }
          }

      }

      private void showPicture(ImageView iv){

          OutputStream os;
          File imageFile = null;
          Bitmap bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();

          try{
              imageFile = createImageFile();
              os = new FileOutputStream(imageFile);
              bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
              os.flush();
              os.close();
          } catch (Exception e){

          }

          //Abrir novo intent com Uri
          //View, um das extrahiertes Bild anzuzeigen, vorbereiten
          Intent viewPicture = new Intent(Intent.ACTION_VIEW)//
                  .setDataAndType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                                  android.support.v4.content.FileProvider.getUriForFile(this,"willow.provider", imageFile) : Uri.fromFile(imageFile),
                          "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

          startActivity(viewPicture);

      }

      private File createImageFile() throws IOException {
          // Create an image file name
          String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
          String imageFileName = "JPEG_" + timeStamp + "_";


          File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
          File image = File.createTempFile(
                  imageFileName,
                  ".jpg",
                  storageDir
          );

          return image;
      }




    private class JSONTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            System.out.println("Select Records Example by using the Prepared Statement!");
            Connection con = null;


            int count = 0;
            String result = "";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://68.66.251.6:3306/vinicius_appquest2018", "vinic_vinilodeon", "150891vini*");
                //jdbc:jtds:mysql://68.66.251.6:3306/vinicius_appquest2018", "vinic_vinilodeon", "150891vini*
                try {
                    String sql;
                    //	  sql
                    //	  = "SELECT title,year_made FROM movies WHERE year_made >= ? AND year_made <= ?";
                    sql
                            = "SELECT MemoryCard FROM iMemory";
                    PreparedStatement prest = con.prepareStatement(sql);

                    ResultSet rs = prest.executeQuery();
                    while (rs.next()) {
                        result = rs.getString(1);
                        count++;
                    }
                    System.out.println("Number of records: " + count);
                    prest.close();
                    con.close();
                } catch (SQLException s) {
                    System.out.println("SQL statement is not executed!");


                }
            } catch (Exception e) {
                e.printStackTrace();

            }

            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("result: " + result);
            try {
                if (result.equals("1")) {
                   // numberOfRemainingLoginAttempts=3;
                   // Intent i = new Intent(getApplicationContext(), Main2Activity.class);
                    //i.putExtra("username", username.getText().toString());
                    //i.putExtra("json",result.split("-")[2]);
                    //startActivity(i);

                    Toast.makeText(getApplicationContext(), "Hello admin!", Toast.LENGTH_SHORT).show();
//                    label.setVisibility(View.VISIBLE);
//                    label.setText(result);

                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
        }
    }






    public static class MyCaptureActivity extends CaptureActivity {}
}
