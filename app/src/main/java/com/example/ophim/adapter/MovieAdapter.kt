package com.example.ophim.adapter

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ophim.R
import com.example.ophim.model.Movie

class MovieAdapter(
    private val list: List<Movie>,
    private val base: String,
    private val onClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.img)
        val name: TextView = v.findViewById(R.id.name)
        val lang: TextView = v.findViewById(R.id.txtLang)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): ViewHolder {
        val view = LayoutInflater.from(p.context)
            .inflate(R.layout.item_movie, p, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(h: ViewHolder, i: Int) {
        val m = list[i]
        h.name.text = m.name
        h.lang.text = m.lang ?: "HD"

        // 🔥 FIX LỖI ẢNH: Kiểm tra nếu thumb_url đã là link full (từ lịch sử) thì dùng luôn
        val fullImageUrl = if (m.thumb_url?.startsWith("http") == true) {
            m.thumb_url
        } else {
            // Cấu trúc mặc định từ API: base + /uploads/movies/ + thumb_url
            "${base.trimEnd('/')}/uploads/movies/${m.thumb_url}"
        }

        Glide.with(h.itemView.context)
            .load(fullImageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(android.R.drawable.progress_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(h.img)

        h.itemView.setOnClickListener { onClick(m) }
    }

    override fun getItemCount() = list.size
}
