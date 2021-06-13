package com.spongycode.blankspace.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.model.modelsImages.ImageList
import com.spongycode.blankspace.model.modelsImages.ImageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MemesFetchr {

    private val memesApi: MemesApi

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgflip.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        memesApi = retrofit.create(MemesApi::class.java)
    }

    fun fetchContents(): LiveData<List<Image>> {
        val responseLiveData: MutableLiveData<List<Image>> = MutableLiveData()
        val memesResponse: Call<ImageResponse> = memesApi.fetchContents()

        memesResponse.enqueue(object : Callback<ImageResponse> {
            override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                Log.d("memes", "Response received")
                val imageResponse: ImageResponse? = response.body()
                val imageList: ImageList? = imageResponse?.data
                var images: List<Image> = imageList?.memes ?: mutableListOf()
                images = images.filterNot { it.url.isBlank() }
                responseLiveData.value = images
            }

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                Log.e("memes", "failded to fetch")
            }
        })

        return responseLiveData
    }

}