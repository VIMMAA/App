package com.hits.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivityApplicationEditorBinding

class ApplicationEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityApplicationEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        supportActionBar?.hide()
    }
}
