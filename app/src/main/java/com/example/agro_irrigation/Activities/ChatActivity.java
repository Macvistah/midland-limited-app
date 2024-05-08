package com.example.agro_irrigation.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.agro_irrigation.Adapter.MessageListAdapter;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.ChatUsers;
import com.example.agro_irrigation.Models.Message;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mMessageRecycler;
    SessionManager sessionManager;
    RequestQueue requestQueue;
    private MessageListAdapter mMessageAdapter;
    private List<Message> lstMessage = new ArrayList<>();
    private EditText edMessage;

    private TextView txtUsername, txtUsertype;
    private ImageButton btnSend;

    private ImageView imgProfile;

    RequestOptions options ;
    private Handler handler;

    String recipientId, recipientType, recipientName, baseUrl, userId, profile;

    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if(savedInstanceState == null) {
            recipientId = Objects.requireNonNull(getIntent().getExtras()).getString("recipient_id");
            recipientName = Objects.requireNonNull(getIntent().getExtras()).getString("recipient_name");
            recipientType = Objects.requireNonNull(getIntent().getExtras()).getString("recipient_type");
            profile = Objects.requireNonNull(getIntent().getExtras()).getString("photo");
        }
        init();
        getAllMessages();
        startRepeatingRequest();
    }
    private void init (){
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();
        HashMap<String, String> user = sessionManager.getUserDetail();
        userId = user.get(sessionManager.ID);
        baseUrl = Constants.BASE_URL;
        handler = new Handler();

        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler);
        mMessageAdapter = new MessageListAdapter(this, lstMessage);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar);
       

        edMessage = (EditText) findViewById(R.id.edit_message);
        btnSend = (ImageButton) findViewById(R.id.button_send);
        imgProfile = (ImageView) findViewById(R.id.avatar);

        txtUsername = (TextView) findViewById(R.id.username);
        txtUsertype= (TextView) findViewById(R.id.user_type);

        txtUsername.setText(recipientName);
        txtUsertype.setText(recipientType);

        Glide.with(this).load(profile).apply(options).into(imgProfile);

        btnSend.setOnClickListener(v -> {
            sendMessage();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRepeatingRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllMessages();
        startRepeatingRequest();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the handler callbacks when the activity is destroyed to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
    }

    private void startRepeatingRequest() {
        // Create a Runnable that will be executed every second
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getAllMessages(); // Make your Volley request here
                handler.postDelayed(this, 5000); // Repeat the task every 1000 milliseconds (1 second)
            }
        }, 5000); // Start the task after 1000 milliseconds (1 second)
    }
    public void onBackClick(View view){
        onBackPressed();
    }


    private void sendMessage(){
        String message = edMessage.getText().toString().trim();
        sendNewMessage(message);
    }
    public void sendNewMessage (String txtMessage){
        String URL_JSON= Constants.BASE_URL +"chat/?action=send_message";

        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        if (success.equals("1")) {
                            getAllMessages();
                            edMessage.setText("");
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
                params.put ("message", txtMessage);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    public void getAllMessages (){
        String URL_JSON= Constants.BASE_URL +"chat/?action=view_chats";

        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        JSONArray jsonArray = jsonObject.getJSONArray("read");
                        if (success.equals("1")) {
                            lstMessage.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                Message message = new Message();
                                ChatUsers chatUser = new ChatUsers();

                                //set the chat details
                                message.setId(Integer.parseInt(object.getString("id")));
                                message.setMessage(object.getString("message"));
                                message.setCreatedAt(object.getString("date"));
                                //set the user details
                                chatUser.setUserId(Integer.parseInt(object.getString("recipient_id")));
                                chatUser.setUsername(object.getString("recipient_name"));
                                chatUser.setUserType(String.valueOf(recipientType));
                                chatUser.setLastMessage("");
                                chatUser.setDate(object.getString("date"));
                                chatUser.setProfileImage("");
                                chatUser.setMessageStatus("");
                                message.setSender(chatUser);
                                lstMessage.add(message);
                            }
                            mMessageAdapter.notifyDataSetChanged();
                            setRvMessages();
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
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void setRvMessages (){
        mMessageRecycler.setLayoutManager(layoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);
    }

}