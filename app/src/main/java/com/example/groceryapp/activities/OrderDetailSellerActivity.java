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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.groceryapp.Adapter.AdapterOderItem;
import com.example.groceryapp.Constants;
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
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        FirebaseMessaging.getInstance().subscribeToTopic("news");
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
                        String message="Order is now"+selected;
                        Toast.makeText(OrderDetailSellerActivity.this,message,Toast.LENGTH_LONG).show();


                        prepareNotificationMesseage(orderId,message);

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

    private void prepareNotificationMesseage(String orderId,String message){
        String NOTIFICATION_TOPIC="/topics/"+ Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE="Your Order"+orderId;
        String NOTIFICATION_MESSAGE=""+message;
        String NOTIFICATION_TYPE="OrderStatusChanged";

        JSONObject notificationJo=new JSONObject();
        JSONObject notificationBodyJo=new JSONObject();
        try {
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",orderBy);
            notificationBodyJo.put("sellerUid",firebaseAuth.getUid());
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);

            semdFcmNotifications(notificationJo);
        }catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    private void semdFcmNotifications(JSONObject notificationJo) {
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(OrderDetailSellerActivity.this,""+ error.toString(),Toast.LENGTH_LONG).show();
                Log.e("ERRO", error.getMessage());
                Log.e("ERRO1", error.toString());

            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+ Constants.FCM_KEY);

                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void sen(){
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("to","/topics/"+"news");
            JSONObject jsonObject1=new JSONObject();
            jsonObject1.put("title","any title");
            jsonObject1.put("body","any body");
            jsonObject.put("notification",jsonObject1);

            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers=new HashMap<>();
                    headers.put("content-type","application/json");
                    headers.put("authorization","key=AAAAcxQCwk8:APA91bGHiVS2FzvRgJRX2jYeN4EjOkdEOzYDF3oyumaJxS_Am7cHOjUOGYgzL3G-N0O3KRRLqiD4ChvZLAQwweXNhSUHSGP2v9oGkqseWGWPgo2mA5z7YR7s_7DjkiZruKqJWHXUEYZV");

                    return headers;
                }
            };
            Volley.newRequestQueue(this).add(request);
        }
        catch (Exception e){

        }

    }
}