package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.launch


interface RequestController {
    suspend fun fetchTrendingGifs(limit: Int = 5, offset: Int): Result<List<Data>>
    suspend fun serchGifs(limit: Int = 5, offset: Int, query: String): Result<List<Data>>
}


class MainViewModel(
    private val requestController: RequestController,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val gifs = savedStateHandle.getStateFlow("gifs", emptyList<Data>())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    private var currentOffset = savedStateHandle["currentOffset"] ?: 0
    private val limit = 5
    private var currentQuery: String? = savedStateHandle["currentQuery"]

    fun fetchTrendingGifs(offset: Int = 0) {
        viewModelScope.launch {
            isLoading.value = true
            val result = requestController.fetchTrendingGifs(limit = limit, offset = offset)
            when (result) {
                is Result.Ok -> {
                    val updatedGifs = if (offset == 0) result.data else gifs.value + result.data
                    savedStateHandle["gifs"] = updatedGifs
                    savedStateHandle["currentOffset"] = offset + limit
                    savedStateHandle["currentQuery"] = null
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
                    val updatedGifs = if (offset == 0) result.data else gifs.value + result.data
                    savedStateHandle["gifs"] = updatedGifs
                    savedStateHandle["currentOffset"] = offset + limit
                    savedStateHandle["currentQuery"] = query
                    errorMessage.value = null
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

class MainViewModelFactory(
    private val requestController: RequestController,
    owner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(requestController, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}