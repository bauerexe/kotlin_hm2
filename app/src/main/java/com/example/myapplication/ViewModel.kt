package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val requestController: RequestController) : ViewModel() {
    val gifs = mutableStateOf<List<GifData>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    private var currentOffset = 0
    private val limit = 5
    private var endReached = false

    fun fetchTrendingGifs(offset: Int = 0) {
        viewModelScope.launch {
            isLoading.value = true
            val apiKey = "iKhtjMYD68PwiDe6lmgWQSJXoQyHbunq"
            when (val result = requestController.fetchTrendingGifs(
                apiKey = apiKey,
                limit = limit,
                offset = offset
            )) {
                is Result.Ok -> {
                    val data = result.data
                    gifs.value = if (offset == 0) {
                        data
                    } else {
                        gifs.value + data
                    }
                    endReached = data.isEmpty()
                    currentOffset = offset + limit
                    errorMessage.value = null
                }

                is Result.Error -> {
                    errorMessage.value = "Ошибка загрузки: ${result.error}"
                }
            }
            isLoading.value = false
        }
    }

    fun loadNextPage() {
        if (isLoading.value || endReached) return
        fetchTrendingGifs(offset = currentOffset)
    }
}
