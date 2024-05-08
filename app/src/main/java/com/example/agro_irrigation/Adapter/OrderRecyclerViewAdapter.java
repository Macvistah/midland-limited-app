package com.example.agro_irrigation.Adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Models.AccessToken;
import com.example.agro_irrigation.Models.Driver;
import com.example.agro_irrigation.Models.Order_Item;
import com.example.agro_irrigation.Models.Order_Item_Details;
import com.example.agro_irrigation.Models.STKPush;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.Services.DarajaApiClient;
import com.example.agro_irrigation.Services.Utils;
import com.example.agro_irrigation.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.santalu.maskara.widget.MaskEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import timber.log.Timber;


public class OrderRecyclerViewAdapter extends RecyclerView.Adapter<OrderRecyclerViewAdapter.MyViewHolder>implements Filterable {

    private Context mContext ;
    private List<Order_Item> mData ;
    private List<Order_Item> mDataFiltered;
    private Dialog myDialog;
    private BottomSheetDialog bottomSheetDialog;
    private RequestQueue requestQueue;
    private List<Order_Item_Details> lstDetails= new ArrayList<>();
    private SessionManager sessionManager;
    String baseUrl,userType,phone,userName;
    ArrayAdapter<Driver> driverAdapter;
    private List<Driver> driversList = new ArrayList<>();
    DarajaApiClient mApiClient;

    public OrderRecyclerViewAdapter(Context mContext, List lst) {
        this.mContext = mContext;
        this.mData = lst;
        this.mDataFiltered= mData;
        myDialog = new Dialog(mContext);
        bottomSheetDialog = new BottomSheetDialog(mContext,R.style.BottomSheetDialogTheme);
        sessionManager = new SessionManager(mContext);
        mApiClient = new DarajaApiClient();
        baseUrl = Constants.BASE_URL;
        HashMap<String, String> user = sessionManager.getUserDetail();
        userType = user.get(sessionManager.USER_TYPE);
        userName = user.get(SessionManager.NAME);
        HashMap<String, String> account  = sessionManager.getAccountDetail();
        phone = account.get(SessionManager.PHONE);
        Driver driver = new Driver("0","-Choose Driver-");
        driversList.add(driver);
        getDrivers();
        driverAdapter  = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_dropdown_item,driversList);
        driverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.order_list_item,parent,false);
        final MyViewHolder viewHolder = new MyViewHolder(view);
        getAccessToken();

        // click listener here
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Order_Item order = mDataFiltered.get(position);
        //String orderId = String.valueOf(Integer.valueOf(order.getOrder_id())+1);
        String orderId = String.valueOf(Integer.valueOf(order.getOrder_id()));
        final String orderNo = order.getOrder_no();
        final String location = order.getOrder_address();
        NumberFormat format = NumberFormat.getNumberInstance();
        double total = Double.parseDouble(order.getOrder_amount());
        double shippingFee = Double.parseDouble(order.getOrder_charge());
        final String orderDate =order.getOrder_date();
        final String orderPaymentStatus = order.getOrder_payment();
        final String orderStatus = order.getOrder_status();
        final int pos = mData.indexOf(mData.get(holder.getAdapterPosition()));



        holder.txtOrderNo.setText(orderNo);
        holder.txtOrderStatus.setText(orderStatus);
        holder.txtOrderPayment.setText(orderPaymentStatus);
        holder.txtOrderAmount.setText("Ksh "+format.format(total));
        holder.txtOrderDate.setText(orderDate);
        holder.txtLocation.setText(location);
        if (userType.equals("customer")){
            holder.layoutAction.setVisibility(View.GONE);
        }

        if (userType.equals("sales manager")|| userType.equals("shipment manager")){
            if (orderStatus.equals("Dispatched") ||orderStatus.equals("Approved")){
                holder.layoutAddress.setVisibility(View.VISIBLE);
                holder.layoutPayment.setVisibility(View.VISIBLE);
                if (userType.equals("shipment manager")){
                    if (orderStatus.equals("Dispatched")){
                        holder.btnAssignDriver.setVisibility(View.GONE);
                    }else{
                        holder.btnAssignDriver.setVisibility(View.VISIBLE);
                    }
                    holder.layoutPayment.setVisibility(View.GONE);
                }
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
            }else if(orderStatus.equals("Completed")||orderStatus.equals("Delivered")|| orderStatus.equals("Cancelled")){
                holder.layoutAction.setVisibility(View.GONE);
                holder.layoutAddress.setVisibility(View.VISIBLE);
            }
        }


        if (userType.equals("driver")){
            if (orderStatus.equals("Dispatched")){

                holder.btnAssignDriver.setVisibility(View.VISIBLE);
                holder.btnAssignDriver.setText("Yes,Delivered");

            }
            holder.layoutAddress.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.layoutPayment.setVisibility(View.GONE);
        }

        //set Onclick Listener to the view Option
        holder.txtViewOrder.setOnClickListener(v -> {
            showPopup(orderNo,orderDate,total,shippingFee,orderId,orderStatus,orderPaymentStatus,position);
        });
        holder.btnApprove.setOnClickListener(v -> {
            new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Approve Order No. "+orderNo+ "?")
                    .setContentText("You cannot undo this action!")
                    .setConfirmText("Yes, Approve order!")
                    .setConfirmClickListener(sDialog -> {
                        updateStatus(sDialog, orderId,"Approved",pos,"");
                    })
                    .show();

        });
        holder.btnCancel.setOnClickListener(v -> {
            new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Cancel Order No. "+orderNo+ "?")
                    .setContentText("You cannot undo this action!")
                    .setConfirmText("Yes, Cancel order!")
                    .setConfirmClickListener(sDialog -> {
                        updateStatus(sDialog, orderId,"Cancelled",pos,"");
                    })
                    .show();
        });
        holder.btnAssignDriver.setOnClickListener(v -> {
            if (userType.equals("driver")){
                new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirmation")
                        .setContentText("Delivered Order No. "+orderNo+" ?!")
                        .setConfirmText("Yes, I have!")
                        .setConfirmClickListener(sDialog -> {
                            updateStatus(sDialog,orderId,"Delivered",pos,"");
                        })
                        .show();
            }else{
                assignDriverPopup(orderId,orderNo,location,pos);
            }

        });

    }

    @Override
    public int getItemCount() {
        return mDataFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mDataFiltered = mData;
                } else {
                    List<Order_Item> filteredList = new ArrayList<>();
                    for (Order_Item row : mData) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getOrder_no().toLowerCase().contains(charString.toLowerCase()) ||  row.getOrder_date().contains(charSequence)||  row.getOrder_payment().contains(charSequence) ) {
                            filteredList.add(row);
                        }
                        mDataFiltered = filteredList;
                    }

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mDataFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                mDataFiltered = (ArrayList<Order_Item>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderNo, txtOrderStatus, txtOrderPayment, txtOrderAmount, txtOrderDate,txtLocation,txtViewOrder;
        Button btnApprove,btnCancel,btnAssignDriver;
        LinearLayout layoutAddress,layoutPayment,layoutAction;


        public MyViewHolder(View itemView) {
            super(itemView);
            txtOrderNo = (TextView) itemView.findViewById(R.id.order_no);
            txtOrderStatus = (TextView) itemView.findViewById(R.id.orderStatus);
            txtOrderPayment = (TextView) itemView.findViewById(R.id.orderPayment);
            txtOrderAmount = (TextView) itemView.findViewById(R.id.orderAmount);
            txtOrderDate = (TextView) itemView.findViewById(R.id.orderDate);
            txtViewOrder = (TextView) itemView.findViewById(R.id.viewOrder);
            btnApprove = (Button) itemView.findViewById(R.id.approve);
            btnCancel = (Button) itemView.findViewById(R.id.cancel);
            btnAssignDriver = (Button) itemView.findViewById(R.id.assign_driver);
            txtLocation = (TextView) itemView.findViewById(R.id.location);
            layoutAddress = (LinearLayout) itemView.findViewById(R.id.orderAddress);
            layoutPayment = (LinearLayout) itemView.findViewById(R.id.payment);
            layoutAction = (LinearLayout) itemView.findViewById(R.id.action);
        }
    }
    public void showPopup(String orderNo, String date, double Amount, double Fee, String Id,String status,String paymentStatus,int position){
        final TextView txtClose,txtDate,txtAmount,txtShipment,txtOrderNo;
        final Button btnPay, btnCancel, btnPrintReceipt;
        final LinearLayout linearLayoutActions;
        RecyclerView myrv;
        NumberFormat format =  NumberFormat.getNumberInstance();
        bottomSheetDialog.setContentView(R.layout.order_list_dialog);

        txtClose = (TextView) bottomSheetDialog.findViewById(R.id.dismiss);
        txtOrderNo = (TextView) bottomSheetDialog.findViewById(R.id.order_no);
        txtDate = (TextView) bottomSheetDialog.findViewById(R.id.date);
        txtAmount = (TextView) bottomSheetDialog.findViewById(R.id.order_amount);
        txtShipment = (TextView) bottomSheetDialog.findViewById(R.id.shipment_fee);
        myrv = (RecyclerView) bottomSheetDialog.findViewById(R.id.rvOrderItems);
        btnPay = (Button) bottomSheetDialog.findViewById(R.id.pay);
        btnCancel = (Button) bottomSheetDialog.findViewById(R.id.cancel_order);
        btnPrintReceipt = (Button) bottomSheetDialog.findViewById(R.id.print_receipt);
        linearLayoutActions = (LinearLayout) bottomSheetDialog.findViewById(R.id.actions);

        //initializing values
        txtOrderNo.setText(orderNo);
        txtDate.setText(date);
        txtAmount.setText("Ksh "+format.format(Amount));
        txtShipment.setText(format.format(Fee));
        lstDetails.clear();
        getOrders(Id,myrv);
        if (userType.equals("customer")){
            linearLayoutActions.setVisibility(View.VISIBLE);
            if (status.equals("Pending")){
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Cancel Order No. "+orderNo+ "?")
                                .setContentText("You cannot undo this action!")
                                .setConfirmText("Yes, Cancel order!")
                                .setConfirmClickListener(sDialog -> {
                                    updateStatus(sDialog, Id,"Cancelled",position,"");
                                })
                                .show();
                    }
                });
            }
            else if (status.equals("Delivered")){
                btnCancel.setText("Confirm Order");
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Confirmation!")
                                .setContentText("Have you received order "+orderNo+ "?")
                                .setConfirmText("Yes, I have!")
                                .setConfirmClickListener(sDialog -> {
                                    updateStatus(sDialog, Id,"Completed",position,"");
                                })
                                .show();
                    }
                });
            }
            if (!status.equals("Pending")){
                btnPrintReceipt.setVisibility(View.VISIBLE);
                btnPrintReceipt.setOnClickListener(v -> {
                    printReport(Id,Amount,Fee,orderNo);
                });
            }
        }
        bottomSheetDialog.show();


    }

    public void printReport(String id,Double totalAmount, Double Charge, String OrderNo){
        //Declare Variables
        Date dateObj;
        DateFormat dateFormat;
        Bitmap bmp,scaleBmp;
        int pageWidth = 1200;
        bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.midland_logo);
        scaleBmp = Bitmap.createScaledBitmap(bmp,200,200,false);
        PdfDocument myPdfDocument = new PdfDocument();
        Paint myPaint = new Paint();
        Paint titlePaint = new Paint();
        dateObj = new Date();
        NumberFormat format = NumberFormat.getNumberInstance();
        String val = OrderNo +" "+((int)(Math.random()*900000)+100000);
        dateFormat= new SimpleDateFormat("dd/MM/YYYY ");
        PdfDocument.PageInfo myPageInfo1 = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
        PdfDocument.Page myPage1 = myPdfDocument.startPage(myPageInfo1);
        Canvas canvas = myPage1.getCanvas();
        canvas.drawBitmap(scaleBmp,500,20,myPaint);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));

        //titlePaint.setColor(Color.rgb(247,147,30));
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
        titlePaint.setTextSize(35);
        canvas.drawText("MIDLAND LTD",pageWidth/2,290,titlePaint);
        titlePaint.setTextSize(35);
        canvas.drawText("Njambini, Kinangop- Nyandarua",pageWidth/2,350,titlePaint);
        titlePaint.setTextSize(30);
        canvas.drawText(" Tel No: 0724536789",pageWidth/2,400,titlePaint);
        titlePaint.setTextSize(55);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("Payment Receipt",pageWidth/2,500,titlePaint);

        titlePaint.setColor(Color.GRAY);
        canvas.drawRect(20,540,pageWidth-20,550,titlePaint);
        myPaint.setTextAlign(Paint.Align.LEFT);
        myPaint.setTextSize(30f);
        myPaint.setColor(Color.BLACK);
        canvas.drawText("Receipt No",30,590,myPaint);
        canvas.drawText(": "+OrderNo,250,590,myPaint);
        canvas.drawText("Received From ",30,645,myPaint);
        canvas.drawText(": "+userName,250,645,myPaint);

        myPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Date  : "+dateFormat.format(dateObj),pageWidth-30,590,myPaint);

        myPaint.setColor(Color.BLACK);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(2);
        canvas.drawRect(20,750,pageWidth-20,830,myPaint);
        myPaint.setTextAlign(Paint.Align.LEFT);
        //myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        myPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("No.",40,800,myPaint);
        canvas.drawText("Description",200,800,myPaint);
        canvas.drawText("Qty",700,800,myPaint);
        canvas.drawText("Unit Price",850,800,myPaint);
        canvas.drawText("Total",1050,800,myPaint);
        canvas.drawLine(180,760,180,810,myPaint);
        canvas.drawLine(680,760,680,810,myPaint);
        canvas.drawLine(800,760,800,810,myPaint);
        canvas.drawLine(1030,760,1030,810,myPaint);


        int y_index = 1200;
        for (int i = 0; i < lstDetails.size(); i++){
            int y = 920;
            for(int j = 0;j<lstDetails.size();j++){
                myPaint.setColor(Color.DKGRAY);
                myPaint.setTextSize(30f);
                canvas.drawText((j+1)+".",40,y,myPaint);
                canvas.drawText(lstDetails.get(j).getOrder_prod_name(),200,y,myPaint);
                myPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(format.format(Integer.valueOf(lstDetails.get(j).getOrder_prod_qty())),750,y,myPaint);
                canvas.drawText(format.format(Double.parseDouble(lstDetails.get(j).getOrder_prod_price())),1000,y,myPaint);


                Double total = null;
                total = Double.parseDouble(lstDetails.get(j).getOrder_prod_price()) * Double.parseDouble( lstDetails.get(j).getOrder_prod_qty());
                canvas.drawText(format.format(Double.parseDouble(String.valueOf(total))) ,pageWidth-40,y,myPaint);
                myPaint.setTextAlign(Paint.Align.LEFT);
                y = y+100;
            }
            // subTotal += Double.parseDouble(lstDetails.get(i).getOrder_prod_price()) * Double.parseDouble( lstDetails.get(i).getOrder_prod_qty());
            y_index = y;
        }

        Double subTotal = totalAmount - Charge;
        canvas.drawLine(680,y_index+50,pageWidth-20,y_index+50,myPaint);
        canvas.drawText("Sub-Total",700,y_index+100,myPaint);
        canvas.drawText(":",900,y_index+100,myPaint);
        myPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(format.format(subTotal),pageWidth-40,y_index+100,myPaint);
        myPaint.setTextAlign(Paint.Align.LEFT);

        //float tax = (float) (0.0*subTotal);
        canvas.drawText("Shipment Fee",700,y_index+150,myPaint);
        canvas.drawText(":",900,y_index+150,myPaint);
        myPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(format.format(Charge),pageWidth-40,y_index+150,myPaint);
        myPaint.setTextAlign(Paint.Align.LEFT);

        //myPaint.setColor(Color.rgb(247,147,30));
        myPaint.setColor(Color.GRAY);
        canvas.drawRect(680,y_index+200,pageWidth-20,y_index+300,myPaint);

        myPaint.setColor(Color.WHITE);
        myPaint.setTextSize(40f);
        myPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Total",700,y_index+265,myPaint);
        myPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Ksh. "+format.format(totalAmount),pageWidth-40,y_index+265,myPaint);




        myPdfDocument.finishPage(myPage1);
        String fileName = "receipt"+id+".pdf";
        File file = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!file.exists()) {
            file.mkdir();
            Timber.i("Created a new directory for PDF");
        }
        file = new File(file.getAbsolutePath(), fileName);
        //File file = new File(Environment.getExternalStorageDirectory(),"document/"+fileName);
        try {
            myPdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        myPdfDocument.close();
        previewPdf(file);
    }
    private void previewPdf(File file) {
        PackageManager packageManager = mContext.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Intent intent = new Intent();
            Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
            intent.setAction(Intent.ACTION_VIEW);
            //Uri uri = Uri.fromFile(pdfFile);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mContext.startActivity(intent);
        } else {
            Toast.makeText(mContext, "Download a PDF Viewer to see the generated PDF", Toast.LENGTH_SHORT).show();
        }
    }



    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull retrofit2.Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

            }
        });
    }
    public void performSTKPush(String phone_number,String amount,String order_id) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Processing Your Request");
        progressDialog.setTitle("Please wait...");
        progressDialog.setIndeterminate(true);
        // progressDialog.setCancelable(false);
        progressDialog.show();
        myDialog.dismiss();
        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
                Constants.BUSINESS_SHORT_CODE,
                Utils.getPassword( Constants.BUSINESS_SHORT_CODE,  Constants.PASSKEY, timestamp),
                timestamp,
                Constants.TRANSACTION_TYPE,
                String.valueOf(1),
                Utils.sanitizePhoneNumber(phone_number),
                Constants.PARTYB,
                Utils.sanitizePhoneNumber(phone_number),
                Constants.CALLBACKURL+"?id="+order_id,
                "Agro Irrigation & Pumping Services", //Account reference
                "Payments"  //Transaction description
        );


        mApiClient.setGetAccessToken(false);

        //Sending the data to the Mpesa API, remember to remove the logging when in production.

        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull retrofit2.Response<STKPush> response) {
                progressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        // clearCart();
                        Timber.d("post submitted to API. %s", response.body());
                    } else {
                        Timber.e("Response %s", response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Timber.e(t);
            }
        });
    }
    public void assignDriverPopup(String id, String orderNo, String location, int pos){
        Button btnSubmit;
        TextView txtClose,txtOrderNo,txtOrderLocation;
        final TextInputLayout txtDriver;
        final AutoCompleteTextView drpDrivers;

        myDialog.setContentView(R.layout.assign_driver_dialog);

        txtClose = (TextView) myDialog.findViewById(R.id.close);
        txtOrderNo = myDialog.findViewById(R.id.orderNo);
        txtOrderLocation = myDialog.findViewById(R.id.location);
        drpDrivers = myDialog.findViewById(R.id.drivers);
        btnSubmit = myDialog.findViewById(R.id.submit);
        txtDriver = myDialog.findViewById(R.id.textDriver);

        myDialog.setCancelable(false);

        txtOrderNo.setText(orderNo);
        txtOrderLocation.setText(location);
        drpDrivers.setText("-Choose Driver-");
        drpDrivers.setAdapter(driverAdapter);

        txtClose.setOnClickListener(v -> myDialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String driver = ((AutoCompleteTextView)txtDriver.getEditText()).getText().toString().trim();
            if(driver.equals("-Choose Driver-"))
            {
                Toast.makeText(mContext,"Please choose driver",Toast.LENGTH_LONG).show();
            }else{
                new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Dispatch Order No. "+orderNo+ "?")
                        .setContentText("You cannot undo this action!")
                        .setConfirmText("Yes, Dispatch order!")
                        .setConfirmClickListener(sDialog -> {
                            updateStatus(sDialog,id,"Dispatched",pos,driver);
                            myDialog.dismiss();
                        })
                        .show();

            }
            });

        myDialog.show();
    }
    public void getOrders(final String id, final RecyclerView myrv) {
        String URL_JSON = baseUrl+"customer/";
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
                                    Order_Item_Details item = new Order_Item_Details();
                                    item.setOrderDetailId(object.getString("id"));
                                    item.setOrder_prod_name(object.getString("prod_name"));
                                    item.setOrder_prod_qty(object.getString("qty"));
                                    item.setOrder_prod_price(object.getString("price"));
                                    lstDetails.add(item);
                                }
                            }
                            else if(success.equals("0")){

                            }
                        } catch (JSONException e) {

                            Toast.makeText(mContext, "Error! " + e.toString(), Toast.LENGTH_SHORT).show();
                            // Toast.makeText(OrderActivity.this, lstAnime.get(1).toString(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }
                            setRvadapter(lstDetails,myrv);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               Toast.makeText(mContext, "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                params.put("action","view_order_details");
                params.put("order_id",id);
                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);
    }
    public void getOrdersDetails(final String id) {
        String URL_JSON = baseUrl+"customer/";
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
                                    Order_Item_Details item = new Order_Item_Details();
                                    item.setOrderDetailId(object.getString("id"));
                                    item.setOrder_prod_name(object.getString("prod_name"));
                                    item.setOrder_prod_qty(object.getString("qty"));
                                    item.setOrder_prod_price(object.getString("price"));
                                    lstDetails.add(item);
                                }
                            }
                            else if(success.equals("0")){

                            }
                        } catch (JSONException e) {

                            Toast.makeText(mContext, "Error! " + e.toString(), Toast.LENGTH_SHORT).show();
                            // Toast.makeText(OrderActivity.this, lstAnime.get(1).toString(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String>params =new HashMap<>();
                params.put("action","view_order_details");
                params.put("order_id",id);
                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);
    }
    public void setRvadapter (List<Order_Item_Details> lstDetails, RecyclerView myrv) {
        OrderDetailsRecyclerViewAdapter myAdapter = new OrderDetailsRecyclerViewAdapter(mContext,lstDetails);
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new GridLayoutManager(mContext,1);
        myrv.setLayoutManager(layoutManager);
        myrv.setHasFixedSize(true);
        myrv.setAdapter(myAdapter);
    }
    public String transferdata(){

        JSONArray jsonArray = new JSONArray();
        try {

            for (Order_Item_Details details : lstDetails ) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("prod_name", details.getOrder_prod_name());
                jsonObject.put("prod_qty",details.getOrder_prod_qty());
                //jsonObject.put("prod_price",cart.getCart_prod_price());
                jsonArray.put(jsonObject);
            }

        } catch (JSONException jse){
            jse.printStackTrace();
        }
        return String.valueOf(jsonArray);
    }
    public void sendData(final String data){
        RequestQueue requestQueue;
        Log.e("Response",data);
        String URL_Send=baseUrl+"salesmanager/";
        StringRequest request = new StringRequest(Request.Method.POST,URL_Send,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
                params.put("jsonArray",data);
                params.put("action","decrement_stock");
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);
    }
    public void updateStatus(SweetAlertDialog sDialog, String id, String status, int pos, String driverName){
        String URL_APPROVE=baseUrl+"customer/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_APPROVE,
                response -> {
                    Timber.i("onResponse: %s", response);


                    try {
                        JSONObject jsonObject= new JSONObject(response);
                        String success = jsonObject.getString("success");

                        if(success.equals("1")){
                            if (status.equals("Dispatched")){
                                updateDeliveryInfo(id, pos, driverName);
                            }
                            sDialog
                                    .setTitleText("Confirmed!")
                                    .setContentText("Order status updated successfully!")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            if (mData.size() > 0){
                                mData.remove(pos);
                                notifyItemRemoved(pos);
                            }

//                            if (status.equals("Approved")){
//                                sendData(transferdata());
//                            }
                        }
                        else if(success.equals("0"))
                        {
                            sDialog
                                    .setTitleText("Error!")
                                    .setContentText("Failed to action on the order!")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            //Toast.makeText(mContext,"Failed to update order status", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(mContext,"Error!"+e.toString(), Toast.LENGTH_SHORT).show();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(mContext,"Error!"+error.toString(),Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params= new HashMap<>();
                params.put("action","update_order_status");
                params.put("status",status);
                params.put("order_id",id);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }
    public void updateDeliveryInfo(String id,int pos,String driverName){
        String URL_APPROVE=baseUrl+"shipmentmanager/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_APPROVE,
                response -> {
                    Timber.i("onResponse: %s", response);


                    try {
                        JSONObject jsonObject= new JSONObject(response);
                        String success = jsonObject.getString("success");

                        if(success.equals("1")){
                            myDialog.dismiss();
                        }
                        else if(success.equals("0"))
                        {
                            //Toast.makeText(mContext,"Failed to update order status", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(mContext,"Error!"+e.toString(), Toast.LENGTH_SHORT).show();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(mContext,"Error!"+error.toString(),Toast.LENGTH_SHORT).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params= new HashMap<>();
                params.put("action","update_shipment_details");
                params.put("driver_name",driverName);
                params.put("order_id",id);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }
    public void getDrivers(){
        String URL_JSON = baseUrl+"shipmentmanager/";
        StringRequest request = new StringRequest(Request.Method.POST,URL_JSON,
                response -> {
                    Log.i("getDrivers", "onResponse: "+response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        JSONArray jsonArray = jsonObject.getJSONArray("read");

                        if (success.equals("1")) {
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);
                               Driver driver = new Driver();
                               driver.setDriverId(object.getString("id"));
                               driver.setDriverName(object.getString("name"));
                                driversList.add(driver);

                            }
                        }
                        else if(success.equals("0")){

                        }
                    } catch (JSONException e) {

                        //Toast.makeText(getContext(), "Error! " + e.toString(), Toast.LENGTH_SHORT).show();
                        // Toast.makeText(OrderActivity.this, lstAnime.get(1).toString(), Toast.LENGTH_SHORT).show();

                        e.printStackTrace();
                    }

                }, error -> {
                    //Toast.makeText(getContext(), "Error! " + error.toString(), Toast.LENGTH_SHORT).show();
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("action","view_drivers");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);
    }
}