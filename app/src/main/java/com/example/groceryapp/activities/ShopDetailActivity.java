package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Adapter.AdapterProductUsers;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.Model.ModelShop;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopDetailActivity extends AppCompatActivity {
ImageView ImShop;
TextView TvShopName,TvPhone,TvEmail,TvOpenClose,TvDelivery,TvAdress,TvFilterProduct;
ImageButton btnCall,btnMaps,btnCart,btnFilter;
EditText EdsearchProduct;
RecyclerView RvProduct;
private String shopUid;

private FirebaseAuth firebaseAuth;
String myLatitude,myLongtidude;
String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongtidude;

private ArrayList<ModelProduct> list;
private AdapterProductUsers adapterProductUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        ImShop=findViewById(R.id.ImShop);
        TvShopName=findViewById(R.id.TvShopName);
        TvPhone=findViewById(R.id.TvPhone);
        TvEmail=findViewById(R.id.TvEmail);
        TvOpenClose=findViewById(R.id.TvOpenClose);
        TvDelivery=findViewById(R.id.TvDelivery);
        TvAdress=findViewById(R.id.TvAdress);
        TvFilterProduct=findViewById(R.id.TvFilterProduct);

        btnCall=findViewById(R.id.btnCall);
        btnMaps=findViewById(R.id.btnMaps);
        btnCart=findViewById(R.id.btnCart);
        btnFilter=findViewById(R.id.btnFilter);

        EdsearchProduct=findViewById(R.id.EdsearchProduct);

        RvProduct=findViewById(R.id.RvProduct);


        //get uid shop
        shopUid=getIntent().getStringExtra("shopUid");
        firebaseAuth=FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();

        //search
        EdsearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUsers.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context;
                AlertDialog.Builder builder=new AlertDialog.Builder(ShopDetailActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategori1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get select item
                                String seleced=Constants.productCategori1[which];
                                TvFilterProduct.setText(seleced);
                                if(seleced.equals("All")){
                                    //load all
                                    loadShopProducts();
                                }
                                else {
                                    adapterProductUsers.getFilter().filter(seleced);
                                }
                            }
                        }).show();
            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
    }

    private void openMap() {
        String address="https//:maps.google.com/maps?saddr="+myLatitude+","+myLongtidude+"&daddr="+shopLatitude+","+shopLongtidude;
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);

    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this,""+shopPhone,Toast.LENGTH_LONG).show();
    }

    private void loadShopDetails() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                String name=""+snapshot.child("name").getValue();
                shopName=""+snapshot.child("shopName").getValue();
                shopPhone=""+snapshot.child("phone").getValue();
                shopEmail=""+snapshot.child("email").getValue();
                shopLatitude=""+snapshot.child("latitude").getValue();
                shopLongtidude=""+snapshot.child("longitude").getValue();
                String deliveryFee=""+snapshot.child("deliveryFee").getValue();
                String address=""+snapshot.child("address").getValue();
                String profileImage=""+snapshot.child("profileImage").getValue();
                String shopOpen=""+snapshot.child("shopOpen").getValue();


                //set data
                TvShopName.setText(shopName);
                TvEmail.setText(shopEmail);
                TvDelivery.setText("Delevery Fee: "+deliveryFee);
                TvAdress.setText(address);
                TvPhone.setText(shopPhone);
                ImShop.setImageResource(R.drawable.shop1);

                if(shopOpen.equals("true")){
                    TvOpenClose.setText("Open");
                }
                else {
                    TvOpenClose.setText("Close");
                }
                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.shop1).into(ImShop);
                }
                catch (Exception e){
                    ImShop.setImageResource(R.drawable.shop1);
                }

            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });


    }

    private void loadMyInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        for(DataSnapshot s: snapshot.getChildren()){
                            //get user data
                            String name=""+s.child("name").getValue();
                            String email=""+s.child("email").getValue();
                            String phone=""+s.child("phone").getValue();
                            String city=""+s.child("city").getValue();
                            String profileImage=""+s.child("profileImage").getValue();
                            String accountType=""+s.child("accountType").getValue();
                            myLatitude=""+s.child("latitude").getValue();
                            myLongtidude=""+s.child("longitude").getValue();


                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void loadShopProducts() {
        list=new ArrayList<>();

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot s:snapshot.getChildren()){
                            ModelProduct modelProduct=s.getValue(ModelProduct.class);
                            list.add(modelProduct);
                        }
                        adapterProductUsers=new AdapterProductUsers(ShopDetailActivity.this,list);
                        RvProduct.setAdapter(adapterProductUsers);

                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

}