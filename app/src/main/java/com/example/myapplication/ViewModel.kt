package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class GiphyResponse(
    val data: List<GifData>
)

data class GifData(
    val id: String,
    val images: Images
)

data class Images(
    val original: Original
)

data class Original(
    val url: String
) {
    val height: Float = 0.0f
    val width: Float = 0.0f
}

interface GiphyApi {
    @GET("v1/gifs/trending")
    suspend fun getTrendingGIFs(
        @Query("api_key") apiKey: String,
        @Query("q") query: String = "funny cat",
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
    ): GiphyResponse

}


val retrofit = Retrofit.Builder()
    .baseUrl("https://api.giphy.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()


val api = retrofit.create(GiphyApi::class.java)


class MainViewModel : ViewModel() {
    val gifs = mutableStateOf<List<GifData>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    private var currentOffset = 0
    private val limit = 10
    private var endReached = false

    fun fetchTrendingGifs(offset: Int = 0) {
        viewModelScope.launch {
            isLoading.value = true
            runCatching {
                giphyApi.getTrendingGIFs(
                    apiKey = "iKhtjMYD68PwiDe6lmgWQSJXoQyHbunq",
                    limit = limit,
                    offset = offset
                )
            }.onSuccess { response ->
                gifs.value = if (offset == 0) {
                    response.data
                } else {
                    gifs.value + response.data
                }
                endReached = response.data.isEmpty()
                currentOffset = offset + limit
                errorMessage.value = null
            }.onFailure { e ->
                errorMessage.value = "Ошибка загрузки: ${e.message}"
            }.also {
                isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (isLoading.value || endReached) return
        fetchTrendingGifs(offset = currentOffset)
    }
}


