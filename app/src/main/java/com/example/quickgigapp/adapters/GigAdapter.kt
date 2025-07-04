package com.example.quickgigapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quickgigapp.databinding.ItemGigBinding
import com.example.quickgigapp.models.Gig

class GigAdapter(
    private val gigs: List<Gig>,
    private val onGigClick: (Gig) -> Unit
) : RecyclerView.Adapter<GigAdapter.GigViewHolder>() {

    inner class GigViewHolder(private val binding: ItemGigBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(gig: Gig) {
            binding.tvGigTitle.text = gig.title
            binding.tvGigDescription.text = gig.description
            binding.tvGigPrice.text = gig.price

            binding.root.setOnClickListener {
                onGigClick(gig)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GigViewHolder {
        val binding = ItemGigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GigViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GigViewHolder, position: Int) {
        holder.bind(gigs[position])
    }

    override fun getItemCount(): Int = gigs.size
}
