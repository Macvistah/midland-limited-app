package com.example.agro_irrigation.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProductDetailsActivity extends AppCompatActivity {
    Toolbar toolbar;
    String get_id, name, category, qty,new_price, image, price, session_id,desc;
    SessionManager sessionManager;
    private TextView txt_name, txt_status, txt_price,txt_desc,txt_new_price,txtAvailableStock;
    private LinearLayout lAvailableStock;
    private Button btn_add_to_cart, btn_update_cart,btn_buy_now;
    private EditText edQty;
    private ImageView imgProduct,increase,decrease;
    TextView textCartItemCount;
    int cartCount=0;
    String baseUrl;
    NumberFormat format = NumberFormat.getNumberInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        changeStatusBarColor();
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();
        toolbar = (Toolbar) findViewById(R.id.toolbar_product);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("Product Details");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if(savedInstanceState == null) {
            //getting the details
            get_id = getIntent().getExtras().getString("prod_id");
            name = getIntent().getExtras().getString("prod_name");
            category = getIntent().getExtras().getString("category");
            new_price = getIntent().getExtras().getString("new_price");
            qty = getIntent().getExtras().getString("quantity");
            image = getIntent().getExtras().getString("prod_image");
            price = getIntent().getExtras().getString("prod_price");
            desc = getIntent().getExtras().getString("prod_desc");
        }

        HashMap<String, String> user = sessionManager.getUserDetail();
        session_id = user.get(sessionManager.ID);

   
        baseUrl = Constants.BASE_URL;



        //Initialize the variables
        txt_name = (TextView) findViewById(R.id.prod_name);
        txt_price = (TextView) findViewById(R.id.prod_price);
        txt_new_price = (TextView) findViewById(R.id.new_price);
        txt_status = (TextView) findViewById(R.id.prod_status);
        txt_desc = (TextView) findViewById(R.id.prod_description);
        increase = (ImageView) findViewById(R.id.increaseQty);
        decrease = (ImageView) findViewById(R.id.decreaseQuantity);
        txtAvailableStock = (TextView) findViewById(R.id.stock_qty);
        lAvailableStock = (LinearLayout) findViewById(R.id.available_stock);



        edQty = (EditText) findViewById(R.id.qty);
        imgProduct = (ImageView) findViewById(R.id.prod_image);

        btn_add_to_cart = (Button) findViewById(R.id.add_to_cart_btn);


        // btn_update_cart = (Button) findViewById(R.id.update_cart_btn);

        //setting values to each variable
        txt_name.setText(name);
        txtAvailableStock.setText(qty+" Units");

        if (format.format(Double.parseDouble(new_price)).equals(format.format(Double.parseDouble(price)))){
            txt_price.setVisibility(GONE);
            txt_new_price.setText("Ksh " + format.format(Double.parseDouble(price)));
        }else{
            txt_price.setVisibility(VISIBLE);
            txt_price.setText("Ksh " + format.format(Double.parseDouble(price)));
            txt_new_price.setText("Ksh " + format.format(Double.parseDouble(new_price)));
        }
        txt_desc.setText(desc);
        edQty.setText("1");
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.welcome_midland)
                .error(R.drawable.welcome_midland);

        //load image using glide
        Glide.with(this).load(image).apply(requestOptions).into(imgProduct);


        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQty();
            }
        });
        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQty();
            }
        });
        getCartCount(session_id);
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Determines the status of the item
        productStatus();
        verifyProduct(get_id, session_id);

        //Toast.makeText(ProductDetailsActivity.this,"Cart count "+cartCount,Toast.LENGTH_SHORT).show();
    }

    private void verifyProduct(final String id, final String user_id) {
        String URL_VERIFY = baseUrl+"customer/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_VERIFY,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");

                            if (success.equals("1")) {
                                String qty = jsonObject.getString("qty");
                                btn_add_to_cart.setText("Update Cart");
                                edQty.setText(qty);
                                btn_add_to_cart.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        verifyQty();
                                    }
                                });
                            } else if (success.equals("0")) {
                                //set onclick listener  to add button
                                btn_add_to_cart.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String buttonText = btn_add_to_cart.getText().toString().trim();
                                        String qty = edQty.getText().toString().trim();
                                        if (buttonText.equals("Update Cart")){
                                            if (qty.isEmpty()){
                                                edQty.setError("Please input quantity");
                                            }else{
                                                verifyQty();
                                            }
                                        }
                                        else{
                                            if (qty.isEmpty()){
                                                edQty.setError("Please input quantity");
                                            }else{
                                                verifyQuantity();
                                            }

                                        }

                                    }
                                });
                            }
                        } catch (JSONException e) {

                            Toast.makeText(ProductDetailsActivity.this,
                                    "Error Reading Details" + e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(ProductDetailsActivity.this,
                                "Error Reading Details" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "verify_product_cart");
                params.put("prod_id", id);
                params.put("user_id", user_id);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void addToCart() {
        String URL_ADD_TO_CART = baseUrl+"customer/";
        final String prod_id = get_id;
        final String user_id = session_id;
        final String user_qty = this.edQty.getText().toString().trim();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ADD_TO_CART,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");

                            if (success.equals("1")) {
                                btn_add_to_cart.setText("Update Cart");
                                Toast.makeText(ProductDetailsActivity.this,
                                        "Product added to Cart", Toast.LENGTH_SHORT).show();
                                getCartCount(session_id);
                            } else if (success.equals("0")) {
                                Toast.makeText(ProductDetailsActivity.this,
                                        "Error Reading Details", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();

                            Toast.makeText(ProductDetailsActivity.this,
                                    "Error Reading Details" + e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                        Toast.makeText(ProductDetailsActivity.this,
                                "Error Reading Details" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "add_to_cart");
                params.put("prod_id", prod_id);
                params.put("user_id", user_id);
                params.put("qty", user_qty);
                params.put("specs","");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }

    public void productStatus() {
        int prodQty;
        prodQty = Integer.parseInt(qty);
        if (prodQty <= 0) {
            btn_add_to_cart.setVisibility(GONE);
            edQty.setFocusable(false);
            edQty.setFocusableInTouchMode(false);
            txt_status.setVisibility(VISIBLE);
            lAvailableStock.setVisibility(GONE);
        } else {
            txt_status.setVisibility(GONE);
        }
    }

    private void verifyQuantity() {
        int prodQty;
        int edQuantity = 0;
        prodQty = Integer.parseInt(qty);
        String eQty = this.edQty.getText().toString().trim();
        edQuantity = Integer.parseInt(eQty);
        if (edQuantity > prodQty) {
            edQty.setText(qty);
            Toast.makeText(ProductDetailsActivity.this, "Sorry! Maximum quantity available is " + prodQty,
                    Toast.LENGTH_SHORT).show();
        } else if (edQuantity > 0 && edQuantity <= prodQty) {
            addToCart();
        }
    }

    private void verifyQty() {
        int prodQty;
        int edQuantity = 0;
        prodQty = Integer.parseInt(qty);
        String eQty = this.edQty.getText().toString().trim();
        edQuantity = Integer.parseInt(eQty);
        if (edQuantity > prodQty) {
            edQty.setText(qty);
            Toast.makeText(ProductDetailsActivity.this, "Sorry! Maximum quantity available is " + prodQty,
                    Toast.LENGTH_SHORT).show();
        } else if (edQuantity > 0 && edQuantity <= prodQty) {
            UpdateToCart();
        }
    }

    private void UpdateToCart() {
        String URL_UPDATE_TO_CART = baseUrl+"customer/";
        final String prod_id = get_id;
        final String user_id = session_id;
        final String user_qty = this.edQty.getText().toString().trim();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Cart...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_TO_CART,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");

                            if (success.equals("1")) {
                                Toast.makeText(ProductDetailsActivity.this,
                                        "Product updated to Cart", Toast.LENGTH_SHORT).show();

                            } else if (success.equals("0")) {
                                Toast.makeText(ProductDetailsActivity.this,
                                        "Error updating product", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();

                            Toast.makeText(ProductDetailsActivity.this,
                                    "Error Reading Details" + e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                        Toast.makeText(ProductDetailsActivity.this,
                                "Error Reading Details" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "update_cart");
                params.put("prod_id", prod_id);
                params.put("user_id", user_id);
                params.put("qty", user_qty);
                params.put("specs", "");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void decreaseQty() {
        int qty = 0;
        String eQty = this.edQty.getText().toString().trim();
        qty = Integer.parseInt(eQty);
        if (qty > 1) {
            qty--;
            String value = String.valueOf(qty);
            edQty.setText(value);
        } else {
            Toast.makeText(ProductDetailsActivity.this, "Quantity should be greater than 0"
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseQty() {
        int qty = 0;
        String eQty = this.edQty.getText().toString().trim();
        qty = Integer.parseInt(eQty);
        qty++;
        String value = String.valueOf(qty);
        edQty.setText(value);
    }
    public void getCartCount(final String userId){
        String URL_CART_COUNT =baseUrl+"customer/" ;
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_CART_COUNT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                String count=jsonObject.getString("count");
                                cartCount = Integer.parseInt(count);
                                setupBadge(cartCount);
                                //Toast.makeText(ProductDetailsActivity.this,"Cart count "+cartCount,Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","get_cart_count");
                params.put("user_id",userId);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.menu_cart_id);
        View actionView = menuItem.getActionView();
        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_count);

        getCartCount(session_id);

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cart_id: {
                Intent i = new Intent(this, CartActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBadge(int cartCount) {
        if (textCartItemCount != null) {
            if (cartCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(cartCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
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
