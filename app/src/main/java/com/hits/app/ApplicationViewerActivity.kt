package com.hits.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivityAppicationViewerBinding

class ApplicationViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppicationViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppicationViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        supportActionBar?.hide()
    }
}
