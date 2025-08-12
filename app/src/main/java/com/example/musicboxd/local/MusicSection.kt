package com.example.musicboxd.local

import com.example.musicboxd.network.Track

data class MusicSection(
    val title: String,
    val tracks: List<Track>
)
