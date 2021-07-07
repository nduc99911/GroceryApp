package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Adapter.AdapterOderItem;
import com.example.groceryapp.Model.ModelOrderItem;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderDetailSellerActivity extends AppCompatActivity {
private ImageButton btnBack,btnEdit,btnMaps;
private TextView TvOrderId,TvDate,TvOrderStatus,TvEmail,TvPhone,TvTotalItems,TvAmount,TvAddress;
private RecyclerView RvItems;
String orderId,orderBy;
String sourceLatitude,sourceLongtitude,destinationlatitude,destinationLongtitude;

private FirebaseAuth firebaseAuth;

private ArrayList<ModelOrderItem> orderList;
private AdapterOderItem adapterOderItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_seller);
        btnBack=findViewById(R.id.btnBack);
        btnEdit=findViewById(R.id.btnEdit);
        btnMaps=findViewById(R.id.btnMaps);
        TvOrderId=findViewById(R.id.TvOrderId);
        TvDate=findViewById(R.id.TvDate);
        TvOrderStatus=findViewById(R.id.TvOrderStatus);
        TvEmail=findViewById(R.id.TvEmail);
        TvPhone=findViewById(R.id.TvPhone);
        TvTotalItems=findViewById(R.id.TvTotalItems);
        TvAmount=findViewById(R.id.TvAmount);
        TvAddress=findViewById(R.id.TvAddress);
        RvItems=findViewById(R.id.RvItems);

        orderId=getIntent().getStringExtra("orderId");
        orderBy=getIntent().getStringExtra("orderBy");

        firebaseAuth=FirebaseAuth.getInstance();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetail();
        loadOrderItems();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        String[] options={"In Progress","Complete","Cancelled"};
        Context context;
        AlertDialog.Builder builder=new AlertDialog.Builder(OrderDetailSellerActivity.this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selected=options[which];
                        editOrderStatus(selected);
                    }
                }).show();
    }

    private void editOrderStatus(String selected) {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("orderStatus",""+selected);

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(OrderDetailSellerActivity.this,"Order is now"+selected,Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(OrderDetailSellerActivity.this,"  "+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadOrderDetail() {
        //load details
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {

                        String orderBy=""+snapshot.child("orderBy").getValue();
                        String orderCost=""+snapshot.child("orderCost").getValue();
                        String orderId=""+snapshot.child("orderId").getValue();
                        String orderStatus=""+snapshot.child("orderStatus").getValue();
                        String orderTime=""+snapshot.child("orderTime").getValue();
                        String orderTo=""+snapshot.child("orderTo").getValue();
                        String deliveryFee=""+snapshot.child("deliveryFee").getValue();
                        String latitude=""+snapshot.child("latitude").getValue();
                        String longtitude=""+snapshot.child("longitude").getValue();

                        //convert time
                        Calendar calendar=Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormat= DateFormat.format("dd//MM/yyyy",calendar).toString();

                        if(orderStatus.equals("In Progress")){
                            TvOrderStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if(orderStatus.equals("Completed")){
                            TvOrderStatus.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(orderStatus.equals("Cancelled")){
                            TvOrderStatus.setTextColor(getResources().getColor(R.color.red));
                        }

                        TvOrderId.setText(orderId);
                        TvOrderStatus.setText(orderStatus);
                        TvAmount.setText("$"+orderCost+"[Including delivery fee $"+deliveryFee+"]");
                        TvDate.setText(dateFormat);

                        findAdsress(latitude,longtitude);
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void findAdsress(String latitude, String longtitude) {


        try {
            double lat=Double.parseDouble(latitude);
            double lon=Double.parseDouble(longtitude);

            Geocoder geocoder;
            List<Address> addresses;
            Context context;
            geocoder=new Geocoder(this, Locale.getDefault());
            addresses=geocoder.getFromLocation(lat,lon,1);

            String address=addresses.get(0).getAddressLine(0);
            TvAddress.setText(address);
        }
        catch (Exception e){

        }
    }


    private void openMap() {
        String address="https://maps.google.com/maps?saddr="+sourceLatitude+","+sourceLongtitude+"&daddr="+destinationlatitude+","+destinationLongtitude;
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);

    }

    private void loadMyInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        sourceLatitude=""+snapshot.child("latitude").getValue();
                        sourceLongtitude=""+snapshot.child("latitude").getValue();
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        //get buyer info
                        destinationlatitude=""+snapshot.child("latitude").getValue();
                        destinationLongtitude=""+snapshot.child("latitude").getValue();
                        String email=""+snapshot.child("email").getValue();
                        String phone=""+snapshot.child("phone").getValue();

                        //set info

                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }
    private void loadOrderItems(){
        orderList=new ArrayList<>();

        DatabaseReference referenc=FirebaseDatabase.getInstance().getReference("Users");
        referenc.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        orderList.clear();
                        for(DataSnapshot s:snapshot.getChildren()){
                            ModelOrderItem modelOrderItem=s.getValue(ModelOrderItem.class);

                            orderList.add(modelOrderItem);
                        }
                        adapterOderItem=new AdapterOderItem(OrderDetailSellerActivity.this,orderList);

                        RvItems.setAdapter(adapterOderItem);

                        TvTotalItems.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }
}