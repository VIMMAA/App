package com.hits.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.hits.app.application.getDay
import com.hits.app.application.getMonth
import com.hits.app.application.getTimeSlot
import com.hits.app.application.getYear
import com.hits.app.application.updateLessonsSelections
import com.hits.app.data.remote.Network
import com.hits.app.data.remote.dto.ApplicationDto
import com.hits.app.databinding.ActivityApplicationViewerBinding
import com.hits.app.databinding.CalendarBinding
import com.hits.app.utils.CalendarDay
import com.hits.app.utils.WeekCalculator
import com.hits.app.utils.WeekLesson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ApplicationViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationViewerBinding
    private lateinit var calendarBinding: CalendarBinding

    private var schedule = mutableListOf<WeekLesson>()
    private var attachedFiles: ArrayList<MutableMap<String, String>> = arrayListOf()
    private var presentationMode = PresentationMode.WEEK
    private val days = mutableMapOf<Int, Button>()
    lateinit var id: String

    private val weekCalculator = WeekCalculator()

    private val dateFormatter by lazy {
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd",
            Locale("ru")
        ).withZone(ZoneId.systemDefault())
    }
    private val calendar = Calendar.getInstance()
    private var currentMonth = calendar.get(Calendar.MONTH)
    private var currentYear = calendar.get(Calendar.YEAR)
    private var dayOfWeek =
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1) 7 else calendar.get(Calendar.DAY_OF_WEEK) - 1

    private var differenceMonth: Int = 0
    private var differenceYear: Int = 0

    private val selectedDaysList = mutableListOf<CalendarDay>()
    private val selectedLessonsList = mutableListOf<WeekLesson>()

    private enum class PresentationMode { WEEK, MONTH }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra("id").toString()

        binding = ActivityApplicationViewerBinding.inflate(layoutInflater)
        calendarBinding = CalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateApplication()
        updateSchedule()

        binding.back.setOnClickListener {
            finish()
        }

        binding.left.setOnClickListener {
            weekCalculator.previousWeek()
            updateWeek()
            updateSchedule()
        }
        binding.right.setOnClickListener {
            weekCalculator.nextWeek()
            updateWeek()
            updateSchedule()
        }
        binding.edit.setOnClickListener {
            val intent = Intent(this, ApplicationEditorActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)

            finish()
        }

        days.clear()
        days.putAll(
            listOf(
                1 to binding.calendar1,
                2 to binding.calendar2,
                3 to binding.calendar3,
                4 to binding.calendar4,
                5 to binding.calendar5,
                6 to binding.calendar6,
            )
        )

        days.forEach { (day, button) ->
            button.setOnClickListener {
                binding.calendar7.setBackgroundResource(R.drawable.grey_button)
                switchToWeekMode()

                days.values.forEach {
                    if (it != button) {
                        it.apply {
                            setBackgroundResource(R.color.transparent)
                        }
                    }
                }

                button.setBackgroundResource(R.drawable.blue_button)

                dayOfWeek = day
                selectDay(dayOfWeek)
            }
        }

        val button = days[dayOfWeek]
        button?.setBackgroundResource(R.drawable.blue_button)

        binding.calendar7.setOnClickListener {
            switchToMonthMode()
        }

        updateWeek()
    }

    private fun updateApplication() {
        val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
        val token = preferences.getString("token", null)

        CoroutineScope(Dispatchers.IO).launch {
            val api = Network.applicationApi
            val response = api.getApplication(
                "Bearer $token",
                id
            )

            withContext(Dispatchers.Main) {
                updateData(response)

                response.body()?.attachedFiles
            }
        }

        // разложить по полочкам lessons и attachedFiles

        attachedFiles = arrayListOf(
            mutableMapOf(
                "name" to "turnir",
                "data" to UUID.randomUUID().toString() // Сюда положить fileId
            )
        )
    }

    private fun updateSchedule() {
        val dateFrom = weekCalculator.getStartDateOfWeek()
        val dateTo = weekCalculator.getEndDateOfWeek()

        CoroutineScope(Dispatchers.IO).launch {
            val api = Network.scheduleApi
            val response = api.getSchedule(
                dateFrom = dateFormatter.format(dateFrom.toInstant()) + "T00:00:00Z",
                dateTo = dateFormatter.format(dateTo.toInstant()) + "T00:00:00Z",
            )

            schedule = arrayListOf()
            response.body()?.let { lessons ->
                schedule.addAll(lessons.map { lesson ->
                    WeekLesson(
                        id = lesson.id,
                        name = lesson.name,
                        year = getYear(lesson.startTime),
                        month = getMonth(lesson.startTime),
                        day = getDay(lesson.startTime),
                        timeSlot = getTimeSlot(lesson.startTime),
                        selected = false,
                    )
                })
            }
            withContext(Dispatchers.Main) {
                selectDay(dayOfWeek)
            }
        }
    }

    private fun updateWeek() {
        val startOfWeek = weekCalculator.getStartOfWeek()
        val endOfWeek = weekCalculator.getEndOfWeek()

        binding.weekText.text = "$startOfWeek - $endOfWeek"
    }

    // Установить основные данные
    private fun updateData(response: Response<ApplicationDto>) {
        val extraFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .withZone(ZoneId.systemDefault())

        updateLessonsSelections(
            resources = resources,
            packageName = packageName,
            selectedLessons = response.body()!!.lessons,
            highlightedLessonsInWeek = selectedLessonsList,
            highlightedDaysInMonth = selectedDaysList,
        )

        val timeInstant = Instant.parse(response.body()?.submissionDate)
        val submissionDate = extraFormatter.format(timeInstant)
        val status = response.body()?.status

        binding.submissionDate.text = submissionDate

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
        binding.prefixText.text = when (status) {
            "NotDefined" -> "На проверке"
            "Approved" -> "Одобрена"
            "Declined" -> "Отклонена"
            else -> "На проверке"
        }

        // update attachedFiles

        /*attachedFiles.forEach {
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
        }*/

        selectDay(dayOfWeek)
    }

    private fun selectDay(day: Int) {

        binding.subjects.removeAllViews()

        weekCalculator.getStartDay()
        var month = weekCalculator.getMonth()

        val editedDay = if (day == 7) 1 else day + 1
        val dayOfMonth = weekCalculator.getDayOfMonth(editedDay, month)
        month = weekCalculator.getMonth()
        val year = weekCalculator.getYear()

        schedule.filter {
            it.year == year &&
                    it.month == month + 1 &&
                    it.day == dayOfMonth
        }.forEach { lesson ->

            var selected = selectedLessonsList.find {
                it.year == lesson.year &&
                        it.month == lesson.month &&
                        it.day == lesson.day &&
                        it.timeSlot == lesson.timeSlot
            }?.selected

            val subjectView = CardView(binding.subjects.context)

            subjectView.layoutParams = binding.subject.layoutParams
            subjectView.setCardBackgroundColor(
                getColor(if (selected == true) R.color.blue else R.color.dark_grey)
            )
            subjectView.radius = 8f

            // Название предмета
            val subjectNameView = TextView(subjectView.context)

            subjectNameView.layoutParams = binding.subjectName.layoutParams
            subjectNameView.setBackgroundResource(R.color.transparent)
            subjectNameView.text = lesson.name
            subjectNameView.setTextColor(getColor(R.color.white))

            // Порядковый номер предмета
            val subjectOrderView = TextView(subjectView.context)

            subjectOrderView.layoutParams = binding.subjectOrder.layoutParams
            subjectOrderView.setBackgroundResource(R.color.transparent)
            subjectOrderView.text = "${lesson.timeSlot}-ая пара"
            subjectOrderView.setTextColor(getColor(R.color.grey_faded))

            subjectView.addView(subjectNameView)
            subjectView.addView(subjectOrderView)

            binding.subjects.addView(subjectView)
        }
    }

    private fun switchToMonthMode() {
        if (presentationMode == PresentationMode.WEEK) {
            presentationMode = PresentationMode.MONTH

            binding.calendar7.setBackgroundResource(R.drawable.blue_button)
            days.values.forEach {
                it.apply {
                    setBackgroundResource(R.color.transparent)
                    setTextColor(getColor(R.color.white))
                }
            }

            binding.calendarContainer.removeView(binding.subjects)
            binding.calendarContainer.addView(calendarBinding.calendarContainer, 0)

            setMonthDifference()
            setMonth()
            switchMonthView()
        }
    }

    private fun setMonthDifference() {
        binding.left.setOnClickListener {
            differenceMonth--
            if (currentMonth == 0) {
                currentMonth = 12
                differenceYear--
            }
            setMonth()
            switchMonthView()
        }
        binding.right.setOnClickListener {
            differenceMonth++
            if (currentMonth == 11) {
                currentMonth = -1
                differenceYear++
            }
            setMonth()
            switchMonthView()
        }
    }

    private fun switchMonthView() {
        for (i in 1..42) {
            val button = calendarBinding.root.findViewById<Button>(
                resources.getIdentifier("day_$i", "id", packageName)
            )

            button.setBackgroundResource(R.color.transparent)
        }

        selectedDaysList.forEach {
            if (it.year == currentYear && it.month == currentMonth) {
                val button = calendarBinding.root.findViewById<Button>(
                    resources.getIdentifier(it.buttonName, "id", packageName)
                )

                button.setBackgroundResource(R.drawable.blue_button)
            }
        }
    }

    private fun setMonth() {
        // Получаем текущую дату
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        currentMonth += differenceMonth
        differenceMonth = 0
        currentYear += differenceYear
        differenceYear = 0

        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)

        val dayOfWeek = countDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))

        // Получаем числа для текущего, предыдущего и следующего месяцев
        val currentMonthDays = getDaysInMonth(currentMonth, currentYear)
        val prevMonthDays = getDaysInMonth(currentMonth - 1, currentYear)

        var day = 1
        var nextMonthDay = 1
        for (i in 1..<dayOfWeek) {
            val button = calendarBinding.root.findViewById<Button>(
                resources.getIdentifier("day_$i", "id", packageName)
            )
            button.text = (prevMonthDays.size - dayOfWeek + i + 1).toString()
            button.setTextColor(getColor(R.color.grey_border))
            button.isEnabled = false
        }
        for (i in dayOfWeek..42) {
            val button = calendarBinding.root.findViewById<Button>(
                resources.getIdentifier("day_$i", "id", packageName)
            )

            // Устанавливаем цвет текста в зависимости от месяца
            if (day in currentMonthDays) {
                button.text = day.toString()
                button.setTextColor(getColor(R.color.white))
                button.isEnabled = true
                day++
            } else {
                button.text = nextMonthDay.toString()
                button.setTextColor(getColor(R.color.grey_border))
                button.isEnabled = false
                nextMonthDay++
            }
        }

        binding.weekText.text = getCurrentMonthName(currentMonth)
    }

    private fun countDayOfWeek(day: Int): Int {
        return when (day) {
            1 -> 7
            else -> day - 1
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): List<Int> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return (1..maxDay).toList()
    }

    private fun getCurrentMonthName(currentMonth: Int): String {
        // Массив с названиями месяцев на русском языке
        val monthNames = arrayOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )

        // Возвращаем название текущего месяца
        return monthNames[currentMonth]
    }

    private fun switchToWeekMode() {
        if (presentationMode == PresentationMode.MONTH) {
            presentationMode = PresentationMode.WEEK
            binding.calendarContainer.removeView(calendarBinding.calendarContainer)
            binding.calendarContainer.addView(binding.subjects, 0)

            binding.left.setOnClickListener {
                weekCalculator.previousWeek()
                updateWeek()
                updateSchedule()
            }
            binding.right.setOnClickListener {
                weekCalculator.nextWeek()
                updateWeek()
                updateSchedule()
            }

            updateWeek()
            updateSchedule()
        }
    }
}
