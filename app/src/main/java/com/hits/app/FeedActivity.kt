package com.hits.app

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivityFeedBinding
import com.hits.app.databinding.ActivitySignupBinding

class FeedActivity : AppCompatActivity () {
    private lateinit var binding : ActivityFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.hide()
    }
}