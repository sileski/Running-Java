package com.example.runningevents.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CountriesApiClient {

    public static final String BASE_URL = "https://countriesnow.space/api/v0.1/";
    public static Retrofit retrofit;

    public static Retrofit getCountriesApiClient(){
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
