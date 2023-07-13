package com.example.videoplayerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class VideoAdapter(private val videos: ArrayList<String>, private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return VideoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)
    }

    override fun getItemCount(): Int = videos.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(video: String) {
            // Bind data to the views in the item layout
            val file = File(video)
            val date = java.util.Date(file.lastModified())
            val formattedDate = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", date)
            val fileSizeKB = file.length() / 1024.toDouble()
            val fileSizeMB = fileSizeKB / 1024.toDouble()
            itemView.findViewById<TextView>(R.id.textViewTitle).text = file.name
            if(fileSizeKB>1024) {
                itemView.findViewById<TextView>(R.id.textViewSize).text =
                    "Size : " + fileSizeMB.toString().substring(0, fileSizeMB.toString().lastIndexOf('.') + 2) + " MB"
            }else{
                itemView.findViewById<TextView>(R.id.textViewSize).text =
                    "Size : " + fileSizeKB.toString().substring(0, fileSizeMB.toString().lastIndexOf('.') + 2) + " KB"
            }
            itemView.findViewById<TextView>(R.id.textViewDate).text = "Date : $formattedDate"

            // Load thumbnail using an image loading library like Picasso or Glide
            val thumbnailImageView = itemView.findViewById<ImageView>(R.id.thumbnailImageView)
            Glide.with(itemView.context).load(video).into(thumbnailImageView)

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(position)
                }
            }
        }
    }

    // Interface for click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
