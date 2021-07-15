package com.example.groceryapp.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelReview;
import com.example.groceryapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {

private Context context;
private ArrayList<ModelReview> list;

    public AdapterReview(Context context, ArrayList<ModelReview> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public HolderReview onCreateViewHolder( ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.row_reviews, parent,false);

        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder( AdapterReview.HolderReview holder, int position) {

        ModelReview modelReview=list.get(position);
        String uid=modelReview.getUid();
        String ratings=modelReview.getRatings();
        String timestamp=modelReview.getTimestamp();
        String review=modelReview.getReview();
        
        loadUserDetail(modelReview,holder);
        
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat= DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.TvReview.setText(review);
        holder.TvDate.setText(dateFormat);
        

    }

    private void loadUserDetail(ModelReview modelReview, HolderReview holder) {
        String uid=modelReview.getUid();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        String name=""+snapshot.child("name").getValue();
                        String profileImage=""+snapshot.child("profileImage").getValue();

                        holder.TvName.setText(name);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_pin_24).into(holder.IvProfile);
                        }
                        catch (Exception e){
                            holder.IvProfile.setImageResource(R.drawable.ic_baseline_person_pin_24);
                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HolderReview extends RecyclerView.ViewHolder{


        private ImageView IvProfile;
        private TextView TvName,TvDate,TvReview;
        private RatingBar ratingBar;
        public HolderReview( View itemView) {
            super(itemView);
            IvProfile=itemView.findViewById(R.id.IvProfile);
            TvName=itemView.findViewById(R.id.TvName);
            ratingBar=itemView.findViewById(R.id.ratingBar);
            TvDate=itemView.findViewById(R.id.TvDate);
            TvReview=itemView.findViewById(R.id.TvReview);
        }
    }
}
