package com.example.ophim.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ophim.databinding.ItemEpisodeBinding
import com.example.ophim.model.Episode

class EpisodeAdapter(
    private val episodes: List<Episode>,
    private val onClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    private var selectedIndex = -1

    class ViewHolder(val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = episodes[position]

        holder.binding.tvEpisodeName.text = item.name

        // ✅ dùng state selected thay vì setBackground thủ công
        holder.itemView.isSelected = (position == selectedIndex)

        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
            onClick(item)
        }
    }

    override fun getItemCount() = episodes.size
}
