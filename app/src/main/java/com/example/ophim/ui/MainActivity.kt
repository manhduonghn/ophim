package com.example.ophim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ophim.R
import com.example.ophim.api.RetrofitClient
import com.example.ophim.adapter.MovieAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var layoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recyclerView)
        val btnLogo = findViewById<View>(R.id.btnLogoHome)
        val btnSearch = findViewById<View>(R.id.btnSearch)

        // Logic chia cột của bạn
        setupGrid()

        btnLogo.setOnClickListener { recycler.smoothScrollToPosition(0) }
        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Gọi API
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getHome()
                recycler.adapter = MovieAdapter(res.data.items, res.data.APP_DOMAIN_CDN_IMAGE) { movie ->
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("slug", movie.slug)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupGrid() // Cập nhật lại số cột ngay lập tức khi xoay
    }

    private fun setupGrid() {
        val spanCount = if (resources.configuration.screenWidthDp > 600) 4 else 2
        if (recycler.layoutManager == null) {
            layoutManager = GridLayoutManager(this, spanCount)
            recycler.layoutManager = layoutManager
        } else {
            layoutManager.spanCount = spanCount
        }
    }
}
