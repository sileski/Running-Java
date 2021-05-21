package com.example.runningevents.api;

import com.example.runningevents.models.Location;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface LocationApiService {

    @GET("forward")
    Call<Location> getLatAndLan(@Query("access_key") String accessKey, @Query("query") String locationName);
}
