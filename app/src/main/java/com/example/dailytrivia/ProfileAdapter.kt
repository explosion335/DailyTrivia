package com.example.dailytrivia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytrivia.databinding.ItemProfileBinding

class ProfileAdapter(
    private val profiles: List<ProfileSelectionFragment.Profile>,
    private val onProfileClick: (String) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size

    inner class ProfileViewHolder(private val binding: ItemProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: ProfileSelectionFragment.Profile) {
            binding.profileName.text = profile.name

            // Handle profile click
            binding.root.setOnClickListener {
                onProfileClick(profile.id)
            }
        }
    }
}
