package com.example.agro_irrigation.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Adapter.CartItemRecyclerViewAdapter;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.AccessToken;
import com.example.agro_irrigation.Models.Cart_Item;
import com.example.agro_irrigation.Models.Pick_Up_Points;
import com.example.agro_irrigation.Models.STKPush;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.Services.DarajaApiClient;
import com.example.agro_irrigation.Services.Utils;
import com.example.agro_irrigation.SessionManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.santalu.maskara.widget.MaskEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import timber.log.Timber;

public class CartActivity extends AppCompatActivity  {
    private RequestQueue requestQueue ;
    private List<Cart_Item> lstCart = new ArrayList<>();
    private TextView grandTotal;
    private RecyclerView myrv;
    private ShimmerFrameLayout shimmerFrameLayoutCart,shimmerFrameLayoutTotal;
    private Dialog myDialog;
    private ArrayList<String> paymentList= new ArrayList<>();
    private List<Pick_Up_Points> locationList = new ArrayList<>();
    private CardView cvEmptyCart;
    ArrayAdapter<String> adapter;
    ArrayAdapter<Pick_Up_Points> locationAdapter;
    SessionManager sessionManager;
    Double totalAmount;
    String session_id,username,phone;
    NumberFormat format;
    Button orderBtn;
    CartItemRecyclerViewAdapter myAdapter;
    Handler handler;
    DarajaApiClient mApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        changeStatusBarColor();
        init();
        if(savedInstanceState == null){
            getCartProducts(session_id);
            Pick_Up_Points points = new Pick_Up_Points("0","Select Point",0.0);
            locationList.add(points);
            getPickUpPoints();
            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,paymentList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            locationAdapter  = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,locationList);
            locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (myDialog != null) {
            myDialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
       // getAccessToken();
    }
    public void init() {
        //initialize variables
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        orderBtn = findViewById(R.id.placeOrder);
        myrv = findViewById(R.id.cart_list);
        format = NumberFormat.getNumberInstance();
        shimmerFrameLayoutCart = findViewById(R.id.shimmer_cart);
        shimmerFrameLayoutTotal = findViewById(R.id.shimmer_total_amount);
        cvEmptyCart  = findViewById(R.id.emptyCart);

        sessionManager = new SessionManager(this);
        handler = new Handler();
        HashMap<String, String> user = sessionManager.getUserDetail();
        session_id = user.get(SessionManager.ID);
        username = user.get(SessionManager.NAME);
        HashMap<String, String>  details  = sessionManager.getAccountDetail();
        phone = details.get(SessionManager.PHONE);

        myDialog = new Dialog(this);
        mApiClient = new DarajaApiClient();
    }
    public void  placeOrderDialog(){
        //This generates a popup to make to place order.
        Button btnProceed;
        final TextInputEditText edName,edPhone;
        final TextInputLayout txtLocation, txtPayment;
        final TextView txtShipping, txtGrandTotal,txtTotal;
        final AutoCompleteTextView drpLocation,drpPayment;
        final String[] Amount = new String[1];
        final double[] charge = {0};
        final double[] total = { 0 };
        TextView txtClose;

        myDialog.setContentView(R.layout.delivery_details_dialog);

        //initializing variables
        txtClose = (TextView) myDialog.findViewById(R.id.close);
        edName  = (TextInputEditText) myDialog.findViewById(R.id.name);
        edPhone = (TextInputEditText) myDialog.findViewById(R.id.phone);
        drpLocation = (AutoCompleteTextView)myDialog.findViewById(R.id.location);
        drpPayment = (AutoCompleteTextView)myDialog.findViewById(R.id.payment);
        txtLocation = (TextInputLayout) myDialog.findViewById(R.id.textLocation);
        //txtPayment = (TextInputLayout) myDialog.findViewById(R.id.textPayment);
        txtGrandTotal = (TextView) myDialog.findViewById(R.id.total_amount);
        txtTotal = (TextView) myDialog.findViewById(R.id.amount);
        txtShipping = (TextView) myDialog.findViewById(R.id.shipment_fee);
        btnProceed = (Button) myDialog.findViewById(R.id.proceed);

        //set Values
        edName.setText(username);
        edPhone.setText("0712345678");
        txtGrandTotal.setText(format.format(totalAmount));
        edName.setVisibility(View.GONE);
        edPhone.setVisibility(View.GONE);
        drpPayment.setText("M-pesa");
        txtTotal.setText(format.format(totalAmount));
        adapter.clear();
        adapter.add("Cash");
        adapter.add("M-pesa");
        drpLocation.setText("Select Point");
        drpPayment.setAdapter(adapter);
        drpLocation.setAdapter(locationAdapter);

        drpLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pick_Up_Points point = (Pick_Up_Points) locationAdapter.getItem(position);

                charge[0] = point.getCharge();
                total[0] =totalAmount+ charge[0];
                Amount[0] = format.format(charge[0]);
                txtShipping.setText(Amount[0]);
                txtGrandTotal.setText(format.format(total[0]));
            }
        });

        myDialog.setCancelable(false);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String point = ((AutoCompleteTextView)txtLocation.getEditText()).getText().toString().trim();

                if(point.equals("Select Point")){
                    // drpLocation.setError("Please select pick-up point");
                    Toast.makeText(CartActivity.this,"Choose a pick-up point",Toast.LENGTH_LONG).show();
                }
                else{
                    paymentPopUp(point,format.format(total[0]));
                }
            }
        });
        myDialog.show();
    }
    public void paymentPopUp(final String point,final String total){
        Button btnProceed;
        TextView txtClose,txtSteps,txtAmount;
        final EditText edTransactionCode;
        myDialog.setContentView(R.layout.payment_dialog);

        //initializing variables
        txtClose = (TextView) myDialog.findViewById(R.id.close);
        txtSteps = (TextView) myDialog.findViewById(R.id.steps);
        txtAmount = (TextView) myDialog.findViewById(R.id.amount);
        btnProceed = (Button) myDialog.findViewById(R.id.proceed);
        edTransactionCode = (EditText) myDialog.findViewById(R.id.payment_code);

        //get values for phone number and total amount
        txtAmount.setText(total);

        txtSteps.setText(Html.fromHtml(this.getString(R.string.steps)));
        myDialog.setCancelable(false);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transaction= edTransactionCode.getText().toString().trim();
//                String regexPattern = "^[A-Z][A-Z0-9]{8}[A-Z]$";
                String regexPattern = "^[A-Z][A-Z0-9]{9}";
                Pattern pattern = Pattern.compile(regexPattern);
                Matcher matcher = pattern.matcher(transaction);
                if(transaction.isEmpty())
                {
                    edTransactionCode.setError("Please enter payment code");
                    return;
                }
                if (!matcher.matches()) {
                    edTransactionCode.setError("Invalid MPESA Code, should be 10 characters long and start with a letter");
                    return;
                }

                placeOrder(point,transaction);
            }
        });
        myDialog.show();
    }
    public void placeOrder(final String point, final  String transaction_code){
        String URL_PLACE_ORDER=Constants.BASE_URL+"customer/";
        StringRequest request = new StringRequest(Request.Method.POST,URL_PLACE_ORDER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Error", "onResponse: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            if (success.equals("1"))
                            {
                                myDialog.dismiss();
                                new SweetAlertDialog(CartActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success!")
                                        .setContentText("Your Order was placed successfully!")
                                        .show();
                                //After placing order, You clear the cart
                                clearCart();
                            }
                            else if (success.equals("0")){
                                Toast.makeText(CartActivity.this,"Sorry! Items not placed. Try Again!",Toast.LENGTH_LONG).show();
                            }
                            else if (success.equals("2")){
                                Toast.makeText(CartActivity.this,"Opps! The transaction No is already used.",Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CartActivity.this,"Error!"+e.toString(),Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CartActivity.this,"Error!"+error.toString(),Toast.LENGTH_LONG).show();
            }

        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                params.put("action", "place_order");
                params.put("id", session_id);
                params.put("total",String.valueOf(totalAmount));
                params.put("jsonArray",transferdata());
                params.put("name",username);
                params.put("point",point);
                params.put("phone",phone);
                params.put("transaction_code",transaction_code);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    public void clearCart(){
        String URL_PLACE_ORDER=Constants.BASE_URL+"customer/";
        StringRequest request = new StringRequest(Request.Method.POST,URL_PLACE_ORDER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Error", "onResponse: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            if (success.equals("1"))
                            {
                                lstCart.clear();
                                myAdapter.notifyDataSetChanged();
                                myrv.setVisibility(View.GONE);
                                cvEmptyCart.setVisibility(View.VISIBLE);
                                shimmerFrameLayoutTotal.setVisibility(View.VISIBLE);
                                grandTotal.setVisibility(View.GONE);

                            }
                            else if (success.equals("0")){
                                Toast.makeText(CartActivity.this,"Sorry!  Try Again!",Toast.LENGTH_LONG).show();
                            }
                            else if (success.equals("2")){
                                Toast.makeText(CartActivity.this,"Failed",Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CartActivity.this,"Error!"+e.toString(),Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CartActivity.this,"Error!"+error.toString(),Toast.LENGTH_LONG).show();
            }

        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                params.put("action", "clear_cart");
                params.put("user_id", session_id);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    public String transferdata(){
        JSONArray jsonArray = new JSONArray();
        try {

            for (Cart_Item cart : lstCart) {
                double price;
                if (cart.getCart_prod_discount().equals("0")){
                    price = Double.parseDouble(cart.getCart_prod_price());
                }
                else{
                    price = ((100- Double.parseDouble(cart.getCart_prod_discount())) * Double.parseDouble(cart.getCart_prod_price()))/100;
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("prod_id", cart.getCart_prod_id());
                jsonObject.put("prod_qty",cart.getCart_qty());
                jsonObject.put("prod_specs",cart.getCart_specs());
                jsonObject.put("prod_price",String.valueOf(price));
                jsonArray.put(jsonObject);
            }

        } catch (JSONException jse){
            jse.printStackTrace();
        }
        return String.valueOf(jsonArray);
    }
    public void getPickUpPoints(){
        String URL_JSON = Constants.BASE_URL+"shipmentmanager/";
        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("Error", "onResponse: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            if (success.equals("1")) {
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Pick_Up_Points point = new Pick_Up_Points();
                                    point.setId(object.getString("id"));
                                    point.setName(object.getString("name"));
                                    point.setCharge(object.getDouble("charge"));
                                    locationList.add(point);

                                }
                            }
                            else if(success.equals("0")){

                            }
                        } catch (JSONException e) {


                            e.printStackTrace();
                        }

                    }
                }, error -> {
                    //Toast.makeText(getContext(), "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","view_pick_up_points");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayoutCart.startShimmer();
        shimmerFrameLayoutTotal.startShimmer();
    }
    @Override
    protected void onPause() {
        super.onPause();
        shimmerFrameLayoutTotal.stopShimmer();
        shimmerFrameLayoutCart.stopShimmer();
    }

    public void getCartProducts(final String user_id) {
        String URL_JSON= Constants.BASE_URL +"customer/";

        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            if (success.equals("1")) {
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Cart_Item cart= new Cart_Item();
                                    cart.setCart_id(object.getString("cart_id"));
                                    cart.setCart_prod_id(object.getString("prod_id"));
                                    cart.setCart_prod_name(object.getString("prod_name"));
                                    cart.setCart_prod_Category(object.getString("prod_category"));
                                    cart.setCart_prod_img(object.getString("prod_image"));
                                    cart.setCart_prod_price(object.getString("prod_price"));
                                    cart.setCart_prod_discount(object.getString("prod_discount"));
                                    cart.setCart_prod_qty(object.getString("prod_qty"));
                                    cart.setCart_qty(object.getString("cart_qty"));
                                    cart.setCart_prod_desc(object.getString("prod_desc"));
                                    cart.setCart_specs(object.getString("prod_specs"));
                                    lstCart.add(cart);
                                    grandTotal.setText("KSH "+format.format(grandTotal()));
                                    totalAmount = grandTotal();
                                    orderBtn.setVisibility(View.VISIBLE);
                                    myrv.setVisibility(View.VISIBLE);
                                    grandTotal.setVisibility(View.VISIBLE);
                                    shimmerFrameLayoutTotal.setVisibility(View.GONE);
                                    orderBtn.setOnClickListener(v -> {
                                        if (lstCart.isEmpty()){
                                            new SweetAlertDialog(CartActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops...")
                                                    .setContentText("Your cart is empty!")
                                                    .show();
                                        }
                                        else {
                                            //Proceed to place order
                                             placeOrderDialog();
                                        }
                                    });

                                }
                            }
                            else if(success.equals("0")){
                                cvEmptyCart.setVisibility(View.VISIBLE);

                                //orderBtn.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        shimmerFrameLayoutCart.setVisibility(View.GONE);

                        setRvadapter(lstCart);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                params.put("action","read_cart");
                params.put("user_id",user_id);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(CartActivity.this);
        requestQueue.add(request);
    }
    public void setRvadapter (List<Cart_Item> lstCart) {
        myAdapter = new CartItemRecyclerViewAdapter(CartActivity.this,lstCart) ;
        myrv.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        myrv.setAdapter(myAdapter);
    }
    public Double grandTotal(){
        Double totalPrice=0.0;
        Double unitPrice = 0.0;
        Double discount = 0.0;
        for(int i = 0; i<lstCart.size();i++){
            discount = Double.parseDouble(lstCart.get(i).getCart_prod_discount());
            if (discount.equals(0)){
                unitPrice = Double.parseDouble(lstCart.get(i).getCart_prod_price());
            }else{
                unitPrice = (100- Double.parseDouble(lstCart.get(i).getCart_prod_discount())) * Double.parseDouble(lstCart.get(i).getCart_prod_price())/100;
            }
            totalPrice+= (Integer.parseInt(lstCart.get(i).getCart_qty()) * unitPrice);
        }
        return totalPrice;
    }
    public void setTotalPrice(Double price){
        Double initialTotal = totalAmount;
        Double totalPrice = initialTotal - price;
        grandTotal.setText("KSH "+format.format(totalPrice));
        totalAmount = totalPrice;
    }
    public void deleteProductCart(final String cartId, final Context c){
        final String URL_DELETE_PROD = Constants.BASE_URL+"customer/";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_DELETE_PROD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                // Toast.makeText(c,"Item removed successfully",Toast.LENGTH_LONG).show();

                                if (lstCart.isEmpty()){
                                    myrv.setVisibility(View.GONE);
                                    cvEmptyCart.setVisibility(View.VISIBLE);
                                    grandTotal.setVisibility(View.GONE);
                                    shimmerFrameLayoutTotal.setVisibility(View.VISIBLE);
                                    //orderBtn.setVisibility(View.GONE);
                                }
                                else{

                                }

                            }
                            else if(success.equals("0")){
                                Toast.makeText(c ,"Sorry! delete failed",Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(c,"Error "+ e.toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Toast.makeText(CartActivity.this,"Error! "+ error.toString(),Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","remove_from_cart");
                params.put("id",cartId);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(c);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
    public void onBackClick(View view){
        onBackPressed();
    }
}
