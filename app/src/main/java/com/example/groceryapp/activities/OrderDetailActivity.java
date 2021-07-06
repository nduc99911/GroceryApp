package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.groceryapp.Adapter.AdapterOderItem;
import com.example.groceryapp.Model.ModelOrderItem;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView TvOrderId,TvDate,TvOrderStatus,TvShopName,TvTotalItem,TvAmount,TvDeliveryAdress;
    private RecyclerView Rvitems;
    private String orderTo,orderId;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderItem> modelOrderItems;
    private AdapterOderItem adapterOderItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        btnBack=findViewById(R.id.btnBack);
        TvOrderId=findViewById(R.id.TvOrderId);
        TvDate=findViewById(R.id.TvDate);
        TvOrderStatus=findViewById(R.id.TvOrderStatus);
        TvTotalItem=findViewById(R.id.TvTotalItem);
        TvAmount=findViewById(R.id.TvAmount);
        TvShopName=findViewById(R.id.TvShopName);
        TvDeliveryAdress=findViewById(R.id.TvDeliveryAdress);
        Rvitems=findViewById(R.id.Rvitems);


        Intent intent=getIntent();
        orderTo=intent.getStringExtra("orderTo");
        orderId=intent.getStringExtra("orderId");


        firebaseAuth=FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderItems();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadOrderItems() {
        modelOrderItems=new ArrayList<>();

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).child("items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        modelOrderItems.clear();//befere loading items clear list
                        for(DataSnapshot s:snapshot.getChildren()){
                           ModelOrderItem modelOrderItem=s.getValue(ModelOrderItem.class);
                                   modelOrderItems.add(modelOrderItem);
                        }

                        adapterOderItem=new AdapterOderItem(OrderDetailActivity.this,modelOrderItems);
                        Rvitems.setAdapter(adapterOderItem);

                        TvTotalItem.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        //get data
                        String orderBy=""+snapshot.child("OrderBy").getValue();
                        String orderCost=""+snapshot.child("orderCost").getValue();
                        String orderId=""+snapshot.child("orderId").getValue();
                        String orderStatus=""+snapshot.child("orderStatus").getValue();
                        String orderTime=""+snapshot.child("orderTime").getValue();
                        String orderTo=""+snapshot.child("orderTo").getValue();
                        String deliveryFee=""+snapshot.child("deliveryFee").getValue();
                        String latitude=""+snapshot.child("latitude").getValue();
                        String longitude=""+snapshot.child("longitude").getValue();

                        TvOrderId.setText(orderId);
                        TvOrderStatus.setText(orderStatus);
                        TvAmount.setText("$"+orderCost+"[Including delivery fee $"+deliveryFee+"]");
                        try {
                            Calendar calendar=Calendar.getInstance();
                            calendar.setTimeInMillis(Long.parseLong(orderTime));
                            String formatDate= DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();
                            TvDate.setText(formatDate);
                        }
                        catch (Exception e) {

                        }


                        if(orderStatus.equals("In Progress")){
                            TvOrderStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                       else if(orderStatus.equals("Completed")){
                            TvOrderStatus.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                       else if(orderStatus.equals("Cancelled")){
                            TvOrderStatus.setTextColor(getResources().getColor(R.color.red));
                        }



                       findAdress(latitude,longitude);

                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void findAdress(String latitude, String longitude) {



        try {
            double lat=Double.parseDouble(latitude);
            double lon=Double.parseDouble(longitude);

            Geocoder geocoder;
            List<Address> addresses;
            geocoder=new Geocoder(this, Locale.getDefault());
            addresses=geocoder.getFromLocation(lat,lon,1);
            String address=addresses.get(0).getAddressLine(0);
            TvDeliveryAdress.setText(address);
        }catch (Exception e){

        }
    }

    private void loadShopInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("shopName").getValue();
                        TvShopName.setText(shopName);
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }
}