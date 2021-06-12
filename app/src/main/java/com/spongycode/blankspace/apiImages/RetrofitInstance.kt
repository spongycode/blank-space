package com.spongycode.blankspace.apiImages

import retrofit2.Retrofit

class RetrofitInstance {

    companion object {

        private val imageRetrofit by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.imgflip.com/get_memes")
        }

    }

}