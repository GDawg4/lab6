package com.example.garoz.lab6.Activities

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.MenuItem;
import android.view.View;
import com.example.garoz.lab6.Activities.Classes.MusicService
import com.example.garoz.lab6.Activities.Classes.MusicService.MusicBinder
import com.example.garoz.lab6.R
import kotlinx.android.synthetic.main.activity_main.view.*
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast
import com.example.garoz.lab6.Activities.Classes.MusicController

class MainActivity : AppCompatActivity(), MediaPlayerControl {
    override fun getAudioSessionId(): Int {
        return musicSrv!!.getAudioSession()
    }

    override fun isPlaying(): Boolean {
        val test = this.musicSrv

        if (test != null && musicBound){
            return test.isPng()
        }
        return false
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        val test = this.musicSrv

        if(musicSrv!=null && musicBound && test?.isPng() == true)
            return test.getDur()
        else
            return 0;
    }

    override fun pause() {
        playbackPaused=true;
        musicSrv?.pausePlayer()
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun seekTo(pos: Int) {
        musicSrv?.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        val test = this.musicSrv

        if(musicSrv!=null && musicBound && test?.isPng() == true)
            return test.getPosn()
        else
            return 0;
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        musicSrv?.go()
    }

    override fun canPause(): Boolean {
        return true
    }

    private lateinit var songList: ArrayList<Song>
    private lateinit var songView: ListView
    private var musicSrv: MusicService? = null
    private var playIntent : Intent? = null
    private var musicBound = false
    private lateinit var controller:MusicController
    private var paused = false
    var playbackPaused = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()
        val songView = findViewById<ListView>(R.id.song_list)
        songList = ArrayList<Song>()
        getSongList()
        Collections.sort(songList) { a, b -> a.title.compareTo(b.title) }
        val songAdt = SongAdapter(this, songList)
        songView.adapter = songAdt
        setController()
    }

    override fun onPause() {
        super.onPause();
        paused=true;
    }

    override fun onResume() {
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    override fun onStop() {
        controller.hide();
        super.onStop();
    }

    private fun setController() {
        controller = MusicController(this)

        fun playNext(){
            musicSrv?.playNext();
            if(playbackPaused){
                setController();
                playbackPaused=false;
            }
            controller.show(0);
        }

        fun playPrev(){
            musicSrv?.playPrev();
            if(playbackPaused){
                setController();
                playbackPaused=false;
            }
            controller.show(0);
        }

        controller.setPrevNextListeners({playNext()}, {playPrev()})

        controller.setMediaPlayer(this)
        controller.setAnchorView(findViewById(R.id.song_list))
        controller.isEnabled = true

    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    //connect to the service
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            //get service
            musicSrv = binder.service
            //pass list
            musicSrv?.setList(songList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
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

    fun songPicked(view: View) {
        musicSrv?.setSong(Integer.parseInt(view.tag.toString()));
        musicSrv?.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicSrv = null
        super.onDestroy()
    }

    private val RECORD_REQUEST_CODE = 1

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }

    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }
}
