package com.example.groceryapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Model.ModelCartItem;
import com.example.groceryapp.Model.ModelOrderUser;
import com.example.groceryapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HorderOrderUser>{

    private Context context;
    private ArrayList<ModelOrderUser> orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @Override
    public HorderOrderUser onCreateViewHolder( ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.row_order_user, parent,false);

        return new HorderOrderUser(view);
    }

    @Override
    public void onBindViewHolder( AdapterOrderUser.HorderOrderUser holder, int position) {
        //get data
        ModelOrderUser modelOrderUser=orderUserList.get(position);
        String orderId=modelOrderUser.getOrderId();
        String orderBy=modelOrderUser.getOrderBy();
        String orderCost=modelOrderUser.getOrderCost();
        String orderStatus=modelOrderUser.getOrderStatus();
        String orderTime=modelOrderUser.getOrderTime();
        String orderTo=modelOrderUser.getOrderTo();

        //get shopping info
        loadShopInfo(modelOrderUser,holder);

        //set data
        holder.TvAmount.setText("Amount $; "+orderCost);
        holder.TvStatus.setText(orderStatus);
        holder.TvOderId.setText("OrderID : "+orderId);
        if(orderStatus.equals("In Process")){
            //change order star text color
            holder.TvStatus.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else if(orderStatus.equals("Complete")){
            holder.TvStatus.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if(orderStatus.equals("Cancelled")){
            holder.TvStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convere timestamp to proper format
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        DateFormat dateFormat;
        String formatDate= android.text.format.DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.TvDate.setText(formatDate);

    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, HorderOrderUser holder) {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("shopName").getValue();
                        holder.TvShopName.setText(shopName);
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    class HorderOrderUser extends RecyclerView.ViewHolder{
       private TextView TvOderId,TvDate,TvShopName,TvAmount,TvStatus;

        public HorderOrderUser( View itemView) {
            super(itemView);
            TvOderId=itemView.findViewById(R.id.TvOderId);
            TvDate=itemView.findViewById(R.id.TvDate);
            TvShopName=itemView.findViewById(R.id.TvShopName);
            TvAmount=itemView.findViewById(R.id.TvAmount);
            TvStatus=itemView.findViewById(R.id.TvStatus);
            TvOderId=itemView.findViewById(R.id.TvOderId);


        }
    }
}
