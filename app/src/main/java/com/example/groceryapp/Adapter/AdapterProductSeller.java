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
import com.example.groceryapp.ModelProduct;
import com.example.groceryapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productsList,filterList;
    private FilterProduct filter;
    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList=productsList;
    }


    @Override
    public HolderProductSeller onCreateViewHolder( ViewGroup parent, int viewType) {
        //inflate layout
        View view=LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent,false);

        return new HolderProductSeller(view);
    }

    @Override
        public void onBindViewHolder( AdapterProductSeller.HolderProductSeller holder, int position) {
                    //get data
        ModelProduct modelProduct=productsList.get(position);
        String id=modelProduct.getProductId();
        String uid=modelProduct.getUid();
        String discountAvaliable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String quantyti=modelProduct.getProductQuantity();
        String tittle=modelProduct.getProductTitle();
        String timestamp=modelProduct.getTimestamp();
        String origialPrice=modelProduct.getOriginalPrice();

        //setdata
        holder.TvTittle.setText(tittle);
        holder.TvQuantity.setText(quantyti);
        holder.TvdiscountNote.setText(discountNote);
        holder.TvdiscountPrice.setText("$"+discountPrice);
        holder.TvOrialPrice.setText("$"+origialPrice);
        if(discountAvaliable.equals(true)){
            holder.TvdiscountPrice.setVisibility(View.VISIBLE);
            holder.TvdiscountNote.setVisibility(View.VISIBLE);
            holder.TvOrialPrice.setPaintFlags(holder.TvOrialPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            holder.TvdiscountPrice.setVisibility(View.GONE);
            holder.TvdiscountNote.setVisibility(View.GONE);
        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_24).into(holder.ImproducIcon);

        }catch (Exception e){
            holder.ImproducIcon.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
        }
        holder.Imnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //han;de item clicks,show item details
                detailBottomSheet(modelProduct);//hể modelProduct contain detals of licked product

            }
        });
    }

    private void detailBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(context);
        //inflate view for bottomsheet
        View view=LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        bottomSheetDialog.setContentView(view);
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterProduct(this,filterList);

        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{
          //holes view of reclycleview
        private ImageView ImproducIcon,Imnext;
        private TextView TvdiscountNote;
        private TextView TvTittle;
        private TextView TvQuantity;
        private TextView TvdiscountPrice;
        private TextView TvOrialPrice;
        public HolderProductSeller( View itemView) {
            super(itemView);
            ImproducIcon=itemView.findViewById(R.id.ImproducIcon);
            TvdiscountNote=itemView.findViewById(R.id.TvdiscountNote);
            TvTittle=itemView.findViewById(R.id.TvTittle);
            TvQuantity=itemView.findViewById(R.id.TvQuantity);
            TvdiscountPrice=itemView.findViewById(R.id.TvdiscountPrice);
            TvOrialPrice=itemView.findViewById(R.id.TvOrialPrice);
            Imnext=itemView.findViewById(R.id.Imnext);


        }
    }
}
