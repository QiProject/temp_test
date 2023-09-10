package com.example.bdfuv

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bdfuv.model.MeasureDataModel

class MeasureViewModel : ViewModel() {

    val datas = MutableLiveData<List<MeasureDataModel>>().apply { value = emptyList() }
    val macAddress = MutableLiveData<String>().apply { value = null }
}