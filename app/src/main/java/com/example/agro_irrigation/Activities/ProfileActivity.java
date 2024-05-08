package com.example.agro_irrigation.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.itextpdf.text.DocumentException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class ProfileActivity extends AppCompatActivity {
    Toolbar toolbar;
    private static final String TAG = HomeActivity.class.getSimpleName() ;
    private ArrayList<String> genderList= new ArrayList<>();
    AutoCompleteTextView drpGender;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    String userProfilePic, userId, userType, userEmail,encoded_image, userName;
    SessionManager sessionManager;
    TextInputEditText edFname,edSname,edEmail,edPhone;
    TextInputLayout txtGender;
    CircleImageView imgProfile;
    AwesomeValidation awesomeValidation;
    Bitmap bitmap;
    ImageView btnSelectImage;
    Button btnSubmit;
    Menu action;
    String fname,sname,gender,phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        changeStatusBarColor();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("My Account");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        edFname = findViewById(R.id.fname);
        edSname = findViewById(R.id.sname);
        edPhone = findViewById(R.id.phone);
        edEmail = findViewById(R.id.email);
        drpGender = findViewById(R.id.gender);
        imgProfile  = findViewById(R.id.profile_img);
        txtGender = findViewById(R.id.textGender);
        btnSubmit = (Button) findViewById(R.id.submit);
        btnSelectImage = (ImageView) findViewById(R.id.select_pic);

        sessionManager = new SessionManager(this);

        HashMap<String, String> user = sessionManager.getUserDetail();
        userId = user.get(sessionManager.ID);
        userType = user.get(sessionManager.USER_TYPE);
        userProfilePic = user.get(sessionManager.PICTURE);
        userEmail    = user.get(sessionManager.EMAIL);
        userName    = user.get(sessionManager.NAME);

        HashMap<String, String> account = sessionManager.getAccountDetail();
        fname = account.get(sessionManager.FNAME);
        sname = account.get(sessionManager.SNAME);
        gender = account.get(sessionManager.GENDER);
        phone = account.get(sessionManager.PHONE);


        //setting the edText values
        edFname.setText(fname);
        edSname.setText(sname);
        edPhone.setText(phone);
        edEmail.setText(userEmail);
        drpGender.setText(gender);

        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar);

        //load image using glide
        Glide.with(this).load(userProfilePic).apply(requestOptions).into(imgProfile);

        //initialize validation style
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        // add validations
        awesomeValidation.addValidation(ProfileActivity.this,R.id.fname,
                RegexTemplate.NOT_EMPTY,R.string.invalid_name);
        awesomeValidation.addValidation(ProfileActivity.this,R.id.sname,
                RegexTemplate.NOT_EMPTY,R.string.invalid_name);
        awesomeValidation.addValidation(ProfileActivity.this,R.id.email,
                Patterns.EMAIL_ADDRESS,R.string.invalid_email);
        awesomeValidation.addValidation(ProfileActivity.this,R.id.phone,
                "^[0].{9,}",R.string.invalid_phone_no);

        //get user Details and set the edit text values


        // Setting the edit texts to view mode, i.e, restrict editing
        edFname.setFocusable(false);
        drpGender.setFocusable(false);
        edSname.setFocusable(false);
        edEmail.setFocusable(false);
        edPhone.setFocusable(false);
        edFname.setFocusableInTouchMode(false);
        edSname.setFocusableInTouchMode(false);
        drpGender.setFocusableInTouchMode(false);
        edPhone.setFocusableInTouchMode(false);
        edEmail.setFocusableInTouchMode(false);
        btnSubmit.setVisibility(GONE);
        btnSelectImage.setVisibility(GONE);

        genderList.add("Male");
        genderList.add("Female");


        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(ProfileActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                pickImage();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }


        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(awesomeValidation.validate()){

                    try {
                        createPdfWrapper();
                        //createReport();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException | IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    Toast.makeText(ProfileActivity.this,"Please Enter the Required Field(s)", Toast.LENGTH_SHORT).show();

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater= getMenuInflater();
        menuInflater.inflate(R.menu.menu_action,menu);
        action=menu;
        action.findItem(R.id.menu_save).setVisible(false);
        return true;
    }
    private void createPdfWrapper() throws IOException, DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Storage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            updateAccount();
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_edit:
                editMode();
                return true;

            case R.id.menu_save:
                viewMode();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void pickImage() {
        CropImage.startPickImageActivity(this);
    }

    //CROP REQUEST JAVA
    private void croprequest(Uri imageUri){
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    private void updateAccount() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();
        String URL_ADD_PROD= Constants.BASE_URL+"account/";
        final String fname = Objects.requireNonNull(edFname.getText()).toString().trim();
        final String sname = Objects.requireNonNull(edSname.getText()).toString().trim();
        final String gender = ((AutoCompleteTextView)txtGender.getEditText()).getText().toString().trim();
        final String email = Objects.requireNonNull(edEmail.getText()).toString().trim();
        final String phone = Objects.requireNonNull(edPhone.getText()).toString().trim();

        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_ADD_PROD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "onResponse: "+response);
                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                String username = fname+" "+ sname ;
                                sessionManager.createSession(username,email,userId,userType,userProfilePic);
                                sessionManager.createAccount(fname,sname,gender,phone);
                                Toast.makeText(ProfileActivity.this,"Profile Updated successfully!",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                viewMode();

                            }
                            else if(success.equals("2")){
                                Toast.makeText(ProfileActivity.this,"Failed!",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                            else if(success.equals("0")){
                                Toast.makeText(ProfileActivity.this,"Sorry! An account with the same email exists",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ProfileActivity.this,"Error "+ e.toString(),Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ProfileActivity.this,"Error! "+ error.toString(),Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","update_profile");
                params.put("id",userId);
                params.put("user_type",userType);
                params.put("fname",fname);
                params.put("sname",sname);
                params.put("email",email);
                params.put("initial_email",userEmail);
                params.put("phone",phone);
                params.put("gender",gender);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();
        String URL_ADD_PROD=Constants.BASE_URL+"account/";
        final String photo=encoded_image;
        //Log.i(TAG,prod_category);
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_ADD_PROD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,response);
                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                String path = jsonObject.getString("path");
                                sessionManager.createSession(userName,userEmail,userId,userType,path);
                                Toast.makeText(ProfileActivity.this,"Image uploaded successfully!",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                            else if(success.equals("0")) {
                                Toast.makeText(ProfileActivity.this, "Failed to update Image!", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ProfileActivity.this,"Error "+ e.toString(),Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ProfileActivity.this,"Upload Error! "+ error.toString(),Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","upload_image");
                params.put("id",userId);
                params.put("image",photo);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //RESULT FROM SELECTED IMAGE
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode
                == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            croprequest(imageUri);
        }

        //RESULT FROM CROPPING ACTIVITY

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    imgProfile.setImageBitmap(bitmap);
                    imageStore(bitmap);
                    uploadImage();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void imageStore(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte [] imageBytes = stream.toByteArray();
        encoded_image= Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    public void editMode(){
        edFname.setFocusable(true);
        edSname.setFocusable(true);
        drpGender.setFocusable(true);
        edPhone.setFocusable(true);
        edEmail.setFocusable(true);

        edFname.setFocusableInTouchMode(true);
        edSname.setFocusableInTouchMode(true);
        drpGender.setFocusableInTouchMode(true);
        edPhone.setFocusableInTouchMode(true);
        edEmail.setFocusableInTouchMode(true);

        btnSubmit.setVisibility(View.VISIBLE);
        //btnCancel.setVisibility(View.VISIBLE);
        btnSelectImage.setVisibility(View.VISIBLE);
        //populate the spinner with drivers



        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,genderList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drpGender.setAdapter(adapter);


        action.findItem(R.id.menu_edit).setVisible(false);
        action.findItem(R.id.menu_save).setVisible(true);
    }

    public void viewMode(){
        action.findItem(R.id.menu_edit).setVisible(true);
        action.findItem(R.id.menu_save).setVisible(false);
        edFname.setFocusable(false);
        edSname.setFocusable(false);
        drpGender.setFocusable(false);
        edPhone.setFocusable(false);
        edEmail.setFocusable(false);

        edFname.setFocusableInTouchMode(false);
        edSname.setFocusableInTouchMode(false);
        edPhone.setFocusableInTouchMode(false);
        drpGender.setFocusableInTouchMode(false);
        edEmail.setFocusableInTouchMode(false);

        btnSubmit.setVisibility(GONE);
        //categoryList.clear();
        // btnCancel.setVisibility(GONE);
        drpGender.setAdapter((ArrayAdapter<String>)null);
        btnSelectImage.setVisibility(GONE);

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

