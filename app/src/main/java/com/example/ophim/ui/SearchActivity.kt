package com.example.ophim.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ophim.adapter.MovieAdapter
import com.example.ophim.api.RetrofitClient
import com.example.ophim.databinding.ActivitySearchBinding
import com.example.ophim.model.Movie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Khởi tạo Grid linh hoạt ngay từ đầu
        setupResponsiveGrid()

        binding.btnBack.setOnClickListener { finish() }

        // 2. Xử lý nút tìm kiếm thủ công
        binding.btnDoSearch.setOnClickListener {
            hideKeyboard()
            performNewSearch()
        }

        // 3. Xử lý Enter/Search trên bàn phím
        binding.edtSearch.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                performNewSearch()
                true
            } else false
        }

        // 4. Logic Gợi ý (Debounce) khi đang gõ
        binding.edtSearch.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(600) // Đợi 0.6s sau khi ngừng gõ mới gọi API
                val query = text.toString().trim()
                if (query.isNotEmpty() && query != currentKeyword) {
                    performNewSearch()
                }
            }
        }

        // 5. Cuộn để tải thêm trang
        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: 0
                if (!isLoading && !isLastPage && lastVisible >= movieList.size - 4 && movieList.isNotEmpty()) {
                    currentPage++
                    executeSearch()
                }
            }
        })
    }

    // Tự động cập nhật số cột khi xoay màn hình mà không cần load lại Activity
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupResponsiveGrid()
    }

    private fun setupResponsiveGrid() {
        // Nếu chiều rộng > 600dp (Tablet/Ngang) thì 4 cột, ngược lại 2 cột
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
        if (currentKeyword.isEmpty()) {
            movieList.clear()
            movieAdapter?.notifyDataSetChanged()
            binding.tvSearchResult.visibility = View.GONE
            return
        }
        // Reset mọi thứ về trạng thái trang 1
        currentPage = 1
        isLastPage = false
        movieList.clear()
        movieAdapter = null 
        executeSearch()
    }

    private fun executeSearch() {
        if (isLoading) return
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.search(currentKeyword, currentPage)
                val newItems = res.data.items
                val pagin = res.data.params.pagination
                
                // Kiểm tra nếu đã hết phim để dừng phân trang
                if (newItems.size < pagin.totalItemsPerPage) isLastPage = true

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
                } else if (currentPage == 1) {
                    binding.tvSearchResult.text = "Không tìm thấy phim phù hợp"
                }
            } catch (e: Exception) {
                if (currentPage == 1) {
                    Toast.makeText(this@SearchActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        binding.edtSearch.clearFocus()
    }
}
