package com.example.googlemapsapp.retrofit;


import com.example.googlemapsapp.response.ResponseMap;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("nearbysearch/json?radius=5000&type=hospital")
    Call<ResponseMap> getDataMaps(
        @Query("location") String location,
        @Query("key") String key

    );

}
