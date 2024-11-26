package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    private val apiBaseUrl = "https://api.giphy.com/"
    private val apiKey = "KADgXmsMF9LDc8tHx4A2XmVI39da233d" //  https://giphy.com/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val requestController = RetrofitController(apiBaseUrl, apiKey)
        val viewModel= MainViewModel(requestController)

        setContent {
            viewModel.fetchTrendingGifs()
            GifList(viewModel = viewModel)
        }
    }
}
