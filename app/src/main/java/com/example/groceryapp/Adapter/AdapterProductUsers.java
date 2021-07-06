package com.example.groceryapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.FilterProduct;
import com.example.groceryapp.FilterProductUser;
import com.example.groceryapp.Model.ModelProduct;
import com.example.groceryapp.R;
import com.example.groceryapp.activities.ShopDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

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
              showQuantityDialog(modelProduct);
          }
      });

        holder.Imnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details
            }
        });
    }
private double cost=0;
    private double finalCost=0;
private int quantity=0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        View view=LayoutInflater.from(context).inflate(R.layout.dialog_quantiy, null);

        ImageView ImProduct=view.findViewById(R.id.ImProduct);
        TextView TvTitle=view.findViewById(R.id.TvTitle);
        TextView TvPQuantity=view.findViewById(R.id.TvPQuantity);
        TextView TvDescription=view.findViewById(R.id.TvDescription);
        TextView TvdiscountNote=view.findViewById(R.id.TvdiscountNote);
        TextView TvOrialPrice=view.findViewById(R.id.TvOrialPrice);
        TextView TvdiscountPrice=view.findViewById(R.id.TvdiscountPrice);
        TextView TvFinal=view.findViewById(R.id.TvFinal);
        ImageButton BtnDecremen=view.findViewById(R.id.BtnDecremen);
        ImageButton BtnIncremen=view.findViewById(R.id.BtnIncremen);
        TextView TvQuantity=view.findViewById(R.id.TvQuantity);
        Button Btncountinune=view.findViewById(R.id.Btncountinune);

        //get data

        String id=modelProduct.getProductId();
        String tittle=modelProduct.getProductTitle();
        String quantyti=modelProduct.getProductQuantity();
        String productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String discountNote=modelProduct.getDiscountNote();

        String price;
        if(modelProduct.getDiscountAvailable().equals(true)){
            price=modelProduct.getDiscountPrice();
            TvdiscountNote.setText(View.VISIBLE);
            TvOrialPrice.setPaintFlags(TvOrialPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else {
            TvdiscountNote.setVisibility(View.GONE);
            TvdiscountPrice.setVisibility(View.GONE);
            price=modelProduct.getOriginalPrice();

        }

        cost=Double.parseDouble(price.replaceAll("$",""));
        finalCost=Double.parseDouble(price.replaceAll("$",""));
        quantity=1;

        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setView(view);
        //set data
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_24).into(ImProduct);

        }
        catch (Exception e){
            ImProduct.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
        }
        TvTitle.setText(""+tittle);
        TvPQuantity.setText(""+quantyti);
        TvDescription.setText(""+productDescription);
        TvdiscountNote.setText(""+discountNote);
        TvQuantity.setText(""+quantity);
        TvOrialPrice.setText("$"+modelProduct.getOriginalPrice());
        TvdiscountPrice.setText("$"+modelProduct.getDiscountPrice());
        TvFinal.setText("$"+finalCost);

        AlertDialog dialog= builder.create();
        dialog.show();

        //increme quantity the product
        BtnIncremen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost=finalCost+cost;
                quantity++;

                TvFinal.setText("$"+finalCost);
                TvQuantity.setText(""+quantity);
            }
        });
        //decreme quantity of price
        BtnDecremen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(quantity>1){
                        finalCost=finalCost-cost;
                        quantity --;

                        TvFinal.setText("$"+finalCost);
                        TvQuantity.setText(""+quantity);
                    }
            }
        });
        Btncountinune.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title=TvTitle.getText().toString().trim();
                String priceEach=price;
                String totalPrice=TvFinal.getText().toString().trim().replace("$","");
                String quantity=TvQuantity.getText().toString().trim();

                //add to db(SQlite)
                addToCart(id,title,priceEach,totalPrice,quantity);

                dialog.dismiss();
            }
        });
    }

    private int itemId=1;
    private void addToCart(String id, String title, String priceEach, String totalPrice, String quantity) {
        itemId++;
        EasyDB easyDB=EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b=easyDB.addData("Item_Id",itemId)
                .addData("Item_PID",id)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",totalPrice)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();
        Toast.makeText(context,"Added to cart...",Toast.LENGTH_LONG).show();
        //update cart count
        ((ShopDetailActivity)context).cartCount();
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
