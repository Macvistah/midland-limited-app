package com.example.agro_irrigation.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.agro_irrigation.Activities.CartActivity;
import com.example.agro_irrigation.Activities.ProductDetailsActivity;
import com.example.agro_irrigation.Models.Cart_Item;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import java.text.NumberFormat;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CartItemRecyclerViewAdapter extends RecyclerView.Adapter<CartItemRecyclerViewAdapter.MyViewHolder> {
    private Context mContext ;
    private List<Cart_Item> mData ;
    SessionManager sessionManager;
    RequestOptions options ;
    String Total;


    public CartItemRecyclerViewAdapter(Context mContext, List lst) {
        this.mContext = mContext;
        this.mData = lst;
        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.welcome_midland)
                .error(R.drawable.welcome_midland);
        sessionManager = new SessionManager(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.cartlist_row_item,parent,false);
        final MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder,@NonNull  final int position) {
        Double price;
        int qty = Integer.parseInt(mData.get(position).getCart_qty());
        if(mData.get(position).getCart_prod_discount().equals("0")){
            price = Double.parseDouble(mData.get(position).getCart_prod_price());
        }
        else{
            price =  (100- Double.parseDouble(mData.get(position).getCart_prod_discount())) * Double.parseDouble(mData.get(position).getCart_prod_price())/100;
        }

        final Double total = qty*price;
        NumberFormat format = NumberFormat.getNumberInstance();
        final Double unitPrice = price;

        holder.tvProdId.setText(mData.get(position).getCart_prod_id());
        holder.tvProdName.setText(mData.get(position).getCart_prod_name());
        holder.tvProdUnitPrice.setText("["+format.format(unitPrice)+"/=");
        holder.tvQty.setText("x "+mData.get(position).getCart_qty()+"Unit(s)]");
        holder.tvProdPrice.setText(format.format(total)+" /=");


        // load image from the internet using Glide
        Glide.with(mContext).load(mData.get(position).getCart_prod_img()).apply(options).into(holder.imgProd);

        holder.cartOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating popup menu
                PopupMenu popupMenu = new PopupMenu(mContext,holder.cartOptions);
                //inflating menu from xml resource
                popupMenu.inflate(R.menu.cart_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.opt_edit_cart:
                                Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                                intent.putExtra("prod_id",mData.get(holder.getAdapterPosition()).getCart_prod_id());
                                intent.putExtra("prod_name",mData.get(holder.getAdapterPosition()).getCart_prod_name());
                                intent.putExtra("category",mData.get(holder.getAdapterPosition()).getCart_prod_Category());
                                intent.putExtra("new_price",String.valueOf(price));
                                intent.putExtra("prod_image",mData.get(holder.getAdapterPosition()).getCart_prod_img());
                                intent.putExtra("prod_price",mData.get(holder.getAdapterPosition()).getCart_prod_price());
                                intent.putExtra("quantity",mData.get(holder.getAdapterPosition()).getCart_prod_qty());
                                intent.putExtra("prod_desc",mData.get(holder.getAdapterPosition()).getCart_prod_desc());
                                mContext.startActivity(intent);

                                break;
                            case R.id.opt_delete_cart:
                                final String pname =mData.get(holder.getAdapterPosition()).getCart_prod_name();
                                final String cId =mData.get(holder.getAdapterPosition()).getCart_id();


                                new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Are you sure?")
                                        .setContentText("Won't be able to undo the action!")
                                        .setConfirmText("Yes,delete it!")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog

                                                        .setTitleText("Deleted!")
                                                        .setContentText("Removed Successfully!")
                                                        .setConfirmText("OK")
                                                        .setConfirmClickListener(null)
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                                                ((CartActivity) mContext).deleteProductCart(cId,mContext);
                                                ((CartActivity) mContext).setTotalPrice(total);
                                                int pos = mData.indexOf(mData.get(holder.getAdapterPosition()));
                                                mData.remove(pos);
                                                notifyDataSetChanged();
                                            }
                                        })
                                        .show();

                                break;
                        }
                        return false;
                    }
                });
                //displaying the popUp
                popupMenu.show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvProdName,tvProdId,tvProdPrice,tvQty,tvProdUnitPrice;
        ImageView imgProd;
        LinearLayout cartItem;
        ImageButton cartOptions;

        public MyViewHolder(View itemView) {
            super(itemView);

            imgProd =  (ImageView) itemView.findViewById(R.id.cart_prod_img);
            tvProdId = (TextView) itemView.findViewById(R.id.cart_prod_id);
            tvProdName = (TextView) itemView.findViewById(R.id.cart_prod_name);
            tvProdPrice = (TextView) itemView.findViewById(R.id.cart_prod_price);
            tvProdUnitPrice = (TextView) itemView.findViewById(R.id.cart_prod_unit_price);
            tvQty = (TextView) itemView.findViewById(R.id.cart_prod_qty);
            cartOptions =(ImageButton) itemView.findViewById(R.id.options_btn);
            cartItem = (LinearLayout) itemView.findViewById(R.id.container_cart);
        }

    }



}

