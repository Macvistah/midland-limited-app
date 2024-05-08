package com.example.agro_irrigation.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Adapter.OrderRecyclerViewAdapter;
import com.example.agro_irrigation.Constants;
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

public class PendingFragment extends Fragment {
    SessionManager sessionManager;
    String baseUrl;
    private RequestQueue requestQueue ;
    private List<Order_Item> lstOrder= new ArrayList<>();
    private RecyclerView myrv ;
    RecyclerView.LayoutManager layoutManager;
    OrderRecyclerViewAdapter myAdapter;
    TabLayout tabLayout;
    ShimmerFrameLayout shimmerFrameLayoutOrder;
    Handler handler;
    public PendingFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_order, container, false);
        init(view);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 1.5s = 1500ms
                getOrders();
            }
        }, 1000);
        return view;
    }

    private void init(View view) {
        myrv= (RecyclerView) view.findViewById(R.id.orderList);
        shimmerFrameLayoutOrder = view.findViewById(R.id.shimmer_orders);
        tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        baseUrl = Constants.BASE_URL;

        myAdapter = new OrderRecyclerViewAdapter(view.getContext(),lstOrder);
        layoutManager = new LinearLayoutManager(view.getContext());
        sessionManager = new SessionManager(view.getContext());
        handler = new Handler();

        myrv.setLayoutManager(layoutManager);

    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayoutOrder.startShimmer();
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayoutOrder.stopShimmer();
    }
    public void getOrders() {
        String URL_JSON=baseUrl+"salesmanager/";
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
                            if(lstOrder.size()==0){
                                // show no items found
                                //Toast.makeText(getContext(), "No item FOUND", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else if(success.equals("0")){
//                            new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
//                                    .setTitleText("NOTICE!")
//                                    .setContentText("No Pending orders found!")
//                                    .show();


                        }
                    } catch (JSONException e) {

                        Toast.makeText(getContext(), "Error! " + e.toString(), Toast.LENGTH_SHORT).show();

                        e.printStackTrace();
                    }

                    setRvadapter();
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                 Toast.makeText(getContext(), "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                params.put("action","view_orders");
                params.put("status","Pending");
                params.put("driver_id","");
                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);
    }
    public void setRvadapter () {
        myrv.setLayoutManager(layoutManager);
        myrv.setHasFixedSize(true);
        myrv.setAdapter(myAdapter);
    }
}