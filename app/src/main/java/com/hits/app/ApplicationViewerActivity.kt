package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import com.hits.app.databinding.ActivityApplicationViewerBinding
import com.hits.app.utils.WeekCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplicationViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationViewerBinding

    private lateinit var status: String
    private lateinit var applicationDate: Date

    private var schedule: ArrayList<MutableMap<String, Any>> = arrayListOf()
    private var attachedFiles: ArrayList<MutableMap<String, Any?>> = arrayListOf()
    private var additionalComments = ""
    private var currentDayOfWeek = 1

    private val weekCalculator = WeekCalculator()
    private val formatter = SimpleDateFormat("HH:mm", Locale("ru"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateApplication()
        updateSchedule()

        binding = ActivityApplicationViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        var previousButton: View = binding.calendar1

        arrayOf(
            binding.calendar1,
            binding.calendar2,
            binding.calendar3,
            binding.calendar4,
            binding.calendar5,
            binding.calendar6,
            binding.calendar7
        ).forEachIndexed { index, button ->
            button.setOnClickListener {
                if (previousButton is ImageButton) {
                    previousButton.setBackgroundResource(R.drawable.grey_button)
                } else {
                    (previousButton as Button).apply {
                        setBackgroundResource(R.color.transparent)
                        setTextColor(getColor(R.color.teal_200))
                    }
                }

                button.setBackgroundResource(R.drawable.blue_button)
                if (button is Button) button.setTextColor(getColor(R.color.white))

                previousButton = button
                selectDay(index + 1)
            }
        }

        binding.attachedFiles.removeAllViews()

        binding.additionalCommentsText.doAfterTextChanged {
            additionalComments = it.toString()
        }

        // Если роль студента позволяет это, то он может отредактировать заявку
        val editModeIsAvailable = true

        if (editModeIsAvailable) {
            binding.edit.setOnClickListener {
                val intent = Intent(this, ApplicationEditorActivity::class.java)
                intent.putExtra("id", intent.extras?.getString("id"))
                startActivity(intent)
            }
        } else {
            binding.buttons.removeView(binding.edit)
        }

        updateData()

        supportActionBar?.hide()
    }

    // Заполнить информацию о заявке
    // TODO: Выполнить запрос на бэкенд для получения заявки
    private fun updateApplication() {
        val id = intent.extras?.getString("id")
        if (id.isNullOrEmpty()) return finish()

        applicationDate = Date(2024, 6, 0)
        weekCalculator.setDate(applicationDate)

        val dateFromExample = weekCalculator.getStartOfDayOfWeek(0)
        val dateToExample = Date(dateFromExample.time + 1000 * 60 * 90)

        schedule = arrayListOf(
            mutableMapOf(
                "id" to "any",
                "lessonName" to "Английский язык",
                "dateFrom" to dateFromExample,
                "dateTo" to dateToExample,
                "selected" to true
            )
        )

        attachedFiles = arrayListOf(
            mutableMapOf(
                "fileId" to "any",
                "name" to "turnir"
            )
        )

        status = "NotDefined"
        additionalComments = "Это было не просто смело.\nЭто было..."
    }

    // Обновить расписание на неделю
    // TODO: Выполнить запрос на бэкенд для получения расписания
    private fun updateSchedule() {
        val dateFrom = weekCalculator.getStartOfDayOfWeek(0)
        val dateTo = weekCalculator.getEndOfDayOfWeek(5)

        val newSchedule: ArrayList<MutableMap<String, Any>> = arrayListOf()

        for (i in 0..3) {
            val dateFromExample = Date(dateFrom.time + 1000 * 60 * 60 * i * 2)
            val dateToExample = Date(dateFromExample.time + 1000 * 60 * 90)

            newSchedule.add(
                mutableMapOf(
                    "id" to if (i == 0) "any" else "anyTwo",
                    "lessonName" to "Английский язык",
                    "dateFrom" to dateFromExample,
                    "dateTo" to dateToExample,
                    "selected" to false
                )
            )
        }

        schedule = newSchedule.map {
            schedule.find { lesson -> it["id"] === lesson["id"] } ?: it
        } as ArrayList<MutableMap<String, Any>>
    }

    // Установить основные данные
    private fun updateData() {
        val extraFormatter = SimpleDateFormat("dd.MM.yy", Locale("ru"))

        binding.submissionDate.text = extraFormatter.format(applicationDate)

        binding.prefix.setCardBackgroundColor(
            getColor(
                when (status) {
                    "NotDefined" -> R.color.prefix_waiting
                    "Approved" -> R.color.prefix_approved
                    "Declined" -> R.color.prefix_rejected
                    else -> 0
                }
            )
        )

        attachedFiles.forEach {
            // Файл
            val fileView = CardView(binding.attachedFiles.context)

            fileView.layoutParams = binding.attachedFile.layoutParams
            fileView.setCardBackgroundColor(getColor(R.color.light_grey))
            fileView.radius = 8f

            // Название файла
            val fileNameView = TextView(fileView.context)

            fileNameView.layoutParams = binding.attachedFileName.layoutParams
            fileNameView.setBackgroundResource(R.color.transparent)
            fileNameView.text = it["name"] as String
            fileNameView.setTextColor(getColor(R.color.white))

            fileView.addView(fileNameView)

            binding.attachedFiles.addView(fileView)
        }

        binding.additionalCommentsText.text = additionalComments

        selectDay(currentDayOfWeek)
    }

    // Выбрать день
    private fun selectDay(number: Int) {
        binding.subjects.removeAllViews()

        if (number < 7) {
            currentDayOfWeek = number

            val startOfDayOfWeek = weekCalculator.getStartOfDayOfWeek(currentDayOfWeek - 1)
            val endOfDayOfWeek = weekCalculator.getEndOfDayOfWeek(currentDayOfWeek - 1)

            // Установка экранов расписания
            schedule.forEachIndexed { index, subject ->
                val dateFrom = subject["dateFrom"] as Date

                if (dateFrom < startOfDayOfWeek || endOfDayOfWeek <= dateFrom) {
                    return@forEachIndexed
                }

                // Предмет
                val subjectView = CardView(binding.subjects.context)

                subjectView.layoutParams = binding.subject.layoutParams
                subjectView.setCardBackgroundColor(
                    getColor(
                        if (subject["selected"] as Boolean) R.color.dark_blue else R.color.dark_grey
                    )
                )
                subjectView.radius = 8f

                // Название предмета
                val subjectNameView = TextView(subjectView.context)

                subjectNameView.layoutParams = binding.subjectName.layoutParams
                subjectNameView.setBackgroundResource(R.color.transparent)
                subjectNameView.text = subject["lessonName"] as String
                subjectNameView.setTextColor(getColor(R.color.white))

                // Порядковый номер предмета
                val subjectOrderView = TextView(subjectView.context)

                subjectOrderView.layoutParams = binding.subjectOrder.layoutParams
                subjectOrderView.setBackgroundResource(R.color.transparent)
                subjectOrderView.text = "${index + 1}-ая пара"
                subjectOrderView.setTextColor(getColor(R.color.teal_200))

                subjectView.addView(subjectNameView)
                subjectView.addView(subjectOrderView)

                if (index > 0) {
                    val time1 = (subject["dateFrom"] as Date).time
                    val time2 = (schedule[index - 1]["dateTo"] as Date).time

                    // Если разница во времени больше чем 5 минут, то был перерыв
                    if (time1 - time2 > 1000 * 60 * 5) {
                        val boundView = TextView(binding.subjects.context)

                        val t1 = formatter.format(time2)
                        val t2 = formatter.format(time1)

                        boundView.layoutParams = binding.bound.layoutParams
                        boundView.setBackgroundResource(R.color.transparent)
                        boundView.gravity = Gravity.CENTER
                        boundView.text = "$t1 - $t2 • перерыв"
                        boundView.setTextColor(getColor(R.color.teal_200))

                        binding.subjects.addView(boundView)
                    }
                }

                binding.subjects.addView(subjectView)
            }

            return
        }

        // Открыть или закрыть календарь
        // TODO: Доделать Маше
    }
}
