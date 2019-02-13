package com.example.garoz.lab6.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.garoz.lab6.R
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import android.net.Uri
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.widget.ListView
import com.example.garoz.lab6.Activities.Classes.Song
import com.example.garoz.lab6.Activities.Adapters.SongAdapter



class MainActivity : AppCompatActivity() {
    private lateinit var songList: ArrayList<Song>
    private lateinit var songView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        val songView:ListView = findViewById(R.id.song_list)
        songList = ArrayList<Song>()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getSongList()
        Collections.sort(songList) { a, b -> a.title.compareTo(b.title) }
        val songAdt = SongAdapter(this, songList)
        songView.adapter = songAdt
    }

    fun getSongList() {
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                songList.add(Song(thisId, thisTitle, thisArtist))
            } while (musicCursor.moveToNext())
        }
    }
}
