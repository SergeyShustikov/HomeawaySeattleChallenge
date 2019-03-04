package com.example.myapplication.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.data.places.details.FullPlaceInfo
import com.example.myapplication.model.storage.PlacesRepository
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@SuppressLint("CheckResult")
class DetailViewModel @Inject constructor(
    private var repository: PlacesRepository
) : ViewModel() {

    private val _placeDetailLiveData = MutableLiveData<FullPlaceInfo>()
    val placeDetailLiveData = _placeDetailLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData = _loadingLiveData

    private val _errorLiveData = MutableLiveData<Throwable>()
    val errorLiveData = _errorLiveData

    fun getPlaceDetails(placeId: String) {
        repository.getPlaceInfo(placeId).subscribe {
            if (it.isLoading) {
                _loadingLiveData.postValue(true)
            } else {
                _loadingLiveData.postValue(false)
            }
            if (it.hasData()) {
                _placeDetailLiveData.postValue(it.data)
            }
            if (it.isError()) {
                _errorLiveData.postValue(it.throwable)
            }
        }
    }
}