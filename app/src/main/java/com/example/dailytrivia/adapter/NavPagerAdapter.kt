package com.example.dailytrivia.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dailytrivia.HomeFragment
import com.example.dailytrivia.LeaderboardFragment
import com.example.dailytrivia.SettingsActivity
import com.example.dailytrivia.StatsFragment

class NavPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        HomeFragment(),
        LeaderboardFragment(),
        StatsFragment(),
        SettingsActivity.SettingsFragment()
    )

    override fun createFragment(index: Int): Fragment {
        return fragments[index]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}