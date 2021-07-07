package com.example.groceryapp;

import android.widget.Filter;

import com.example.groceryapp.Adapter.AdapterOrderShop;
import com.example.groceryapp.Adapter.AdapterProductSeller;
import com.example.groceryapp.Model.ModelOrderSeller;
import com.example.groceryapp.Model.ModelProduct;

import java.util.ArrayList;

public class FilterOrders extends Filter {
    private AdapterOrderShop adapterOrderShop;
    private ArrayList<ModelOrderSeller> filterList;

    public FilterOrders(AdapterOrderShop adapterOrderShop, ArrayList<ModelOrderSeller> filterList) {
        this.adapterOrderShop = adapterOrderShop;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //vaditadate data to search query
        if(constraint!=null && constraint.length()>0){
            //search filed not emty,searching something,perfrom search

            //change to upper case,to make case insenstitive
            constraint=constraint.toString().toUpperCase();
            //store our fillered list
            ArrayList<ModelOrderSeller> filerMode=new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                //search tittle and category
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){

                    //add filteres data to list
                    filerMode.add(filterList.get(i));

            }
            }
            results.count=filerMode.size();
            results.values=filerMode;
        }
        else {
            //cearch filed empty,not seaching,trturn origital//all/comple list
            results.count=filterList.size();
            results.values=filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterOrderShop.list=(ArrayList<ModelOrderSeller>)results.values;
        //refesh adapter
        adapterOrderShop.notifyDataSetChanged();
    }
}
