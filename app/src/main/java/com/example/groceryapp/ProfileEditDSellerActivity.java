package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.groceryapp.activities.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditDSellerActivity extends AppCompatActivity implements LocationListener {
    private ImageButton btnBack, btnGps;
    private CircularImageView imageView;
    private EditText edFullName, edShopName, edPhone, edDeliveryFee, edCountry, edState, edCity, edAdress;
    private Button btnUpdate;
    private SwitchCompat switchCompat;

    //permission constants
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

    private Uri image_uri;

    private double latitude = 0.0;
    private double longgitude = 0.0;

    private ProgressDialog progressDialog;

    //fireauth
    private FirebaseAuth firebaseAuth;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_dseller);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationEnabled();
        getLocation();

        btnGps = findViewById(R.id.btngps);
        imageView = findViewById(R.id.profileIv);
        btnBack = findViewById(R.id.btnBack);
        edFullName = findViewById(R.id.edName);
        edShopName = findViewById(R.id.edshopName);
        edDeliveryFee = findViewById(R.id.edDeliveryFee);
        edCountry = findViewById(R.id.edCountry);
        edCity = findViewById(R.id.edCity);
        edState = findViewById(R.id.edstate);
        edAdress = findViewById(R.id.edAddress);
        edPhone=findViewById(R.id.edPhone);
        switchCompat = findViewById(R.id.shopOpenSwitch);
        btnUpdate = findViewById(R.id.btnUpdate);

        //initr permission array
        locationPermisson = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermisson = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //setupDialog
        Context context;
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //gps
        btnGps.setOnClickListener(new View.OnClickListener() {
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
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }
    private String name, shopName, phoneNumber, deliveryFee, country, state, city, address;
    private Boolean shopOpen;
    private void inputData() {
        name = edFullName.getText().toString().trim();
        shopName = edShopName.getText().toString().trim();
        phoneNumber = edPhone.getText().toString().trim();
        deliveryFee = edDeliveryFee.getText().toString().trim();
        country = edCountry.getText().toString().trim();
        state = edState.getText().toString().trim();
        city = edCity.getText().toString().trim();
        address = edAdress.getText().toString().trim();
        shopOpen=switchCompat.isChecked();

        updateProfile();
    }

    private void updateProfile() {
        progressDialog.setMessage("Update Profile...");
        progressDialog.show();

        if (image_uri == null) {
            //save info with image

            //setdata to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", "" + name);
            hashMap.put("shopName", "" + shopName);
            hashMap.put("phone", "" + phoneNumber);
            hashMap.put("deliveryFee", "" + deliveryFee);
            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);;
            hashMap.put("shopOpen", ""+shopOpen);

            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditDSellerActivity.this,"Profile update",Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure( Exception e) {
                            Toast toast = Toast.makeText(ProfileEditDSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });

    } else {
            String filePathAndName="profile_image/"+""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of upload image
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful()){
                                Uri uri=uriTask.getResult();
                                if(uriTask.isSuccessful()){
                                    //setdata to save
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("name", "" + name);
                                    hashMap.put("shopName", "" + shopName);
                                    hashMap.put("phone", "" + phoneNumber);
                                    hashMap.put("deliveryFee", "" + deliveryFee);
                                    hashMap.put("country", "" + country);
                                    hashMap.put("state", "" + state);
                                    hashMap.put("city", "" + city);
                                    hashMap.put("address", "" + address);
                                    hashMap.put("shopOpen", "true");
                                    hashMap.put("profileImage", ""+uri);//url of uploades image
                                    //save to db
                                    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                                    reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //db update
                                                    progressDialog.dismiss();
                                                    Toast.makeText(ProfileEditDSellerActivity.this,"Profile update...",Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure( Exception e) {
                                            //fail update db
                                            progressDialog.dismiss();
                                            Toast.makeText(ProfileEditDSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure( Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditDSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        } else {
            loadMyInfo();
        }

    }

    private void loadMyInfo() {
        //load user info and set to view
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            String acountType = "" + s.child("accountType").getValue();
                            String address = "" + s.child("address").getValue();
                            String city = "" + s.child("city").getValue();
                            String state = "" + s.child("state").getValue();
                            String country = "" + s.child("country").getValue();
                            String deliveryFee = "" + s.child("deliveryFee").getValue();
                            String email = "" + s.child("email").getValue();
                            String name = "" + s.child("name").getValue();
                            String online = "" + s.child("online").getValue();
                            String phone = "" + s.child("phone").getValue();
                            String profileImage = "" + s.child("profileImage").getValue();
                            String timestamp = "" + s.child("timestamp").getValue();
                            String shopOpen = "" + s.child("shopOpen").getValue();
                            String shopName = "" + s.child("shopName").getValue();
                            String uid = "" + s.child("uid").getValue();

                            edFullName.setText(name);
                            edPhone.setText(phone);
                            edDeliveryFee.setText(deliveryFee);
                            edShopName.setText(shopName);
                            edCity.setText(city);
                            edCountry.setText(country);
                            edState.setText(state);
                            edAdress.setText(address);

                            if (shopOpen.equals("true")) {
                                switchCompat.setChecked(true);
                            } else {
                                switchCompat.setChecked(false);
                            }

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_shopping_bag_24).into(imageView);
                            } catch (Exception e) {
                                imageView.setImageResource(R.drawable.ic_baseline_person_pin_24);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }


    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), IMAGE_PICK_GALLERY_CODE);
    }


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
            new AlertDialog.Builder(ProfileEditDSellerActivity.this)
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


    private void requestLocationPermisson() {
        ActivityCompat.requestPermissions(this, locationPermisson, LOCATION_REQUEST_CODE);
    }

    private void detecLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
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

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            String address = addresses.get(0).getAddressLine(0);//complete addres
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set address
            edCity.setText(city);
            edState.setText(state);
            edCountry.setText(country);
            edAdress.setText(address);

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAcccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAcccepted) {
                        //permisson alloewed
                        detecLocation();
                    } else {
                        Toast.makeText(this, "Location permisson iss nescesssary", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }
}