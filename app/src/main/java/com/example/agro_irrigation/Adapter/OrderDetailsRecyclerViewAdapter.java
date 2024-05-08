package com.example.agro_irrigation.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agro_irrigation.Models.Order_Item_Details;
import com.example.agro_irrigation.R;

import java.text.NumberFormat;
import java.util.List;

public class OrderDetailsRecyclerViewAdapter extends RecyclerView.Adapter<OrderDetailsRecyclerViewAdapter.MyViewHolder> {
    private Context mContext ;
    private List<Order_Item_Details> mData ;

    public OrderDetailsRecyclerViewAdapter(Context mContext, List lst) {
        this.mContext = mContext;
        this.mData = lst;
    }

    @Override
    public OrderDetailsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.order_list_item_row,parent,false);
        final OrderDetailsRecyclerViewAdapter.MyViewHolder viewHolder = new OrderDetailsRecyclerViewAdapter.MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderDetailsRecyclerViewAdapter.MyViewHolder holder, @NonNull  final int position) {
        NumberFormat format = NumberFormat.getNumberInstance();
        String prodName = mData.get(position).getOrder_prod_name();
        String prodQty = mData.get(position).getOrder_prod_qty();
        String prodPrice = mData.get(position).getOrder_prod_price();

        double total = Double.parseDouble(prodQty) * Double.parseDouble(prodPrice);
        holder.txtProdNo.setText(String.valueOf(position+1));
        holder.txtProdName.setText(prodName);
        holder.txtProdQty.setText(prodQty);
        holder.txtProdPrice.setText(format.format(Double.parseDouble(prodPrice)));
        holder.txtProdTotal.setText(format.format(total));

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtProdName,txtProdQty,txtProdPrice, txtProdTotal,txtProdNo;


        public MyViewHolder(View itemView) {
            super(itemView);

            txtProdNo = (TextView) itemView.findViewById(R.id.item_no);
            txtProdName = (TextView) itemView.findViewById(R.id.item_name);
            txtProdQty = (TextView) itemView.findViewById(R.id.item_qty);
            txtProdPrice = (TextView) itemView.findViewById(R.id.item_price);
            txtProdTotal = (TextView) itemView.findViewById(R.id.item_amount);


        }

    }

}
