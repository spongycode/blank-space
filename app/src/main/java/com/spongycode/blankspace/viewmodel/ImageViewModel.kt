package com.spongycode.blankspace.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.spongycode.blankspace.api.MemesFetchr
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.storage.getTemplateFromFavs

class ImageViewModel: ViewModel() {

    val imageList = mutableListOf<Image>()

    val imageLiveData: LiveData<List<Image>>
    init {
        imageLiveData = MemesFetchr().fetchContents()
    }

    val savedImageLiveData: LiveData<List<Image>>
    init {
        savedImageLiveData = getTemplateFromFavs()
    }

}