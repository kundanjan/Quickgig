package com.example.quickgigapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quickgigapp.databinding.ItemFreelancerBinding
import com.example.quickgigapp.models.FreelancerProfile
import com.google.android.material.chip.Chip

class FreelancerAdapter(
    private val freelancers: List<FreelancerProfile>,
    private val onClick: (FreelancerProfile) -> Unit
) : RecyclerView.Adapter<FreelancerAdapter.FreelancerViewHolder>() {

    private val TAG = "FreelancerAdapter"

    inner class FreelancerViewHolder(val binding: ItemFreelancerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: FreelancerProfile) {
            binding.tvFreelancerName.text = profile.name
            binding.tvFreelancerEmail.text = profile.email

            // Debug logging
            Log.d(TAG, "Binding freelancer: ${profile.name}")
            Log.d(TAG, "Skills: ${profile.skills}")
            Log.d(TAG, "Skills size: ${profile.skills?.size ?: 0}")

            // Clear existing chips
            binding.chipGroupSkills.removeAllViews()

            // Add skills as chips
            if (profile.skills != null && profile.skills.isNotEmpty()) {
                Log.d(TAG, "Adding ${profile.skills.size} skills as chips")
                profile.skills.forEach { skill ->
                    if (skill.isNotBlank()) {
                        val chip = Chip(binding.root.context)
                        chip.text = skill.trim()
                        chip.isClickable = false
                        chip.isCheckable = false

                        // Use more compatible color setting
                        try {
                            chip.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                            chip.setTextColor(binding.root.context.getColor(android.R.color.white))
                        } catch (e: Exception) {
                            // Fallback for older API levels
                            chip.setChipBackgroundColorResource(android.R.color.darker_gray)
                            chip.setTextColor(binding.root.context.resources.getColor(android.R.color.white))
                        }

                        binding.chipGroupSkills.addView(chip)
                        Log.d(TAG, "Added chip for skill: $skill")
                    }
                }
            } else {
                Log.d(TAG, "No skills found for ${profile.name}")
                // Add a placeholder chip to show "No skills listed"
                val chip = Chip(binding.root.context)
                chip.text = "No skills listed"
                chip.isClickable = false
                chip.isCheckable = false
                chip.setChipBackgroundColorResource(android.R.color.darker_gray)
                binding.chipGroupSkills.addView(chip)
            }

            binding.root.setOnClickListener {
                onClick(profile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FreelancerViewHolder {
        val binding = ItemFreelancerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FreelancerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FreelancerViewHolder, position: Int) {
        holder.bind(freelancers[position])
    }

    override fun getItemCount(): Int = freelancers.size
}