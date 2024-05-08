package com.example.agro_irrigation.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.Message;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import java.util.HashMap;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_HEADER = 3;

    private Context mContext;
    private List<Message> mMessageList;

    private SessionManager sessionManager;
    private String userId;



    public MessageListAdapter(Context context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
        sessionManager = new SessionManager(context);
        HashMap<String, String> user = sessionManager.getUserDetail();
        userId = user.get(sessionManager.ID);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = (Message) mMessageList.get(position);



        if (message.getSender().getUserId() != Integer.parseInt(userId)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;


        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.me_chat_item, parent, false);
            return new SentMessageHolder(view);
        }
        else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.other_chat_item, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, timeDate;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_timestamp_me);
            timeDate = (TextView) itemView.findViewById(R.id.text_date_me);
        }

        void bind(Message message) {
            String formattedTime = Constants.formatDateTimeToAmPm(message.getCreatedAt());
            String formattedDate = Constants.formatDateShot(message.getCreatedAt());
            messageText.setText(message.getMessage());
            timeDate.setText(formattedDate);
            // Format the stored timestamp into a readable String using method.
            timeText.setText(formattedTime);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, txtDate;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_message_other);
            timeText = (TextView) itemView.findViewById(R.id.text_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_user_other);
            txtDate = (TextView) itemView.findViewById(R.id.text_date_other);
            //profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }

        void bind(Message message) {
            String formattedTime = Constants.formatDateTimeToAmPm(message.getCreatedAt());
            String formattedDate = Constants.formatDateShot(message.getCreatedAt());
            messageText.setText(message.getMessage());
            timeText.setText(formattedTime);
            nameText.setText(message.getSender().getUsername());
            txtDate.setText(formattedDate);

        }
    }

}