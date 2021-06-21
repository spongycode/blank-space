package com.spongycode.blankspace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.spongycode.blankspace.api.ApiFetchr
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.getMemeFromFavs

class MemeViewModel: ViewModel() {

    val memeList = mutableListOf<MemeModel>()

    fun memeViewModel(category: String = "Random"): LiveData<List<MemeModel>>{
        val memeViewModel: LiveData<List<MemeModel>>
        = ApiFetchr().fetchMemeByCategory(category)

        return memeViewModel
    }

    val savedMemeLiveData: LiveData<List<MemeModel>>
    init {
        savedMemeLiveData = getMemeFromFavs()
    }

}