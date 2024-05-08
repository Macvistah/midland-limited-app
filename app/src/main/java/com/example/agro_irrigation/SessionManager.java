package com.example.agro_irrigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.agro_irrigation.Activities.HomeActivity;
import com.example.agro_irrigation.Activities.LoginActivity;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE=0;

    private static final String PREF_NAME = "LOGIN";
    private static final String LOGIN = "IS_LOGIN";
    public static final String NAME = "NAME";
    public static final String EMAIL = "EMAIL";
    public static final String ID = "ID";
    public static final String USER_TYPE = "USER_TYPE";
    public static final String PICTURE = "PICTURE";
    public static final String FNAME = "FNAME";
    public static final String SNAME = "SNAME";
    public static final String GENDER = "GENDER";
    public static final String PHONE = "PHONE";


   public SessionManager(Context context){
       this.context=context;
       sharedPreferences=context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
       editor=sharedPreferences.edit();
   }



    public void createSession(String name, String email, String id, String user_type,String picture){
       editor.putBoolean(LOGIN,true);
       editor.putString(NAME,name);
       editor.putString(EMAIL,email);
       editor.putString(ID,id);
       editor.putString(USER_TYPE,user_type);
       editor.putString(PICTURE,picture);
       editor.apply();
   }

   public void createAccount(String fname, String sname, String gender, String phone){
       editor.putString(FNAME,fname);
       editor.putString(SNAME,sname);
       editor.putString(GENDER,gender);
       editor.putString(PHONE,phone);
       editor.apply();
   }

   public boolean isLogin(){
       return sharedPreferences.getBoolean(LOGIN,false);
   }
   public void checkLogin(){
       if(!this.isLogin()){
           Intent i =new Intent(context, LoginActivity.class);
           context.startActivity(i);
          ((HomeActivity) context).finish();
       }
   }
   public HashMap<String, String> getUserDetail(){
       HashMap<String, String> user =new HashMap<>();
       user.put(NAME,sharedPreferences.getString(NAME,null));
       user.put(EMAIL,sharedPreferences.getString(EMAIL,null));
       user.put(ID,sharedPreferences.getString(ID,null));
       user.put(USER_TYPE,sharedPreferences.getString(USER_TYPE,null));
       user.put(PICTURE,sharedPreferences.getString(PICTURE,null));
       return user;
   }
    public HashMap<String, String> getAccountDetail(){
        HashMap<String, String> account =new HashMap<>();
        account.put(FNAME,sharedPreferences.getString(FNAME,null));
        account.put(SNAME,sharedPreferences.getString(SNAME,null));
        account.put(GENDER,sharedPreferences.getString(GENDER,null));
        account.put(PHONE,sharedPreferences.getString(PHONE,null));
        return account;
    }

   public void logout(){
       editor.clear();
       editor.commit();
       Intent i= new Intent(context,LoginActivity.class);
       context.startActivity(i);
      ((HomeActivity)context).finish();
   }


}
