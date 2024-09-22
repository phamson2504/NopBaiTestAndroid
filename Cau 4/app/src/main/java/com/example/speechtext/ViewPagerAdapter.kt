package com.example.speechtext

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SpeechToTextFragment()
            1 -> TextToSpeechFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}