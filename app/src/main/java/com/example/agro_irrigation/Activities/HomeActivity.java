package com.example.agro_irrigation.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    SessionManager sessionManager;
    private TextView txtWelcomeTxt,txtUserName,txtUserType;
    private CircleImageView imgProfile;
    private CardView cvShop,cvMyOrders,cvProfile,cvSettings,cvHelp,cvOrders,cvPurchases, cvPurchases1, cvPurchases3, cvPurchases2, cvStore,cvPickUpPoints,cvDelivery,cvStock;
    private ImageButton btnLogout;
    private String userId, userType,userName,userProfile;
    private LinearLayout linearLayoutManager,linearLayoutCustomer,linearLayoutSupplier, linearLayoutManager1,linearLayoutDriver,linearLayoutProfile,linearLayoutManager3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        changeStatusBarColor();
        init();

        sessionManager.checkLogin();

    }

    private void checkUserType() {
        if (userType.equals("customer")){
            linearLayoutCustomer.setVisibility(View.VISIBLE);
        }
        else if (userType.equals("supplier")){
            linearLayoutSupplier.setVisibility(View.VISIBLE);
        }
        else if(userType.equals("sales manager")){
            linearLayoutManager.setVisibility(View.VISIBLE);
        }
        else if(userType.equals("shipment manager")){
            linearLayoutManager1.setVisibility(View.VISIBLE);
        }
        else if (userType.equals("store manager")){
            linearLayoutManager3.setVisibility(View.VISIBLE);
        }
        else if (userType.equals("driver")){
            linearLayoutDriver.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        //initialize variables
        txtWelcomeTxt = findViewById(R.id.welcome_txt);
        imgProfile = findViewById(R.id.profile_image);
        btnLogout = findViewById(R.id.logout);
        txtUserName = findViewById(R.id.txtUsername);
        txtUserType = findViewById(R.id.txtUserType);

        //all users
        linearLayoutProfile = (LinearLayout) findViewById(R.id.profile);
        cvProfile = (CardView) findViewById(R.id.my_profile);
        cvSettings = (CardView) findViewById(R.id.settings);
        cvHelp = (CardView) findViewById(R.id.help);

        //customer
        linearLayoutCustomer = (LinearLayout) findViewById(R.id.customer);
        linearLayoutSupplier = (LinearLayout) findViewById(R.id.supplier);
        cvShop = (CardView) findViewById(R.id.shop);
        cvMyOrders = (CardView) findViewById(R.id.my_orders);


        //manager
        linearLayoutManager = findViewById(R.id.manager);
        linearLayoutManager1 = findViewById(R.id.stockControl);
        linearLayoutManager3 = findViewById(R.id.manager3);
        cvOrders = (CardView) findViewById(R.id.orders);
        cvPurchases = (CardView) findViewById(R.id.purchases);
        cvPurchases1 = (CardView) findViewById(R.id.purchases1);
        cvPurchases2 = (CardView) findViewById(R.id.purchases2);
        cvPurchases3 = (CardView) findViewById(R.id.purchases3);
        cvStore = (CardView) findViewById(R.id.stock);
        cvStock = (CardView) findViewById(R.id.store);
        cvPickUpPoints =  (CardView) findViewById(R.id.pickUpPoints);

        //driver
        linearLayoutDriver = findViewById(R.id.driver);
        cvDelivery = (CardView) findViewById(R.id.ordersDelivery);


        //instantiate variables
        sessionManager = new SessionManager(this);
        cvShop.setOnClickListener(this);
        cvMyOrders.setOnClickListener(this);

        cvOrders.setOnClickListener(this);
        cvPurchases.setOnClickListener(this);
        cvPurchases1.setOnClickListener(this);
        cvPurchases2.setOnClickListener(this);
        cvPurchases3.setOnClickListener(this);
        cvStore.setOnClickListener(this);
        cvPickUpPoints.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        cvProfile.setOnClickListener(this);
        cvSettings.setOnClickListener(this);
        cvDelivery.setOnClickListener(this);
        cvStock.setOnClickListener(this);
        cvHelp.setOnClickListener(this);
        btnLogout.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        String user_type;
        HashMap<String, String> user = sessionManager.getUserDetail();
        userId      = user.get(SessionManager.ID);
        userType    = user.get(SessionManager.USER_TYPE);
        userProfile = user.get(SessionManager.PICTURE);

        userName = user.get(SessionManager.NAME);
        checkUserType();
        if (userType.equals("customer")){
            user_type = "Personal Account";
        }else if(userType.equals("sales manager")) {
            user_type = "Financial Manager";
        }
        else{
            user_type = userType.substring(0, 1).toUpperCase() + userType.substring(1).toLowerCase();
        }


//        HashMap<String, String> account = sessionManager.getAccountDetail();
//        userName = account.get(SessionManager.FNAME);
        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar);
        //load image using glide
        Glide.with(this).load(userProfile).apply(requestOptions).into(imgProfile);

        //set values
        Constants.setWelcomeMessage(txtWelcomeTxt,userName);
        txtUserName.setText(userName);
        txtUserType.setText(user_type);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.shop:
                i = new Intent(this, ShopActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            case  R.id.my_profile:
            case  R.id.profile_image:
                i = new Intent(this, ProfileActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            case R.id.settings:
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            case R.id.help:
                i = new Intent(this, HelpActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.my_orders:
            case  R.id.orders :
            case  R.id.stock :
            case  R.id.ordersDelivery :
                i = new Intent(this, OrdersActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            case R.id.purchases:
            case R.id.purchases1:
            case R.id.purchases2:
            case R.id.purchases3:
                i = new Intent(this, SalesActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            case R.id.pickUpPoints:
                i = new Intent(this, LocationActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            case R.id.store:
                i = new Intent(this, StoreActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                break;
            default:
                break;
        }
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


    @Override
    public void onBackPressed() {
        new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
                .setContentText("Exit Application?")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
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
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
}
