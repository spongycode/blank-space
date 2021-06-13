package com.spongycode.blankspace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.spongycode.blankspace.api.MemesFetchr
import com.spongycode.blankspace.model.modelsImages.Image

class ImageViewModel: ViewModel() {

    val imageLiveData: LiveData<List<Image>>
    init {
        imageLiveData = MemesFetchr().fetchContents()
    }

}