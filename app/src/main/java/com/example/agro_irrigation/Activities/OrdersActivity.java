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
import com.example.agro_irrigation.Fragments.CancelledFragment;
import com.example.agro_irrigation.Fragments.CompletedFragment;
import com.example.agro_irrigation.Fragments.DeliveredFragment;
import com.example.agro_irrigation.Fragments.DispatchedFragment;
import com.example.agro_irrigation.Fragments.PendingFragment;
import com.example.agro_irrigation.Models.Order_Item;
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

public class OrdersActivity extends AppCompatActivity {
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
    private List<Order_Item> lstOrder= new ArrayList<>();
    private RecyclerView myrv ;
    private LinearLayout linearLayoutCustomer, linearLayoutManager;
    RecyclerView.LayoutManager layoutManager;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        changeStatusBarColor();
        init();
        if(userType.equals("customer")) {
            linearLayoutManager.setVisibility(View.GONE);
            linearLayoutCustomer.setVisibility(View.VISIBLE);
            getOrders(userId);
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
            txtToolbar.setText("Orders");
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
        myAdapter = new OrderRecyclerViewAdapter(this,lstOrder);
        handler = new Handler();
    }
    private void addTabs(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (userType.equals("sales manager")){
            adapter.addFrag(new ApprovedFragment("Pending"), "Pending");
            adapter.addFrag(new ApprovedFragment("Approved"), "Approved");
            adapter.addFrag(new ApprovedFragment("Dispatched"), "Dispatched");
            adapter.addFrag(new ApprovedFragment("Delivered"), "Delivered");
            adapter.addFrag(new ApprovedFragment("Completed"), "Completed");
            adapter.addFrag(new ApprovedFragment("Cancelled"), "Cancelled");

//            adapter.addFrag(new PendingFragment(), "Pending");
//            adapter.addFrag(new ApprovedFragment(), "Approved");
//            adapter.addFrag(new DispatchedFragment(), "Dispatched");
//            adapter.addFrag(new DeliveredFragment(), "Delivered");
//            adapter.addFrag(new CompletedFragment(), "Completed");
//            adapter.addFrag(new CancelledFragment(), "Cancelled");
        }
        else if (userType.equals("shipment manager")){
            adapter.addFrag(new ApprovedFragment("Approved"), "Approved");
            adapter.addFrag(new ApprovedFragment("Dispatched"), "Dispatched");
            adapter.addFrag(new ApprovedFragment("Delivered"), "Delivered");

//            adapter.addFrag(new ApprovedFragment(), "Approved");
//            adapter.addFrag(new DispatchedFragment(), "Dispatched");
//            adapter.addFrag(new DeliveredFragment(), "Delivered");
        }
        else if (userType.equals("driver")){
            adapter.addFrag(new ApprovedFragment("Dispatched"), "Assigned Orders");
            adapter.addFrag(new ApprovedFragment("Delivered"), "Delivered Orders");
//            adapter.addFrag(new DispatchedFragment(), "Assigned Orders");
//            adapter.addFrag(new DeliveredFragment(), "Delivered Orders");
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
    public void getOrders(final String id) {
        String URL_JSON=baseUrl+"customer/";
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
                                Order_Item item = new Order_Item();
                                item.setOrder_id(object.getString("id"));
                                item.setOrder_no(object.getString("order_no"));
                                item.setOrder_amount(object.getString("total_amount"));
                                item.setOrder_address(object.getString("location"));
                                item.setOrder_date(object.getString("date"));
                                item.setOrder_payment(object.getString("payment"));
                                item.setOrder_status(object.getString("status"));
                                item.setOrder_charge(object.getString("charge"));
                                lstOrder.add(item);

                            }
                            shimmerFrameLayoutOrder.setVisibility(View.GONE);
                            myrv.setVisibility(View.VISIBLE);
                            // orderCount.setText(String.valueOf(lstOrder.size()));
                            if(lstOrder.size()==0){
                               // show no items found
                                shimmerFrameLayoutOrder.setVisibility(View.GONE);
                            }

                        }
                        else if(success.equals("0")){
                            new SweetAlertDialog(OrdersActivity.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("NOTICE!")
                                    .setContentText("No orders found!")
                                    .show();


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
                params.put("action","view_order");
                params.put("user_id",id);
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
