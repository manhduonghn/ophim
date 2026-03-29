package com.example.ophim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ophim.R
import com.example.ophim.api.RetrofitClient
import com.example.ophim.adapter.MovieAdapter
import com.example.ophim.utils.Constants
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private var layoutManager: GridLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recyclerView)
        val btnLogo = findViewById<View>(R.id.btnLogoHome)
        val btnSearch = findViewById<View>(R.id.btnSearch)
        
        // Ánh xạ Menu từ layout_header
        val menuHistory = findViewById<TextView>(R.id.menuHistory)
        val menuType = findViewById<TextView>(R.id.menuType)
        val menuCategory = findViewById<TextView>(R.id.menuCategory)
        val menuCountry = findViewById<TextView>(R.id.menuCountry)

        setupGrid()

        // Sự kiện Click
        btnLogo.setOnClickListener { recycler.smoothScrollToPosition(0) }
        btnSearch.setOnClickListener { startActivity(Intent(this, SearchActivity::class.java)) }
        
        menuHistory.setOnClickListener { startActivity(Intent(this, HistoryActivity::class.java)) }
        menuType.setOnClickListener { showTypeMenu(it) }
        menuCategory.setOnClickListener { showFilterMenu(it, "CATEGORY") }
        menuCountry.setOnClickListener { showFilterMenu(it, "COUNTRY") }

        loadHomeData()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupGrid()
    }

    private fun setupGrid() {
        val spanCount = if (resources.configuration.screenWidthDp > 600) 4 else 2
        if (layoutManager == null) {
            layoutManager = GridLayoutManager(this, spanCount)
            recycler.layoutManager = layoutManager
        } else {
            layoutManager?.spanCount = spanCount
        }
    }

    private fun loadHomeData() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getHome()
                recycler.adapter = MovieAdapter(res.data.items, res.data.APP_DOMAIN_CDN_IMAGE) { movie ->
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("slug", movie.slug)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTypeMenu(view: View) {
        val popup = PopupMenu(this, view)
        Constants.MOVIE_TYPES.keys.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { item ->
            navigateToFilter(item.title.toString(), Constants.MOVIE_TYPES[item.title] ?: "")
            true
        }
        popup.show()
    }

    private fun showFilterMenu(view: View, type: String) {
        lifecycleScope.launch {
            try {
                val response = if (type == "CATEGORY") RetrofitClient.api.getCategories() else RetrofitClient.api.getCountries()
                val popup = PopupMenu(this@MainActivity, view)
                response.data.items.forEach { popup.menu.add(it.name) }
                popup.setOnMenuItemClickListener { menuItem ->
                    val selected = response.data.items.find { it.name == menuItem.title }
                    selected?.let {
                        val path = if (type == "CATEGORY") "v1/api/the-loai/${it.slug}" else "v1/api/quoc-gia/${it.slug}"
                        navigateToFilter(it.name, path)
                    }
                    true
                }
                popup.show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Không thể tải danh mục", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToFilter(title: String, path: String) {
        val intent = Intent(this, FilterActivity::class.java)
        intent.putExtra("TITLE", title)
        intent.putExtra("API_PATH", path)
        startActivity(intent)
    }
}
