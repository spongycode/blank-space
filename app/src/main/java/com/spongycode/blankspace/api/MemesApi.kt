package com.spongycode.blankspace.api

import com.spongycode.blankspace.model.modelsImages.ImageResponse
import retrofit2.Call
import retrofit2.http.GET

interface MemesApi {

    @GET("/get_memes")
    fun fetchContents(): Call<ImageResponse>

}