package com.example.photogrid_bootcamp.CatApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PhotoService {
    @GET("/v1/images/search")
    Call<List<PhotoResponse>> get(@Query("api_key") String token);

}
