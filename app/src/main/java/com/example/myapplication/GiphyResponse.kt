package com.example.myapplication

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
    val url: String,
    val width: String,
    val height: String
)
