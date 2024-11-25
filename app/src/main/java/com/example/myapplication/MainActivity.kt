package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

lateinit var viewModel: MainViewModel

class MainActivity : ComponentActivity() {

    private val apiBaseUrl = "https://api.giphy.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requestController = RetrofitController(apiBaseUrl)
        val viewModelFactory = MainViewModel(requestController)
        viewModel = viewModelFactory

        setContent {
            viewModel.fetchTrendingGifs()
            GifList(viewModel = viewModel)
        }
    }
}
