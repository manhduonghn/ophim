package com.example.ophim.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ophim.R

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        playerView.keepScreenOn = true

        val url = intent.getStringExtra("url") ?: return
        initializePlayer(url)
    }

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(this).build().apply {
            playerView.player = this
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            // Hiển thị ProgressBar khi đang load dữ liệu
                            loadingProgressBar.visibility = View.VISIBLE
                        }
                        Player.STATE_READY -> {
                            // Ẩn ProgressBar khi video đã sẵn sàng phát
                            loadingProgressBar.visibility = View.GONE
                        }
                        Player.STATE_ENDED -> {
                            loadingProgressBar.visibility = View.GONE
                            Toast.makeText(this@PlayerActivity, "Đã xem hết phim", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    loadingProgressBar.visibility = View.GONE
                    Toast.makeText(this@PlayerActivity, "Lỗi: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })

            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
