package com.example.videoplayerapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.videoplayerapp.databinding.ItemRowBinding
import java.io.File
import java.text.DecimalFormat

class VideoAdapter(private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private lateinit var videos: ArrayList<String>

    fun setVideoData(videoList : ArrayList<String>){
        videos = videoList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)
    }

    override fun getItemCount(): Int = videos.size

    inner class VideoViewHolder(private val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {



        fun bind(video: String) {
            // Bind data to the views in the item layout
            val file = File(video)
            val formattedDate = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", java.util.Date(file.lastModified()))
            val fileSizeKB = file.length() / 1024.toDouble()
            val fileSizeMB = fileSizeKB / 1024.toDouble()
            binding.textViewTitle.text = file.name
            if(fileSizeKB>1024) {
                binding.textViewSize.text = "Size : " + DecimalFormat("#.##").format(fileSizeMB).toString() + " MB"
            }else{
                binding.textViewSize.text = "Size : " + DecimalFormat("#.##").format(fileSizeKB).toString() + " KB"
            }
            binding.textViewDate.text = "Date : $formattedDate"

            // Load thumbnail using an image loading library like Picasso or Glide
            Glide.with(itemView.context).load(video).into(binding.thumbnailImageView)

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
