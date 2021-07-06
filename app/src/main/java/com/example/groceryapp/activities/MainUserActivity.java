package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Adapter.AdapterOrderUser;
import com.example.groceryapp.Adapter.AdapterShop;
import com.example.groceryapp.Model.ModelOrderUser;
import com.example.groceryapp.Model.ModelShop;
import com.example.groceryapp.ProfileEditUserActivity;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {
private TextView tvName,tvEmail,tvPhone,tabShop,tabOrders;
private ImageButton btnLogout,btnEdit;
private ImageView profileIv;
private RelativeLayout RlShop,RlOrder;
private RecyclerView RvShops,RvOder;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> list;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> listOrder;
    private AdapterOrderUser adapterOrderUser;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        tvName=findViewById(R.id.tvName);
        tvEmail=findViewById(R.id.tvEmail);
        tvPhone=findViewById(R.id.tvPhone);
        tabShop=findViewById(R.id.tabShop);
        tabOrders=findViewById(R.id.tabOrders);
        btnLogout=findViewById(R.id.btnlogout);
        btnEdit=findViewById(R.id.btnEditProfile);
        profileIv=findViewById(R.id.profileIv);
        RlShop=findViewById(R.id.RlShop);
        RlOrder=findViewById(R.id.RlOrder);
        RvShops=findViewById(R.id.RvShop);
        RvOder=findViewById(R.id.RvOder);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();

        ShowshopUI();
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap=new HashMap<>();
                hashMap.put("online","false");

                //update value to db
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //update successfully

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure( Exception e) {
                                //fail updateing
                                progressDialog.dismiss();
                                Toast.makeText(MainUserActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                firebaseAuth.signOut();
                checkUser();
            }
        });

        //edit profile
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainUserActivity.this, ProfileEditUserActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tabShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowshopUI();
            }
        });

        tabOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowOrderUI();
            }
        });
    }

    private void ShowshopUI() {
        RlShop.setVisibility(View.VISIBLE);
        RlOrder.setVisibility(View.GONE);

        tabShop.setTextColor(getResources().getColor(R.color.black));
        tabShop.setBackgroundResource(R.drawable.shape_rect04);

        tabOrders.setTextColor(getResources().getColor(R.color.white));
        tabShop.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void ShowOrderUI() {
        RlShop.setVisibility(View.GONE);
        RlOrder.setVisibility(View.VISIBLE);

        tabShop.setTextColor(getResources().getColor(R.color.white));
        tabShop.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrders.setTextColor(getResources().getColor(R.color.white));
        tabShop.setBackgroundResource(R.drawable.shape_rect04);

    }


    private void loginInfo() {
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

                            //set user data
                            tvName.setText(name+"("+accountType+")");
                            tvEmail.setText(""+email);
                            tvPhone.setText(""+phone);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.shop).into(profileIv);
                            }
                            catch (Exception e){
                                profileIv.setImageResource(R.drawable.ic_baseline_person_pin_24);
                            }

                            //load shop with city
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });


    }

    private void loadOrders() {
         listOrder=new ArrayList<>();

         DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange( DataSnapshot snapshot) {
                listOrder.clear();
                for(DataSnapshot s:snapshot.getChildren()){
                    String uid=""+s.getRef().getKey();

                    DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    reference1.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange( DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for (DataSnapshot s:snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser=s.getValue(ModelOrderUser.class);

                                            listOrder.add(modelOrderUser);

                                        }
                                        adapterOrderUser=new AdapterOrderUser(MainUserActivity.this,listOrder);
                                        RvOder.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled( DatabaseError error) {

                                }
                            });
                }
             }

             @Override
             public void onCancelled( DatabaseError error) {

             }
         });
    }

    private void loadShops(String city) {

        //init list
        list=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot s:snapshot.getChildren()){
                            ModelShop modelShop=s.getValue(ModelShop.class);

                            String shopCity=""+s.child("city").getValue();
                            //show only user city shops
                            if(shopCity.equals(city)){
                                list.add(modelShop);
                            }

                        }
                        adapterShop=new AdapterShop(MainUserActivity.this,list);
                        RvShops.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void makeOfflie() {
        progressDialog.setMessage("Loging out...");

        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        //fail updateing
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user == null){
            Intent intent=new Intent(MainUserActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            loginInfo();
        }
    }
}