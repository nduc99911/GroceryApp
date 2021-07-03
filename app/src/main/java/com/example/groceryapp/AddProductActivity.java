package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.provider.ContactsContract;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {
private ImageButton btnBack;
private CircularImageView imageProduct;
private EditText edTittle,edDescription,edQuantity,edPrice,edDiscountPrice,edDiscountNote;
private SwitchCompat switchCompat;
private Button BtnaddProduct;
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
        setContentView(R.layout.activity_add_product);
        btnBack=findViewById(R.id.btnBack);
        imageProduct=findViewById(R.id.imgProduct);
        edTittle=findViewById(R.id.edTittle);
        edDescription=findViewById(R.id.edDescription);
        edQuantity=findViewById(R.id.edQuantity);
        tvCategory=findViewById(R.id.TvCategory);
        edPrice=findViewById(R.id.edPrice);
        switchCompat=findViewById(R.id.SwichDiscount);
        edDiscountPrice=findViewById(R.id.edDiscountPrice);
        edDiscountNote=findViewById(R.id.edDiscountNote);
        BtnaddProduct=findViewById(R.id.btnaddProduct);

        edDiscountNote.setVisibility(View.GONE);
        edDiscountPrice.setVisibility(View.GONE);

        firebaseAuth=FirebaseAuth.getInstance();

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
        BtnaddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product
                /*
                * 1.Input Data
                * 2.Validate data
                * 3.Add data to db
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
        addProduct();

    }

    private void addProduct() {
        progressDialog.setMessage("Add Product...");
        progressDialog.show();

        String timestamp=""+System.currentTimeMillis();

        if(image_uri==null){
            HashMap<String, Object> hashMap=new HashMap<>();
            hashMap.put("productId",""+timestamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productIcon","");
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvaliable);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("uid",""+firebaseAuth.getUid());

            //add to db
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this,"Product add...",Toast.LENGTH_LONG).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure( Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

        }
        else {
            String filePathAndName="product_images/"+timestamp;
            StorageReference storageReference=FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());
                                Uri dowloadImageUri=uriTask.getResult();
                                if(uriTask.isSuccessful()){

                                    HashMap<String, Object> hashMap=new HashMap<>();
                                    hashMap.put("productId",""+timestamp);
                                    hashMap.put("productTitle",""+productTitle);
                                    hashMap.put("productDescription",""+productDescription);
                                    hashMap.put("productCategory",""+productCategory);
                                    hashMap.put("productIcon",""+dowloadImageUri);
                                    hashMap.put("originalPrice",""+originalPrice);
                                    hashMap.put("discountPrice",""+discountPrice);
                                    hashMap.put("discountNote",""+discountNote);
                                    hashMap.put("discountAvailable",""+discountAvaliable);
                                    hashMap.put("timestamp",""+timestamp);
                                    hashMap.put("uid",""+firebaseAuth.getUid());

                                    //add to db
                                    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                                    reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AddProductActivity.this,"Product add...",Toast.LENGTH_LONG).show();
                                                    clearData();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure( Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AddProductActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            });


                                }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure( Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void clearData() {
        edTittle.setText("");
        edDescription.setText("");
        tvCategory.setText("");
        edQuantity.setText("");
        edPrice.setText("");
        edDiscountPrice.setText("");
        edDiscountNote.setText("");
        imageProduct.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
        image_uri=null;
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
        Context context;
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