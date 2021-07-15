package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.groceryapp.Adapter.AdapterProductUsers;
import com.example.groceryapp.Adapter.AdapterReview;
import com.example.groceryapp.Adapter.AdpaterCartItem;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Model.ModelCartItem;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.Model.ModelReview;
import com.example.groceryapp.Model.ModelShop;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailActivity extends AppCompatActivity {
ImageView ImShop;
TextView TvShopName,TvPhone,TvEmail,TvOpenClose,TvDelivery,TvAdress,TvFilterProduct,TvCartCount;
ImageButton btnCall,btnMaps,btnCart,btnFilter,btnShowReview;
EditText EdsearchProduct;
RecyclerView RvProduct;
private String shopUid;

private ProgressDialog progressDialog;
private FirebaseAuth firebaseAuth;
String myLatitude,myLongtidude,myPhone;
String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongtidude;
private RatingBar RatingBar;
public String deliveryFee;

private ArrayList<ModelProduct> list;
private AdapterProductUsers adapterProductUsers;

//cart
    private ArrayList<ModelCartItem> cartItems;
    private AdpaterCartItem adpaterCartItem;

    private EasyDB easyDB;
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
        TvCartCount=findViewById(R.id.TvCartCount);
        RatingBar=findViewById(R.id.RatingBar);

        btnCall=findViewById(R.id.btnCall);
        btnMaps=findViewById(R.id.btnMaps);
        btnCart=findViewById(R.id.btnCart);
        btnFilter=findViewById(R.id.btnFilter);
        btnShowReview=findViewById(R.id.btnShowReview);

        EdsearchProduct=findViewById(R.id.EdsearchProduct);

        RvProduct=findViewById(R.id.RvProduct);

        Context context;
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid shop
        shopUid=getIntent().getStringExtra("shopUid");
        firebaseAuth=FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();

         easyDB=EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        deleteCart();
        cartCount();
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

        //open reviews activity
        btnShowReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ShopDetailActivity.this,ShopReviewActivity.class);
               intent.putExtra("shopUid",shopUid);
                startActivity(intent);
            }
        });
    }


    private float ratingSum=0;
    private void loadReviews() {

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        ratingSum=0;
                        for(DataSnapshot s:snapshot.getChildren()){
                            float rating=Float.parseFloat(""+s.child("ratings").getValue());
                            ratingSum=ratingSum+rating;
                        }
                        long numberofReviews=snapshot.getChildrenCount();
                        float avgratings=ratingSum/numberofReviews;

                        RatingBar.setRating(avgratings);
                    }
                    @Override
                    public void onCancelled( DatabaseError error) {
                    }
                });
    }

    private void deleteCart() {

        easyDB.deleteAllDataFromTable();//delete all recoeds from cart
    }

    public void cartCount(){
        //keep it public so we can access in dapter
        int count=easyDB.getAllData().getCount();
        if(count<=0){
            //no item in cart,hide cart count text view
            TvCartCount.setVisibility(View.GONE);
        }
        else {
            //have item in cart,hide cart count text view
            TvCartCount.setVisibility(View.VISIBLE);
            TvCartCount.setText(""+count);
        }
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


        //place order
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myLatitude.equals("") || myLongtidude.equals("") || myLongtidude.equals("null") || myLongtidude.equals("null")){
                    Toast.makeText(ShopDetailActivity.this,"Please enter your address in you profile before pacing order...",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(myPhone.equals("") || myPhone.equals("null") ){
                    Toast.makeText(ShopDetailActivity.this,"Please enter your phone in you profile before pacing order...",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(cartItems.size()==0){
                    Toast.makeText(ShopDetailActivity.this,"No item in cart...",Toast.LENGTH_SHORT).show();
                    return;
                }
                sumbitOrder();
            }
        });
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

    private void sumbitOrder() {
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        String cost = TvTotal.getText().toString().trim().replace("$", "");

        //set up order data
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress");//In progress/complete/cancelled
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongtidude);

        //add to db
        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        reference.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for(int i=0;i<cartItems.size();i++){
                            String pid=cartItems.get(i).getPid();
                            String id=cartItems.get(i).getId();
                            String name=cartItems.get(i).getName();
                            String cost=cartItems.get(i).getCost();
                            String price=cartItems.get(i).getPrice();
                            String quantity=cartItems.get(i).getQuantity();

                            HashMap<String,String> hashMap11=new HashMap<>();
                            hashMap11.put("pid",pid);
                            hashMap11.put("name",name);
                            hashMap11.put("cost",cost);//In progress/complete/cancelled
                            hashMap11.put("price",price);
                            hashMap11.put("quantity",quantity);

                            reference.child(timestamp).child("items").child(pid).setValue(hashMap11);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailActivity.this,"Oeder Sucesssfully...",Toast.LENGTH_LONG).show();

                        prepareNotificationMesseage(timestamp);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
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
                            myPhone=""+s.child("phone").getValue();
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

    private void prepareNotificationMesseage(String orderId){
        String NOTIFICATION_TOPIC="/topics/"+Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE="New Order"+orderId;
        String NOTIFICATION_MESSAGE="Congratulations...!You have new order.";
        String NOTIFICATION_TYPE="NewOrder";

        JSONObject notificationJo=new JSONObject();
        JSONObject notificationBodyJo=new JSONObject();
        try {
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);

            semdFcmNotifications(notificationJo,orderId);
        }catch (Exception e){
            Toast.makeText(ShopDetailActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    private void semdFcmNotifications(JSONObject notificationJo, String orderId) {
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Intent intent=new Intent(ShopDetailActivity.this, OrderDetailActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent intent=new Intent(ShopDetailActivity.this, OrderDetailActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);

                Toast.makeText(ShopDetailActivity.this,""+ error.toString(),Toast.LENGTH_LONG).show();
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

}