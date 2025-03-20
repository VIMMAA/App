package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hits.app.application.ApplicationAdapter
import com.hits.app.application.ApplicationItem
import com.hits.app.data.remote.Network
import com.hits.app.data.remote.api.ApplicationApi
import com.hits.app.databinding.ActivityFeedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var list: ListView
    private var items = ArrayList<ApplicationItem>()
    private lateinit var api: ApplicationApi
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api = Network.applicationApi

        list = binding.listView

        adapter = ApplicationAdapter(this, api, items)
        list.adapter = adapter

        binding.newReq.setOnClickListener() {
            val intent = Intent(this, ApplicationCreatorActivity::class.java)
            startActivity(intent)

            val newReq = ApplicationItem(items.size + 1)
            items.add(0, newReq)

            adapter.notifyDataSetChanged()
        }

        binding.profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        //updateStudentApplicationList()

        supportActionBar?.hide()
    }

    private fun updateStudentApplicationList () {
        lifecycleScope.launch(Dispatchers.IO) {

            val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
            val token = preferences.getString("token", null)

            val request = Network.userApi.getProfile("Bearer $token")
            request.body()?.let { response ->
                val response1 = api.getStudentApplicationList("Bearer $token", response.profile.id)

                if (response1.isSuccessful) {
                    items.clear()

                    response1.body().let { ApplicationDto ->
                        ApplicationDto?.let { it ->
                            items.addAll(it.map {
                                ApplicationItem(it.id, items.size + 1, it.submissionDate, it.status)
                            })
                        }
                    }
                    withContext(Dispatchers.Main) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}