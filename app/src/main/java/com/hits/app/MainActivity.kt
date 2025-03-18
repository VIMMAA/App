package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val SPLASH_TIMER = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
            val token = preferences.getString("token", null)
            val role = preferences.getString("role", null)

            finish()

            val intent = Intent(
                this,
                if (token != null && role != null) FeedActivity::class.java
                else WelcomeActivity::class.java
            )
            startActivity(intent)
        }, SPLASH_TIMER.toLong())

        supportActionBar?.hide()
    }
}
