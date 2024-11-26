package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private val apiBaseUrl = "https://api.giphy.com/"
    private val apiKey = "Ваш_Ключ"

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val requestController = RetrofitController(apiBaseUrl, apiKey)
        val viewModelFactory = MainViewModelFactory(requestController, this)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        setContent {
            if (viewModel.gifs.value.isEmpty()) {
                viewModel.fetchTrendingGifs()
            }
            GifList(viewModel = viewModel)
        }

    }
}



