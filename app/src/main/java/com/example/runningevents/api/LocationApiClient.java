package com.example.runningevents.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationApiClient {
    public static final String BASE_URL = "http://api.positionstack.com/v1/";
    public static Retrofit retrofit;

    public static Retrofit geLocationApiClient(){
        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return  retrofit;
    }
}
