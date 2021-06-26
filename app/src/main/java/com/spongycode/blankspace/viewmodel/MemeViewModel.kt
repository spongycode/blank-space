package com.spongycode.blankspace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.spongycode.blankspace.api.ApiFetchr
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.getMemeFromFavs

class MemeViewModel: ViewModel() {

    val memeList = mutableListOf<MemeModel>()

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