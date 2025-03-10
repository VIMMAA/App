package com.hits.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.hits.app.databinding.ActivityApplicationCreatorBinding
import com.hits.app.utils.WeekCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplicationCreatorActivity : AppCompatActivity() {
    private val formatter = SimpleDateFormat("HH:mm", Locale("ru"))

    private lateinit var weekCalculator: WeekCalculator

    private lateinit var binding: ActivityApplicationCreatorBinding

    private var schedule: ArrayList<MutableMap<String, Any>> = arrayListOf()
    private var currentDayOfWeek = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weekCalculator = WeekCalculator()

        updateSchedule()

        binding = ActivityApplicationCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        binding.left.setOnClickListener {
            weekCalculator.previousWeek()
            setWeek()
            updateSchedule()
        }
        binding.right.setOnClickListener {
            weekCalculator.nextWeek()
            setWeek()
            updateSchedule()
        }

        var previosButton: View = binding.calendar1

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
                previosButton.setBackgroundResource(R.color.transparent)
                button.setBackgroundResource(R.drawable.blue_button)

                previosButton = button
                selectDay(index + 1)
            }
        }

        setWeek()

        supportActionBar?.hide()
    }

    // Обновить расписание на неделю
    // TODO: Выполнить запрос на бэкенд для получения расписания
    private fun updateSchedule() {
        val dateFrom = weekCalculator.getStartOfDayOfWeek(0)
        val dateTo = weekCalculator.getEndOfDayOfWeek(5)

        schedule = arrayListOf()

        for (i in 0..3) {
            val dateFromExample = Date(dateFrom.time + 1000 * 60 * 60 * i * 2)
            val dateToExample = Date(dateFromExample.time + 1000 * 60 * 90)

            schedule.add(
                mutableMapOf(
                    "lessonName" to "Английский язык",
                    "dateFrom" to dateFromExample,
                    "dateTo" to dateToExample,
                    "selected" to false
                )
            )
        }
    }

    // Установить текущую неделю
    private fun setWeek() {
        val startOfWeek = weekCalculator.getStartOfWeek()
        val endOfWeek = weekCalculator.getEndOfWeek()

        binding.week.text = "$startOfWeek - $endOfWeek"

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
                    return
                }

                // Предмет
                val subjectView = CardView(binding.subjects.context)

                subjectView.layoutParams = binding.subject.layoutParams
                subjectView.setCardBackgroundColor(
                    resources.getColor(
                        if (subject["selected"] as Boolean) R.color.blue else R.color.dark_grey
                    )
                )
                subjectView.radius = 8f

                // Название предмета
                val subjectNameView = TextView(subjectView.context)

                subjectNameView.layoutParams = binding.subjectName.layoutParams
                subjectNameView.setBackgroundResource(R.color.transparent)
                subjectNameView.gravity = Gravity.CENTER_VERTICAL
                subjectNameView.text = subject["lessonName"] as String
                subjectNameView.setTextColor(resources.getColor(R.color.white))

                // Порядковый номер предмета
                val subjectOrderView = TextView(subjectView.context)

                subjectOrderView.layoutParams = binding.subjectOrder.layoutParams
                subjectOrderView.setBackgroundResource(R.color.transparent)
                subjectOrderView.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                subjectOrderView.text = "${index + 1}-ая пара"
                subjectOrderView.setTextColor(resources.getColor(R.color.teal_200))

                subjectView.setOnClickListener {
                    subject["selected"] = !(subject["selected"] as Boolean)
                    subjectView.setCardBackgroundColor(
                        resources.getColor(
                            if (subject["selected"] as Boolean) R.color.blue else R.color.dark_grey
                        )
                    )
                }

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
                        boundView.setTextColor(resources.getColor(R.color.teal_200))

                        binding.subjects.addView(boundView)
                    }
                }

                binding.subjects.addView(subjectView)
            }

            return
        }

        // Открыть или закрыть календарь
    }
}
