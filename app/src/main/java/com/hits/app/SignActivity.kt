package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivitySignBinding

class SignActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener() {
            finish()
        }

        binding.signIn.setOnClickListener() {
            binding.signIn.setOnClickListener() {
                val intent = Intent(this, FeedActivity::class.java)
                startActivity(intent)
            }
        }

        supportActionBar?.hide();

    }
}