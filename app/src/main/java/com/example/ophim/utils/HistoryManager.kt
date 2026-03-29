package com.example.ophim.utils

import android.content.Context
import com.example.ophim.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryManager {
    // Đảm bảo class này nằm trong object HistoryManager
    data class HistoryItem(
        val movie: Movie,
        val serverIndex: Int,
        val episodeIndex: Int,
        val position: Long,
        val duration: Long = 0L // Thêm duration để tính %
    )

    private const val PREF_NAME = "OPhimHistory"
    private const val KEY_HISTORY = "history_data"

    fun getHistoryList(context: Context): List<HistoryItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveHistory(context: Context, movie: Movie, server: Int, episode: Int, pos: Long, dur: Long = 0L) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentList = getHistoryList(context).toMutableList()
        currentList.removeAll { it.movie.slug == movie.slug }
        currentList.add(0, HistoryItem(movie, server, episode, pos, dur))
        if (currentList.size > 50) currentList.removeAt(currentList.size - 1)
        prefs.edit().putString(KEY_HISTORY, Gson().toJson(currentList)).apply()
    }

    // HÀM XÓA TỪNG PHIM
    fun deleteItem(context: Context, slug: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val newList = getHistoryList(context).filter { it.movie.slug != slug }
        prefs.edit().putString(KEY_HISTORY, Gson().toJson(newList)).apply()
    }

    // HÀM XÓA TẤT CẢ
    fun clearAll(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun getSavedData(context: Context, slug: String): HistoryItem? {
        return getHistoryList(context).find { it.movie.slug == slug }
    }
}
