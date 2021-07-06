package com.example.groceryapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelCartItem;
import com.example.groceryapp.R;
import com.example.groceryapp.activities.ShopDetailActivity;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdpaterCartItem extends RecyclerView.Adapter<AdpaterCartItem.HolderCartItem>{

    private Context context;
    private ArrayList<ModelCartItem> list;

    public AdpaterCartItem(Context context, ArrayList<ModelCartItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public HolderCartItem onCreateViewHolder( ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(context).inflate(R.layout.row_cart_item, parent,false);

        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder( AdpaterCartItem.HolderCartItem holder, int position) {

        //get data
        ModelCartItem modelCartItem=list.get(position);

        String id=modelCartItem.getId();
        String getPid=modelCartItem.getPid();
        String title=modelCartItem.getPrice();
        String cost=modelCartItem.getCost();
        String price=modelCartItem.getPrice();
        String quantity=modelCartItem.getQuantity();

        //set data
        holder.tvItemTittle.setText(""+title);
        holder.tvItemPrice.setText(""+cost);
        holder.tvItemQuantity.setText("["+quantity+"]");
        holder.tvItemPriceEach.setText(""+price);

        holder.tvRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will crate table if not exists,but in that case will must exits
                EasyDB easyDB=EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();
                easyDB.deleteRow(1,id);
                Toast.makeText(context,"Remove from cart...",Toast.LENGTH_LONG).show();

                //refesh líst
                list.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx=Double.parseDouble((((ShopDetailActivity)context).TvTotal.getText().toString().trim().replace("$","")));
                double totalPrice=tx - Double.parseDouble(cost.replace("$",""));
                double deliveryFee=Double.parseDouble((((ShopDetailActivity)context).deliveryFee.replace("$","")));
                double sTotalPrice=Double.parseDouble(String.format("$.2f",totalPrice))-Double.parseDouble(String.format("%.2f",deliveryFee));
                ((ShopDetailActivity)context).allTotalPrice=0.00;
                ((ShopDetailActivity)context).TvdTotal.setText("$"+String.format("%.2f",sTotalPrice));
                ((ShopDetailActivity)context).TvTotal.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));

                //after removing item from car,update cart count
                ((ShopDetailActivity)context).cartCount();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class HolderCartItem extends RecyclerView.ViewHolder{

        private TextView tvItemTittle,tvItemPrice,tvItemPriceEach,tvItemQuantity,tvRemoveItem;
        public HolderCartItem(View itemView) {
            super(itemView);

            tvItemTittle=itemView.findViewById(R.id.tvItemTittle);
            tvItemPrice=itemView.findViewById(R.id.tvItemPrice);
            tvItemPriceEach=itemView.findViewById(R.id.tvItemPriceEach);
            tvItemQuantity=itemView.findViewById(R.id.tvItemQuantity);
            tvRemoveItem=itemView.findViewById(R.id.tvRemoveItem);

        }
    }
}
