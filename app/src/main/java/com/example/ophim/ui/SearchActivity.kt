package com.example.ophim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ophim.adapter.MovieAdapter
import com.example.ophim.api.RetrofitClient
import com.example.ophim.databinding.ActivitySearchBinding
import com.example.ophim.model.Movie
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private var layoutManager: GridLayoutManager? = null
    private val movieList = mutableListOf<Movie>()
    private var movieAdapter: MovieAdapter? = null
    
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false
    private var currentKeyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupResponsiveGrid()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnDoSearch.setOnClickListener { performNewSearch() }
        binding.edtSearch.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_SEARCH) { performNewSearch(); true } else false
        }

        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!isLoading && !isLastPage) {
                    val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: 0
                    if (lastVisible >= movieList.size - 4 && movieList.isNotEmpty()) {
                        currentPage++
                        executeSearch()
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
            binding.rvSearch.layoutManager = layoutManager
        } else {
            layoutManager?.spanCount = span
        }
    }

    private fun performNewSearch() {
        currentKeyword = binding.edtSearch.text.toString().trim()
        if (currentKeyword.isEmpty()) return
        currentPage = 1; isLastPage = false; movieList.clear(); movieAdapter = null
        executeSearch()
    }

    private fun executeSearch() {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.search(currentKeyword, currentPage)
                val newItems = res.data.items
                val pagin = res.data.params.pagination

                if (currentPage >= (pagin.totalItems / pagin.totalItemsPerPage + 1)) isLastPage = true
                
                binding.tvSearchResult.apply {
                    visibility = View.VISIBLE
                    text = "Kết quả cho '$currentKeyword' - Trang $currentPage"
                }

                if (newItems.isNotEmpty()) {
                    movieList.addAll(newItems)
                    if (movieAdapter == null) {
                        movieAdapter = MovieAdapter(movieList, res.data.APP_DOMAIN_CDN_IMAGE) { movie ->
                            val intent = Intent(this@SearchActivity, DetailActivity::class.java)
                            intent.putExtra("slug", movie.slug)
                            startActivity(intent)
                        }
                        binding.rvSearch.adapter = movieAdapter
                    } else {
                        movieAdapter?.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
