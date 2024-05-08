package com.example.agro_irrigation.Activities;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsActivity extends AppCompatActivity {
    private CardView cvLogout,cvPassword;
    Dialog myDialog;
    SessionManager sessionManager;
    String userId;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        changeStatusBarColor();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("Settings Panel");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        cvLogout   =    (CardView) findViewById(R.id.logout);
        cvPassword        =    (CardView) findViewById(R.id.manage_password);
        myDialog = new Dialog(this);
        sessionManager = new SessionManager(this);

        HashMap<String, String> user = sessionManager.getUserDetail();
        userId = user.get(sessionManager.ID);
        cvPassword.setOnClickListener(v -> showPopup());
        cvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }
    private void logout() {
        new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
                .setContentText("Do you want to Log out?")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sessionManager.logout();
                        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
                    }
                })
                .setCancelText("No")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .show();
    }
    public void showPopup(){
        Button btnReset;
        final TextInputEditText edCurrentPswd,edNewPswd,edConfirmPswd;
        TextView txtClose;

        myDialog.setContentView(R.layout.reset_password_dialog);

        //initializing variables
        txtClose = (TextView) myDialog.findViewById(R.id.close);
        edCurrentPswd =  myDialog.findViewById(R.id.current_password);
        edNewPswd     =  myDialog.findViewById(R.id.new_password);
        edConfirmPswd =  myDialog.findViewById(R.id.confirm_password);

        btnReset = (Button) myDialog.findViewById(R.id.reset);
        myDialog.setCancelable(false);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currentPass = edCurrentPswd.getText().toString().trim();
                final String newPass     = edNewPswd.getText().toString().trim();
                final String confirmPass = edConfirmPswd.getText().toString().trim();
                if(currentPass.equals("") && newPass.equals("") && confirmPass.equals("")){
                    edCurrentPswd.setError("Enter current Password");
                    edNewPswd.setError("Enter new Password");
                    edConfirmPswd.setError("Confirm Password");
                }
                else if(currentPass.equals("")){
                    edCurrentPswd.setError("Enter new Password");
                }
                else if(newPass.equals("")){
                    edNewPswd.setError("Enter new Password");
                }
                else if(confirmPass.equals("")){
                    edConfirmPswd.setError("Confirm Password");
                }
                else{
                    if(newPass.equals(confirmPass)){
                        //change password
                        resetPassword(currentPass,newPass,edCurrentPswd);
                    }
                    else{
                        edConfirmPswd.setError("Passwords do not match");
                    }
                }
            }
        });
        myDialog.show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
    private void resetPassword(final String currentPsw, final String newPsw, final TextInputEditText edCurrentPsw) {
        String URL_REGISTER= Constants.BASE_URL +"account/";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                myDialog.dismiss();
                                new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success!")
                                        .setContentText("Password changed successfully").show();
                                //  Toast.makeText(SettingsActivity.this,"Password changed successfully",Toast.LENGTH_LONG).show();

                            }
                            else if(success.equals("0")){
                                Toast.makeText(SettingsActivity.this,"Current Password is incorrect",Toast.LENGTH_LONG).show();
                                edCurrentPsw.setError("Password is incorrect!");
                            }
                            else if(success.equals("2")){
                                Toast.makeText(SettingsActivity.this,"Failed to update password",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SettingsActivity.this,"Error! "+ e.toString(),Toast.LENGTH_LONG).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SettingsActivity.this,"Errors! "+ error.toString(),Toast.LENGTH_LONG).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","reset_password");
                params.put("id",userId);
                params.put("current_psw",currentPsw);
                params.put("new_psw",newPsw);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}
