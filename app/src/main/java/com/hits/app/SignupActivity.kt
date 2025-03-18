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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hits.app.data.remote.Network
import com.hits.app.data.remote.api.UserApi
import com.hits.app.databinding.ActivitySignupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate

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

        binding.back.setOnClickListener {
            finish()
        }

        binding.signIn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val dateNumbers = dateOfBirth.text.toString().split(".")

                val selectedDay = dateNumbers[0].toInt()
                val selectedMonth = dateNumbers[1].toInt() - 1
                val selectedYear = dateNumbers[2].toInt()

                val selectedDate = LocalDate.of(selectedYear, selectedMonth, selectedDay)

                val request = Network.userApi.register(
                    UserApi.RegisterParams(
                        firstName = firstName.text.toString(),
                        middleName = surname.text.toString(),
                        lastName = secondName.text.toString(),
                        birthday = selectedDate.toString(),
                        email = email.text.toString(),
                        password = password.text.toString()
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

        calendar = binding.calendar

        calendar.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this, { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay.${selectedMonth + 1}.$selectedYear"
                    binding.dateOfBirth.setText(selectedDate)
                }, year, month, day
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
                charSequence: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            @SuppressLint("ResourceAsColor")
            override fun onTextChanged(
                charSequence: CharSequence?, start: Int, before: Int, after: Int
            ) {
                val isFirstNameNotEmpty = firstName.text.toString().trim().isNotEmpty()
                val isSecondNameNotEmpty = secondName.text.toString().trim().isNotEmpty()
                val isSurnameNotEmpty = surname.text.toString().trim().isNotEmpty()
                val isDateNotEmpty = dateOfBirth.text.toString().trim().isNotEmpty()
                val isEmailNotEmpty = email.text.toString().trim().isNotEmpty()
                val isPasswordNotEmpty = password.text.toString().trim().isNotEmpty()

                if (isFirstNameNotEmpty && isSecondNameNotEmpty && isSurnameNotEmpty && isDateNotEmpty && isEmailNotEmpty && isPasswordNotEmpty) {
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

        supportActionBar?.hide()
    }
}
