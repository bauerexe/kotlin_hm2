package com.example.myapplication

interface RequestController {
    suspend fun fetchTrendingGifs(
        apiKey: String,
        limit: Int = 5,
        offset: Int = 0
    ): Result<List<GifData>>
}
