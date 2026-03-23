package com.example.ophim.ui

import android.content.Intent
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
                val movie = response.data.item
                currentMovie = movie

                // ===== INFO =====
                binding.tvName.text = movie.name

                binding.tvContent.text =
                    Html.fromHtml(movie.content, Html.FROM_HTML_MODE_COMPACT)

                binding.tvInfo.text =
                    "${movie.year ?: ""} • ${movie.quality ?: ""} • ${movie.lang ?: ""}"

                // ===== IMAGE =====
                val imgUrl =
                    "https://img.ophim.live/uploads/movies/${movie.poster_url ?: ""}"

                Glide.with(this@DetailActivity)
                    .load(imgUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(android.R.drawable.progress_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.imgPoster)

                // ===== LABEL =====
                if (movie.episodes.firstOrNull()?.server_data?.size == 1) {
                    binding.labelEpisodes.text = "Xem phim"
                } else {
                    binding.labelEpisodes.text = "Danh sách tập"
                }

                // ===== PLAY BUTTON =====
                binding.btnPlay.setOnClickListener {
                    val first = movie.episodes
                        .getOrNull(selectedServerIndex)
                        ?.server_data
                        ?.firstOrNull()

                    if (first != null) {
                        val intent = Intent(
                            this@DetailActivity,
                            PlayerActivity::class.java
                        )
                        intent.putExtra("url", first.link_m3u8)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@DetailActivity,
                            "Không có tập để phát",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                renderServers(movie)
                updateEpisodes()

            } catch (e: Exception) {
                Toast.makeText(
                    this@DetailActivity,
                    e.message ?: "Lỗi không xác định",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun renderServers(movie: MovieDetail) {
        binding.layoutServers.removeAllViews()

        movie.episodes.forEachIndexed { index, server ->
            val btn = TextView(this@DetailActivity).apply {
                text = server.server_name
                setPadding(40, 16, 40, 16)
                setTextColor(resources.getColor(android.R.color.white))

                setBackgroundResource(
                    if (index == selectedServerIndex)
                        R.drawable.bg_server_selected
                    else
                        R.drawable.bg_server_normal
                )

                setOnClickListener {
                    selectedServerIndex = index
                    renderServers(movie)
                    updateEpisodes()
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(12, 0, 12, 0)

            binding.layoutServers.addView(btn, params)
        }
    }

    private fun updateEpisodes() {
        val movie = currentMovie ?: return

        val listEpisode =
            movie.episodes.getOrNull(selectedServerIndex)?.server_data ?: emptyList()

        binding.rvEpisodes.layoutManager =
            GridLayoutManager(this@DetailActivity, 4)

        binding.rvEpisodes.adapter =
            EpisodeAdapter(listEpisode) { episode ->
                val intent = Intent(
                    this@DetailActivity,
                    PlayerActivity::class.java
                )
                intent.putExtra("url", episode.link_m3u8)
                startActivity(intent)
            }
    }
}
