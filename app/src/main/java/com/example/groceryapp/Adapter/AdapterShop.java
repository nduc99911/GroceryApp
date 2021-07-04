package com.example.groceryapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelShop;
import com.example.groceryapp.R;
import com.example.groceryapp.activities.ShopDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {
private Context context;
public ArrayList<ModelShop> listShop;

    public AdapterShop(Context context, ArrayList<ModelShop> listShop) {
        this.context = context;
        this.listShop = listShop;
    }

    @Override
    public HolderShop onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.row_shop, parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder( AdapterShop.HolderShop holder, int position) {
        //getData
        ModelShop modelShop=listShop.get(position);
        String accountType=modelShop.getAccountType();
        String address=modelShop.getAddress();
        String city=modelShop.getCity();
        String country=modelShop.getCountry();
        String deliveryFee=modelShop.getDeliveryFee();
        String email=modelShop.getEmail();
        String latitude=modelShop.getLatitude();
        String longitude=modelShop.getLongitude();
        String online=modelShop.getOnline();
        String name=modelShop.getName();
        String phone=modelShop.getPhone();
        String uid=modelShop.getUid();
        String timestamp=modelShop.getTimestamp();
        String shopOpen=modelShop.getShopOpen();
        String state=modelShop.getState();
        String shopName=modelShop.getShopName();
        String profileImage=modelShop.getProfileImage();

        //set data
        holder.tvShopName.setText(shopName);
        holder.tvPhone.setText(phone);
        holder.tvAdress.setText(address);

        //check online
        if(online.equals("true")){
            holder.ImOnline.setVisibility(View.VISIBLE);
        }
        else {
            holder.ImOnline.setVisibility(View.GONE);
        }
        //check if shop open
        if(shopOpen.equals("true")){
            holder.TvShopClosed.setVisibility(View.GONE);
        }
        else {
            holder.TvShopClosed.setVisibility(View.VISIBLE);
        }
        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.shop).into(holder.ImShop);
        }
        catch (Exception e){
            holder.ImShop.setImageResource(R.drawable.shop);
        }

        holder.ImNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ShopDetailActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return listShop.size();
    }

    class HolderShop extends RecyclerView.ViewHolder{
        private ImageView ImShop,ImOnline;
        private ImageButton ImNext;
        private TextView TvShopClosed,tvShopName,tvPhone,tvAdress;
        private android.widget.RatingBar RatingBar;
        public HolderShop( View itemView) {
            super(itemView);
            ImShop=itemView.findViewById(R.id.ImShop);
            ImOnline=itemView.findViewById(R.id.ImOnline);
            ImShop=itemView.findViewById(R.id.ImShop);
            TvShopClosed=itemView.findViewById(R.id.TvShopClosed);
            tvShopName=itemView.findViewById(R.id.tvShopName);
            tvPhone=itemView.findViewById(R.id.tvPhone);
            tvAdress=itemView.findViewById(R.id.tvAdress);
            ImShop=itemView.findViewById(R.id.ImShop);
            RatingBar=itemView.findViewById(R.id.radingBar);
            ImNext=itemView.findViewById(R.id.ImNext);

        }
    }
}
