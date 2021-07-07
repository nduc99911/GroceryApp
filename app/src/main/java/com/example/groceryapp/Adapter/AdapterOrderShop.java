package com.example.groceryapp.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.FilterOrders;
import com.example.groceryapp.Model.ModelOrderItem;
import com.example.groceryapp.Model.ModelOrderSeller;
import com.example.groceryapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HoderOrderShop> implements Filterable {
   private Context context;
   public ArrayList<ModelOrderSeller> list,filterlist;
   private FilterOrders filter;
    public AdapterOrderShop(Context context, ArrayList<ModelOrderSeller> list) {
        this.context = context;
        this.list = list;
        this.filterlist=list;
    }

    @Override
    public HoderOrderShop onCreateViewHolder( ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent,false);
        return new HoderOrderShop(view);
    }

    @Override
    public void onBindViewHolder( AdapterOrderShop.HoderOrderShop holder, int position) {
        ModelOrderSeller modelOrderSeller=list.get(position);

        String orderId= modelOrderSeller.getOrderId();
        String orderTime= modelOrderSeller.getOrderTime();
        String orderStatus= modelOrderSeller.getOrderStatus();
        String orderCost= modelOrderSeller.getOrderCost();
        String orderBy= modelOrderSeller.getOrderBy();
        String orderTo= modelOrderSeller.getOrderTo();
        String latitude= modelOrderSeller.getLatitude();
        String longitude= modelOrderSeller.getLongitude();
        String deliveryFee= modelOrderSeller.getDeliveryFee();

        //load user info
        loadUserInfo(modelOrderSeller,holder);

        //set data
        holder.TvAmount.setText("Amount: $ "+orderCost);
        holder.TvStatus.setText(orderStatus);
        holder.TvOrderId.setText(orderId);

        if(orderStatus.equals("In Progress")){
            holder.TvStatus.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
       else if(orderStatus.equals("Completed")){
            holder.TvStatus.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if(orderStatus.equals("Cancelled")){
            holder.TvStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatDate= DateFormat.format("dd//MM/yyyy",calendar).toString();

        holder.TvDateOrder.setText(formatDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void loadUserInfo(ModelOrderSeller modelOrderSeller, HoderOrderShop holder) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderSeller.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        String email=""+snapshot.child("email").getValue();
                        holder.TvEmail.setText(email);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

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
            filter=new FilterOrders(this,filterlist);
        }
        return filter;
    }

    public class HoderOrderShop extends RecyclerView.ViewHolder{
        private TextView TvOrderId,TvDateOrder,TvEmail,TvAmount,TvStatus;
        private ImageView IvNext;
        public HoderOrderShop( View itemView) {
            super(itemView);
            TvOrderId=itemView.findViewById(R.id.TvOrderId);
            TvDateOrder=itemView.findViewById(R.id.TvDateOrder);
            TvEmail=itemView.findViewById(R.id.TvEmail);
            TvAmount=itemView.findViewById(R.id.TvAmount);
            TvStatus=itemView.findViewById(R.id.TvStatus);
            IvNext=itemView.findViewById(R.id.IvNext);

        }
    }
}
