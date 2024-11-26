package com.example.myapplication

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface GiphyApiTrend {
    @GET("v1/gifs/trending")
    suspend fun getTrendingGIFs(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<GiphyResponse>

   @GET("v1/gifs/search")
    suspend fun searchGIFs(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<GiphyResponse>
}
