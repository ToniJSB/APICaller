package com.example.apicaller;


import com.example.apicaller.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("v2/top-headlines")
    Call<NewsResponse> getTopHeadlines(@Query("country") String country, @Query("apiKey") String apiKey);
    @GET("v2/everything")
    Call<NewsResponse> getEverything(@Query("q") String query, @Query("apiKey") String apiKey);
}