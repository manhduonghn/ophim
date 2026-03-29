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
import com.example.ophim.model.Movie
import com.example.ophim.utils.HistoryManager

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingProgressBar: ProgressBar

    private var movieName = ""; private var movieSlug = ""; private var movieThumb = ""
    private var episodeLinks = arrayListOf<String>(); private var episodeNames = arrayListOf<String>()
    private var currentIndex = 0; private var serverIndex = 0
    private var savedPos: Long = 0L
    private var isFirstLoad = true // Flag để chỉ seek 1 lần duy nhất lúc mở

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        playerView.keepScreenOn = true

        // Nhận dữ liệu
        movieName = intent.getStringExtra("movieName") ?: ""
        movieSlug = intent.getStringExtra("movieSlug") ?: ""
        movieThumb = intent.getStringExtra("movieThumb") ?: ""
        serverIndex = intent.getIntExtra("serverIndex", 0)
        episodeLinks = intent.getStringArrayListExtra("links") ?: arrayListOf()
        episodeNames = intent.getStringArrayListExtra("names") ?: arrayListOf()
        currentIndex = intent.getIntExtra("currentIndex", 0)
        savedPos = intent.getLongExtra("savedPosition", 0L)

        initializePlayer()
        playEpisode(currentIndex, savedPos)
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                loadingProgressBar.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                
                // QUAN TRỌNG: Chỉ seek khi Player đã READY
                if (state == Player.STATE_READY && isFirstLoad) {
                    if (savedPos > 0) {
                        player?.seekTo(savedPos)
                    }
                    isFirstLoad = false // Tắt flag sau khi seek xong
                }

                if (state == Player.STATE_READY) updateEpisodeUI()
                
                // Tự động chuyển tập khi hết phim
                if (state == Player.STATE_ENDED) {
                    if (currentIndex < episodeLinks.size - 1) {
                        nextEpisode()
                    }
                }
            }
        })
    }

    private fun playEpisode(index: Int, position: Long = 0L) {
        if (episodeLinks.isEmpty() || index >= episodeLinks.size) return
        
        currentIndex = index
        val mediaItem = MediaItem.fromUri(episodeLinks[currentIndex])
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    private fun nextEpisode() {
        if (currentIndex < episodeLinks.size - 1) {
            saveProgress() // Lưu tập cũ trước khi sang tập mới
            currentIndex++
            savedPos = 0L // Reset vị trí về 0 cho tập mới
            isFirstLoad = false // Không cần seek nữa
            playEpisode(currentIndex)
        }
    }

    private fun prevEpisode() {
        if (currentIndex > 0) {
            saveProgress()
            currentIndex--
            savedPos = 0L
            isFirstLoad = false
            playEpisode(currentIndex)
        }
    }

    private fun updateEpisodeUI() {
        playerView.findViewById<TextView>(R.id.tvMovieName)?.text = movieName
        playerView.findViewById<TextView>(R.id.tvEpisodeName)?.text = "Tập ${episodeNames.getOrNull(currentIndex) ?: (currentIndex + 1)}"
        
        playerView.findViewById<ImageButton>(R.id.btnPrev)?.apply {
            isEnabled = currentIndex > 0
            alpha = if (isEnabled) 1f else 0.3f
            setOnClickListener { prevEpisode() }
        }
        playerView.findViewById<ImageButton>(R.id.btnNext)?.apply {
            isEnabled = currentIndex < episodeLinks.size - 1
            alpha = if (isEnabled) 1f else 0.3f
            setOnClickListener { nextEpisode() }
        }
    }

    private fun saveProgress() {
        player?.let {
            val currentPos = it.currentPosition
            if (currentPos > 2000) {
                val m = Movie(null, movieName, movieSlug, null, movieThumb, null, null, null, null, null)
                HistoryManager.saveHistory(this, m, serverIndex, currentIndex, currentPos)
            }
        }
    }

    override fun onPause() { super.onPause(); saveProgress(); player?.pause() }
    override fun onStop() { super.onStop(); saveProgress() }
    override fun onDestroy() { super.onDestroy(); player?.release(); player = null }
}
