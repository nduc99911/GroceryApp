package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class ProductEditActivity extends AppCompatActivity {

    private String productId;
    private ImageButton btnBack;
    private CircularImageView imageProduct;
    private EditText edTittle, edDescription, edQuantity, edPrice, edDiscountPrice, edDiscountNote;
    private SwitchCompat switchCompat;
    private Button BtnUpdateProduct;
    TextView tvCategory;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permissson arays
    private String[] cameraPermisson;
    private String[] storagePermisson;

    private Uri image_uri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        //get id of the product from intent
        productId = getIntent().getStringExtra("productId");

        btnBack = findViewById(R.id.btnBack);
        imageProduct = findViewById(R.id.imgProduct);
        edTittle = findViewById(R.id.edTittle);
        edDescription = findViewById(R.id.edDescription);
        edQuantity = findViewById(R.id.edQuantity);
        tvCategory = findViewById(R.id.TvCategory);
        edPrice = findViewById(R.id.edPrice);
        switchCompat = findViewById(R.id.SwichDiscount);
        edDiscountPrice = findViewById(R.id.edDiscountPrice);
        edDiscountNote = findViewById(R.id.edDiscountNote);
        BtnUpdateProduct = findViewById(R.id.btnupdateProduct);

        edDiscountNote.setVisibility(View.GONE);
        edDiscountPrice.setVisibility(View.GONE);

        firebaseAuth=FirebaseAuth.getInstance();
        loadProductDetail();
        //set progress dialog
        Context context;
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permisstion arrays
        cameraPermisson = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //checked,show discountprice,discountNote
                    edDiscountNote.setVisibility(View.VISIBLE);
                    edDiscountPrice.setVisibility(View.VISIBLE);
                }
                else {
                    edDiscountNote.setVisibility(View.GONE);
                    edDiscountPrice.setVisibility(View.GONE);
                }
            }
        });

        imageProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageDiaLog();
            }
        });
        //add product
        BtnUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product
                /*
                 * 1.Input Data
                 * 2.Validate data
                 * 3.Update data to db
                 * */
                inputData();
            }
        });
        //back activity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick catelory
                categoryDialog();
            }
        });

    }

    private void loadProductDetail() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        String productId=""+snapshot.child("productId").getValue();
                        String productTitle=""+snapshot.child("productTitle").getValue();
                        String productDescription=""+snapshot.child("productDescription").getValue();
                        String productQuantity=""+snapshot.child("productQuantity").getValue();
                        String productCategory=""+snapshot.child("productCategory").getValue();
                        String productIcon=""+snapshot.child("productIcon").getValue();
                        String originalPrice=""+snapshot.child("originalPrice").getValue();
                        String discountPrice=""+snapshot.child("discountPrice").getValue();
                        String discountNote=""+snapshot.child("discountNote").getValue();
                        String discountAvailable=""+snapshot.child("discountAvailable").getValue();
                        String timestamp=""+snapshot.child("timestamp").getValue();
                        String uid=""+snapshot.child("uid").getValue();

                        //set data to views
                        if(discountAvailable.equals("true")){
                            switchCompat.setChecked(true);

                            edDiscountPrice.setVisibility(View.VISIBLE);
                            edDiscountNote.setVisibility(View.VISIBLE);
                        }
                        else{
                            switchCompat.setChecked(false);

                            edDiscountPrice.setVisibility(View.GONE);
                            edDiscountNote.setVisibility(View.GONE);
                        }
                        edTittle.setText(productTitle);
                        edDescription.setText(productDescription);
                        tvCategory.setText(productCategory);
                        edDiscountNote.setText(discountNote);
                        edQuantity.setText(productQuantity);
                        edPrice.setText(originalPrice);
                        edDiscountPrice.setText(discountPrice);

                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_baseline_add_shopping_cart_24).into(imageProduct);
                        }
                        catch (Exception e){
                            imageProduct.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
                        }


                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private String productTitle,productDescription,productCategory,productQuantity,originalPrice,discountPrice,discountNote;
    private boolean discountAvaliable=false;

    private void inputData() {
        productTitle=edTittle.getText().toString().trim();
        productDescription=edDescription.getText().toString().trim();
        productCategory=tvCategory.getText().toString().trim();
        productQuantity=edQuantity.getText().toString().trim();
        originalPrice=edPrice.getText().toString().trim();
        discountAvaliable=switchCompat.isChecked();

        //validate
        if(TextUtils.isEmpty(productTitle)){
            Toast.makeText(this,"Title is require...",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(productDescription)){
            Toast.makeText(this," Description is require...",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this," Price is require...",Toast.LENGTH_LONG).show();
            return;
        }

        if(discountAvaliable){
            discountPrice=edDiscountPrice.getText().toString().trim();
            discountNote=edDiscountNote.getText().toString().trim();

            if(TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this," DiscountPrice is require...",Toast.LENGTH_LONG).show();
                return;
            }

        }
        else {
            discountNote="";
            discountPrice="0";
        }
        //add data to db
        updateProduct();

    }

    private void updateProduct() {
        progressDialog.setMessage("Update Product....");
        progressDialog.show();
        if(image_uri==null){
            //update without image
            HashMap<String, Object> hashMap=new HashMap<>();
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountAvailable",""+discountAvaliable);


            //update to db
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(ProductEditActivity.this,"Update...",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure( Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProductEditActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //update with image
            String filePathAndName="product_images/"+""+productId;
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri dowloadImageUri=uriTask.getResult();
                            if(uriTask.isSuccessful()){

                                HashMap<String, Object> hashMap=new HashMap<>();
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("discountAvailable",""+discountAvaliable);

                                //add to db
                                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(productId).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ProductEditActivity.this,"Product add...",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure( Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ProductEditActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                                            }
                                        });


                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure( Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProductEditActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void categoryDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategori, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category=Constants.productCategori[which];
                        tvCategory.setText(category);
                    }
                }).show();
    }

    private void showImageDiaLog() {
        //optinons to dispaly in dialog
        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            //camera clicked
                            if(checkCameraPermisson()){
                                //permisson granted
                                pickFromCamera();
                            }
                            else {
                                //permisson not granted
                                requestCameraPermisson();
                            }
                        }
                        else {
                            //gallery click
                            if(checkStorgePermission()){
                                pickFromGallery();
                            }
                            else {
                                requestStoragePermisson();
                            }
                        }
                    }
                }).show();
    }
    private void pickFromGallery(){
        //inten to pick image from galley
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){

        //intent to pick image from camera

        //using medua store to pick high/origial qualiti image
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image_title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image_Description");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStorgePermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;//return true / false
    }

    private void requestStoragePermisson(){
        ActivityCompat.requestPermissions(this,storagePermisson,STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermisson(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;//return true / false
    }

    private void requestCameraPermisson(){
        ActivityCompat.requestPermissions(this,cameraPermisson,CAMERA_REQUEST_CODE);

    }
    //permisson result
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccept=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccept){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this,"Camera & Storage permisson are requied...",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccept=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccept){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this," Storage permisson are requied...",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    //image pick result

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){

                //save picked imag_uri
                image_uri=data.getData();

                //set image
                imageProduct.setImageURI(image_uri);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                imageProduct.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}