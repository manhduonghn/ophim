package com.example.ophim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.view.View
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

        // Nhận slug từ Intent (từ MainActivity truyền sang)
        val slug = intent.getStringExtra("slug") ?: return

        loadMovieDetails(slug)
    }

    private fun loadMovieDetails(slug: String) {
        lifecycleScope.launch {
            try {
                // Giả định API trả về đúng cấu trúc bạn đã cung cấp
                val response = RetrofitClient.api.getDetail(slug)
                currentMovie = response.data.item
                displayMovieDetail()
            } catch (e: Exception) {
                Toast.makeText(this@DetailActivity, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun displayMovieDetail() {
        val movie = currentMovie ?: return

        // 1. Hiển thị thông tin văn bản
        binding.tvName.text = movie.name
        // Dùng Html.fromHtml để xử lý các thẻ <p>, <strong> trong content từ API
        binding.tvContent.text = Html.fromHtml(movie.content ?: "", Html.FROM_HTML_MODE_COMPACT)
        binding.tvInfo.text = "${movie.year} • ${movie.quality} • ${movie.lang} • ${movie.episode_current}"

        // 2. Tải ảnh Poster
        val imgUrl = "https://img.ophim.live/uploads/movies/${movie.poster_url}"
        Glide.with(this)
            .load(imgUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(android.R.drawable.progress_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.imgPoster)

        // 3. Xử lý nút Play nhanh (Phát tập đầu tiên của server đang chọn)
        binding.btnPlay.setOnClickListener {
            val episodes = movie.episodes.getOrNull(selectedServerIndex)?.server_data
            if (!episodes.isNullOrEmpty()) {
                startPlayer(0) // Phát từ tập 0 (tập đầu)
            } else {
                Toast.makeText(this, "Chưa có link phim cho server này", Toast.LENGTH_SHORT).show()
            }
        }

        renderServers(movie)
        updateEpisodes()
    }

    // Vẽ danh sách các Server (Vietsub, Lồng tiếng...)
    private fun renderServers(movie: MovieDetail) {
        binding.layoutServers.removeAllViews()
        movie.episodes.forEachIndexed { index, server ->
            val btn = TextView(this).apply {
                text = server.server_name
                setPadding(32, 16, 32, 16)
                setTextColor(resources.getColor(android.R.color.white, null))
                // Thay đổi background tùy theo server đang chọn
                setBackgroundResource(
                    if (index == selectedServerIndex) R.drawable.bg_server_selected 
                    else R.drawable.bg_server_normal
                )
                setOnClickListener {
                    selectedServerIndex = index
                    renderServers(movie) // Vẽ lại để cập nhật màu nút
                    updateEpisodes()     // Cập nhật lại danh sách tập tương ứng
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 0, 8, 0) }
            binding.layoutServers.addView(btn, params)
        }
    }

    // Vẽ danh sách các tập phim (1, 2, 3...)
    private fun updateEpisodes() {
        val movie = currentMovie ?: return
        val serverInfo = movie.episodes.getOrNull(selectedServerIndex)
        val episodes = serverInfo?.server_data ?: emptyList()

        // Responsive: Dọc 4 cột, Ngang 8 cột
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 4
        binding.rvEpisodes.layoutManager = GridLayoutManager(this, spanCount)
        
        // Khởi tạo Adapter với callback trả về vị trí index của tập phim
        binding.rvEpisodes.adapter = EpisodeAdapter(episodes) { episode ->
            val clickedIndex = episodes.indexOf(episode)
            startPlayer(clickedIndex)
        }
        
        binding.labelEpisodes.text = if (episodes.size <= 1) "Xem phim" else "Danh sách tập"
    }

    /**
     * Hàm quan trọng: Gửi toàn bộ dữ liệu cần thiết sang PlayerActivity
     */
    private fun startPlayer(episodeIndex: Int) {
        val movie = currentMovie ?: return
        val serverData = movie.episodes.getOrNull(selectedServerIndex)?.server_data ?: return

        val intent = Intent(this, PlayerActivity::class.java)
        
        // 1. Gửi tên phim để hiển thị ở Header của Player
        intent.putExtra("movieName", movie.name)
        
        // 2. Chuyển đổi danh sách tập phim thành 2 mảng String (Links và Names)
        val links = ArrayList<String>()
        val names = ArrayList<String>()
        for (ep in serverData) {
            links.add(ep.link_m3u8)
            names.add(ep.name)
        }

        intent.putStringArrayListExtra("links", links)
        intent.putStringArrayListExtra("names", names)
        
        // 3. Gửi vị trí tập phim muốn phát
        intent.putExtra("currentIndex", episodeIndex)
        
        startActivity(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Khi người dùng xoay điện thoại ở màn hình chi tiết, cập nhật lại số cột của RecyclerView
        updateEpisodes()
    }
}
