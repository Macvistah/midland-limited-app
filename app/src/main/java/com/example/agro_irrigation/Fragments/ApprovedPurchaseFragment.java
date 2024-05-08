package com.example.agro_irrigation.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.example.agro_irrigation.Activities.SalesActivity;
import com.example.agro_irrigation.Adapter.PurchasesRecyclerViewAdapter;
import com.example.agro_irrigation.Adapter.PurchasesRecyclerViewAdapter;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Interfaces.PurchaseUpdateListener;
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
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ApprovedPurchaseFragment extends Fragment implements PurchaseUpdateListener {
    SessionManager sessionManager;
    private String status = "", userId, userType;
    String baseUrl;
    private RequestQueue requestQueue ;
    private List<Purchases> lstOrder= new ArrayList<>();
    private RecyclerView myrv ;
    RecyclerView.LayoutManager layoutManager;
    PurchasesRecyclerViewAdapter myAdapter;
    TabLayout tabLayout;
    ShimmerFrameLayout shimmerFrameLayoutOrder;
    Handler handler;

    TextView txtNoItems;


    public ApprovedPurchaseFragment(){
    }

    public ApprovedPurchaseFragment(String status){
        this.status = status;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_order, container, false);
        init(view);
        getPurchases();
        return view;
    }

    private void init(View view) {
        myrv= (RecyclerView) view.findViewById(R.id.orderList);
        shimmerFrameLayoutOrder = view.findViewById(R.id.shimmer_orders);
        txtNoItems = view.findViewById(R.id.noItemsTxt);
        tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        baseUrl = Constants.BASE_URL;

        myAdapter = new PurchasesRecyclerViewAdapter(view.getContext(),lstOrder, this);
        layoutManager = new LinearLayoutManager(view.getContext());
        sessionManager = new SessionManager(view.getContext());
        handler = new Handler();

        sessionManager = new SessionManager(view.getContext());
        HashMap<String, String> user = sessionManager.getUserDetail();
        userId      = user.get(SessionManager.ID);
        userType    = user.get(SessionManager.USER_TYPE);


        myrv.setLayoutManager(layoutManager);

    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayoutOrder.startShimmer();
        lstOrder.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayoutOrder.stopShimmer();
    }



    public void getPurchases() {
        String id = "";
        if (userType.equals("supplier")) {
            id = userId;
        }
        String URL_JSON=baseUrl+"farmer/?action=view_purchases";
        shimmerFrameLayoutOrder.setVisibility(View.VISIBLE);
        String finalId = id;
        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                response -> {
                     Log.i("Error", "onResponse: "+response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        JSONArray jsonArray = jsonObject.getJSONArray("read");
//                        Log.i("Error", "onResponse: "+jsonArray);
//                        Toast.makeText(getContext(), "Success! " + success, Toast.LENGTH_LONG).show();
                        if (success.equals("1")) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                Purchases item = new Purchases();
                                item.setId(object.getString("id"));
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
                                lstOrder.add(item);

                            }

                            shimmerFrameLayoutOrder.setVisibility(View.GONE);
                            myrv.setVisibility(View.VISIBLE);
                            txtNoItems.setVisibility(View.GONE);
                            // orderCount.setText(String.valueOf(lstOrder.size()));
                            if(lstOrder.size()==0){
                                // show no items found
                                shimmerFrameLayoutOrder.setVisibility(View.GONE);
                                txtNoItems.setVisibility(View.VISIBLE);
                            }

                        }
                        else if(success.equals("0")){
//                            new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.WARNING_TYPE)
//                                    .setTitleText("NOTICE!")
//                                    .setContentText("No purchases found!")
//                                    .show();
                            shimmerFrameLayoutOrder.setVisibility(View.GONE);
                            txtNoItems.setVisibility(View.VISIBLE);

                        }
                    } catch (JSONException e) {

//                          Toast.makeText(getContext(), "Error! " + e.toString(), Toast.LENGTH_LONG).show();

                        e.printStackTrace();
                    }

                    setRvadapter();
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                  Toast.makeText(getContext(), "Error! " + error.toString(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                if (!finalId.isEmpty()){
                    params.put("user_id", finalId);
                }
                if (!Objects.equals(status, "")){
                    params.put("status",status);
                }
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

    @Override
    public void onPurchaseRemoved(int position) {
        getPurchases();
    }
}