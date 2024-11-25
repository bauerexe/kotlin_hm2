package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitController(apiBaseUrl: String) : RequestController {

    private val retrofit = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val giphyApi = retrofit.create(GiphyApi::class.java)

    override suspend fun fetchTrendingGifs(
        apiKey: String,
        limit: Int,
        offset: Int
    ): Result<List<GifData>> {
        return try {
            val response = giphyApi.getTrendingGIFs(
                apiKey = apiKey,
                limit = limit,
                offset = offset
            )
            Result.Ok(response.data)
        } catch (e: Exception) {
            Result.Error("Ошибка при получении GIF: ${e.message}")
        }
    }
}
