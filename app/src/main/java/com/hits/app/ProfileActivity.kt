package com.hits.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        binding.logout.setOnClickListener {
            finish()

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)

            // +back махинации
        }

        supportActionBar?.hide()
    }
}