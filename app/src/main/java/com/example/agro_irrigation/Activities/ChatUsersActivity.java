package com.example.agro_irrigation.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Adapter.ChatUserRecyclerViewAdapter;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.ChatUsers;
import com.example.agro_irrigation.Models.Products;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUsersActivity extends AppCompatActivity {
    SessionManager sessionManager;
    private EditText edSearch;
    String userId, baseUrl;
    private RecyclerView rvChatUsers;
    private Handler handler;
    private List<ChatUsers> lstUsers = new ArrayList<>();
    private ChatUserRecyclerViewAdapter myAdapter;
    private RequestQueue requestQueue ;
    private ImageView imgNewChat;

    private LinearLayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users);
        init();
        getAllChatUsers();
        startRepeatingRequest();
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                myAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void init(){
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();
        HashMap<String, String> user = sessionManager.getUserDetail();
        userId = user.get(sessionManager.ID);
        baseUrl = Constants.BASE_URL;
        handler = new Handler();

        edSearch = (EditText) findViewById(R.id.search);
        rvChatUsers = (RecyclerView) findViewById(R.id.usersList);
        imgNewChat = (ImageView) findViewById(R.id.new_chat);

        myAdapter = new ChatUserRecyclerViewAdapter(this, lstUsers, false);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        imgNewChat.setOnClickListener(v-> {
            Intent i  = new Intent(this, NewChartActivity.class);
            this.startActivity(i);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startRepeatingRequest();
        //getAllChatUsers();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    public void onBackClick(View view){
        onBackPressed();
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
                getAllChatUsers(); // Make your Volley request here
                handler.postDelayed(this, 2000); // Repeat the task every 1000 milliseconds (1 second)
            }
        }, 2000); // Start the task after 1000 milliseconds (1 second)
    }

    public void getAllChatUsers (){
        String URL_JSON= Constants.BASE_URL +"chat/?action=view_chat_contacts";
        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        JSONArray jsonArray = jsonObject.getJSONArray("read");
                        if (success.equals("1")) {
                            lstUsers.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                ChatUsers chatUser = new ChatUsers();
                                chatUser.setUserId(object.getInt("recipient_id"));
                                chatUser.setSenderId(object.getInt("sender_id"));
                                chatUser.setUsername(object.getString("recipient_name"));
                                chatUser.setUserType(object.getString("recipient_type"));
                                chatUser.setLastMessage(object.getString("message"));
                                chatUser.setDate(object.getString("date"));
                                chatUser.setProfileImage(object.getString("photo"));
                                chatUser.setMessageStatus(object.getString("status"));
                                lstUsers.add(chatUser);
                            }
                            myAdapter.notifyDataSetChanged();
                            setRvChatUsers();
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
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    public void setRvChatUsers (){
        rvChatUsers.setLayoutManager(layoutManager);
        rvChatUsers.setAdapter(myAdapter);
    }
}