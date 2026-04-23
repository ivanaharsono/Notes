package com.angels.notes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnNext: Button

    // Datanya ditaro di sini biar gampang dipanggil
    private val onboardingItems = listOf(
        OnboardingItem(
            R.drawable.img_onboard_idea,
            "Catch Every Thought",
            "Never let a single brilliant idea slip away from your mind again"
        ),
        OnboardingItem(
            R.drawable.img_onboard_organize,
            "Keep Everything Organized",
            "Easily manage and archive all your notes"
        ),
        OnboardingItem(
            R.drawable.img_onboard_write,
            "Capture Inspiration Everywhere",
            "Capture your inspiration anytime and anywhere you go instantly"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        btnNext = findViewById(R.id.btnNext)

        // pasang adapter dan kirim datanya ke adapter
        onboardingAdapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = onboardingAdapter

        // ini yang bikin titiknya nyambung otomatis tanpa kode manual
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        updateButtonText(0)

        btnNext.setOnClickListener {
            if (viewPager.currentItem + 1 < onboardingItems.size) {
                viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonText(position)
            }
        })
    }

    private fun updateButtonText(position: Int) {
        // cuma update teks tombol
        if (position == onboardingItems.size - 1) {
            btnNext.text = "Get Started"
        } else {
            btnNext.text = "Next"
        }
    }
}