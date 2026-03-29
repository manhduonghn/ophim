package com.example.ophim.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ophim.R
import com.example.ophim.model.Movie
import com.example.ophim.utils.HistoryManager
import com.example.ophim.utils.M3u8Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingProgressBar: ProgressBar

    private var movieName = ""; private var movieSlug = ""; private var movieThumb = ""
    private var episodeLinks = arrayListOf<String>(); private var episodeNames = arrayListOf<String>()
    private var currentIndex = 0; private var serverIndex = 0
    private var savedPos: Long = 0L
    private var isFirstLoad = true 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        playerView.keepScreenOn = true

        movieName = intent.getStringExtra("movieName") ?: ""
        movieSlug = intent.getStringExtra("movieSlug") ?: ""
        movieThumb = intent.getStringExtra("movieThumb") ?: ""
        serverIndex = intent.getIntExtra("serverIndex", 0)
        episodeLinks = intent.getStringArrayListExtra("links") ?: arrayListOf()
        episodeNames = intent.getStringArrayListExtra("names") ?: arrayListOf()
        currentIndex = intent.getIntExtra("currentIndex", 0)
        savedPos = intent.getLongExtra("savedPosition", 0L)

        initializePlayer()
        playEpisode(currentIndex)
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                loadingProgressBar.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                
                if (state == Player.STATE_READY && isFirstLoad) {
                    if (savedPos > 0) player?.seekTo(savedPos)
                    isFirstLoad = false
                }

                if (state == Player.STATE_READY) updateEpisodeUI()
                
                if (state == Player.STATE_ENDED && currentIndex < episodeLinks.size - 1) {
                    nextEpisode()
                }
            }
        })
    }

    private fun playEpisode(index: Int) {
        if (episodeLinks.isEmpty() || index >= episodeLinks.size) return
        currentIndex = index
        val originalUrl = episodeLinks[currentIndex]

        loadingProgressBar.visibility = View.VISIBLE
        
        // Xử lý Playlist M3U8 trong luồng phụ (IO)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Tải nội dung thô
                val rawContent = URL(originalUrl).readText()
                
                // 2. Lọc quảng cáo + Chuẩn hóa domain
                val filteredContent = M3u8Filter.processM3u8(rawContent, originalUrl)
                
                // 3. Tạo Data URI (Base64)
                val dataUriString = M3u8Filter.createDataUri(filteredContent)

                withContext(Dispatchers.Main) {
                    // 4. ÉP KIỂU MIME TYPE để ExoPlayer không bị lỗi nhận diện
                    val mediaItem = MediaItem.Builder()
                        .setUri(dataUriString)
                        .setMimeType(MimeTypes.APPLICATION_M3U8) 
                        .build()

                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.play()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlayerActivity, "Lỗi: Link phim không khả dụng", Toast.LENGTH_SHORT).show()
                    loadingProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun nextEpisode() {
        if (currentIndex < episodeLinks.size - 1) {
            saveProgress()
            currentIndex++
            savedPos = 0L
            isFirstLoad = false
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
            val duration = it.duration
            if (currentPos > 2000 && duration > 0) {
                val m = Movie(null, movieName, movieSlug, null, movieThumb, null, null, null, null, null)
                HistoryManager.saveHistory(this, m, serverIndex, currentIndex, currentPos, duration)
            }
        }
    }

    override fun onPause() { super.onPause(); saveProgress(); player?.pause() }
    override fun onStop() { super.onStop(); saveProgress() }
    override fun onDestroy() { super.onDestroy(); player?.release(); player = null }
}
