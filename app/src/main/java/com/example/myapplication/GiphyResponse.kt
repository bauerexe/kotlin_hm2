package com.example.myapplication

import com.google.gson.annotations.SerializedName


data class GiphyResponse(
    @SerializedName("data" ) var data : ArrayList<Data> = arrayListOf()
)

data class Data (
    @SerializedName("id"     ) var id     : String? = null,
    @SerializedName("images" ) var images : Images? = Images()
)

data class Images (
    @SerializedName("original" ) var original : Original? = Original()
)

data class Original (
    @SerializedName("url"    ) var url    : String? = null,
    @SerializedName("width"  ) var width  : String? = null,
    @SerializedName("height" ) var height : String? = null
)