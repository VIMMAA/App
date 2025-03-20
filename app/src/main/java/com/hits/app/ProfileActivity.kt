package com.hits.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.data.remote.Network
import com.hits.app.databinding.ActivityProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)

        CoroutineScope(Dispatchers.IO).launch {
            val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
            val token = preferences.getString("token", null)

            val request = Network.userApi.getProfile("Bearer $token")

            request.body()?.let { response ->
                val firstName = response.profile.firstName
                val middleName = response.profile.middleName
                val lastName = response.profile.lastName

                withContext(Dispatchers.Main) {
                    setContentView(binding.root)

                    binding.fio.text = "$lastName $firstName $middleName"
                }
            }

            request.errorBody()?.let {
                withContext(Dispatchers.Main) {
                    finish()

                    val intent = Intent(applicationContext, WelcomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        binding.back.setOnClickListener {
            finish()

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        binding.logout.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
                val token = preferences.getString("token", null)

                preferences.edit().remove("token").remove("role").commit()

                Network.userApi.logout("Bearer $token")

                withContext(Dispatchers.Main) {
                    finish()

                    val intent = Intent(applicationContext, WelcomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
