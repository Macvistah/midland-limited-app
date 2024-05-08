package com.example.agro_irrigation.Services;



import com.example.agro_irrigation.Models.AccessToken;
import com.example.agro_irrigation.Models.STKPush;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface STKPushService {
        @POST("mpesa/stkpush/v1/processrequest")
        Call<STKPush> sendPush(@Body STKPush stkPush);

        @GET("oauth/v1/generate?grant_type=client_credentials")
        Call<AccessToken> getAccessToken();
}
