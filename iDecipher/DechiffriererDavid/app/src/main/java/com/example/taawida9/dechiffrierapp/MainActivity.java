package com.example.taawida9.dechiffrierapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int Request_Image_Capture = 1;
    static final int REQUEST_WRITE_STORAGE_AND_CAMERA = 2;
    ImageView imageView;
    float angle = 90;
    Matrix matrix = new Matrix();


    String mCurrentPhotoPath;
    Uri contentUri;

    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasCameraPermission() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_AND_CAMERA); // your request code
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasCameraPermission() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        matrix.postRotate(angle);

        //imageView.setScaleType(ImageView.ScaleType.MATRIX);


        Button button = findViewById(R.id.button2);
        requestAppPermissions();
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openCameraIntent();
                galleryAddPic();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_WRITE_STORAGE_AND_CAMERA && resultCode == RESULT_OK) {
            Bitmap preview = readImageFile(contentUri);
            Bitmap spinnedPreview = Bitmap.createBitmap(preview, 0, 0, preview.getWidth(), preview.getHeight(), matrix, true);
            imageView.setImageBitmap(spinnedPreview);
        }
    }

    private void openCameraIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception e) {
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.taawida9.dechiffrierapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_WRITE_STORAGE_AND_CAMERA);
            }
        }
    }

    private File createImageFile() throws Exception {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
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

    private void galleryAddPic() {

        Intent mediaScantIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        contentUri = Uri.fromFile(f);
        mediaScantIntent.setData(contentUri);
        this.sendBroadcast(mediaScantIntent);
        readImageFile(contentUri);
    }

    private Bitmap readImageFile(Uri imageUri) {

        File file = new File(imageUri.getPath());
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(is);


            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e("Decoder", "Could not find image file", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private Bitmap applyFilter(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];

         bitmap.getPixels(data, 0, width, 0, 0, width, height);

        return Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
    }
    

}