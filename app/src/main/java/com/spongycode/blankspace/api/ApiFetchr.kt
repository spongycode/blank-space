package com.spongycode.blankspace.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.spongycode.blankspace.model.modelmemes.MemeList
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.ui.main.MainActivity.Companion.firestore
import com.spongycode.blankspace.util.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ApiFetchr {

    lateinit var apiInterface: Call<MemeList?>
    private val memberEditsList: MutableList<MemeModel> = mutableListOf()
    private val responseMemberEditsLiveData = MutableLiveData<List<MemeModel>>()


    fun fetchMemeByCategory(category: String): LiveData<List<MemeModel>> {
        val responseLiveData = MutableLiveData<List<MemeModel>>()

        if (category == "Member Edits") {
            return memberEditsFirebaseFetch()
        }

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
                        i.gif = i.url.substring(i.url.lastIndexOf(".")).toLowerCase(Locale.ROOT)
                            .trim() == ".gif"
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
//        firestore.collection("memberEdits")
//            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener { documents ->
//                try {
//                    val memberEditsList: MutableList<MemeModel> = mutableListOf()
//                    for (document in documents) {
//                        val oneEdit = document.toObject(MemeModel::class.java)
//                        memberEditsList.add(oneEdit)
//                    }
//                    responseMemberEditsLiveData.value = memberEditsList
//                } catch (ex: Exception) {
//
//                }
//            }

        firestore.collection("memberEdits")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                memberEditsList.clear()
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED-> {
                            val oneEdit = dc.document.toObject(MemeModel::class.java)
                            memberEditsList.add(oneEdit)
                        }
                    }
                }
                responseMemberEditsLiveData.value = memberEditsList
            }

        return responseMemberEditsLiveData
    }
}