package com.spongycode.blankspace.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.spongycode.blankspace.model.modelmemes.MemeList
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.ui.main.MainActivity.Companion.firestore
import com.spongycode.blankspace.util.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiFetchr{

    lateinit var apiInterface: Call<MemeList?>

    fun fetchMemeByCategory(category: String): LiveData<List<MemeModel>> {
        val responseLiveData = MutableLiveData<List<MemeModel>>()

        if (category == "Member Edits"){ return memberEditsFirebaseFetch() }

        when (category) {
            "Random" -> apiInterface = ApiInterface.create().getMemesRandom()
            "Coding" -> apiInterface = ApiInterface.create().getMemesProgram()
            "Science" -> apiInterface = ApiInterface.create().getMemesScience()
            "Gaming" -> apiInterface = ApiInterface.create().getMemesGaming()
        }


        apiInterface.enqueue(object : Callback<MemeList?> {
            override fun onResponse(call: Call<MemeList?>, response: Response<MemeList?>) {

                if (response.body() != null) {
                    val memeList: MutableList<MemeModel> = mutableListOf()
                    for (i in response.body()!!.memes!!) {
                        memeList.add(i)
                    }
                    responseLiveData.value = memeList

                }

            }

            override fun onFailure(call: Call<MemeList?>, t: Throwable) {
                Log.d(Constants.TAG, "Error Fetching: ${t.printStackTrace()}")
            }
        })

        return responseLiveData
    }

    private fun memberEditsFirebaseFetch(): LiveData<List<MemeModel>> {
        val responseMemberEditsLiveData = MutableLiveData<List<MemeModel>>()
        firestore.collection("memberEdits")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                try {
                    val memberEditsList: MutableList<MemeModel> = mutableListOf()
                    for (document in documents) {
                        val oneEdit = document.toObject(MemeModel::class.java)
                        memberEditsList.add(oneEdit)
                    }
                    responseMemberEditsLiveData.value = memberEditsList
                } catch (ex: Exception) {

                }
            }
        return responseMemberEditsLiveData
    }
}