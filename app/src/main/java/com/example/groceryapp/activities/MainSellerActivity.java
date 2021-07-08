package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Adapter.AdapterOrderShop;
import com.example.groceryapp.Adapter.AdapterOrderUser;
import com.example.groceryapp.Adapter.AdapterProductSeller;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Model.ModelOrderSeller;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.ProfileEditDSellerActivity;
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

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {
private TextView tvName,tvShopName,tvEmail,tabProduct,tabOrders,tvFilterProduct,TvFilterOrder;
private ImageButton btnLogout,btnedit,btnaddProduct,btnFilterproduct,BtnFilterOrder,btnSetting;
private ImageView imageView;
private RelativeLayout RlProduct,RlOrders;
private EditText edFilterProduct;
private FirebaseAuth firebaseAuth;
private ProgressDialog progressDialog;
private RecyclerView recyclerView,RvOrder;

private ArrayList<ModelProduct> productList;
private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderSeller> listorder;
    private AdapterOrderShop adapterOrderShop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);
        tvName=findViewById(R.id.tvName);
        tvShopName=findViewById(R.id.tvShopName);
        tvEmail=findViewById(R.id.tvEmail);
        tvFilterProduct=findViewById(R.id.TvFilterProduct);
        edFilterProduct=findViewById(R.id.EdsearchProduct);
        TvFilterOrder=findViewById(R.id.TvFilterOrder);
        tabProduct=findViewById(R.id.tabProduct);
        tabOrders=findViewById(R.id.tabOrders);
        btnaddProduct=findViewById(R.id.btnaddProduct);
        btnFilterproduct=findViewById(R.id.Btnfilterproduct);
        btnSetting=findViewById(R.id.btnSetting);
        BtnFilterOrder=findViewById(R.id.BtnFilterOrder);
        imageView=findViewById(R.id.profileIv);
        RlProduct=findViewById(R.id.RlProduct);
        RlOrders=findViewById(R.id.RlOrders);
        RvOrder=findViewById(R.id.RvOrder);

        recyclerView=findViewById(R.id.RVprduct);
        btnLogout=findViewById(R.id.btnlogout);
        btnedit=findViewById(R.id.btnEditProfilee);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();
        loadAllProduct();
        showProductsUI();
        loadAllOrders();
        //search
        edFilterProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeOfflie();

            }
        });

        //edit profile
        btnedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainSellerActivity.this, ProfileEditDSellerActivity.class);
                startActivity(intent);
            }
        });

        btnaddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainSellerActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

        //setting
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainSellerActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });

        tabProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load product
                showProductsUI();
            }
        });
        tabOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();
            }
        });
        btnFilterproduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context;
                AlertDialog.Builder builder=new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategori1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get select item
                                String seleced=Constants.productCategori1[which];
                                tvFilterProduct.setText(seleced);
                                if(seleced.equals("All")){
                                    //load all
                                    loadAllProduct();
                                }
                                else {
                                    lodaFilterProduct(seleced);
                                }
                            }
                        }).show();
            }
        });
        BtnFilterOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] options={"All","In Progress","Cancelled"};

                Context context;
                AlertDialog.Builder builder=new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0){
                                    TvFilterOrder.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");
                                }
                                else {
                                    String optonClicked=options[which];
                                    TvFilterOrder.setText("Showing "+optonClicked+"Orders");
                                    adapterOrderShop.getFilter().filter(optonClicked);

                                }
                            }
                        }).show();
            }
        });
    }

    private void loadAllOrders() {
        listorder=new ArrayList<>();

        //load orders of shop
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        listorder.clear();
                        for(DataSnapshot s:snapshot.getChildren()){
                            ModelOrderSeller seller=s.getValue(ModelOrderSeller.class);
                            listorder.add(seller);
                        }
                        adapterOrderShop=new AdapterOrderShop(MainSellerActivity.this,listorder);
                        RvOrder.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

    private void lodaFilterProduct(String seleced) {
        productList=new ArrayList<>();

        //get all product
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        //before getiing reset list
                        for(DataSnapshot s:snapshot.getChildren()){
                            String peoductCategory=""+s.child("productCategory").getValue();
                           //if select category matches
                            if(seleced.equals(peoductCategory)){
                                ModelProduct modelProduct=s.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }


                        }
                        //setup adapter
                        adapterProductSeller=new AdapterProductSeller(MainSellerActivity.this,productList);
                        //ser adapter
                        recyclerView.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void loadAllProduct() {
        productList=new ArrayList<>();

        //get all product
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        //before getiing reset list
                        for(DataSnapshot s:snapshot.getChildren()){
                            ModelProduct modelProduct=s.getValue(ModelProduct.class);
                            productList.add(modelProduct);

                        }
                        //setup adapter
                        adapterProductSeller=new AdapterProductSeller(MainSellerActivity.this,productList);
                        //ser adapter
                        recyclerView.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void showProductsUI() {
        //show product UI
        RlProduct.setVisibility(View.VISIBLE);
        RlOrders.setVisibility(View.GONE);

        tabProduct.setTextColor(getResources().getColor(R.color.black));
        tabProduct.setBackgroundResource(R.drawable.shape_rect04);

        tabOrders.setTextColor(getResources().getColor(R.color.white));
        tabOrders.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        //show Orders UI
        RlProduct.setVisibility(View.GONE);
        RlOrders.setVisibility(View.VISIBLE);

        tabProduct.setTextColor(getResources().getColor(R.color.white));
        tabProduct.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrders.setTextColor(getResources().getColor(R.color.black));
        tabOrders.setBackgroundResource(R.drawable.shape_rect04);

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
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        //fail updateing
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user== null){
            Intent intent=new Intent(MainSellerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            loginInfo();
        }
    }

    private void loginInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        for(DataSnapshot s: snapshot.getChildren()){
                            String name=""+s.child("name").getValue();
                            String accountType=""+s.child("accountType").getValue();
                            String email=""+s.child("email").getValue();
                            String shopName=""+s.child("shopName").getValue();
                            tvName.setText(name+"("+accountType+")");
                            tvShopName.setText(shopName);
                            tvEmail.setText(email);


                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });


    }
}