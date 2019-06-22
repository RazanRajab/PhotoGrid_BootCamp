package com.example.photogrid_bootcamp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photogrid_bootcamp.CatApi.PhotoResponse;
import com.example.photogrid_bootcamp.CatApi.PhotoService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0;
    private Uri outputFileUri;
    private GridLayout mainGrid;
    private ImageView imageView;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainGrid = findViewById(R.id.mainGrid);
        dialog = new Dialog(this);
        setSingleEvent(mainGrid);
    }

    private void setSingleEvent(final GridLayout mainGrid) {

        dialog.setContentView(R.layout.pop_up_dialog);
        Button Gallery = dialog.findViewById(R.id.gallery);
        Button Camera = dialog.findViewById(R.id.camera);
        Button Download = dialog.findViewById(R.id.download);
        TextView exit = dialog.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                gallery();
            }
        });
        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                checkCameraPermission();
            }
        });
        Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                download();
            }
        });
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            ImageView m = (ImageView) mainGrid.getChildAt(i);
            m.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageView = (ImageView) view;
                    dialog.show();
                }
            });
            m.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(""), "image/*");
                    MainActivity.this.startActivity(intent);
                    return false;
                }
            });
        }
    }

            @Override
            protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data)
            {
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    imageView.setImageURI(selectedImage);
                }
                else if(requestCode== REQUEST_IMAGE_CAPTURE && resultCode== RESULT_OK && data!=null){
                    Bundle Extras=data.getExtras();
                    Bitmap imgBitmap=(Bitmap)Extras.get("data");
                    imageView.setImageBitmap(imgBitmap);
                }
            }

            private void gallery () {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }

            private void checkCameraPermission () {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST_CODE);
                }
                else{
                    camera();
                }

            }

    private void camera() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    camera();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void download () {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.thecatapi.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                //api   G36y0EaJl2m6dutPOdnjKlI3yt2DZH2o
                PhotoService service = retrofit.create(PhotoService.class);
                //Run the Request
                Callback<List<PhotoResponse>> call=new Callback<List<PhotoResponse>>() {
                    @Override
                    public void onResponse(Call<List<PhotoResponse>> call, Response<List<PhotoResponse>> response) {

                        if (response.body() != null) {

                            Toast.makeText(MainActivity.this,"response"+response.body(),Toast.LENGTH_LONG).show();
                            String url=response.body().get(0).getUrl();
                            Picasso.with(MainActivity.this).load(url).into(imageView);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PhotoResponse>> call, Throwable t) {

                    }
                };
                service.get("5efb6db6-a47e-4ea1-95a0-fa69f31b94e2")
                        .enqueue(call);
            }

            private void selectImage () {
                final String fname = "img_" + System.currentTimeMillis() + ".jpg";
                final File sdImageMainDirectory = new File
                        (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fname);
                outputFileUri = Uri.fromFile(sdImageMainDirectory);

// Camera.
                final List<Intent> cameraIntents = new ArrayList<Intent>();
                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getPackageManager();
                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);

                for (ResolveInfo res : listCam) {
                    final String packageName = res.activityInfo.packageName;
                    final Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    cameraIntents.add(intent);
                }

// Filesystem.
                final Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");    // Show only images, no videos or anything else
                galleryIntent.setAction(Intent.ACTION_PICK);

// Choose of filesystem options.
// Always show the chooser (if there are multiple options available)
                final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

                // Add the camera options.
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

                startActivityForResult(chooserIntent, 1);
            }
        }

