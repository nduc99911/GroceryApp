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
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.groceryapp.activities.MainUserActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {
    private ImageButton imgback;
    private ImageView gpsbtn;
    private EditText edName,edPhone,edCountry,edCity,edState,edAddress,edEmail,edPassword,edCpPassword;
    private Button btnRegister;
    private TextView tvRegiter;
    private CircularImageView profile;
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
    //permissson arays
    private String[] cameraPermisson;
    private String[] storagePermisson;

    private Uri imageUri;

    private double latidute, longitude;
    private LocationManager locationManager;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

//
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationEnabled();
        getLocation();
        //

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

        firebaseAuth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("please wait...");
        progressDialog.setCanceledOnTouchOutside(true);


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
                showImagePickDialog();

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //regiter
                inpuData();

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
    private String fullName, phoneNumber, country, state, city, address, email, password, cofirmPassword;

    private void inpuData() {
        fullName = edName.getText().toString().trim();
        phoneNumber = edPhone.getText().toString().trim();
        country = edCountry.getText().toString().trim();
        state = edState.getText().toString().trim();
        city = edCity.getText().toString().trim();
        address = edAddress.getText().toString().trim();
        email = edEmail.getText().toString().trim();
        password = edPassword.getText().toString().trim();
        cofirmPassword = edCpPassword.getText().toString().trim();
//validate data
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Name ...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "PhoneNumber ...", Toast.LENGTH_LONG).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email pattern ...", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "password must be 6 charaters long ...", Toast.LENGTH_LONG).show();
            return;
        }
//        if (password.equals(cofirmPassword)) {
//            Toast.makeText(this, "Password doesn't match ...", Toast.LENGTH_LONG).show();
//            return;
//        }

        createAcount();

    }

    //
    private void createAcount() {
        progressDialog.setMessage("Creating Acount....");
        progressDialog.show();

        //create acount
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //acount create
                        saveFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("Saving Acount Info...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();
        if (imageUri == null) {
            //save info with image

            //setdata to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + fullName);
            hashMap.put("phone", "" + phoneNumber);
            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latidute);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("accountType", "User" );
            hashMap.put("online", "true");
            hashMap.put("profileImage", "");
            //save to db
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db update
                            progressDialog.dismiss();
                            Intent intent=new Intent(RegisterUserActivity.this, MainUserActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure( Exception e) {
                    //fail update db
                    progressDialog.dismiss();
                    Intent intent=new Intent(RegisterUserActivity.this,MainUserActivity.class);
                    startActivity(intent);
                }
            });

        } else {
            //save info with image
            String filePathAndName="profile_images/"+""+timestamp;
            //upload image
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of upload image
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful()){
                                Uri dowloadImageUri=uriTask.getResult();
                                if(uriTask.isSuccessful()){
                                    //setdata to save
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", ""+firebaseAuth.getUid());
                                    hashMap.put("email", ""+email);
                                    hashMap.put("name", ""+fullName);
                                    hashMap.put("phone", ""+phoneNumber);
                                    hashMap.put("country", ""+country);
                                    hashMap.put("state", ""+state);
                                    hashMap.put("city", ""+city);
                                    hashMap.put("address", ""+address);
                                    hashMap.put("latitude", ""+latidute);
                                    hashMap.put("longitude", ""+longitude);
                                    hashMap.put("timestamp", ""+timestamp);
                                    hashMap.put("accountType", "User" );
                                    hashMap.put("online", "true");
                                    hashMap.put("profileImage", ""+dowloadImageUri);//url of uploades image
                                    //save to db
                                    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                                    reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //db update
                                                    progressDialog.dismiss();
                                                    Intent intent=new Intent(RegisterUserActivity.this,MainUserActivity.class);
                                                    startActivity(intent);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure( Exception e) {
                                            //fail update db
                                            progressDialog.dismiss();
                                            Intent intent=new Intent(RegisterUserActivity.this,MainUserActivity.class);
                                            startActivity(intent);

                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure( Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterUserActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //
    private void locationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(RegisterUserActivity.this)
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show Near Places around you.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
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
//        Intent i = new Intent();
//        i.setType("image/*");
//        i.setAction(Intent.ACTION_GET_CONTENT);
//
//        // pass the constant to compare it
//        // with the returned requestCode
//        startActivityForResult(Intent.createChooser(i, "Select Picture"),200);

        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image_title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image_Description");

        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

    }
    //
    private boolean checkStoragePermisson(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }
    //
    private void requestStoregePermisson(){
        ActivityCompat.requestPermissions(this,storagePermisson,STORAGE_REQUEST_CODE);
    }
    //
    private boolean checkCameraPermisson(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    //
    private void requestCameraPermisson(){
        ActivityCompat.requestPermissions(this,cameraPermisson,CAMERA_REQUEST_CODE);
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


    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            longitude=addresses.get(0).getLongitude();
            latidute=addresses.get(0).getLatitude();
            String address=addresses.get(0).getAddressLine(0);//complete addres
            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();

            //set address
            edCity.setText(city);
            edState.setText(state);
            edCountry.setText(country);
            edAddress.setText(address);

        } catch (Exception e) {
        }

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

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccept=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccept){
                        //permisson alloewed
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this,"Storeage permisson iss nescesssary",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {

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
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){

                //save picked imag_uri
                imageUri=data.getData();

                //set image
                profile.setImageURI(imageUri);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                profile.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
