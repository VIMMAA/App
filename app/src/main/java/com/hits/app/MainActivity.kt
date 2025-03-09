package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var SPLASH_TIMER = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler().postDelayed(Runnable {
            val intent = Intent(
                this@MainActivity,
                WelcomeActivity::class.java
            )
            startActivity(intent)
            finish()
        }, SPLASH_TIMER.toLong())


        supportActionBar?.hide()
    }
}
