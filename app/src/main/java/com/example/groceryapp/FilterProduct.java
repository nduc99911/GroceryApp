package com.example.groceryapp;

import android.widget.Filter;


import com.example.groceryapp.Adapter.AdapterProductSeller;

import java.util.ArrayList;

public class FilterProduct extends Filter {
    private AdapterProductSeller adapterProductSeller;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProductSeller adapterProductSeller, ArrayList<ModelProduct> filterList) {
        this.adapterProductSeller = adapterProductSeller;
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
            ArrayList<ModelProduct> filerMode=new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                //search tittle and category
                if(filterList.get(i).getProductTitle().toUpperCase().contains(constraint)||
                filterList.get(i).getProductCategory().toUpperCase().contains(constraint)){
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
        adapterProductSeller.productsList=(ArrayList<ModelProduct>)results.values;
        //refesh adapter
        adapterProductSeller.notifyDataSetChanged();
    }
}
