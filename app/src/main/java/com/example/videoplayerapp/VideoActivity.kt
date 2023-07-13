package com.example.videoplayerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.videoplayerapp.databinding.ActivityVideoBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class VideoActivity : AppCompatActivity(), Player.Listener {

    private val binding by lazy {
        ActivityVideoBinding.inflate(layoutInflater)
    }

    private lateinit var player: ExoPlayer
    private var videoString = 0
    private var videoList: ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        videoString = intent.getIntExtra("video", 0)
        videoList = intent.getStringArrayListExtra("list")!!

        setupPlayer()
        addMP4Files()

        // restore playstate on Rotation
        if (savedInstanceState != null) {
            if (savedInstanceState.getInt("mediaItem") != 0) {
                val restoredMediaItem = savedInstanceState.getInt("mediaItem")
                val seekTime = savedInstanceState.getLong("SeekTime")
                player.seekTo(restoredMediaItem, seekTime)
                player.play()
            }
        }

    }

    private fun addMP4Files() {
        val list: MutableList<MediaItem> = mutableListOf()
        for (i in 0 until videoList.size) {
            val mediaItem = MediaItem.fromUri(videoList[i])
            list.add(mediaItem)
        }

        player.addMediaItems(list)
        player.prepare()
        player.seekTo(videoString, C.TIME_UNSET)
        player.play()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.videoView.player = player
        player.addListener(this)
    }

    // handle loading
    override fun onPlaybackStateChanged(state: Int) {
        when (state) {
            Player.STATE_BUFFERING -> {
                binding.progressBar.visibility = View.VISIBLE

            }

            Player.STATE_READY -> {
                binding.progressBar.visibility = View.INVISIBLE
            }

            Player.STATE_ENDED -> {

            }

            Player.STATE_IDLE -> {

            }
        }
    }


    // save details if Activity is destroyed
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: " + player.currentPosition)
        // current play position
        outState.putLong("SeekTime", player.currentPosition)
        // current mediaItem
        outState.putInt("mediaItem", player.currentMediaItemIndex)
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onSaveInstanceState: " + player.currentPosition)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}