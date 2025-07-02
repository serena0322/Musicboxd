package com.example.musicboxd.viewModels

import androidx.lifecycle.*
import com.example.musicboxd.remote.RetrofitInstance
import com.example.musicboxd.remote.RemoteTrack
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _tracks = MutableLiveData<List<RemoteTrack>>()
    val tracks: LiveData<List<RemoteTrack>> = _tracks

    fun search(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchTracks(query)
                _tracks.value = response.data
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
