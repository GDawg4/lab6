package com.example.garoz.lab6.Activities.Classes

class Song(
        var id: Long,
        var title: String,
        var artist: String
){

    fun Song(songID: Long, songTitle: String, songArtist: String) {
        id = songID
        title = songTitle
        artist = songArtist
    }
}