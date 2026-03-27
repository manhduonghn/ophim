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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ophim.R
import com.example.ophim.api.RetrofitClient
import com.example.ophim.adapter.EpisodeAdapter
import com.example.ophim.databinding.ActivityDetailBinding
import com.example.ophim.model.MovieDetail
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

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getDetail(slug)
                currentMovie = response.data.item
                displayMovieDetail()
            } catch (e: Exception) {
                Toast.makeText(this@DetailActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMovieDetail() {
        val movie = currentMovie ?: return

        // 1. Gán text
        binding.tvName.text = movie.name
        binding.tvContent.text = Html.fromHtml(movie.content ?: "", Html.FROM_HTML_MODE_COMPACT)
        binding.tvInfo.text = "${movie.year} • ${movie.quality} • ${movie.lang}"

        // 2. Load Poster (Sử dụng URL từ API OPhim)
        val imgUrl = "https://img.ophim.live/uploads/movies/${movie.poster_url}"
        Glide.with(this)
            .load(imgUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(android.R.drawable.progress_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.imgPoster)

        // 3. Nút Play nhanh (Tập đầu tiên của server đang chọn)
        binding.btnPlay.setOnClickListener {
            val link = movie.episodes.getOrNull(selectedServerIndex)?.server_data?.firstOrNull()?.link_m3u8
            if (link != null) startPlayer(link)
        }

        renderServers(movie)
        updateEpisodes()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Khi xoay màn hình, chỉ cần vẽ lại danh sách tập phim cho khớp diện tích
        updateEpisodes()
    }

    private fun renderServers(movie: MovieDetail) {
        binding.layoutServers.removeAllViews()
        movie.episodes.forEachIndexed { index, server ->
            val btn = TextView(this).apply {
                text = server.server_name
                setPadding(32, 16, 32, 16)
                setTextColor(resources.getColor(android.R.color.white))
                setBackgroundResource(
                    if (index == selectedServerIndex) R.drawable.bg_server_selected 
                    else R.drawable.bg_server_normal
                )
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
        val episodes = movie.episodes.getOrNull(selectedServerIndex)?.server_data ?: emptyList()

        // Responsive: Dọc 4 cột, Ngang 8 cột
        val span = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 4
        binding.rvEpisodes.layoutManager = GridLayoutManager(this, span)
        binding.rvEpisodes.adapter = EpisodeAdapter(episodes) { ep ->
            startPlayer(ep.link_m3u8)
        }
        
        binding.labelEpisodes.text = if (episodes.size <= 1) "Xem phim" else "Danh sách tập"
    }

    private fun startPlayer(url: String) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }
}
