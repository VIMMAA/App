package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivityFeedBinding
import com.hits.app.request.RequestAdapter
import com.hits.app.request.RequestItem


class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var list: ListView
    private var items = ArrayList<RequestItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        list = binding.listView

        val adapter = RequestAdapter(this, items)
        list.adapter = adapter

        binding.newReq.setOnClickListener() {
            val intent = Intent(this, ApplicationCreatorActivity::class.java)
            startActivity(intent)

            val newReq = RequestItem(items.size + 1)
            items.add(0, newReq)

            adapter.notifyDataSetChanged()
        }

        binding.profile.setOnClickListener {
            finish()

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        supportActionBar?.hide()
    }
}