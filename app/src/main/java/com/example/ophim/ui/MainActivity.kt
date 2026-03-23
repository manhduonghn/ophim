package com.example.ophim.ui

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ Auto theo system dark/light
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.recyclerView)

        // ✅ Responsive: tablet sẽ nhiều cột hơn
        val spanCount = if (resources.configuration.screenWidthDp > 600) 4 else 2
        recycler.layoutManager = GridLayoutManager(this, spanCount)

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getHome()
                val base = res.data.APP_DOMAIN_CDN_IMAGE

                recycler.adapter = MovieAdapter(res.data.items, base) { movie ->
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("slug", movie.slug)
                    startActivity(intent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MainActivity,
                    "Lỗi tải dữ liệu!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
