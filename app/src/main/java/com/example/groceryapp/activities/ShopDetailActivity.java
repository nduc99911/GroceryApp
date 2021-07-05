package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Adapter.AdapterProductUsers;
import com.example.groceryapp.Adapter.AdpaterCartItem;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Model.ModelCartItem;
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

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

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
public String deliveryFee;
private ArrayList<ModelProduct> list;
private AdapterProductUsers adapterProductUsers;

//cart
    private ArrayList<ModelCartItem> cartItems;
    private AdpaterCartItem adpaterCartItem;
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
        deleteCart();
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
                showCartDialog();
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

    private void deleteCart() {
        EasyDB easyDB=EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        easyDB.deleteAllDataFromTable();//delete all recoeds from cart
    }


    public double allTotalPrice=0.00;
    public TextView TvdTotal,TvdFee,TvTotal,btnCheckout;
    private void showCartDialog() {
        //init list
        cartItems=new ArrayList<>();

        View view= LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init View
         TextView TvShopName=view.findViewById(R.id.TvShopName1);
        TvdTotal=view.findViewById(R.id.TvdTotal);
        TvdFee=view.findViewById(R.id.TvdFee);
        TvTotal=view.findViewById(R.id.TvTotal);
         btnCheckout=view.findViewById(R.id.btnCheckout);
        RecyclerView RvcartItem=view.findViewById(R.id.RvcartItem);

        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        TvShopName.setText(shopName);

        EasyDB easyDB=EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //get all recods from db
        Cursor res=easyDB.getAllData();
        while ((res.moveToNext())){
            String id=res.getString(1);
            String pId=res.getString(2);
            String name=res.getString(3);
            String price=res.getString(4);
            String cost=res.getString(5);
            String quantity=res.getString(6);

            allTotalPrice=allTotalPrice+Double.parseDouble(cost);

            ModelCartItem modelCartItem=new ModelCartItem(""+id,""+pId,""+name,""+price,""+cost,""+quantity);
            cartItems.add(modelCartItem);
        }
        //set adapter
        adpaterCartItem=new AdpaterCartItem(this,cartItems);
        RvcartItem.setAdapter(adpaterCartItem);

        TvdFee.setText("$"+deliveryFee);
        TvdTotal.setText("$"+String.format("%.2f",allTotalPrice));
        TvTotal.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))));

        //show dialog
        AlertDialog dialog=builder.create();
        dialog.show();

        //reset dialog
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice=0.00;
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
                 deliveryFee=""+snapshot.child("deliveryFee").getValue();
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