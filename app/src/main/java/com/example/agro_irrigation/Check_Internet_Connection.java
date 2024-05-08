package com.example.agro_irrigation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Check_Internet_Connection {

    public boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo !=null && netInfo.isConnectedOrConnecting()){
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting())|| (wifi != null &&
            wifi.isConnectedOrConnecting()))
                return true;

            else return false;
        }
        else
            return false;
    }


}
