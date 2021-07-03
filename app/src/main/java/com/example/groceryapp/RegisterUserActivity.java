package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {
    private ImageButton imgback;
    private ImageView gpsbtn,profile;
    private EditText edName,edPhone,edCountry,edCity,edState,edAddress,edEmail,edPassword,edCpPassword;
    private Button btnRegister;
    private TextView tvRegiter;
//    CircularImageView profile;
    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permissson arays
    private String[] locationPermisson;
    private String[] cameraPermisson;
    private String[] storagePermisson;

    private Uri imageUri;

    private double latidute, longitude;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        imgback=findViewById(R.id.btnback);
        profile=findViewById(R.id.Improfile);
        edName=findViewById(R.id.edName);
        edPhone=findViewById(R.id.edPhone);
        edCountry=findViewById(R.id.edCountry);
        edCity=findViewById(R.id.edCity);
        edState=findViewById(R.id.edstate);
        edAddress=findViewById(R.id.edAddress);
        edEmail=findViewById(R.id.edEmail);
        edPassword=findViewById(R.id.edPassword);
        edCpPassword=findViewById(R.id.edCpPassword);
        tvRegiter=findViewById(R.id.resgisterSellerTv);
        btnRegister=findViewById(R.id.btnRegiter);
        gpsbtn=findViewById(R.id.btngps);

        //initr permission array
        locationPermisson = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermisson = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        gpsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocationPermission()) {
                    //allow
                    detecLocation();
                } else {
                    requestLocationPermisson();
                }
            }
        });
        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image
//                showImagePickDialog();
                imageChooser();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //regiter

            }
        });

        tvRegiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open seller activity
                Intent intent=new Intent(RegisterUserActivity.this,ResgisterSellerActivity.class);
                startActivity(intent);

            }
        });
    }
    //


    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"),IMAGE_PICK_GALLERY_CODE);
    }

    private void showImagePickDialog() {
        String[] options={"Camera","Gallery"};
        //doalog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //hanle clicks
                if(which==0){
                    //camera click
                    if(checkCameraPermisson()){
                        //camrea permisson allowed
                        pickFromCamera();
                    }
                    else {
                        //not alloew
                        requestCameraPermisson();
                    }
                }
                else {

                    if(checkStoragePermisson()){
                        //storage permisson allowed
                        pickFromGallery();
                    }
                    else {
                        //not alloew
                        requestStoregePermisson();
                    }
                }
            }
        }).show();
    }
    private void pickFromGallery(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"),200);

//        Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_desc Desctription");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }
    //
    private void detecLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider callingstartActivityForResult
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void finAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder=new Geocoder(this, Locale.getDefault());
        try {
            addresses=geocoder.getFromLocation(latidute,longitude,1);

            String address=addresses.get(0).getAddressLine(0);//complete addres
            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();

            //set address
            edCity.setText(city);
            edState.setText(state);
            edCountry.setText(country);
            edAddress.setText(address);
            Toast.makeText(this,"city"+city+"country"+country,Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkLocationPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private void requestLocationPermisson(){
        ActivityCompat.requestPermissions(this,locationPermisson,LOCATION_REQUEST_CODE);
    }
    //
    private boolean checkStoragePermisson(){
        boolean result=ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    //
    private void requestStoregePermisson(){
        ActivityCompat.requestPermissions(this,storagePermisson,STORAGE_REQUEST_CODE);
    }
    //
    private boolean checkCameraPermisson(){
        boolean result=ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    //
    private void requestCameraPermisson(){
        ActivityCompat.requestPermissions(this,storagePermisson,CAMERA_REQUEST_CODE);
    }
    //
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latidute=location.getLatitude();
        longitude=location.getLongitude();

        finAddress();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this,"please turn on location",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean locationAcccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(locationAcccepted){
                        //permisson alloewed
                        detecLocation();
                    }
                    else {
                        Toast.makeText(this,"Location permisson iss nescesssary",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;

            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccept=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccept=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccept && storageAccept){
                        //permisson alloewed
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this,"Storeage permisson iss nescesssary",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccept=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccept){
                        //permisson alloewed
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this,"Storeage permisson iss nescesssary",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;



        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode==RESULT_OK){
//            if(requestCode== IMAGE_PICK_GALLERY_CODE){
//                Toast.makeText(this,"ok",Toast.LENGTH_LONG).show();
//                //get picked image
//                imageUri=data.getData();
//                //ser to imageview
//                profile.setImageURI(imageUri);
//
//
//            }
//            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
//                profile.setImageURI(imageUri);
//            }
//        }
        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    profile.setImageURI(selectedImageUri);
                }
            }
        }

    }
}
