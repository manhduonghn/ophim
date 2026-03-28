package com.example.ophim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ophim.adapter.MovieAdapter
import com.example.ophim.api.RetrofitClient
import com.example.ophim.databinding.ActivityFilterBinding
import com.example.ophim.model.Movie
import kotlinx.coroutines.launch

class FilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterBinding
    private var layoutManager: GridLayoutManager? = null
    private var movieAdapter: MovieAdapter? = null
    private val movieList = mutableListOf<Movie>()
    
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false
    private var apiPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiPath = intent.getStringExtra("API_PATH") ?: ""
        binding.tvTitle.text = intent.getStringExtra("TITLE") ?: "Danh mục"
        binding.btnBack.setOnClickListener { finish() }

        setupResponsiveGrid()
        loadData()

        binding.rvFilter.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!isLoading && !isLastPage) {
                    val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: 0
                    if (lastVisible >= movieList.size - 4 && movieList.isNotEmpty()) {
                        currentPage++
                        loadData()
                    }
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupResponsiveGrid()
    }

    private fun setupResponsiveGrid() {
        val span = if (resources.configuration.screenWidthDp > 600) 4 else 2
        if (layoutManager == null) {
            layoutManager = GridLayoutManager(this, span)
            binding.rvFilter.layoutManager = layoutManager
        } else {
            layoutManager?.spanCount = span
        }
    }

    private fun loadData() {
        if (isLoading || apiPath.isEmpty()) return
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getMoviesByPath(apiPath, currentPage)
                val newItems = res.data.items
                val pagin = res.data.params.pagination

                if (currentPage >= (pagin.totalItems / pagin.totalItemsPerPage + 1)) isLastPage = true

                if (newItems.isNotEmpty()) {
                    movieList.addAll(newItems)
                    if (movieAdapter == null) {
                        movieAdapter = MovieAdapter(movieList, res.data.APP_DOMAIN_CDN_IMAGE) { movie ->
                            val intent = Intent(this@FilterActivity, DetailActivity::class.java)
                            intent.putExtra("slug", movie.slug)
                            startActivity(intent)
                        }
                        binding.rvFilter.adapter = movieAdapter
                    } else {
                        movieAdapter?.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@FilterActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
