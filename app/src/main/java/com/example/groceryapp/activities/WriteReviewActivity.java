package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    private String shopUid;
    private ImageButton btnBack;
    private ImageView IVProfile;
    private TextView TvShopName;
    private RatingBar ratingBar;
    private EditText EdReview;
    private FloatingActionButton btnSumbit;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        btnBack=findViewById(R.id.btnBack);
        IVProfile=findViewById(R.id.IVProfile);
        TvShopName=findViewById(R.id.TvShopName);
        ratingBar=findViewById(R.id.ratingBar);
        EdReview=findViewById(R.id.EdReview);
        btnSumbit=findViewById(R.id.btnSumbit);
        shopUid=getIntent().getStringExtra("shopUid");

        firebaseAuth=FirebaseAuth.getInstance();

        loadShopInfo();
        loadMyReview();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        btnSumbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });


    }

    private void loadShopInfo() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("shopName").getValue();
                        String shopImage=""+snapshot.child("profileImage").getValue();

                        TvShopName.setText(shopName);
                        try {
                            Picasso.get().load(shopImage).placeholder(R.drawable.shop).into(IVProfile);

                        }catch (Exception e){
                            IVProfile.setImageResource(R.drawable.shop);
                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void loadMyReview() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String uid=""+snapshot.child("uid").getValue();
                            String ratings=""+snapshot.child("ratings").getValue();
                            String review=""+snapshot.child("review").getValue();
                            String timestamp=""+snapshot.child("timestamp").getValue();

                            float myRating=Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            EdReview.setText(review);

                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings=""+ratingBar.getRating();
        String review=""+EdReview.getText().toString().trim();

        String timestamp=""+System.currentTimeMillis();

        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("ratings",""+ratings);
        hashMap.put("review",""+review);
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                            Toast.makeText(WriteReviewActivity.this,"Review publicshed successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        Toast.makeText(WriteReviewActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }
}