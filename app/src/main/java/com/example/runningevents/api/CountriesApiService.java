package com.example.runningevents.api;

import com.example.runningevents.models.Cities;
import com.example.runningevents.models.Countries;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CountriesApiService {

    @POST("countries/cities")
    Call<Cities> getCities(@Body Countries countries);
}
