package com.example.agro_irrigation.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Adapter.DiscountedProductRecyclerViewAdapter;
import com.example.agro_irrigation.Adapter.ProductCategoryAdapter;
import com.example.agro_irrigation.Adapter.ProductRecyclerViewAdapter;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.ProductCategory;
import com.example.agro_irrigation.Models.Products;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopActivity extends AppCompatActivity {
    private TextView txtWelcome,textCartItemCount;
    private EditText edSearch;
    SessionManager sessionManager;
    String userName,userId;
    Handler handler;
    private List<Products> lstAllProducts = new ArrayList<>();
    private List<Products> lstDiscountedProducts = new ArrayList<>();
    private RecyclerView discountedProductsRecycler,allProductsRecycler ;
    RecyclerView.LayoutManager layoutManager,layoutManagerGrid;
    ProductRecyclerViewAdapter myAdapter;
    DiscountedProductRecyclerViewAdapter discountedProductRecyclerViewAdapter;
    private RequestQueue requestQueue ;
    RecyclerView productCatRecycler;
    ProductCategoryAdapter productCategoryAdapter;
    List<ProductCategory> productCategoryList = new ArrayList<>();
    ShimmerFrameLayout shimmerFrameLayoutAllProducts,shimmerFrameLayoutCategories,shimmerFrameLayoutDiscountedProducts;
    private RelativeLayout relativeLayoutCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        changeStatusBarColor();
        init();
        if(savedInstanceState == null) {
            Constants.setWelcomeMessage(txtWelcome,userName);
            viewCategories();
            getDiscountedProducts("");
            getProducts("");
        }
        textCartItemCount.setVisibility(View.GONE);
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                myAdapter.getFilter().filter(s);
                discountedProductRecyclerViewAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getCartCount(userId);
    }

    public void init() {
        //Initializing variables
        discountedProductsRecycler= (RecyclerView) findViewById(R.id.productRecycler);
        allProductsRecycler= (RecyclerView) findViewById(R.id.allProducts);
        productCatRecycler = findViewById(R.id.categoryRecycler);
        edSearch = findViewById(R.id.search);

        txtWelcome = (TextView) findViewById(R.id.welcome_txt);
        textCartItemCount = findViewById(R.id.cart_count);
        shimmerFrameLayoutAllProducts = findViewById(R.id.shimmer_all_products);
        shimmerFrameLayoutCategories = findViewById(R.id.shimmer_category);
        shimmerFrameLayoutDiscountedProducts = findViewById(R.id.shimmer_discounted_products);
        relativeLayoutCart  = findViewById(R.id.my_cart);

        layoutManagerGrid = new GridLayoutManager(this,2);
        layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        myAdapter = new ProductRecyclerViewAdapter(this,lstAllProducts);
        discountedProductRecyclerViewAdapter = new DiscountedProductRecyclerViewAdapter(this,lstDiscountedProducts);

        sessionManager = new SessionManager(this);
        HashMap<String, String> account = sessionManager.getAccountDetail();
        userName = account.get(sessionManager.FNAME);
        HashMap<String, String> user = sessionManager.getUserDetail();
        userId = user.get(SessionManager.ID);

        handler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayoutDiscountedProducts.startShimmer();
        shimmerFrameLayoutCategories.startShimmer();
        shimmerFrameLayoutAllProducts.startShimmer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shimmerFrameLayoutAllProducts.stopShimmer();
        shimmerFrameLayoutCategories.stopShimmer();
        shimmerFrameLayoutDiscountedProducts.stopShimmer();
    }



    public void getProducts(final String key) {
        String URL_JSON= Constants.BASE_URL +"salesmanager/";
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
                                    Products prod= new Products();
                                    prod.setProductId(object.getString("id"));
                                    prod.setProductName(object.getString("name"));
                                    prod.setProductCat(object.getString("category"));
                                    prod.setProductQty(object.getString("qty"));
                                    prod.setImageUrl(object.getString("image"));
                                    prod.setProductPrice(object.getString("price"));
                                    prod.setProductDiscount(object.getString("discount"));
                                    prod.setProductDesc(object.getString("desc"));

                                    lstAllProducts.add(prod);
                                    myAdapter.notifyDataSetChanged();
                                }
                            }
                            else if(success.equals("0")){
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        allProductsRecycler.setVisibility(View.VISIBLE);
                        shimmerFrameLayoutAllProducts.setVisibility(View.GONE);
                        setAllProductAdapter(lstAllProducts);
                    }
                }, error -> {
            //echo error
                })
        {
            @Override
            protected Map<String, String> getParams () throws AuthFailureError {
                Map <String, String> params = new HashMap<>();
                params.put ("action", "view_products");
                params.put ("key", key);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    public void getDiscountedProducts(final String key) {
        String URL_JSON= Constants.BASE_URL +"salesmanager/";
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
                                    Products prod= new Products();
                                    prod.setProductId(object.getString("id"));
                                    prod.setProductName(object.getString("name"));
                                    prod.setProductCat(object.getString("category"));
                                    prod.setProductDiscount(object.getString("discount"));
                                    prod.setProductQty(object.getString("qty"));
                                    prod.setImageUrl(object.getString("image"));
                                    prod.setProductPrice(object.getString("price"));
                                    prod.setProductDesc(object.getString("desc"));
                                    lstDiscountedProducts.add(prod);
                                    discountedProductRecyclerViewAdapter.notifyDataSetChanged();
                                }
                            }
                            else if(success.equals("0")){
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        discountedProductsRecycler.setVisibility(View.VISIBLE);
                        shimmerFrameLayoutDiscountedProducts.setVisibility(View.GONE);
                        setRvadapter(lstDiscountedProducts);
                    }
                }, error -> {
            //echo error
        })
        {
            @Override
            protected Map<String, String> getParams () throws AuthFailureError {
                Map <String, String> params = new HashMap<>();
                params.put ("action", "view_discounted_products");
                params.put ("key", key);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    private void setRvadapter (List<Products> lstDiscountedProducts) {
        //ProductRecyclerViewAdapter myAdapter = new ProductRecyclerViewAdapter(getContext(),lstAllProducts) ;
        discountedProductsRecycler.setLayoutManager(layoutManager);
        discountedProductsRecycler.setAdapter(discountedProductRecyclerViewAdapter);
    }
    private void setAllProductAdapter(List<Products> lstAllProducts) {
        allProductsRecycler.setLayoutManager(layoutManagerGrid);
        allProductsRecycler.setAdapter(myAdapter);
    }

    private void viewCategories(){
        String URL_JSON = Constants.BASE_URL+"salesmanager/";
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
                                    ProductCategory category = new ProductCategory();
                                    category.setProductId(object.getInt("id"));
                                    category.setProductName(object.getString("name"));
                                    productCategoryList.add(category);
                                    myAdapter.notifyDataSetChanged();
                                }
                                shimmerFrameLayoutCategories.setVisibility(View.GONE);
                                productCatRecycler.setVisibility(View.VISIBLE);
                                setProductRecycler(productCategoryList);
                            }
                            else if(success.equals("0")){

                            }
                        } catch (JSONException e) {

                            //  Toast.makeText(getContext(), "Error! " + e.toString(), Toast.LENGTH_SHORT).show();
                            // Toast.makeText(OrderActivity.this, lstAnime.get(1).toString(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Toast.makeText(getContext(), "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","view_categories");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    private void setProductRecycler(List<ProductCategory> productCategoryList){
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        productCatRecycler.setLayoutManager(layoutManager);
        productCategoryAdapter = new ProductCategoryAdapter(this, productCategoryList);
        productCatRecycler.setAdapter(productCategoryAdapter);

    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
    public void onCartClick(View View){
        startActivity(new Intent(this,CartActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
    }
    public void getCartCount(final String userId){
        String URL_CART_COUNT =Constants.BASE_URL+"customer/" ;
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_CART_COUNT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject= new JSONObject(response);
                            String success=jsonObject.getString("success");
                            if(success.equals("1")){
                                String count=jsonObject.getString("count");
                                setupBadge(Integer.parseInt(count));
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
}
