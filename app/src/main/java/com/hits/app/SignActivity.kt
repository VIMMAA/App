package com.hits.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivitySignBinding


class SignActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignBinding
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
