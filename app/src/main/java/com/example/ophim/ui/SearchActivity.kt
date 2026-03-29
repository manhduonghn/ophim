import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private var layoutManager: GridLayoutManager? = null
    private val movieList = mutableListOf<Movie>()
    private var movieAdapter: MovieAdapter? = null
    
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false
    private var currentKeyword = ""
    private var searchJob: Job? = null // Dùng để quản lý Debounce

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.btnBack.setOnClickListener { finish() }

        // Nút tìm kiếm thủ công
        binding.btnDoSearch.setOnClickListener {
            hideKeyboard()
            performNewSearch()
        }

        // Xử lý nút Enter trên bàn phím
        binding.edtSearch.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                performNewSearch()
                true
            } else false
        }

        // TÍNH NĂNG GỢI Ý KHI GÕ (Debounce 500ms)
        binding.edtSearch.addTextChangedListener { text ->
            searchJob?.cancel() // Hủy bỏ đợt đợi trước đó
            searchJob = lifecycleScope.launch {
                delay(500) // Đợi 500ms sau khi người dùng ngừng gõ
                val query = text.toString().trim()
                if (query.isNotEmpty() && query != currentKeyword) {
                    performNewSearch()
                }
            }
        }

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

    private fun setupRecyclerView() {
        val span = if (resources.configuration.screenWidthDp > 600) 4 else 2
        layoutManager = GridLayoutManager(this, span)
        binding.rvSearch.layoutManager = layoutManager
    }

    private fun performNewSearch() {
        currentKeyword = binding.edtSearch.text.toString().trim()
        if (currentKeyword.isEmpty()) {
            movieList.clear()
            movieAdapter?.notifyDataSetChanged()
            binding.tvSearchResult.visibility = View.GONE
            return
        }
        currentPage = 1
        isLastPage = false
        movieList.clear()
        movieAdapter = null // Tạo mới adapter để reset domain ảnh nếu cần
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
                
                // Cập nhật trạng thái trang cuối
                val pagin = res.data.params.pagination
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
                    binding.tvSearchResult.text = "Không tìm thấy phim nào..."
                }
            } catch (e: Exception) {
                if (currentPage == 1) Toast.makeText(this@SearchActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
        binding.edtSearch.clearFocus() // Bỏ focus để không hiện con trỏ nhấp nháy
    }
}
