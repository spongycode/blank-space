package com.spongycode.blankspace.apiImages

import com.spongycode.blankspace.modelsImages.Data
import retrofit2.Response
import retrofit2.http.GET

interface ImagesApiInterface {

    @GET("get_memes")
    suspend fun getImages(): Response<Data>

}