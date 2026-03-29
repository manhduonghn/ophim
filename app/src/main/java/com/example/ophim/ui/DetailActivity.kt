package com.example.ophim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.ophim.R
import com.example.ophim.adapter.EpisodeAdapter
import com.example.ophim.api.RetrofitClient
import com.example.ophim.databinding.ActivityDetailBinding
import com.example.ophim.model.MovieDetail
import com.example.ophim.utils.HistoryManager
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var selectedServerIndex = 0
    private var currentMovie: MovieDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slug = intent.getStringExtra("slug") ?: return
        loadMovieDetails(slug)
    }

    private fun loadMovieDetails(slug: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getDetail(slug)
                currentMovie = response.data.item
                displayMovieDetail()
            } catch (e: Exception) {
                Toast.makeText(this@DetailActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMovieDetail() {
        val movie = currentMovie ?: return
        binding.tvName.text = movie.name
        binding.tvContent.text = Html.fromHtml(movie.content ?: "", Html.FROM_HTML_MODE_COMPACT)
        
        val fullThumbUrl = if (movie.thumb_url?.startsWith("http") == true) movie.thumb_url 
                          else "https://img.ophim.live/uploads/movies/${movie.thumb_url}"
        
        Glide.with(this).load(fullThumbUrl).into(binding.imgPoster)

        val history = HistoryManager.getSavedData(this, movie.slug)
        val isAutoPlay = intent.getBooleanExtra("AUTO_PLAY", false)

        if (history != null) {
            selectedServerIndex = history.serverIndex
            binding.btnPlay.text = "▶ Xem tiếp: T${history.episodeIndex + 1}"
            binding.btnPlay.setOnClickListener { startPlayer(history.episodeIndex, history.position) }
            
            if (isAutoPlay) {
                startPlayer(history.episodeIndex, history.position)
                intent.removeExtra("AUTO_PLAY") 
            }
        } else {
            binding.btnPlay.text = "▶ Xem ngay"
            binding.btnPlay.setOnClickListener { startPlayer(0, 0L) }
        }

        renderServers(movie)
        updateEpisodes()
    }

    private fun renderServers(movie: MovieDetail) {
        binding.layoutServers.removeAllViews()
        movie.episodes.forEachIndexed { index, server ->
            val btn = TextView(this).apply {
                text = server.server_name
                setPadding(32, 16, 32, 16)
                setTextColor(resources.getColor(android.R.color.white, null))
                setBackgroundResource(if (index == selectedServerIndex) R.drawable.bg_server_selected else R.drawable.bg_server_normal)
                setOnClickListener {
                    selectedServerIndex = index
                    renderServers(movie)
                    updateEpisodes()
                }
            }
            val params = LinearLayout.LayoutParams(-2, -2).apply { setMargins(8, 0, 8, 0) }
            binding.layoutServers.addView(btn, params)
        }
    }

    private fun updateEpisodes() {
        val movie = currentMovie ?: return
        val server = movie.episodes.getOrNull(selectedServerIndex) ?: return
        val episodes = server.server_data
        
        val span = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 4
        binding.rvEpisodes.layoutManager = GridLayoutManager(this, span)
        binding.rvEpisodes.adapter = EpisodeAdapter(episodes) { ep ->
            startPlayer(episodes.indexOf(ep), 0L)
        }
    }

    private fun startPlayer(epIdx: Int, pos: Long) {
        val movie = currentMovie ?: return
        val server = movie.episodes.getOrNull(selectedServerIndex) ?: return
        
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("movieName", movie.name)
            putExtra("movieSlug", movie.slug)
            putExtra("movieThumb", if (movie.thumb_url?.startsWith("http") == true) movie.thumb_url else "https://img.ophim.live/uploads/movies/${movie.thumb_url}")
            putExtra("serverIndex", selectedServerIndex)
            putStringArrayListExtra("links", ArrayList(server.server_data.map { it.link_m3u8 }))
            putStringArrayListExtra("names", ArrayList(server.server_data.map { it.name }))
            putExtra("currentIndex", epIdx)
            putExtra("savedPosition", pos)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }
}
