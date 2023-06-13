package com.capstone.eco_route.datasource.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.capstone.eco_route.databinding.ActivityTrackerBinding
import com.capstone.eco_route.datasource.model.TrackerModel

class TrackAdapter : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    inner class TrackViewHolder(val binding: ActivityTrackerBinding)
        : RecyclerView.ViewHolder(binding.root)

    val diffCallback = object : DiffUtil.ItemCallback<TrackerModel>() {

        override fun areItemsTheSame(oldItem: TrackerModel, newItem: TrackerModel): Boolean {
            return oldItem.idTrip == newItem.idTrip
        }

        override fun areContentsTheSame(oldItem: TrackerModel, newItem: TrackerModel): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<TrackerModel>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {

        val binding = ActivityTrackerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TrackViewHolder(binding)
    }

    override fun getItemCount(): Int {

        return differ.currentList.size

    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {

        val track = differ.currentList[position]
        holder.itemView.apply {
            val mileage = "${track.mileageInMeters / 1000f} km"
//            holder.binding.tripMileageValue.text = mileage
        }

    }


}