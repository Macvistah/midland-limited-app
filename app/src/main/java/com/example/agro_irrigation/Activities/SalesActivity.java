package com.example.agro_irrigation.Activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Adapter.OrderRecyclerViewAdapter;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Fragments.ApprovedFragment;
import com.example.agro_irrigation.Fragments.ApprovedPurchaseFragment;
import com.example.agro_irrigation.Fragments.CancelledFragment;
import com.example.agro_irrigation.Fragments.CompletedFragment;
import com.example.agro_irrigation.Fragments.DeliveredFragment;
import com.example.agro_irrigation.Fragments.DispatchedFragment;
import com.example.agro_irrigation.Fragments.PendingFragment;
import com.example.agro_irrigation.Models.Order_Item;
import com.example.agro_irrigation.Models.Purchases;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.SessionManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SalesActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ShimmerFrameLayout shimmerFrameLayoutOrder;
    private EditText edSearch;
    private TextView txtToolbar;
    String baseUrl = Constants.BASE_URL;
    SessionManager sessionManager;
    String userId,userType;
    private RequestQueue requestQueue;
    OrderRecyclerViewAdapter myAdapter;
    private List<Purchases> lstPurchases= new ArrayList<>();
    private RecyclerView myrv ;
    private LinearLayout linearLayoutCustomer, linearLayoutManager;
    RecyclerView.LayoutManager layoutManager;
    Handler handler;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        changeStatusBarColor();
        init();
        if(userType.equals("customer")) {
            linearLayoutManager.setVisibility(View.GONE);
            linearLayoutCustomer.setVisibility(View.VISIBLE);
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
        else{
            txtToolbar.setText("Purchases");
            linearLayoutManager.setVisibility(View.VISIBLE);
            linearLayoutCustomer.setVisibility(View.GONE);
            addTabs(viewPager);
            tabLayout.setupWithViewPager(viewPager);

        }

    }
    private void init() {
        //initialize variables
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        linearLayoutCustomer = (LinearLayout) findViewById(R.id.customer);
        linearLayoutManager = (LinearLayout) findViewById(R.id.manager);
        shimmerFrameLayoutOrder = findViewById(R.id.shimmer_orders);
        edSearch = findViewById(R.id.search);
        myrv= (RecyclerView) findViewById(R.id.orderList);
        txtToolbar = (TextView) findViewById(R.id.toolbarTxt);
        layoutManager = new LinearLayoutManager(this);
        myrv.setLayoutManager(layoutManager);
        sessionManager = new SessionManager(this);
        HashMap<String,String> user = sessionManager.getUserDetail();
        userId = user.get(SessionManager.ID);
        userType = user.get(SessionManager.USER_TYPE);
        myAdapter = new OrderRecyclerViewAdapter(this,lstPurchases);
        handler = new Handler();
    }
    private void addTabs(ViewPager viewPager) {
        SalesActivity.ViewPagerAdapter adapter = new  SalesActivity.ViewPagerAdapter(getSupportFragmentManager());

        if(userType.equals("sales manager") || userType.equals("shipment manager") ){
            adapter.addFrag(new ApprovedPurchaseFragment("APPROVED"), "Approved");
            adapter.addFrag(new ApprovedPurchaseFragment("COMPLETED"), "Completed");
        }


        if (userType.equals("supplier") || userType.equals("store manager")){
            adapter.addFrag(new ApprovedPurchaseFragment("PENDING"), "Pending");
            adapter.addFrag(new ApprovedPurchaseFragment("APPROVED"), "Approved");
            adapter.addFrag(new ApprovedPurchaseFragment("COMPLETED"), "Completed");
            adapter.addFrag(new ApprovedPurchaseFragment("CANCELED"), "Cancelled");
        }




        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayoutOrder.startShimmer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shimmerFrameLayoutOrder.stopShimmer();
    }
    public void getPurchases(final String id) {
        String URL_JSON=baseUrl+"farmer/action=view_purchases";
        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                response -> {
                    // Log.i("Error", "onResponse: "+response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        JSONArray jsonArray = jsonObject.getJSONArray("read");

                        if (success.equals("1")) {
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);
                                Purchases item = new Purchases();
                                item.setId(object.getString("id"));
                                item.setPurchase_no(object.getString("purchase_no"));
                                item.setStatus(object.getString("status"));
                                item.setPayment_code(object.getString("payment_code"));
                                item.setPurchase_no(object.getString("purchase_no"));
                                item.setSupplier_company(object.getString("supplier_company"));
                                item.setSupplier_name(object.getString("supplier_name"));
                                item.setSupplier_id(object.getString("supplier_id"));
                                item.setAvailable_qty(Integer.parseInt(object.getString("available_qty")));
                                item.setOriginal_qty(Integer.parseInt(object.getString("original_qty")));
                                item.setPrice(Integer.parseInt(object.getString("price")));
                                item.setFinal_price(Integer.parseInt(object.getString("final_price")));
                                item.setProduct_id(object.getString("prod_id"));
                                item.setProduct_name(object.getString("prod_name"));
                                item.setPurchase_date(object.getString("purchase_date"));
                                item.setDescription(object.getString("description"));
                                lstPurchases.add(item);

                            }
                            shimmerFrameLayoutOrder.setVisibility(View.GONE);
                            myrv.setVisibility(View.VISIBLE);
                            // orderCount.setText(String.valueOf(lstPurchases.size()));
                            if(lstPurchases.size()==0){
                                // show no items found
                                shimmerFrameLayoutOrder.setVisibility(View.GONE);
                            }

                        }
                        else if(success.equals("0")){
//                            new SweetAlertDialog( SalesActivity.this, SweetAlertDialog.WARNING_TYPE)
//                                    .setTitleText("NOTICE!")
//                                    .setContentText("No purchases found!")
//                                    .show();
                            shimmerFrameLayoutOrder.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {

                        //  Toast.makeText(getContext(), "Error! " + e.toString(), Toast.LENGTH_SHORT).show();

                        e.printStackTrace();
                    }

                    setRvadapter();
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //  Toast.makeText(getContext(), "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                params.put("supplier_id",id);
                params.put("status","");
                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    public void setRvadapter () {
        myrv.setLayoutManager(layoutManager);
        myrv.setHasFixedSize(true);
        myrv.setAdapter(myAdapter);
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
