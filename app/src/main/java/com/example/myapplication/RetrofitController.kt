package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response

class RetrofitController(apiBaseUrl: String, private val apiKey: String) : RequestController {

    private val retrofit = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val giphyApi= retrofit.create(GiphyApiTrend::class.java)

    override suspend fun fetchTrendingGifs(limit: Int, offset: Int): Result<List<Data>> {
        return try {
            val response: Response<GiphyResponse> = giphyApi.getTrendingGIFs(
                apiKey = apiKey,
                limit = limit,
                offset = offset,
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    return Result.Ok(it.data)
                } ?: run {
                    Result.Error(R.string.error_with_server.toString())
                }
            } else {
                Result.Error("${R.string.error_with_api} (код: ${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error("${R.string.error_with_gif}: ${e.message}")
        }
    }

    override suspend fun serchGifs(limit: Int, offset: Int, query: String): Result<List<Data>> {
        return try {
            val response: Response<GiphyResponse> = giphyApi.searchGIFs(
                apiKey = apiKey,
                query = query,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    return Result.Ok(it.data)
                } ?: run {
                    Result.Error(R.string.error_with_server.toString())
                }
            } else {
                Result.Error("${R.string.error_with_api} (код: ${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error("${R.string.error_with_gif}: ${e.message}")
        }
    }

}
