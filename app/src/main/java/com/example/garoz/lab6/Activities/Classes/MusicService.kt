package com.example.garoz.lab6.Activities.Classes

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.provider.MediaStore
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;
import com.example.garoz.lab6.Activities.Classes.MusicService.MusicBinder
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import com.example.garoz.lab6.Activities.MainActivity
import com.example.garoz.lab6.R
import kotlinx.android.synthetic.main.song.*


class MusicService:Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    //media player
    private lateinit var player: MediaPlayer
    //song list
    private lateinit var songs: ArrayList<Song>
    //current position
    private var songPosn: Int = 0

    private val musicBind = MusicBinder()

    private var songTitle = ""
    private var NOTIFY_ID=1;

    override fun onCreate() {
        super.onCreate()
        songPosn = 0
        player = MediaPlayer()
        initMusicPlayer()
    }

    fun initMusicPlayer(){
        player = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }

        player!!.setOnPreparedListener(this)
        player!!.setOnCompletionListener(this)
        player!!.setOnErrorListener(this)
    }

    fun setList(theSongs: ArrayList<Song>) {
        songs = theSongs
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onBind(arg0: Intent): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.release()
        return false
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp?.reset();
        return false;
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if(player.currentPosition > 0){
            mp?.reset();
            playNext();
        }
    }

    fun playSong() {
        player.reset()
        //get song
        val playSong = songs[songPosn]
        songTitle=playSong.title
        val currSong = playSong.id
        val trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong)
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync();
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()

        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = Notification.Builder(this)

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.abc_btn_colored_material)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle)

        val not:Notification = builder.build()

        startForeground(1, not)
    }

    fun setSong(songIndex: Int) {
        songPosn = songIndex
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playPrev(){
        songPosn--;
        if(songPosn < 0) {
            songPosn=songs.size -1
        }
        playSong();
    }

    fun playNext(){
        songPosn++;
        if(songPosn >= songs.size) {
            songPosn=0
        }
        playSong()
    }

    fun getAudioSession():Int{
        return player!!.audioSessionId

    }

    override fun onDestroy() {
        stopForeground(true)
    }
}