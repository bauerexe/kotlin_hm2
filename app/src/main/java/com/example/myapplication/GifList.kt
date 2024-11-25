package com.example.myapplication

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

@Composable
fun GifList(viewModel: MainViewModel) {
    val gifs = viewModel.gifs
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Column(modifier = Modifier.fillMaxSize()) {
        GifListHeader()
        GifListContent(
            gifs = gifs.value,
            isLoading = isLoading.value,
            errorMessage = errorMessage.value,
            onRetry = { viewModel.fetchTrendingGifs() },
            onLoadMore = { viewModel.loadNextPage() },
            imageLoader = imageLoader
        )
    }
}

@Composable
fun GifListHeader() {
    Text(
        text = "Gifs",
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(Color.Blue),
        fontSize = 56.sp,
        textAlign = TextAlign.Center
    )
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun GifListContent(
    gifs: List<GifData>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    imageLoader: ImageLoader
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && gifs.isEmpty() -> ShowLoadingIndicator()
            errorMessage != null && gifs.isEmpty() -> ShowErrorScreen(errorMessage, onRetry)
            else -> ShowGifList(gifs, isLoading, onLoadMore, imageLoader)
        }
    }
}

@Composable
fun ShowLoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ShowErrorScreen(message: String, onRetry: () -> Unit) {
    ErrorScreen(
        message = message,
        onRetry = onRetry
    )
}

@Composable
fun ShowGifList(
    gifs: List<GifData>,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    imageLoader: ImageLoader
) {
    LazyColumn {
        itemsIndexed(gifs) { index, gif ->
            GifItem(gif, imageLoader)
            if (index == gifs.lastIndex) {
                onLoadMore()
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GifItem(gif: GifData, imageLoader: ImageLoader) {
    val aspectRatio: Float = try {
        val width = gif.images.original.width.toFloat()
        val height = gif.images.original.height.toFloat()
        width / height
    } catch (e: Exception) {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val isLoading = remember { mutableStateOf(true) }
        val isError = remember { mutableStateOf(false) }

        if (isLoading.value) {
            CircularProgressIndicator()
        }

        if (isError.value) {
            Text(
                text = "Error loading image",
                color = Color.Red,
                modifier = Modifier
                    .background(Color.White)
                    .padding(8.dp)
            )
        }

        AsyncImage(
            model = gif.images.original.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            imageLoader = imageLoader,
            onSuccess = {
                isLoading.value = false
                isError.value = false
            },
            onError = {
                isLoading.value = false
                isError.value = true
            }
        )
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Retry",
                modifier = Modifier
                    .background(Color.Gray)
                    .padding(8.dp)
                    .clickable { onRetry() }
            )
        }
    }
}