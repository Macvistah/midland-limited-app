package com.example.agro_irrigation.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class LoginActivity extends AppCompatActivity {
    private EditText txtEmail, txtPassword;

    public String userID = "";

    private LinearLayout aboutUsLayout;
    private CircularProgressButton btnLogin;
    AwesomeValidation awesomeValidation;
    SessionManager sessionManager;
    Dialog myDialog, abtUsDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        changeStatusBarColor();

        //initialize the variables
        txtEmail = (EditText) findViewById(R.id.email);
        txtPassword = (EditText) findViewById(R.id.password);
        btnLogin = (CircularProgressButton) findViewById(R.id.btnLogin);
        myDialog = new Dialog(this);
        abtUsDialog = new Dialog(this);
        aboutUsLayout = (LinearLayout) findViewById(R.id.learn_more);

        //initialize validation style
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        sessionManager = new SessionManager(this);

        // add validations
        awesomeValidation.addValidation(LoginActivity.this,R.id.email,
                RegexTemplate.NOT_EMPTY,R.string.empty_email);
        awesomeValidation.addValidation(LoginActivity.this,R.id.email,
                Patterns.EMAIL_ADDRESS,R.string.invalid_email);
        awesomeValidation.addValidation(LoginActivity.this,R.id.password,
                RegexTemplate.NOT_EMPTY,R.string.invalid_password);

        btnLogin.setOnClickListener(v -> {
            btnLogin.startAnimation();
            if(awesomeValidation.validate()){
                String email    = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                loginUser(email,password);
            }else{
                btnLogin.revertAnimation();
            }
        });
        aboutUsLayout.setOnClickListener(v -> onAboutUsClick());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (abtUsDialog != null) {
            abtUsDialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
        // getAccessToken();
    }

    private void loginUser(String email, String password) {
        String URL_LOGIN = Constants.BASE_URL +"login/";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_LOGIN,
                response -> {
                    btnLogin.revertAnimation();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        JSONArray jsonArray = jsonObject.getJSONArray("login");
                        switch (success) {
                            case "1":
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String username = object.getString("username").trim();
                                    String user_email = object.getString("email").trim();
                                    String id = object.getString("id").trim();
                                    String user_type = object.getString("user_type").trim();
                                    String photo = object.getString("photo").trim();

                                    sessionManager.createSession(username, user_email, id, user_type, photo);
                                    getUserDetails(user_email, user_type);
                                    if (user_type.equals("admin")) {
                                        Toast.makeText(LoginActivity.this, "This is an admin account. Log in using web url",
                                                Toast.LENGTH_LONG).show();

                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        intent.putExtra("username", username);
                                        intent.putExtra("email", user_email);
                                        startActivity(intent);

                                    }
                                }

                                break;
                            case "0":

                                //Toast.makeText(LoginActivity.this, "Wrong Email/password combination ",Toast.LENGTH_SHORT).show();
                                txtPassword.setError("Enter correct password");
                                break;
                            case "2":
                                Toast.makeText(LoginActivity.this,
                                        "User does not exist! Please sign up ", Toast.LENGTH_SHORT).show();
                                break;
                            case "3":

                                Toast.makeText(LoginActivity.this,
                                        "Account is inactive! Please contact the Admin", Toast.LENGTH_LONG).show();
                                break;
                            case "4":

                                Toast.makeText(LoginActivity.this,
                                        "Account not yet approved! Contact the admin", Toast.LENGTH_LONG).show();
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(LoginActivity.this,
                                "Error plus: " + e.toString(), Toast.LENGTH_LONG).show();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btnLogin.revertAnimation();
                        Toast.makeText(LoginActivity.this,
                                "Error: "+error.toString(),Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams () throws AuthFailureError {
                Map <String, String> params = new HashMap<>();
                params.put("action","login");
                params.put ("email", email);
                params.put ("password", password);
                return params;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void getUserDetails(final String userEmail, final String userType) {
        String URL_JSON = Constants.BASE_URL+"account/";
        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log.i(TAG, "onResponse: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            if (success.equals("1")) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String fname = object.getString("fname");
                                    String sname = object.getString("sname");
                                    String email = object.getString("email");
                                    String phone = object.getString("phone");
                                    String gender = object.getString("gender");
                                    sessionManager.createAccount(fname,sname,gender,phone);
                                }
                            }
                            else if(success.equals("0")){

                            }
                        } catch (JSONException e) {

                            // Toast.makeText(MainActivity.this, "Error! " + e.toString(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Toast.makeText(MainActivity.this, "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","view_account");
                params.put("email",userEmail);
                params.put("user_type",userType);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }
    }

    public void onLoginClick(View View){
        startActivity(new Intent(this,SignUpActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
    }
    public void onForgotPasswordClick(View View){
        CircularProgressButton btnRequestPass, btnValidateOTP, btnChangePass;
        LinearLayout stepOne, stepTwo, stepThree;
        EditText edEmail, edPassword, edConfirmPass, edOTPCode;

        TextView txtClose, txtHelper;
        myDialog.setContentView(R.layout.forgot_password_dialog);

        AtomicReference<String> userId = new AtomicReference<>("");

        //initialize variables
        txtClose = (TextView) myDialog.findViewById(R.id.close);
        txtHelper = (TextView) myDialog.findViewById(R.id.helperTxt);

        stepOne = (LinearLayout) myDialog.findViewById(R.id.stepOne);
        stepTwo = (LinearLayout) myDialog.findViewById(R.id.stepTwo);
        stepThree = (LinearLayout) myDialog.findViewById(R.id.stepThree);

        btnRequestPass = (CircularProgressButton) myDialog.findViewById(R.id.btnResetPassword);
        btnValidateOTP = (CircularProgressButton) myDialog.findViewById(R.id.btnValidateOTP);
        btnChangePass = (CircularProgressButton) myDialog.findViewById(R.id.btnChangePass);

        edEmail = (EditText) myDialog.findViewById(R.id.emailAddress);
        edOTPCode = (EditText) myDialog.findViewById(R.id.otp);
        edPassword = (EditText) myDialog.findViewById(R.id.new_password);
        edConfirmPass = (EditText) myDialog.findViewById(R.id.confirm_password);

        //initializeItems
        stepOne.setVisibility(android.view.View.VISIBLE);
        stepTwo.setVisibility(android.view.View.GONE);
        stepThree.setVisibility(android.view.View.GONE);

        myDialog.setCancelable(false);

        txtClose.setOnClickListener(v -> myDialog.dismiss());

        btnRequestPass.setOnClickListener(v ->{
            String email = edEmail.getText().toString().trim();
            validateEmail(email, txtHelper, stepOne, stepTwo, btnRequestPass);
        });
        btnValidateOTP.setOnClickListener(v ->{
            String otp = edOTPCode.getText().toString().trim();
            if (otp.length() != 5){
                edOTPCode.setError("Invalid OTP, should be 5 characters long!");
                return;
            }
            validateOTP(otp, txtHelper, stepTwo, stepThree, btnValidateOTP);
        });

        btnChangePass.setOnClickListener(v -> {
            String password = edPassword.getText().toString().trim();
            String confirmPassword = edConfirmPass.getText().toString().trim();

            if (password.length() < 8){
                edPassword.setError("Password should be at least 8 characters long!");
                return;
            }

            if (!password.equals(confirmPassword)){
                edConfirmPass.setError("Password does not match");
                return;
            }
            changePassword(password, myDialog, btnChangePass);

        });


        myDialog.show();

    }
    
    
    
    public void validateEmail (String email, TextView txtHeader, LinearLayout stepOne, LinearLayout stepTwo, CircularProgressButton  btnRequestPass ){
        String URL_VALIDATE_EMAIL = Constants.URL +"index.php?r=site/forgot-password&email="+email;
        btnRequestPass.startAnimation();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, URL_VALIDATE_EMAIL,
                response -> {
                    btnRequestPass.revertAnimation();
                    try {
                        JSONObject jsonObject= new JSONObject(response);
                        String success=jsonObject.getString("success");
                        String message = jsonObject.getString("message");

                        if (success.equals("1")){
                            userID = jsonObject.getString("user_id");
                            //Toast.makeText(LoginActivity.this, userID,Toast.LENGTH_LONG).show();
                            txtHeader.setText(message);
                            stepOne.setVisibility(android.view.View.GONE);
                            stepTwo.setVisibility(android.view.View.VISIBLE);
                            Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(LoginActivity.this,
                                "Error plus: "+ e,Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    btnRequestPass.revertAnimation();
                    Toast.makeText(LoginActivity.this,
                            "Error: "+error.toString(),Toast.LENGTH_SHORT).show();

                });
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    public void validateOTP (String otp, TextView txtHeader, LinearLayout stepTwo, LinearLayout stepThree, CircularProgressButton  btnValidateOTP ){
        String URL_VALIDATE_OTP = Constants.URL +"index.php?r=site/verify-otp&userId="+userID+"&otp="+otp;
        //Toast.makeText(LoginActivity.this, URL_VALIDATE_OTP,Toast.LENGTH_LONG).show();
        btnValidateOTP.startAnimation();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, URL_VALIDATE_OTP,
                response -> {
                    btnValidateOTP.revertAnimation();
                    try {
                        JSONObject jsonObject= new JSONObject(response);
                        String success=jsonObject.getString("success");
                        String message = jsonObject.getString("message");

                        if (success.equals("1")){
                            txtHeader.setText(message);
                            stepTwo.setVisibility(android.view.View.GONE);
                            stepThree.setVisibility(android.view.View.VISIBLE);
                            Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(LoginActivity.this,
                                "Error plus: "+ e,Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    btnValidateOTP.revertAnimation();
                    Toast.makeText(LoginActivity.this,
                            "Error: "+error.toString(),Toast.LENGTH_SHORT).show();

                });
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    public void changePassword (String password, Dialog myDialog, CircularProgressButton  btnChangePass ){
        String URL_VALIDATE_EMAIL = Constants.URL +"index.php?r=site/reset-password&userId="+userID+"&password="+password;
        btnChangePass.startAnimation();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, URL_VALIDATE_EMAIL,
                response -> {
                    btnChangePass.revertAnimation();
                    try {
                        JSONObject jsonObject= new JSONObject(response);
                        String success=jsonObject.getString("success");
                        String message = jsonObject.getString("message");

                        if (success.equals("1")){
                            Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();
                            myDialog.dismiss();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, message,Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(LoginActivity.this,
                                "Error plus: "+ e,Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    btnChangePass.revertAnimation();
                    Toast.makeText(LoginActivity.this,
                            "Error: "+error.toString(),Toast.LENGTH_SHORT).show();

                });
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }




    public void onAboutUsClick(){
        TextView txtClose, txtAboutUs;
        abtUsDialog.setContentView(R.layout.about_us_dialog);

        txtClose = (TextView) abtUsDialog.findViewById(R.id.close);
        txtAboutUs = (TextView) abtUsDialog.findViewById(R.id.aboutUs);

        txtAboutUs.setText(Html.fromHtml(this.getString(R.string.about_us_long)));

//        abtUsDialog.setCancelable(false);
        txtClose.setOnClickListener(v -> abtUsDialog.dismiss());
        abtUsDialog.show();

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
