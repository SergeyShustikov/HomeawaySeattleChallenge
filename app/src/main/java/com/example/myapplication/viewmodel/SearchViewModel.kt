package com.example.myapplication.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.data.places.search.Place
import com.example.myapplication.model.storage.PlacesRepository
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("CheckResult")
class SearchViewModel @Inject constructor(
    private var repository: PlacesRepository
) : ViewModel() {

    private val _placesLiveData = MutableLiveData<List<Place>>()
    val placesLiveData = _placesLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData = _loadingLiveData

    private val _errorLiveData = MutableLiveData<Throwable>()
    val errorLiveData = _errorLiveData

    private var lastQuery: String = ""

    private var webSearchSubject: PublishSubject<String> = PublishSubject.create()

    fun init() {
        val webSearch = webSearchSubject
            .filter { string ->
                return@filter !string.isEmpty()
            }
            .map { text -> text.toLowerCase().trim() }
            .debounce(500, TimeUnit.MILLISECONDS)
            .switchMap { startingWith ->
                repository.search(startingWith)
            }
        webSearch.subscribe {
            if (it.isLoading) {
                _loadingLiveData.postValue(true)
            } else {
                _loadingLiveData.postValue(false)
            }
            if (it.hasData()) {
                _placesLiveData.postValue(it.data)
            }
            if (it.isError()) {
                _errorLiveData.postValue(it.throwable)
                lastQuery = ""
            }
        }
    }

    fun search(query: String) {
        if (lastQuery != query) {
            lastQuery = query
            webSearchSubject.onNext(lastQuery)
        }
    }
}