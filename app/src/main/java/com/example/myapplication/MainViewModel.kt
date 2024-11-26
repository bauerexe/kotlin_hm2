package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

interface RequestController {
    suspend fun fetchTrendingGifs(limit: Int = 5, offset: Int): Result<List<Data>>
    suspend fun serchGifs(limit: Int = 5, offset: Int, query: String): Result<List<Data>>
}

class MainViewModel(private val requestController: RequestController) : ViewModel() {
    val gifs = mutableStateOf<List<Data>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    private var currentOffset = 0
    private val limit = 5
    private var currentQuery: String? = null

    fun fetchTrendingGifs(offset: Int = 0) {
        viewModelScope.launch {
            isLoading.value = true
            val result = requestController.fetchTrendingGifs(limit = limit, offset = offset)
            when (result) {
                is Result.Ok -> {
                    gifs.value = if (offset == 0) result.data else gifs.value + result.data
                    currentOffset = offset + limit
                    errorMessage.value = null
                }
                is Result.Error -> {
                    errorMessage.value = result.error
                }
            }
            isLoading.value = false
        }
    }

    fun searchGifs(offset: Int = 0, query: String) {
        viewModelScope.launch {
            isLoading.value = true
            currentQuery = query
            when (val result = requestController.serchGifs(limit, offset, query)) {
                is Result.Ok -> {
                    gifs.value = if (offset == 0) result.data else gifs.value + result.data
                    currentOffset = offset + limit
                    errorMessage.value = null
                    println("GIFs loaded: ${gifs.value}")
                }
                is Result.Error -> {
                    errorMessage.value = result.error
                }
            }
            isLoading.value = false
        }
    }

    fun loadNextPage() {
        if (!isLoading.value) {
            if (currentQuery.isNullOrEmpty()) {
                fetchTrendingGifs(offset = currentOffset)
            } else {
                searchGifs(offset = currentOffset, query = currentQuery!!)
            }
        }
    }

}
