package com.example.ophim.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ophim.R

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingProgressBar: ProgressBar

    private var movieName: String = ""
    private var episodeLinks: ArrayList<String> = arrayListOf()
    private var episodeNames: ArrayList<String> = arrayListOf()
    private var currentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        playerView.keepScreenOn = true

        movieName = intent.getStringExtra("movieName") ?: ""
        episodeLinks = intent.getStringArrayListExtra("links") ?: arrayListOf()
        episodeNames = intent.getStringArrayListExtra("names") ?: arrayListOf()
        currentIndex = intent.getIntExtra("currentIndex", 0)

        initializePlayer()
        setupVisibilityLogic()
        playEpisode()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
        
        playerView.player = player

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                loadingProgressBar.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                
                // Cập nhật giao diện khi bắt đầu phát
                if (state == Player.STATE_READY) {
                    updateEpisodeUI()
                }
                
                // Tự động chuyển tập khi kết thúc
                if (state == Player.STATE_ENDED && currentIndex < episodeLinks.size - 1) {
                    currentIndex++
                    playEpisode()
                }
            }
        })
    }

    private fun setupVisibilityLogic() {
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            // Lấy Layout gốc của toàn bộ UI điều khiển
            val controllerRoot = playerView.findViewById<View>(R.id.controller_root) ?: return@ControllerVisibilityListener
            
            if (visibility == View.VISIBLE) {
                // Hiện toàn bộ cùng lúc
                controllerRoot.visibility = View.VISIBLE
                controllerRoot.animate().alpha(1f).setDuration(200).start()
            } else {
                // Ẩn toàn bộ cùng lúc với hiệu ứng mờ dần
                controllerRoot.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        controllerRoot.visibility = View.GONE
                    }
                    .start()
            }
        })
    }

    private fun playEpisode() {
        if (episodeLinks.isEmpty()) return
        val mediaItem = MediaItem.fromUri(episodeLinks[currentIndex])
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    private fun updateEpisodeUI() {
        // Tìm các view bên trong PlayerView (Layout custom)
        val tvTitle = playerView.findViewById<TextView>(R.id.tvMovieName)
        val tvEp = playerView.findViewById<TextView>(R.id.tvEpisodeName)
        val btnPrev = playerView.findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = playerView.findViewById<ImageButton>(R.id.btnNext)

        tvTitle?.text = movieName
        tvEp?.text = "Tập ${episodeNames.getOrNull(currentIndex) ?: (currentIndex + 1)}"

        // Xử lý nút Previous
        btnPrev?.apply {
            isEnabled = currentIndex > 0
            alpha = if (isEnabled) 1.0f else 0.3f
            setOnClickListener { 
                currentIndex--
                playEpisode() 
            }
        }

        // Xử lý nút Next
        btnNext?.apply {
            isEnabled = currentIndex < episodeLinks.size - 1
            alpha = if (isEnabled) 1.0f else 0.3f
            setOnClickListener { 
                currentIndex++
                playEpisode() 
            }
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
