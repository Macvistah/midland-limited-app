package com.example.agro_irrigation.Activities;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ContactUsActivity extends AppCompatActivity {
    Toolbar toolbar;
    private static final String TAG = HomeActivity.class.getSimpleName() ;
    private ArrayList<String> subjectList= new ArrayList<>();
    private ArrayList<String> toList= new ArrayList<>();
    AutoCompleteTextView drpSubject, drpTo;
    String userId,userEmail;
    SessionManager sessionManager;
    TextInputEditText edEmail,edMessage;
    TextInputLayout txtSubject, txtTo;
    AwesomeValidation awesomeValidation;
    Button btnSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        changeStatusBarColor();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("Feedback");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        drpSubject = findViewById(R.id.subject);
        drpTo = (AutoCompleteTextView) findViewById(R.id.toAut);
        edEmail = findViewById(R.id.email);
        edMessage  = findViewById(R.id.message);
        btnSubmit = (Button) findViewById(R.id.submit);
        txtSubject = (TextInputLayout) findViewById(R.id.textSubject);
        txtTo = (TextInputLayout) findViewById(R.id.textTo);

        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetail();
        userId = user.get(sessionManager.ID);
        userEmail = user.get(sessionManager.EMAIL);

        //initialize validation style
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        // add validations
        awesomeValidation.addValidation(ContactUsActivity.this,R.id.email,
                Patterns.EMAIL_ADDRESS,R.string.invalid_email);
        awesomeValidation.addValidation(ContactUsActivity.this,R.id.message,
                RegexTemplate.NOT_EMPTY,R.string.empty_message);
        awesomeValidation.addValidation(ContactUsActivity.this,R.id.toAut,
                RegexTemplate.NOT_EMPTY, R.string.empty_message);

        //populate the spinner with drivers

        edEmail.setText(userEmail);

        //populate subject list
        setSubjectList();

        //populate toList
        setToList();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(awesomeValidation.validate()){
                    sendMessage();

                }
            }
        });
    }

    private void setSubjectList () {
        subjectList.add("App Crashes");
        subjectList.add("Service Issues");
        subjectList.add("Complaints");
        subjectList.add("Unavailable Service");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,subjectList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drpSubject.setAdapter(adapter);
    }

    private void setToList () {
        toList.add("Admin");
        toList.add("Sales Manager");
        toList.add("Store Manager");
        toList.add("Shipment Manager");
        toList.add("Driver");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,toList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drpTo.setAdapter(adapter);
    }


    private void sendMessage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Message...");
        progressDialog.show();
        String URL_ADD_PROD= Constants.BASE_URL +"account/";
        final String email= Objects.requireNonNull(edEmail.getText()).toString().trim();
        final String message= Objects.requireNonNull(edMessage.getText()).toString().trim();
        final String subject =((AutoCompleteTextView)txtSubject.getEditText()).getText().toString().trim();
        final String to =((AutoCompleteTextView)txtTo.getEditText()).getText().toString().trim();

        //Log.i(TAG,prod_category);
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_ADD_PROD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                progressDialog.dismiss();
                                new SweetAlertDialog(ContactUsActivity.this,SweetAlertDialog.SUCCESS_TYPE)
                                        .setContentText("Feedback has been received! Thank You.").show();
                                //Toast.makeText(ContactUsActivity.this,"Message sent successfully!",Toast.LENGTH_LONG).show();
                                cleardetails();
                            }
                            else if(success.equals("2")){
                                //Toast.makeText(ContactUsActivity.this,"Message could not be sent!",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                new SweetAlertDialog(ContactUsActivity.this,SweetAlertDialog.ERROR_TYPE)
                                        .setContentText("Feedback was not sent! Try Again.").show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ContactUsActivity.this,"Error "+ e.toString(),Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ContactUsActivity.this,"Error! "+ error.toString(),Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","send_message");
                params.put("id",userId);
                params.put("email",email);
                params.put("subject",subject);
                params.put("to",to);
                params.put("message",message);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void cleardetails(){
        edMessage.setText(null);
        drpSubject.setText(null);
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
}
