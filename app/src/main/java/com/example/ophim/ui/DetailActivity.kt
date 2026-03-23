package com.example.ophim.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.ophim.api.RetrofitClient
import com.example.ophim.databinding.ActivityDetailBinding
import com.example.ophim.model.Episode
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slug = intent.getStringExtra("slug") ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getDetail(slug)
                val movie = response.data.item

                // Hiển thị thông tin phim
                binding.tvName.text = movie.name
                binding.tvContent.text = Html.fromHtml(movie.content, Html.FROM_HTML_MODE_COMPACT)
                
                val imgUrl = "https://img.ophim.live/uploads/movies/${movie.poster_url}"
                Glide.with(this@DetailActivity).load(imgUrl).into(binding.imgPoster)

                // Hiển thị danh sách tập
                if (movie.episodes.isNotEmpty()) {
                    val listEpisode = movie.episodes[0].server_data
                    
                    binding.rvEpisodes.layoutManager = GridLayoutManager(this@DetailActivity, 4)
                    binding.rvEpisodes.adapter = EpisodeAdapter(listEpisode) { episode ->
                        val intent = Intent(this@DetailActivity, PlayerActivity::class.java)
                        intent.putExtra("url", episode.link_m3u8)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
