package com.hits.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import com.hits.app.application.getDay
import com.hits.app.application.getMonth
import com.hits.app.application.getTimeSlot
import com.hits.app.application.getYear
import com.hits.app.application.updateLessonsSelections
import com.hits.app.data.remote.Network
import com.hits.app.data.remote.dto.ApplicationDto
import com.hits.app.data.remote.dto.AttachedFileDto
import com.hits.app.data.remote.dto.LessonDto
import com.hits.app.data.remote.dto.NewApplicationRequestDto
import com.hits.app.databinding.ActivityApplicationEditorBinding
import com.hits.app.databinding.AttachedFileEditItemBinding
import com.hits.app.databinding.CalendarBinding
import com.hits.app.utils.CalendarDay
import com.hits.app.utils.WeekCalculator
import com.hits.app.utils.WeekLesson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ApplicationEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationEditorBinding
    private lateinit var calendarBinding: CalendarBinding

    private var schedule = mutableListOf<WeekLesson>()
    private var attachedFiles = arrayListOf<MutableMap<String, String>>()
    private var presentationMode = PresentationMode.WEEK
    private val days = mutableMapOf<Int, Button>()
    private lateinit var id: String

    private val weekCalculator = WeekCalculator()

    private val dateFormatter by lazy {
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd",
            Locale("ru")
        ).withZone(ZoneId.systemDefault())
    }
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { attachImage(it) }
        }
    private val calendar = Calendar.getInstance()
    private var currentMonth = calendar.get(Calendar.MONTH)
    private var currentYear = calendar.get(Calendar.YEAR)
    private var dayOfWeek =
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1) 7 else calendar.get(Calendar.DAY_OF_WEEK) - 1

    private var differenceMonth: Int = 0
    private var differenceYear: Int = 0

    private val selectedDaysList: MutableList<CalendarDay> = arrayListOf()
    private val selectedLessonsList: MutableList<WeekLesson> = arrayListOf()

    private enum class PresentationMode { WEEK, MONTH }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityApplicationEditorBinding.inflate(layoutInflater)
        calendarBinding = CalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra("id").toString()

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
        binding.delete.setOnClickListener {
            val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
            val token = preferences.getString("token", null)

            CoroutineScope(Dispatchers.IO).launch {
                val api = Network.applicationApi
                api.deleteApplication("Bearer $token", id)
            }
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

//        binding.calendar7.setOnClickListener {
//            switchToMonthMode()
//        }

        binding.add.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.attachedFiles.removeAllViews()

        binding.save.setOnClickListener {
            if (attachedFiles.isNotEmpty()) saveEditedApplication()
            else Toast.makeText(this, "Choose files", Toast.LENGTH_SHORT).show()
        }

        updateWeek()
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
                button.setOnClickListener() { clickOnDay(button) }
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

        binding.week.text = getCurrentMonthName(currentMonth)
    }

    private fun clickOnDay(button: Button) {
        if (button.background is ColorDrawable) {
            button.setBackgroundResource(R.drawable.blue_button)
            val day = CalendarDay(
                currentYear,
                currentMonth,
                Integer.parseInt(button.text.toString()),
                resources.getResourceName(button.id).substringAfterLast("/")
            )
            selectedDaysList.add(day)
        } else {
            button.setBackgroundResource(R.color.transparent)
            selectedDaysList.removeIf { it.year == currentYear && it.month == currentMonth && it.day.toString() == button.text.toString() }
        }
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


    // Обновить расписание на неделю
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
                attachedFiles = (response.body()?.attachedFiles?.map {
                    mutableMapOf(
                        "name" to it.name,
                        "data" to it.data,
                    )
                } ?: emptyList()) as ArrayList<MutableMap<String, String>>

                updateData(response)
            }
        }
    }

    private fun updateData(response: Response<ApplicationDto>) {

        updateLessonsSelections(
            resources = resources,
            packageName = packageName,
            selectedLessons = response.body()!!.lessons,
            highlightedLessonsInWeek = selectedLessonsList,
            highlightedDaysInMonth = selectedDaysList,
        )

        attachedFiles.forEach { it ->
            val fileView =
                AttachedFileEditItemBinding.inflate(layoutInflater, binding.attachedFiles, true)

            fileView.removeAttachedFile.setOnClickListener { it1 ->
                attachedFiles.remove(it)
                binding.attachedFiles.removeView(fileView.root)
            }
            fileView.attachedFileName.text = it["name"] as String
        }

        selectDay(dayOfWeek)
    }

    private fun saveEditedApplication() {

        val sortedDays =
            selectedDaysList.sortedWith(compareBy<CalendarDay> { it.year }.thenBy { it.month }
                .thenBy { it.day })

        val sortedLessons =
            selectedLessonsList.sortedWith(compareBy<WeekLesson> { it.year }.thenBy { it.month }
                .thenBy { it.day })


        val dateFrom = getFirstDate(sortedDays, sortedLessons)

        var dateTo = getLastDate(sortedDays, sortedLessons)
        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = dateTo
        tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        dateTo = tempCalendar.time

        CoroutineScope(Dispatchers.IO).launch {
            val api = Network.scheduleApi
            val response = api.getSchedule(
                dateFrom = dateFormatter.format(dateFrom.toInstant()) + "T00:00:00Z",
                dateTo = dateFormatter.format(dateTo.toInstant()) + "T00:00:00Z",
            )

            val preferences = getSharedPreferences("preferences", MODE_PRIVATE)
            val token = preferences.getString("token", null)

            response.body()?.let { lessons ->
                val resultList = filterLessons(lessons)
                val apiApplication = Network.applicationApi
                apiApplication.editApplication(
                    "Bearer $token", id,
                    NewApplicationRequestDto(
                        lessons = resultList.map { it.id },
                        files = attachedFiles.map { map ->
                            AttachedFileDto(
                                name = map["name"] ?: "",
                                data = map["data"] ?: ""
                            )
                        },
                    )
                )

            }
        }

        finish()
    }

    private fun filterLessons(resultList: List<LessonDto>): List<LessonDto> {
        fun getDateFromStartTime(startTime: String): LocalDate {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            return LocalDate.parse(startTime, formatter)
        }

        return resultList.filter { lesson ->
            val lessonDate = getDateFromStartTime(lesson.startTime)

            val isDayMatched = selectedDaysList.any { day ->
                day.year == lessonDate.year && day.month == lessonDate.monthValue && day.day == lessonDate.dayOfMonth
            }

            val isLessonMatched = selectedLessonsList.any { weekLesson ->
                weekLesson.year == lessonDate.year &&
                        weekLesson.month == lessonDate.monthValue &&
                        weekLesson.day == lessonDate.dayOfMonth &&
                        weekLesson.timeSlot == getTimeSlot(lesson.startTime)
            }

            isDayMatched || isLessonMatched
        }
    }

    private fun getFirstDate(days: List<CalendarDay>, lessons: List<WeekLesson>): Date {
        val tempCalendar = Calendar.getInstance()
        var date1: Date = tempCalendar.time
        var date2: Date = tempCalendar.time
        if (days.isNotEmpty()) {
            tempCalendar.set(days.first().year, days.first().month - 1, days.first().day)
            date1 = tempCalendar.time
            if (lessons.isEmpty()) return date1
        }
        if (lessons.isNotEmpty()) {
            tempCalendar.set(lessons.first().year, lessons.first().month - 1, lessons.first().day)
            date2 = tempCalendar.time
            if (days.isEmpty()) return date2
        }

        return if (date1 < date2) date1 else date2
    }

    private fun getLastDate(days: List<CalendarDay>, lessons: List<WeekLesson>): Date {
        val tempCalendar = Calendar.getInstance()
        var date1: Date = tempCalendar.time
        var date2: Date = tempCalendar.time
        if (days.isNotEmpty()) {
            tempCalendar.set(days.last().year, days.last().month - 1, days.last().day)
            date1 = tempCalendar.time
            if (lessons.isEmpty()) return date1
        }
        if (lessons.isNotEmpty()) {
            tempCalendar.set(lessons.last().year, lessons.last().month - 1, lessons.last().day)
            date2 = tempCalendar.time
            if (days.isEmpty()) return date2
        }

        return if (date1 > date2) date1 else date2
    }

    // Установить текущую неделю
    private fun updateWeek() {
        val startOfWeek = weekCalculator.getStartOfWeek()
        val endOfWeek = weekCalculator.getEndOfWeek()

        binding.week.text = "$startOfWeek - $endOfWeek"
    }

    // Выбрать день
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

            subjectView.setOnClickListener {
                selected = selectedLessonsList.find {
                    it.year == lesson.year &&
                            it.month == lesson.month &&
                            it.day == lesson.day &&
                            it.timeSlot == lesson.timeSlot
                }?.selected

                lesson.selected = selected != true

                if (lesson.selected == false) {
                    subjectView.setCardBackgroundColor(getColor(R.color.dark_grey))
                    selectedLessonsList.removeIf {
                        it.year == lesson.year &&
                                it.month == lesson.month &&
                                it.day == lesson.day &&
                                it.timeSlot == lesson.timeSlot
                    }
                } else {
                    subjectView.setCardBackgroundColor(getColor(R.color.blue))
                    selectedLessonsList.add(lesson)
                }
            }

            subjectView.addView(subjectNameView)
            subjectView.addView(subjectOrderView)

            binding.subjects.addView(subjectView)
        }
    }


    private fun attachImage(uri: Uri) {
        val imagePath = getRealPathFromURI(uri)
        val imageName = getImageNameFromURI(uri)

        imagePath?.let { path ->
            val imageBase64 = convertImageToBase64(path)

            attachedFiles.add(
                mutableMapOf(
                    "name" to (imageName ?: System.currentTimeMillis().toString()),
                    "data" to imageBase64,
                )
            )

            // Файл
            val fileView = CardView(binding.attachedFiles.context)

            fileView.layoutParams = binding.attachedFile.layoutParams
            fileView.setCardBackgroundColor(getColor(R.color.light_grey))
            fileView.radius = 8f

            // Название файла
            val fileNameView = TextView(fileView.context)

            fileNameView.layoutParams = binding.attachedFileName.layoutParams
            fileNameView.setBackgroundResource(R.color.transparent)
            fileNameView.text = imageName
            fileNameView.setTextColor(getColor(R.color.white))

            // Кнопка удаления файла
            val removeFileButton =
                ImageButton(ContextThemeWrapper(fileView.context, R.style.CustomButton))

            removeFileButton.layoutParams = binding.removeAttachedFile.layoutParams
            removeFileButton.setImageDrawable(
                AppCompatResources.getDrawable(
                    fileView.context, R.drawable.cross_icon
                )
            )
            removeFileButton.setBackgroundResource(R.drawable.grey_button)

            removeFileButton.setOnClickListener {
                attachedFiles.removeIf { it["data"] == imageBase64 }
                binding.attachedFiles.removeView(fileView)
            }

            fileView.addView(fileNameView)
            fileView.addView(removeFileButton)

            binding.attachedFiles.addView(fileView)
        }
    }

    // Конвертация изображения в Base64
    private fun convertImageToBase64(imagePath: String): String {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }

    // Получение реального пути из URI
    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(
            uri, projection, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            }
        }

        return null
    }

    // Получение названия изображения из URI
    private fun getImageNameFromURI(uri: Uri): String? {
        val cursor = contentResolver.query(
            uri, null, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)

                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }

        return null
    }
}
