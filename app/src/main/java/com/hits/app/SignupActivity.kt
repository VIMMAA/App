package com.hits.app

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var calendar: ImageView
    private lateinit var firstName: EditText
    private lateinit var secondName: EditText
    private lateinit var surname: EditText
    private lateinit var dateOfBirth: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var signup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener() {
            finish()
        }

        binding.signIn.setOnClickListener() {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        calendar = binding.calendar

        calendar.setOnClickListener() {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.dateOfBirth.setText(selectedDate)
                },
                year, month, day
            )

            datePickerDialog.datePicker.maxDate = calendar.timeInMillis

            datePickerDialog.show()
        }

        firstName = binding.firstName
        secondName = binding.secondName
        surname = binding.surname
        dateOfBirth = binding.dateOfBirth
        email = binding.email
        password = binding.password
        signup = binding.signIn


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
                val isFirstNameNotEmpty = firstName.text.toString().trim().isNotEmpty()
                val isSecondNameNotEmpty = secondName.text.toString().trim().isNotEmpty()
                val isSurnameNotEmpty = surname.text.toString().trim().isNotEmpty()
                val isDateNotEmpty = dateOfBirth.text.toString().trim().isNotEmpty()
                val isEmailNotEmpty = email.text.toString().trim().isNotEmpty()
                val isPasswordNotEmpty = password.text.toString().trim().isNotEmpty()

                if (isFirstNameNotEmpty && isSecondNameNotEmpty &&
                    isSurnameNotEmpty && isDateNotEmpty &&
                    isEmailNotEmpty && isPasswordNotEmpty) {
                    signup.isEnabled = true
                    signup.setTextColor(resources.getColor(R.color.white, null))
                    signup.setBackgroundResource(R.drawable.blue_button)
                } else {
                    signup.isEnabled = false
                    signup.setTextColor(resources.getColor(R.color.grey_faded, null))
                    signup.setBackgroundResource(R.drawable.grey_button)
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        }

        firstName.addTextChangedListener(textWatcher)
        secondName.addTextChangedListener(textWatcher)
        surname.addTextChangedListener(textWatcher)
        dateOfBirth.addTextChangedListener(textWatcher)
        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        supportActionBar?.hide();
    }
}