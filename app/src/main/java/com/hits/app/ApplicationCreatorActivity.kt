package com.hits.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.hits.app.utils.WeekCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
        }
    }

    private fun switchToWeekMode() {
        if (presentationMode == PresentationMode.MONTH) {
            presentationMode = PresentationMode.WEEK
            binding.calendarContainer.removeView(calendarBinding.calendarContainer)
            binding.calendarContainer.addView(binding.subjects, 0)
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
            subjectOrderView.setTextColor(getColor(R.color.teal_200))

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
                    boundView.setTextColor(getColor(R.color.teal_200))

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
        binding.save.setTextColor(getColor(R.color.teal_700))
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
