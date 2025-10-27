package com.example.musicboxd.viewModels

import androidx.lifecycle.*
import com.example.musicboxd.network.RetrofitInstance
import com.example.musicboxd.network.Track
import kotlinx.coroutines.launch

// ViewModel per la ricerca brani: esegue chiamate API Deezer tramite Retrofit e aggiorna LiveData
// con la lista di tracce trovate, gestendo le operazioni in coroutine tramite viewModelScope.

class SearchViewModel : ViewModel() {
    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

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
