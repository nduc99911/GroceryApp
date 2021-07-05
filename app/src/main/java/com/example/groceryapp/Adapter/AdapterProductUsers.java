package com.example.groceryapp.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.FilterProduct;
import com.example.groceryapp.FilterProductUser;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductUsers extends RecyclerView.Adapter<AdapterProductUsers.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> list,filterList;
    private FilterProductUser filter;
    public AdapterProductUsers(Context context, ArrayList<ModelProduct> list) {
        this.context = context;
        this.list = list;
        this.filterList=list;
    }


    @Override
    public HolderProductUser onCreateViewHolder( ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_product_user, parent,false);

        return new AdapterProductUsers.HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder( AdapterProductUsers.HolderProductUser holder, int position) {
        ModelProduct modelProduct=list.get(position);
        String discountAvaliable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String origialPrice=modelProduct.getOriginalPrice();
        String quantyti=modelProduct.getProductQuantity();
        String id=modelProduct.getProductId();
        String tittle=modelProduct.getProductTitle();
        String timestamp=modelProduct.getTimestamp();
        String icon=modelProduct.getProductIcon();


        //setdata
        holder.TvTittle.setText(tittle);
        holder.TvdiscountNote.setText(discountNote);
        holder.TvDescription.setText(productDescription);
        holder.TvOrialPrice.setText("$"+origialPrice);
        holder.TvdiscountPrice.setText("$"+discountPrice);

        if(discountAvaliable.equals(true)){
            holder.TvdiscountPrice.setVisibility(View.VISIBLE);
            holder.TvdiscountNote.setVisibility(View.VISIBLE);
            holder.TvOrialPrice.setPaintFlags(holder.TvOrialPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            holder.TvdiscountPrice.setVisibility(View.GONE);
            holder.TvdiscountNote.setVisibility(View.GONE);
            holder.TvOrialPrice.setPaintFlags(0);
        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_24).into(holder.ImproducIcon);

        }catch (Exception e){
            holder.ImproducIcon.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
        }
      holder.TvaddToCart.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            //add product to cart
          }
      });

        holder.Imnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterProductUser(this,filterList);

        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{
        //holes view of reclycleview
        private ImageView ImproducIcon,Imnext;
        private TextView TvdiscountNote;
        private TextView TvTittle;
        private TextView TvaddToCart;
        private TextView TvDescription;
        private TextView TvdiscountPrice;
        private TextView TvOrialPrice;
        public HolderProductUser( View itemView) {
            super(itemView);
            ImproducIcon=itemView.findViewById(R.id.ImproducIcon);
            TvdiscountNote=itemView.findViewById(R.id.TvdiscountNote);
            TvTittle=itemView.findViewById(R.id.TvTittle);
            TvaddToCart=itemView.findViewById(R.id.TvaddToCart);
            TvDescription=itemView.findViewById(R.id.TvDescription);
            TvdiscountPrice=itemView.findViewById(R.id.TvdiscountPrice);
            TvOrialPrice=itemView.findViewById(R.id.TvOrialPrice);
            Imnext=itemView.findViewById(R.id.Imnext);


        }
    }
}
