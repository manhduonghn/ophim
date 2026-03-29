package com.example.ophim.adapter

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ophim.R
import com.example.ophim.utils.HistoryManager

class HistoryAdapter(
    private val list: List<HistoryManager.HistoryItem>,
    private val onItemClick: (HistoryManager.HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryManager.HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.img)
        val name: TextView = v.findViewById(R.id.name)
        val txtEpisode: TextView = v.findViewById(R.id.txtLang)
        val btnDelete: View = v.findViewById(R.id.btnDelete) // Nút xóa
        val progress: ProgressBar = v.findViewById(R.id.itemProgress) // Thanh tiến trình
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): ViewHolder {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_history, p, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(h: ViewHolder, i: Int) {
        val item = list[i]
        h.name.text = item.movie.name
        h.txtEpisode.text = "Tập ${item.episodeIndex + 1}"

        val thumb = if (item.movie.thumb_url?.startsWith("http") == true) item.movie.thumb_url 
                    else "https://img.ophim.live/uploads/movies/${item.movie.thumb_url}"

        Glide.with(h.itemView.context).load(thumb).into(h.img)

        // Tính % tiến trình (Ví dụ: xem được bao nhiêu %)
        if (item.duration > 0) {
            h.progress.visibility = View.VISIBLE
            h.progress.progress = ((item.position * 100) / item.duration).toInt()
        } else {
            h.progress.visibility = View.GONE
        }

        h.btnDelete.setOnClickListener { onDeleteClick(item) }
        h.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}
