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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.agro_irrigation.Constants;
import com.example.agro_irrigation.Interfaces.PurchaseUpdateListener;
import com.example.agro_irrigation.Models.AccessToken;
import com.example.agro_irrigation.Models.Driver;
import com.example.agro_irrigation.Models.Order_Item_Details;
import com.example.agro_irrigation.Models.Purchases;
import com.example.agro_irrigation.Models.STKPush;
import com.example.agro_irrigation.R;
import com.example.agro_irrigation.Services.DarajaApiClient;
import com.example.agro_irrigation.Services.Utils;
import com.example.agro_irrigation.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import timber.log.Timber;


public class PurchasesRecyclerViewAdapter extends RecyclerView.Adapter<PurchasesRecyclerViewAdapter.MyViewHolder> implements Filterable {

    private Context mContext ;
    private List<Purchases> mData ;
    private List<Purchases> mDataFiltered;
    private Dialog myDialog;
    private BottomSheetDialog bottomSheetDialog;
    private RequestQueue requestQueue;
    private List<Order_Item_Details> lstDetails= new ArrayList<>();
    private SessionManager sessionManager;
    String baseUrl,userType,phone,userName;
    ArrayAdapter<Driver> driverAdapter;
    private List<Driver> driversList = new ArrayList<>();
    DarajaApiClient mApiClient;

    PurchaseUpdateListener mListener;

    public PurchasesRecyclerViewAdapter(Context mContext, List lst, PurchaseUpdateListener listener) {
        this.mContext = mContext;
        this.mData = lst;
        this.mListener = listener;
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
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view ;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.purchase_list_item,parent,false);
        final MyViewHolder viewHolder = new MyViewHolder(view);
        getAccessToken();

        // click listener here
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Purchases purchase = mDataFiltered.get(position);
        NumberFormat format = NumberFormat.getNumberInstance();
        final String id = purchase.getId();
        final String purchaseNo = purchase.getPurchase_no();
        final String productName = purchase.getProduct_name();
        final String supplierName = purchase.getSupplier_name();
        final String supplierId = purchase.getSupplier_id();
        final String purchaseDate = purchase.getPurchase_date();
        final int availableQty = purchase.getAvailable_qty();
        final int originalQty = purchase.getOriginal_qty();
        final int price = purchase.getPrice();
        final int finalPrice = purchase.getFinal_price();

        final String status = purchase.getStatus();
        final String paymentStatus = Objects.equals(purchase.getPayment_code(), "null") ? "UNPAID" : "PAID";

        final int pos = mData.indexOf(mData.get(holder.getAdapterPosition()));
        
        String PURCHASE_STATUS  = "";

        int totalAmount = price * originalQty;
        if (availableQty != 0){
            totalAmount = price * availableQty;
        }

        holder.txtPurchaseNo.setText(purchaseNo);
        holder.txtPurchaseStatus.setText(status);
        holder.txtOrderPayment.setText(paymentStatus);
        holder.txtProductName.setText(productName);
        holder.txtOrderDate.setText(purchaseDate);
        holder.txtSupplier.setText(supplierName);
        holder.txtOriginalQty.setText(originalQty+ " units");
        holder.txtAvailableQty.setText("Available: "+availableQty);
        holder.txtPurchasePrice.setText(format.format(price)+" per unit");
        holder.txtPurchaseTotal.setText("Total: "+format.format(totalAmount));

        if(status.equals("PENDING")){
            holder.txtAvailableQty.setVisibility(View.GONE);
        }

        if (paymentStatus.equals("PAID")){
            holder.printReceiptLayout.setVisibility(View.VISIBLE);
        }



        if (userType.equals("store manager" ) && status.equals("PENDING")){
            holder.layoutAction.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.GONE);
        }
        if (userType.equals("supplier") && status.equals("PENDING") ){
            PURCHASE_STATUS = "APPROVED";
            holder.layoutAction.setVisibility(View.VISIBLE);
            holder.btnConfirmPurchase.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnConfirmPurchase.setText("Confirm");
            holder.layoutAddress.setVisibility(View.GONE);
        }
        if (userType.equals("sales manager") && status.equals("COMPLETED") && Objects.equals(purchase.getPayment_code(), "null") ){
            holder.layoutAction.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnConfirmPurchase.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnConfirmPurchase.setText("Make Payment");
            PURCHASE_STATUS = "COMPLETED";

        }
        if (userType.equals("store manager") && status.equals("APPROVED")){
            holder.layoutAction.setVisibility(View.VISIBLE);
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnApprove.setText("Confirm Delivery");
            PURCHASE_STATUS = "COMPLETED";
        }

        if(status.equals("CANCELED") || (status.equals("COMPLETED") && !Objects.equals(purchase.getPayment_code(), "null"))){
            holder.layoutAction.setVisibility(View.GONE);
        }
        final Double TOTAL_AMOUNT = Double.parseDouble(String.valueOf(totalAmount));
        //set Onclick Listener to the view Option
        holder.printReceiptLayout.setOnClickListener(v -> {
            //performSTKPush("0701824145","1", "1");
            printReport(purchase);
        });
        String finalPURCHASE_STATUS = PURCHASE_STATUS;
        holder.btnApprove.setOnClickListener(v -> {
            new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Approve Purchase No. "+purchaseNo+ "?")
                    .setContentText("You cannot undo this action!")
                    .setConfirmText("Yes, Approve purchase!")
                    .setConfirmClickListener(sDialog -> {
                        updateStatus(sDialog, id, finalPURCHASE_STATUS,pos,"", "", "");
                    })
                    .show();

        });
        holder.btnCancel.setOnClickListener(v -> {
            new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Cancel Purchase No. "+purchaseNo+ "?")
                    .setContentText("You cannot undo this action!")
                    .setConfirmText("Yes, Cancel!")
                    .setConfirmClickListener(sDialog -> {
                        updateStatus(sDialog, id,"CANCELED",pos,"", "", "");
                    })
                    .show();
        });
        holder.btnConfirmPurchase.setOnClickListener(v -> {
            if (userType.equals("sales manager")){
                makePaymentPopup(id, purchaseNo, price, availableQty,  pos,  finalPURCHASE_STATUS );
                return;
            }
            confirmPurchasePopup(id,purchaseNo,String.valueOf(price),  String.valueOf(originalQty),pos);
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
                    List<Purchases> filteredList = new ArrayList<>();
                    for (Purchases row : mData) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getPurchase_no().toLowerCase().contains(charString.toLowerCase()) ||  row.getPurchase_date().contains(charSequence)) {
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
                mDataFiltered = (ArrayList<Purchases>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView
                txtPurchaseNo,
                txtPurchasePrice,
                txtPurchaseTotal,
                txtOriginalQty,
                txtAvailableQty,
                txtPurchaseStatus,
                txtOrderPayment,
                txtProductName,
                txtOrderDate,
                txtSupplier,
                txtViewOrder;
        Button btnApprove,btnCancel,btnConfirmPurchase;
        LinearLayout layoutAddress,layoutPayment,layoutAction, printReceiptLayout;


        public MyViewHolder(View itemView) {
            super(itemView);
            txtPurchaseNo = (TextView) itemView.findViewById(R.id.order_no);
            txtOriginalQty = (TextView) itemView.findViewById(R.id.orderQty);
            txtAvailableQty = (TextView) itemView.findViewById(R.id.orderAvailable);
            txtPurchaseStatus = (TextView) itemView.findViewById(R.id.orderStatus);
            txtOrderPayment = (TextView) itemView.findViewById(R.id.orderPayment);
            txtProductName = (TextView) itemView.findViewById(R.id.productName);
            txtPurchaseTotal = (TextView) itemView.findViewById(R.id.orderTotal);
            txtPurchasePrice = (TextView) itemView.findViewById(R.id.orderPrice);
            txtOrderDate = (TextView) itemView.findViewById(R.id.orderDate);
            printReceiptLayout = (LinearLayout) itemView.findViewById(R.id.viewOrderLt);
            txtViewOrder = (TextView) itemView.findViewById(R.id.viewOrder);
            btnApprove = (Button) itemView.findViewById(R.id.approve);
            btnCancel = (Button) itemView.findViewById(R.id.cancel);
            btnConfirmPurchase = (Button) itemView.findViewById(R.id.assign_driver);
            txtSupplier = (TextView) itemView.findViewById(R.id.location);
            layoutAddress = (LinearLayout) itemView.findViewById(R.id.orderAddress);
            layoutPayment = (LinearLayout) itemView.findViewById(R.id.payment);
            layoutAction = (LinearLayout) itemView.findViewById(R.id.action);
            
        }
    }
    public void printReport(Purchases purchase){
        final String id = purchase.getId();
        final String purchaseNo = purchase.getPurchase_no();
        final String productName = purchase.getProduct_name();
        final String supplierName = purchase.getSupplier_name();
        final String description = purchase.getDescription();
        final String supplierId = purchase.getSupplier_id();
        final String purchaseDate = purchase.getPurchase_date();
        final int availableQty = purchase.getAvailable_qty();
        final int originalQty = purchase.getOriginal_qty();
        final int price = purchase.getPrice();
        final int finalPrice = purchase.getFinal_price();

        Double totalAmount = (double) (price * availableQty);

        final String status = purchase.getStatus();
        final String paymentCode = purchase.getPayment_code();



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

        String val = purchaseNo +" "+((int)(Math.random()*900000)+100000);
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
        canvas.drawText("Purchase Receipt",pageWidth/2,500,titlePaint);

        titlePaint.setColor(Color.GRAY);
        canvas.drawRect(20,540,pageWidth-20,550,titlePaint);
        myPaint.setTextAlign(Paint.Align.LEFT);
        myPaint.setTextSize(30f);
        myPaint.setColor(Color.BLACK);
        canvas.drawText("Receipt No",30,590,myPaint);
        canvas.drawText(": "+purchaseNo,250,590,myPaint);
        canvas.drawText("Paid To",30,645,myPaint);
        canvas.drawText(": "+supplierName,250,645,myPaint);

        myPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Txn Code  : "+paymentCode,pageWidth-30,590,myPaint);
        canvas.drawText("Date  : "+dateFormat.format(dateObj),pageWidth-30,645,myPaint);


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
        int y = 920;
        myPaint.setColor(Color.DKGRAY);
        myPaint.setTextSize(30f);
        canvas.drawText((1)+".",40,y,myPaint);
        canvas.drawText(productName,200,y,myPaint);
        myPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(format.format(availableQty),750,y,myPaint);
        canvas.drawText(format.format(price),1000,y,myPaint);
        canvas.drawText(format.format(Double.parseDouble(String.valueOf(totalAmount))) ,pageWidth-40,y,myPaint);
        myPaint.setTextAlign(Paint.Align.LEFT);

        canvas.drawLine(680,y_index+50,pageWidth-20,y_index+50,myPaint);
        canvas.drawText("Sub-Total",700,y_index+100,myPaint);
        canvas.drawText(":",900,y_index+100,myPaint);
        myPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(format.format(totalAmount),pageWidth-40,y_index+100,myPaint);
        myPaint.setTextAlign(Paint.Align.LEFT);

//        //float tax = (float) (0.0*subTotal);
//        canvas.drawText("Shipment Fee",700,y_index+150,myPaint);
//        canvas.drawText(":",900,y_index+150,myPaint);
//        myPaint.setTextAlign(Paint.Align.RIGHT);
//        canvas.drawText(format.format(Charge),pageWidth-40,y_index+150,myPaint);
//        myPaint.setTextAlign(Paint.Align.LEFT);

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
        String fileName = "purchase_receipt"+id+".pdf";
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
                "Midland LTD", //Account reference
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
    public void confirmPurchasePopup(String id, String purchaseNo, String price, String originalQty, int pos){
        Button btnSubmit;
        TextView txtClose,txtOriginalQty, txtPrice;
        final EditText edQty;

        myDialog.setContentView(R.layout.approve_purchase_dialog);

        txtClose = (TextView) myDialog.findViewById(R.id.close);
        txtOriginalQty = myDialog.findViewById(R.id.originalQuantity);
        txtPrice = myDialog.findViewById(R.id.price);
        edQty = myDialog.findViewById(R.id.qty);
        btnSubmit = myDialog.findViewById(R.id.submit);

        myDialog.setCancelable(false);

        txtOriginalQty.setText(originalQty);
        txtPrice.setText(price);
        edQty.setText(originalQty);

        txtClose.setOnClickListener(v -> myDialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String qty = edQty.getText().toString().trim();
            if(qty.equals(""))
            {
                Toast.makeText(mContext,"Please input quantity",Toast.LENGTH_LONG).show();
                return;
            }
            if (Integer.valueOf(qty) > Integer.valueOf(originalQty)){
                edQty.setError("Quantity cannot exceed "+ originalQty + " units");
                return;
            }
            new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Confirm Purchase No. "+purchaseNo+ "?")
                    .setContentText("You cannot undo this action!")
                    .setConfirmText("Yes, Confirm!")
                    .setConfirmClickListener(sDialog -> {
                        myDialog.dismiss();
                        updateStatus(sDialog,id,"APPROVED",pos, qty, price, "");
                    })
                    .show();
            });

        myDialog.show();
    }

    public void makePaymentPopup(String id, String purchaseNo, int price, int availableQty, int pos, String status){
        Button btnSubmit;
        TextView txtClose,txtAmount, txtTitle;
        LinearLayout makePaymentLayout, confirmPurchaseLayout;
        final EditText edPaymentCode;
        final int totalPrice = price * availableQty;
        NumberFormat format = NumberFormat.getNumberInstance();

        myDialog.setContentView(R.layout.approve_purchase_dialog);

        txtClose = (TextView) myDialog.findViewById(R.id.close);
        txtTitle = myDialog.findViewById(R.id.title);
        txtAmount = myDialog.findViewById(R.id.amount);
        edPaymentCode = myDialog.findViewById(R.id.payment_code);
        makePaymentLayout = myDialog.findViewById(R.id.makePayment);
        confirmPurchaseLayout = myDialog.findViewById(R.id.confirmPurchase);
        btnSubmit = myDialog.findViewById(R.id.submit);

        confirmPurchaseLayout.setVisibility(View.GONE);
        makePaymentLayout.setVisibility(View.VISIBLE);
        txtTitle.setText(R.string.make_payment);
        txtAmount.setText("Ksh. "+format.format(totalPrice));

        myDialog.setCancelable(false);


        txtClose.setOnClickListener(v -> myDialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String transactionCode = edPaymentCode.getText().toString().trim();
           // String regexPattern = "^[A-Z][A-Z0-9]{8}[A-Z]$";
            String regexPattern = "^[A-Z][A-Z0-9]{9}";
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(transactionCode);
            if(transactionCode.equals(""))
            {
                edPaymentCode.setError("Please input MPESA code to proceed");
                return;
            }
            if (!matcher.matches()) {
                edPaymentCode.setError("Invalid MPESA Code, should be 10 characters long and start with a letter");
                return;
            }

            new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Record Payment for Purchase No. "+purchaseNo+ "?")
                    .setContentText("You cannot undo this action!")
                    .setConfirmText("Yes, Proceed!")
                    .setConfirmClickListener(sDialog -> {
                        myDialog.dismiss();
                        updateStatus(sDialog,id,status ,pos, String.valueOf(availableQty), String.valueOf(price), transactionCode);
                    })
                    .show();
        });

        myDialog.show();
    }
    public void updateStatus(
            SweetAlertDialog sDialog,
            String id,
            String status,
            int pos,
            String availableQty,
            String price,
            String paymentCode
    ){
        String URL_APPROVE = baseUrl+"farmer/?action=update_purchase_status";
        if(status.equals("APPROVED") || !paymentCode.isEmpty()){
            URL_APPROVE = baseUrl+"farmer/?action=approve_purchase";
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_APPROVE,
                response -> {
                    Timber.i("onResponse: %s", response);
                    try {
                        JSONObject jsonObject= new JSONObject(response);
                        String success = jsonObject.getString("success");

                        if(success.equals("1")){
                            sDialog
                                    .setTitleText("Success!")
                                    .setContentText("Status updated Successfully!")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            if (mData.size() > 0){
                                mData.remove(pos);
                                notifyItemRemoved(pos);
                                mListener.onPurchaseRemoved(pos);
                            }


                            bottomSheetDialog.dismiss();
                        }
                        else if(success.equals("0"))
                        {
                            sDialog
                                    .setTitleText("Error!")
                                    .setContentText("Could not process the request. Try again!")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
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
                params.put("status",status);
                params.put("purchase_id",id);
                params.put("price", price);
                params.put("available_qty", availableQty);
                params.put("payment_code", paymentCode);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }

}