package com.example.ophim.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ophim.R
import com.example.ophim.adapter.HistoryAdapter
import com.example.ophim.utils.HistoryManager

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private var layoutManager: GridLayoutManager? = null
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)
        val btnBack = findViewById<View>(R.id.btnBack)
        val btnClearAll = findViewById<TextView>(R.id.btnClearHistory)

        btnBack.setOnClickListener { finish() }

        setupGrid()
        loadHistory()

        btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xóa tất cả")
                .setMessage("Bạn có muốn xóa toàn bộ lịch sử không?")
                .setPositiveButton("Xóa") { _, _ ->
                    HistoryManager.clearAll(this)
                    loadHistory()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupGrid()
    }

    private fun setupGrid() {
        // TÍNH TOÁN CỘT GIỐNG MAINACTIVITY
        val spanCount = if (resources.configuration.screenWidthDp > 600) 4 else 2
        if (layoutManager == null) {
            layoutManager = GridLayoutManager(this, spanCount)
            rvHistory.layoutManager = layoutManager
        } else {
            layoutManager?.spanCount = spanCount
        }
    }

    private fun loadHistory() {
        val historyList = HistoryManager.getHistoryList(this).toMutableList()
        
        adapter = HistoryAdapter(historyList, 
            onItemClick = { item ->
                // Vào thẳng Player với flag AUTO_PLAY
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("slug", item.movie.slug)
                intent.putExtra("AUTO_PLAY", true)
                startActivity(intent)
            },
            onDeleteClick = { item ->
                // Xóa từng phim
                HistoryManager.deleteItem(this, item.movie.slug)
                loadHistory() // Load lại danh sách
            }
        )
        rvHistory.adapter = adapter
    }
}
