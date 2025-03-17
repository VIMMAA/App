package com.hits.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import com.hits.app.data.remote.Network
import com.hits.app.databinding.ActivityApplicationCreatorBinding
import com.hits.app.databinding.CalendarBinding
import com.hits.app.utils.CalendarDay
import com.hits.app.utils.WeekCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ApplicationCreatorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationCreatorBinding
    private lateinit var calendarBinding: CalendarBinding

    private var schedule = mutableListOf<Lesson>()
    private var attachedFiles: ArrayList<MutableMap<String, Any?>> = arrayListOf()
    private var currentDay = DayOfWeek.MONDAY
    private var selectedDay = DayOfWeek.MONDAY
    private var presentationMode = PresentationMode.WEEK
    private val days = mutableMapOf<DayOfWeek, Button>()

    private val weekCalculator = WeekCalculator()
    private val formatter by lazy {
        DateTimeFormatter.ofPattern(
            "hh:mm",
            Locale("ru")
        ).withZone(ZoneOffset.UTC)
    }
    private val dateFormatter by lazy {
        DateTimeFormatter.ofPattern(
            "yyyy-MM-dd",
            Locale("ru")
        ).withZone(ZoneId.systemDefault())
    }
    private val pickImageLauncher = registerForActivityResult(GetContent()) { uri ->
        uri?.let { attachImage(it) }
    }
    private val calendar = Calendar.getInstance()
    private var currentMonth = calendar.get(Calendar.MONTH)
    private var currentYear = calendar.get(Calendar.YEAR)
    private var differenceMonth: Int = 0
    private var differenceYear: Int = 0

    // TODO: только при нажатии на кнопку сохранения все дни конвертируются в пары в список ниже
    private val ChosenDaysList: MutableList<CalendarDay> = arrayListOf()
    private val ChosenLessonsList: List<Date> = arrayListOf()

    private enum class PresentationMode { WEEK, MONTH }
    private enum class DayOfWeek { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY }
    private class Lesson(
        val id: String,
        val name: String,
        val startTime: String,
        val endTime: String,
        var selected: Boolean,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateSchedule()

        binding = ActivityApplicationCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        calendarBinding = CalendarBinding.inflate(layoutInflater)

        binding.back.setOnClickListener {
            finish()
        }

        binding.left.setOnClickListener {
            weekCalculator.previousWeek()
            updateWeek()
            updateSchedule()
            selectDay(currentDay)
        }
        binding.right.setOnClickListener {
            weekCalculator.nextWeek()
            updateWeek()
            updateSchedule()
            selectDay(currentDay)
        }

        days.clear()
        days.putAll(
            listOf(
                DayOfWeek.MONDAY to binding.calendar1,
                DayOfWeek.TUESDAY to binding.calendar2,
                DayOfWeek.WEDNESDAY to binding.calendar3,
                DayOfWeek.THURSDAY to binding.calendar4,
                DayOfWeek.FRIDAY to binding.calendar5,
                DayOfWeek.SATURDAY to binding.calendar6,
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
                            setTextColor(getColor(R.color.white))
                        }
                    }
                }

                button.setBackgroundResource(R.drawable.blue_button)
                button.setTextColor(getColor(R.color.white))

                selectDay(day)
            }
        }

        binding.calendar7.setOnClickListener {
            switchToMonthMode()
        }

        binding.add.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.attachedFiles.removeAllViews()

        binding.save.setOnClickListener {
            createApplication()
        }

        updateWeek()

        supportActionBar?.hide()
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
            ChosenDaysList.add(day)
        } else {
            button.setBackgroundResource(R.color.transparent)
            ChosenDaysList.removeIf { it.year == currentYear && it.month == currentMonth && it.day.toString() == button.text.toString() }
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

        ChosenDaysList.forEach {
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

            // Делать обратно текст и листенеры на неделю
        }
    }


    // Обновить расписание на неделю
    // TODO: Выполнить запрос на бэкенд для получения расписания
    private fun updateSchedule() {
        val dateFrom = weekCalculator.getStartDateOfWeek(0)
        val dateTo = weekCalculator.getEndDateOfWeek(5)

        CoroutineScope(Dispatchers.IO).launch {
            val api = Network.scheduleApi
            val response = api.getSchedule(
                dateFrom = dateFormatter.format(dateFrom.toInstant()) + "T00:00:00Z",
                dateTo = dateFormatter.format(dateTo.toInstant()) + "T00:00:00Z",
            )

            schedule = arrayListOf()
            response.body()?.let { lessons ->
                schedule.addAll(lessons.map { lesson ->
                    Lesson(
                        id = lesson.id,
                        name = lesson.name,
                        startTime = lesson.startTime,
                        endTime = lesson.endTime,
                        selected = false,
                    )
                })
            }
        }
    }

    // Создать заявку
    // TODO: Выполнить запрос на бэкенд для создания
    private fun createApplication() {
        finish()
    }

    // Установить текущую неделю
    private fun updateWeek() {
        val startOfWeek = weekCalculator.getStartOfWeek()
        val endOfWeek = weekCalculator.getEndOfWeek()

        binding.week.text = "$startOfWeek - $endOfWeek"

        selectDay(currentDay)
        updateSaveButton()
    }

    // Выбрать день
    private fun selectDay(day: DayOfWeek) {
        if (selectedDay == day) return

        binding.subjects.removeAllViews()
        selectedDay = day

        val startDateOfWeek = weekCalculator.getStartDateOfWeek(selectedDay.ordinal)
        val endDateOfWeek = weekCalculator.getEndDateOfWeek(selectedDay.ordinal)

        var order = 1
        var prev: Lesson? = null

        // Установка экранов расписания
        schedule.filter {
            val dateFrom = Date.from(Instant.parse(it.startTime))
            dateFrom >= startDateOfWeek && dateFrom <= endDateOfWeek
        }.forEach { lesson ->
            // Предмет
            val subjectView = CardView(binding.subjects.context)

            subjectView.layoutParams = binding.subject.layoutParams
            subjectView.setCardBackgroundColor(
                getColor(if (lesson.selected) R.color.blue else R.color.dark_grey)
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
            subjectOrderView.text = "${order}-ая пара"
            subjectOrderView.setTextColor(getColor(R.color.grey_faded))

            subjectView.setOnClickListener {
                val selected = lesson.selected

                lesson.selected = !selected
                updateSaveButton()

                subjectView.setCardBackgroundColor(
                    getColor(if (selected) R.color.dark_grey else R.color.blue)
                )
            }

            subjectView.addView(subjectNameView)
            subjectView.addView(subjectOrderView)

            if (order > 1) {
                val time1 = Instant.parse(lesson.startTime)
                val time2 = Instant.parse(prev!!.endTime)

                // Если разница во времени больше чем 5 минут, то был перерыв
                if (time1.toEpochMilli() - time2.toEpochMilli() > 1000 * 60 * 5) {
                    val boundView = TextView(binding.subjects.context)

                    val t1 = formatter.format(time2)
                    val t2 = formatter.format(time1)

                    boundView.layoutParams = binding.bound.layoutParams
                    boundView.setBackgroundResource(R.color.transparent)
                    boundView.gravity = Gravity.CENTER
                    boundView.text = "$t1 - $t2 • перерыв"
                    boundView.setTextColor(getColor(R.color.grey_faded))

                    binding.subjects.addView(boundView)
                }
            }

            order++
            prev = lesson
            binding.subjects.addView(subjectView)
        }
    }

    // Обновить кнопку для сохранения
    private fun updateSaveButton() {
        val countOfSelectedLessons = schedule.count { it.selected }

        if (countOfSelectedLessons > 0) {
            if (binding.save.isEnabled) return

            binding.save.setBackgroundResource(R.drawable.blue_button)
            binding.save.isEnabled = true
            binding.save.setTextColor(getColor(R.color.white))

            return
        }

        if (!binding.save.isEnabled) return

        binding.save.setBackgroundResource(R.drawable.grey_button)
        binding.save.isEnabled = false
        binding.save.setTextColor(getColor(R.color.grey_faded))
    }

    private fun attachImage(uri: Uri) {
        val imagePath = getRealPathFromURI(uri)
        val imageName = getImageNameFromURI(uri)

        imagePath?.let { path ->
            val imageBase64 = convertImageToBase64(path)

            attachedFiles.add(
                mutableMapOf(
                    "name" to imageName, "data" to imageBase64
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
