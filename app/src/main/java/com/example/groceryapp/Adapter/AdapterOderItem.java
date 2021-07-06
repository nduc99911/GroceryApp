package com.example.groceryapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelOrderItem;
import com.example.groceryapp.R;

import java.util.ArrayList;

public class AdapterOderItem extends RecyclerView.Adapter<AdapterOderItem.HolderOrderItem>{

    private Context context;
    private ArrayList<ModelOrderItem> ListOrderItem;

    public AdapterOderItem(Context context, ArrayList<ModelOrderItem> listOrderItem) {
        this.context = context;
        ListOrderItem = listOrderItem;
    }

    @Override
    public HolderOrderItem onCreateViewHolder( ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.row_order_item, parent,false);
        return new HolderOrderItem(view);
    }

    @Override
    public void onBindViewHolder( AdapterOderItem.HolderOrderItem holder, int position) {

        //get data
        ModelOrderItem modelOrderItem=ListOrderItem.get(position);
        String getPid=modelOrderItem.getPid();
        String name=modelOrderItem.getName();
        String cost=modelOrderItem.getCost();
        String price=modelOrderItem.getPrice();
        String quantity=modelOrderItem.getQuantity();

        //set data
        holder.TvTittleItem.setText(name);
        holder.TvPriceItem.setText("$"+cost);
        holder.TvTittleItem.setText("$"+price);
        holder.tvItemQuantity.setText("["+quantity+"]");
    }

    @Override
    public int getItemCount() {
        return ListOrderItem.size();
    }

    public class HolderOrderItem extends RecyclerView.ViewHolder{

        private TextView TvTittleItem,TvPriceItem,TvItemPriceEach,tvItemQuantity;
        public HolderOrderItem( View itemView) {
            super(itemView);

            TvTittleItem=itemView.findViewById(R.id.TvTittleItem);
            TvPriceItem=itemView.findViewById(R.id.TvPriceItem);
            TvItemPriceEach=itemView.findViewById(R.id.TvItemPriceEach);
            tvItemQuantity=itemView.findViewById(R.id.tvItemQuantity);
        }
    }
}
