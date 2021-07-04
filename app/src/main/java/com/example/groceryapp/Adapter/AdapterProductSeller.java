package com.example.groceryapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.FilterProduct;
import com.example.groceryapp.ModelProduct;
import com.example.groceryapp.ProductEditActivity;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        ImageButton btnBack=view.findViewById(R.id.btnBack);
        ImageButton btnDelete=view.findViewById(R.id.btnDelete);
        ImageButton btnEdit=view.findViewById(R.id.btnEdit);
        ImageView ImProduct=view.findViewById(R.id.ImProduct);
        TextView TvdiscountNote=view.findViewById(R.id.TvdiscountNote);
        TextView TvTittle=view.findViewById(R.id.TvTittle);
        TextView TvDescription=view.findViewById(R.id.TvDescription);
        TextView TvCategory=view.findViewById(R.id.TvCategory);
        TextView TvQuantity=view.findViewById(R.id.TvQuantity);
        TextView TvdiscountPrice=view.findViewById(R.id.TvdiscountPrice);
        TextView TvOrialPrice=view.findViewById(R.id.TvOrialPrice);

        //getdata
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
        //set data
        TvTittle.setText(tittle);
        TvDescription.setText(productDescription);
        TvCategory.setText(productCategory);
        TvQuantity.setText(quantyti);
        TvdiscountNote.setText(discountNote);
        TvdiscountPrice.setText("$"+discountPrice);
        TvOrialPrice.setText("$"+origialPrice);

        if(discountAvaliable.equals(true)){
            TvdiscountPrice.setVisibility(View.VISIBLE);
            TvdiscountNote.setVisibility(View.VISIBLE);
             TvOrialPrice.setPaintFlags(TvOrialPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            TvdiscountPrice.setVisibility(View.GONE);
            TvdiscountNote.setVisibility(View.GONE);
        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_24).into(ImProduct);

        }catch (Exception e){
            ImProduct.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
        }
        //show dialog
        bottomSheetDialog.show();

        //edit click
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit product activity
                bottomSheetDialog.dismiss();
                Intent intent=new Intent(context, ProductEditActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);

            }
        });
        //delete
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confim dialog
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete product"+tittle+"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteProduct(id);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel,dimiss dialog
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        //exit
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id) {
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context,"Product delete....",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
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
