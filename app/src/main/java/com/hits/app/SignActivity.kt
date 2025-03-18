package com.hits.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.data.remote.Network
import com.hits.app.data.remote.api.UserApi
import com.hits.app.databinding.ActivitySignBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SignActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignBinding
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        binding.signIn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val request = Network.userApi.login(
                    UserApi.LoginParams(
                        email = emailInput.text.toString(),
                        password = passwordInput.text.toString()
                    )
                )

                request.body()?.let { response ->
                    getSharedPreferences("preferences", MODE_PRIVATE)
                        .edit()
                        .putString("token", response.token)
                        .putString("role", response.role)
                        .commit()

                    withContext(Dispatchers.Main) {
                        val intent = Intent(applicationContext, FeedActivity::class.java)
                        startActivity(intent)
                    }
                }

                request.errorBody()?.let { response ->
                    val json = JSONObject(response.string())
                    val message = json.getString(
                        if (json.has("message")) "message" else "title"
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        supportActionBar?.hide()

        emailInput = binding.email
        passwordInput = binding.password
        loginButton = binding.signIn

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            @SuppressLint("ResourceAsColor")
            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                after: Int
            ) {
                val isEmailNotEmpty = emailInput.text.toString().trim().isNotEmpty()
                val isPasswordNotEmpty = passwordInput.text.toString().trim().isNotEmpty()

                if (isEmailNotEmpty && isPasswordNotEmpty) {
                    loginButton.isEnabled = true
                    loginButton.setTextColor(resources.getColor(R.color.white, null))
                    loginButton.setBackgroundResource(R.drawable.blue_button)
                } else {
                    loginButton.isEnabled = false
                    loginButton.setTextColor(resources.getColor(R.color.grey_faded, null))
                    loginButton.setBackgroundResource(R.drawable.grey_button)
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        }

        emailInput.addTextChangedListener(textWatcher)
        passwordInput.addTextChangedListener(textWatcher)
    }
}
