package com.example.agro_irrigation.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignUpActivity extends AppCompatActivity {
    private EditText txtFname,txtLname, txtEmail, txtPhone,txtPassword,txtConfirmPassword;
    private CircularProgressButton btnRegister;
    private RadioGroup grpGender;
    private RadioButton btnGender;
    AwesomeValidation awesomeValidation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        changeStatusBarColor();
        //initialize the variables
        txtFname = (EditText) findViewById(R.id.fname);
        txtLname = (EditText) findViewById(R.id.lname);
        grpGender = findViewById(R.id.genderGrp);
        txtEmail = (EditText) findViewById(R.id.email);
        txtPhone = (EditText) findViewById(R.id.phone_no);
        txtPassword = (EditText) findViewById(R.id.password);
        txtConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        btnRegister = (CircularProgressButton) findViewById(R.id.btnRegister);

        //initialize validation style
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        // add validations
        awesomeValidation.addValidation(SignUpActivity.this,R.id.fname,
                RegexTemplate.NOT_EMPTY,R.string.invalid_name);
        awesomeValidation.addValidation(SignUpActivity.this,R.id.lname,
                RegexTemplate.NOT_EMPTY,R.string.invalid_name);
        awesomeValidation.addValidation(SignUpActivity.this,R.id.email,
                Patterns.EMAIL_ADDRESS,R.string.invalid_email);
        awesomeValidation.addValidation(SignUpActivity.this,R.id.phone_no,
                "^[0].{9,}",R.string.invalid_phone_no);
        awesomeValidation.addValidation(SignUpActivity.this,R.id.password,
                RegexTemplate.NOT_EMPTY,R.string.invalid_password);
        awesomeValidation.addValidation(SignUpActivity.this,R.id.confirmPassword,
                RegexTemplate.NOT_EMPTY,R.string.confirm_password);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRegister.startAnimation();
                if(awesomeValidation.validate()){
                    final String firstName = txtFname.getText().toString().trim();
                    final String lastName = txtLname.getText().toString().trim();
                    int radioId_gender = grpGender.getCheckedRadioButtonId();
                    btnGender = findViewById(radioId_gender);
                    final String gender=btnGender.getText().toString().trim();
                    final String email    = txtEmail.getText().toString().trim();
                    final String phone_no = txtPhone.getText().toString().trim();
                    final String password = txtPassword.getText().toString().trim();
                    String confirmPassword = txtConfirmPassword.getText().toString().trim();
                    if(confirmPassword.equals(password)){
                        btnRegister.revertAnimation();
                        // Dialog to select the user_type
                     new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Confirmation")
                                .setContentText("Are you sure you want to register account?")
                                .setConfirmText("Yes")
                                .setCancelText("Cancel")
//                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                        String userType = "farmer";
//                                        registerUser(sweetAlertDialog,firstName,lastName,gender,email,phone_no,password,userType);
//                                    }
//                                })
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        String userType = "customer";
                                        registerUser(sDialog,firstName,lastName,gender,email,phone_no,password,userType);
                                    }
                                })
                                .show();

                    }else{
                        btnRegister.revertAnimation();
                        txtConfirmPassword.setError("Passwords do not match. Try Again");
                    }
                }else{
                    btnRegister.revertAnimation();
                }
            }
        });
    }
    private void registerUser(SweetAlertDialog dialog, String firstName, String lastName, String gender, String email, String phone_no, String password, String userType) {
        String URL_REGISTER = Constants.BASE_URL+"login/";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                dialog.setTitleText("Success!")
                                        .setContentText("Account Registered Successfully!")
                                        .setCancelText("Dismiss")
                                        .setCancelClickListener(null)
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                Intent intent=new Intent(SignUpActivity.this,LoginActivity.class);
                                                intent.putExtra("email",email);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }
                            else if(success.equals("0")){
                                dialog.setTitleText("Error!")
                                        .setContentText("Registration Failed!")
                                        .setConfirmText("Try Again")
                                        .setCancelText("Dismiss")
                                        .setCancelClickListener(null)
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            }
                            else if(success.equals("2")){
                                dialog.setTitleText("Warning!")
                                        .setContentText("User with a similar Email Exists!")
                                        .setConfirmText("Try Again")
                                        .setCancelText("Dismiss")
                                        .setCancelClickListener(null)
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .changeAlertType(SweetAlertDialog.WARNING_TYPE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dialog.dismiss();
                            Toast.makeText(SignUpActivity.this,"Registration "+ e.toString(),Toast.LENGTH_LONG).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(SignUpActivity.this,"Registration Error! "+ error.toString(),Toast.LENGTH_LONG).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","signup");
                params.put("fname",firstName);
                params.put("sname",lastName);
                params.put("gender",gender);
                params.put("email",email);
                params.put("phone_no",phone_no);
                params.put("password",password);
                params.put("user_type",userType);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
    public void onLoginClick(View view){
        startActivity(new Intent(this,LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
