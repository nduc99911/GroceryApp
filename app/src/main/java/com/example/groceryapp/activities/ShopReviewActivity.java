package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.groceryapp.Adapter.AdapterReview;
import com.example.groceryapp.Model.ModelReview;
import com.example.groceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewActivity extends AppCompatActivity {
    private ImageButton btnBack;
private String shopUid;
private ImageView ImShop;
private TextView TvShopName,Tvratings;
private RatingBar radingBar;
private RecyclerView Rvreviews;
private FirebaseAuth firebaseAuth;
private ArrayList<ModelReview> list;
private AdapterReview adapterReview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_review);
        btnBack=findViewById(R.id.btnBack);
        ImShop=findViewById(R.id.ImShop);
        TvShopName=findViewById(R.id.TvShopName);
        radingBar=findViewById(R.id.radingBar);
        Tvratings=findViewById(R.id.Tvratings);
        Rvreviews=findViewById(R.id.Rvreviews);

        shopUid=getIntent().getStringExtra("shopUid");
        firebaseAuth=FirebaseAuth.getInstance();
        loadShopDetails();
        loadReviews();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private float ratingSum=0;
    private void loadReviews() {
        list=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        list.clear();
                        ratingSum=0;
                        for(DataSnapshot s:snapshot.getChildren()){
                            float rating=Float.parseFloat(""+s.child("ratings").getValue());
                            ratingSum=ratingSum+rating;

                            ModelReview modelReview=s.getValue(ModelReview.class);
                            list.add(modelReview);
                        }

                        adapterReview=new AdapterReview(ShopReviewActivity.this,list);
                        Rvreviews.setAdapter(adapterReview);

                        long numberofReviews=snapshot.getChildrenCount();
                        float avgratings=ratingSum/numberofReviews;

                        Tvratings.setText(String.format("%.2f",avgratings)+"["+numberofReviews+"]");
                        radingBar.setRating(avgratings);
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("shopName").getValue();
                        String profileImage=""+snapshot.child("profileImage").getValue();

                        TvShopName.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.shop).into(ImShop);
                        }
                        catch (Exception e){
                            ImShop.setImageResource(R.drawable.shop);
                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }
}