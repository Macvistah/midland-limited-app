package com.example.agro_irrigation.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Adapter.ChatUserRecyclerViewAdapter;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.ChatUsers;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewChartActivity extends AppCompatActivity {

SessionManager sessionManager;
    private EditText edSearch;
    String userId, baseUrl;
    private RecyclerView rvChatUsers;
    private List<ChatUsers> lstUsers = new ArrayList<>();
    private ChatUserRecyclerViewAdapter myAdapter;
    private RequestQueue requestQueue ;
    private LinearLayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chart);
        init();
        getAllUsers();
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

        edSearch = (EditText) findViewById(R.id.search);
        rvChatUsers = (RecyclerView) findViewById(R.id.usersList);

        myAdapter = new ChatUserRecyclerViewAdapter(this, lstUsers, true);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllUsers();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    public void onBackClick(View view){
        onBackPressed();
    }

    public void getAllUsers (){
        String URL_JSON= Constants.BASE_URL +"chat/?action=view_contacts";
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