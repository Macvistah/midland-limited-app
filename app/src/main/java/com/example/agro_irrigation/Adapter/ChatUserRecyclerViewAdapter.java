package com.example.agro_irrigation.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.agro_irrigation.Activities.ChatActivity;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.ChatUsers;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUserRecyclerViewAdapter extends RecyclerView.Adapter<ChatUserRecyclerViewAdapter.MyViewHolder> implements Filterable {
    RequestOptions options ;
    private Context mContext ;
    private List<ChatUsers> mData ;
    private List<ChatUsers> mDataFiltered ;

    private RequestQueue requestQueue;
    private SessionManager sessionManager;

    String sessionId;

    Boolean IS_CONTACT;

    

    public ChatUserRecyclerViewAdapter(Context mContext, List lst, Boolean isContact) {
        this.IS_CONTACT = isContact;
        this.mContext = mContext;
        this.mData = lst;
        this.mDataFiltered = lst;
        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar);
        sessionManager = new SessionManager(mContext);
        HashMap<String, String> user = sessionManager.getUserDetail();
        sessionId = user.get(sessionManager.ID);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.chat_users,parent,false);

        // click listener here
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final ChatUsers user = mDataFiltered.get(position);
        int userId = user.getUserId();
        int senderId = user.getSenderId();
        String username = user.getUsername();
        String message = user.getLastMessage();
        String status = user.getMessageStatus();
        String profile = user.getProfileImage();
        String usertype = user.getUserType();
        String date = Constants.getRelativeTimeInfo(user.getDate());

        holder.txtUsername.setText(username);
        holder.txtDatetime.setText(date);

        if (IS_CONTACT){
            holder.txtMessage.setText(usertype);
            holder.txtDatetime.setVisibility(View.GONE);
        }
        else{
            if (senderId != userId){
                holder.txtMessage.setText("Me: "+ message);
            }
            else{
                holder.txtMessage.setText(message);
                if (status.equals("UNREAD")){
                    // Change text color programmatically
                    holder.txtMessage.setTextColor(Color.BLUE);
                    // Make text bold programmatically
                    holder.txtMessage.setTypeface( holder.txtMessage.getTypeface(), Typeface.BOLD);
                }
            }
        }



        // load image from the internet using Glide
        Glide.with(mContext).load(profile).apply(options).into(holder.user_image);
        holder.view_container.setOnClickListener(v -> {
            if (!IS_CONTACT){
                updateMessageStatus( String.valueOf(userId), String.valueOf(sessionId));
            }

            Intent i = new Intent(mContext, ChatActivity.class);
            i.putExtra("recipient_id", String.valueOf(userId));
            i.putExtra("recipient_name", username);
            i.putExtra("recipient_type", usertype);
            i.putExtra("photo", profile);
            mContext.startActivity(i);
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
                    List<ChatUsers> filteredList = new ArrayList<>();
                    for (ChatUsers row : mData) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getUsername().toLowerCase().contains(charString.toLowerCase())) {
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
                mDataFiltered = (ArrayList<ChatUsers>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtMessage, txtDatetime;
        ImageView user_image;
        CardView view_container;


        public MyViewHolder(View itemView) {
            super(itemView);
            txtUsername = (TextView) itemView.findViewById(R.id.name); 
            txtDatetime = (TextView) itemView.findViewById(R.id.date); 
            txtMessage = (TextView) itemView.findViewById(R.id.message); 
            user_image = (ImageView) itemView.findViewById(R.id.avatar);
            view_container = itemView.findViewById(R.id.userItem);
        }
    }

    public void updateMessageStatus (String userId, String recipientId){
        String URL_JSON= Constants.BASE_URL +"chat/?action=update_status";

        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        if (success.equals("1")) {

                        }
                        else if(success.equals("0")){
                            //Toast.makeText(this, )
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            //show error
        })
        {
            @Override
            protected Map<String, String> getParams () throws AuthFailureError {
                Map <String, String> params = new HashMap<>();
                params.put ("user_id", userId);
                params.put ("recipient_id", recipientId);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);
    }



}
