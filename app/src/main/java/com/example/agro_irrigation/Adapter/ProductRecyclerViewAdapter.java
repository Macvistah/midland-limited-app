package com.example.agro_irrigation.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.agro_irrigation.Activities.ProductDetailsActivity;
import com.example.agro_irrigation.Models.Products;
import com.example.agro_irrigation.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductRecyclerViewAdapter extends RecyclerView.Adapter<ProductRecyclerViewAdapter.MyViewHolder> implements Filterable {

    RequestOptions options ;
    private Context mContext ;
    private List<Products> mData ;
    private List<Products> mDataFiltered ;



    public ProductRecyclerViewAdapter(Context mContext, List lst) {
        this.mContext = mContext;
        this.mData = lst;
        this.mDataFiltered = lst;
        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.welcome_midland)
                .error(R.drawable.welcome_midland);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.product_row_item,parent,false);
        final MyViewHolder viewHolder = new MyViewHolder(view);

        // click listener here
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Products product = mDataFiltered.get(position);
        NumberFormat format = NumberFormat.getNumberInstance();
        double newPrice = 0.0;
        if (!product.getProductDiscount().isEmpty()){
            newPrice = (100- Double.parseDouble(product.getProductDiscount())) * Double.parseDouble(product.getProductPrice())/100;
        }
        holder.prod_id.setText(product.getProductId());
        holder.prod_name.setText(product.getProductName());
        holder.prod_Category.setText(product.getProductCat());
        if(product.getProductDiscount().equals("0")|| product.getProductDiscount().isEmpty()){
            holder.prod_price.setText("Ksh "+format.format(Double.valueOf(product.getProductPrice())));
        }else{
            holder.prod_price.setText("Ksh "+format.format(newPrice));
        }

        // load image from the internet using Glide
        Glide.with(mContext).load(product.getImageUrl()).apply(options).into(holder.prod_image);
        double finalNewPrice = newPrice;
        holder.view_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ProductDetailsActivity.class);
               i.putExtra("prod_id",product.getProductId());
                i.putExtra("prod_name",product.getProductName());
                i.putExtra("category",product.getProductCat());
                i.putExtra("new_price",String.valueOf(finalNewPrice));
                i.putExtra("prod_image",product.getImageUrl());
                i.putExtra("prod_desc",product.getProductDesc());
                i.putExtra("prod_price",product.getProductPrice());
                i.putExtra("quantity",product.getProductQty());
                mContext.startActivity(i);


            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mDataFiltered = mData;
                } else {
                    List<Products> filteredList = new ArrayList<>();
                    for (Products row : mData) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getProductName().toLowerCase().contains(charString.toLowerCase()) || row.getProductCat().toLowerCase().contains(charString.toLowerCase())
                                || row.getProductPrice().toLowerCase().contains(charString.toLowerCase())   ) {
                            filteredList.add(row);
                        }
                        mDataFiltered = filteredList;
                    }

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mDataFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                mDataFiltered = (ArrayList<Products>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView prod_name,prod_price,prod_id,prod_Category;
        ImageView prod_image;
        CardView view_container;


        public MyViewHolder(View itemView) {
            super(itemView);
            prod_id =itemView.findViewById(R.id.prod_id);
            prod_name = itemView.findViewById(R.id.prod_name);
            prod_price = itemView.findViewById(R.id.prod_price);
            prod_image = itemView.findViewById(R.id.prod_image);
            //users order quantity
            prod_Category = itemView.findViewById(R.id.prod_Category);
            view_container = itemView.findViewById(R.id.productItem);
        }
    }


}
