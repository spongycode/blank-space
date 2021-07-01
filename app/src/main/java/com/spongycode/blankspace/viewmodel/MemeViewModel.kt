package com.spongycode.blankspace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.spongycode.blankspace.api.ApiFetchr
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.getMemeFromFavs

class MemeViewModel: ViewModel() {

    var count = 0
    var position = 0
    val randomMemeList = mutableListOf<MemeModel>()
    val gamingMemeList = mutableListOf<MemeModel>()
    val codingMemeList = mutableListOf<MemeModel>()
    val scienceMemeList = mutableListOf<MemeModel>()
    val memberEditsMemeList = mutableListOf<MemeModel>()
    var allMemeDb = hashMapOf<String, MutableList<MemeModel>>()
    var currentMemeCategory: String = "Random"


    fun memeFun(category: String = "Random"): LiveData<List<MemeModel>>{
        val memeLiveData: LiveData<List<MemeModel>>
        = ApiFetchr().fetchMemeByCategory(category)

        return memeLiveData
    }

    val savedMemeLiveData: LiveData<List<MemeModel>>
    init {
        savedMemeLiveData = getMemeFromFavs()
    }

}