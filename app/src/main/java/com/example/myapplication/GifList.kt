package com.example.myapplication

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.ImageLoader

lateinit var globalImageLoader: ImageLoader
lateinit var globalViewModel: MainViewModel

fun initImageLoader(context: Context) {
    globalImageLoader = ImageLoader.Builder(context)
        .components {
            if (android.os.Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }.build()
}

@Composable
fun GifList(viewModel: MainViewModel) {
    if (!::globalImageLoader.isInitialized) {
        initImageLoader(LocalContext.current)
    }

    if (!::globalViewModel.isInitialized) {
        globalViewModel = viewModel
    }

    if (!viewModel.errorMessage.value.isNullOrEmpty()) {
        viewModel.errorMessage.value?.let { ErrorText(it, onRetry = {viewModel.fetchTrendingGifs()}) }
    } else{
        var displayMode by remember { mutableStateOf(DisplayMode.LIST) }

        Column(modifier = Modifier.fillMaxSize()) {
            GifHeader()
            Box(modifier = Modifier.weight(1f)) {
                if (displayMode == DisplayMode.LIST) GifListContent(isGrid = false)
                else GifListContent(isGrid = true)
            }
            DisplayModeButtons(
                currentMode = displayMode,
                onModeSelected = { mode -> displayMode = mode }
            )
        }
    }
}

@Composable
fun GifHeader() {
    Text(
        text = stringResource(id = R.string.app_name),
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(Color.Blue),
        fontSize = 56.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DisplayModeButtons(currentMode: DisplayMode, onModeSelected: (DisplayMode) -> Unit) {
    val buttons = listOf(
        stringResource(id = R.string.list_mode) to DisplayMode.LIST,
        stringResource(id = R.string.grid_mode) to DisplayMode.GRID
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { (text, mode) ->
            Button(
                onClick = { onModeSelected(mode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == mode)  Color(0xFF6200EE) else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text(text = text)
            }
        }
    }
}

enum class DisplayMode {
    LIST,
    GRID
}

@Composable
fun GifListContent(isGrid: Boolean) {
    val gifs = globalViewModel.gifs.value
    val isLoading = globalViewModel.isLoading.value
    val columns = if (LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) 3 else 2

    if (isGrid) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gifs.size) { index ->
                GifItem(gifs[index])
                if (index == gifs.lastIndex && !isLoading) globalViewModel.loadNextPage()
            }
            addLoadingIndicator(isLoading)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(8.dp)
        ) {
            itemsIndexed(gifs) { index, gif ->
                GifItem(gif)
                if (index == gifs.lastIndex && !isLoading) globalViewModel.loadNextPage()
            }
            addLoadingIndicator(isLoading)
        }
    }
}

fun LazyGridScope.addLoadingIndicator(isLoading: Boolean) {
    if (isLoading) {
        item { LoadingItem() }
    }
}

fun LazyListScope.addLoadingIndicator(isLoading: Boolean) {
    if (isLoading) {
        item { LoadingItem() }
    }
}

@Composable
fun LoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun GifItem(gif: Data) {
    AsyncImageWithRetry(
        imageUrl = gif.images?.original?.url.orEmpty(),
        aspectRatio = calculateAspectRatio(gif),
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun AsyncImageWithRetry(
    imageUrl: String,
    aspectRatio: Float,
    modifier: Modifier = Modifier
) {
    var retryKey by remember { mutableIntStateOf(0) }
    val isLoading = remember { mutableStateOf(true) }
    val isError = remember { mutableStateOf(false) }

    Box(
        modifier = modifier.aspectRatio(aspectRatio),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading.value) CircularProgressIndicator()
        if (isError.value) {
            ErrorText(stringResource(id = R.string.error), onRetry = {
                retryKey++
                isError.value = false
                isLoading.value = true
            })
        }

        AsyncImage(
            model = "$imageUrl?retry=$retryKey",
            contentDescription = null,
            imageLoader = globalImageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
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
fun ErrorText(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = Color.Red, modifier = Modifier.padding(16.dp))
        Text(
            text = stringResource(id = R.string.retry_button),
            color = Color.Blue,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.LightGray)
                .padding(4.dp)
                .clickable { onRetry() }
        )
    }
}

fun calculateAspectRatio(gif: Data): Float {
    val width = gif.images?.original?.width?.toFloatOrNull()
    val height = gif.images?.original?.height?.toFloatOrNull()
    return if (width != null && height != null && height != 0f) width / height else 1f
}
