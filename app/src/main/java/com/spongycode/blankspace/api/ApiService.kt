package com.spongycode.blankspace.api

import com.spongycode.blankspace.model.modelmemes.MemeList
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiInterface {

    @GET("/gimme/wholesomememes/7")
    fun getMemes(): Call<MemeList?>

    companion object {

        var BASE_URL = "https://meme-api.herokuapp.com"

        fun create() : ApiInterface {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiInterface::class.java)

        }
    }
}