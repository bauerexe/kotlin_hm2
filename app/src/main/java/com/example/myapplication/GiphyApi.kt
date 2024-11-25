package com.example.myapplication

import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApi {
    @GET("v1/gifs/trending")
    suspend fun getTrendingGIFs(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 5,
        @Query("offset") offset: Int = 0,
    ): GiphyResponse
}