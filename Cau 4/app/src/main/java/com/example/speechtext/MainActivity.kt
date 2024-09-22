package com.example.speechtext

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.speechtext.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager(binding.viewPager)
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Speech to Text"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Text to Speech"))

        //TabLayout with ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Speech to Text"
                1 -> "Text to Speech"
                else -> null
            }
        }.attach()

    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
    }
}