package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivitySignBinding
import com.hits.app.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener() {
            finish()
        }

        binding.signIn.setOnClickListener() {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        supportActionBar?.hide();

    }
}