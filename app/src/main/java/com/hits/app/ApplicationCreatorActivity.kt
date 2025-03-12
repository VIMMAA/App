package com.hits.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import com.hits.app.databinding.ActivityApplicationCreatorBinding
import com.hits.app.utils.WeekCalculator
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ApplicationCreatorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationCreatorBinding

    private var schedule: ArrayList<MutableMap<String, Any>> = arrayListOf()
    private var attachedFiles: ArrayList<MutableMap<String, Any?>> = arrayListOf()
    private var additionalComments = ""
    private var currentDayOfWeek = 1

    private val weekCalculator = WeekCalculator()
    private val formatter = SimpleDateFormat("HH:mm", Locale("ru"))
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uri: Uri ->
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
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateSchedule()

        binding = ActivityApplicationCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        binding.left.setOnClickListener {
            weekCalculator.previousWeek()
            updateWeek()
            updateSchedule()
            selectDay(currentDayOfWeek)
        }
        binding.right.setOnClickListener {
            weekCalculator.nextWeek()
            updateWeek()
            updateSchedule()
            selectDay(currentDayOfWeek)
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

        binding.add.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.attachedFiles.removeAllViews()

        binding.additionalCommentsText.doAfterTextChanged {
            additionalComments = it.toString()
        }

        binding.save.setOnClickListener {
            createApplication()
        }

        updateWeek()

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
                    "id" to UUID.randomUUID().toString(),
                    "lessonName" to "Английский язык",
                    "dateFrom" to dateFromExample,
                    "dateTo" to dateToExample,
                    "selected" to false
                )
            )
        }
    }

    // Создать заявку
    // TODO: Выполнить запрос на бэкенд для создания
    private fun createApplication() {
        val lessons =
            schedule.filter { it["selected"] as Boolean }.map { mutableMapOf("id" to it["id"]) }

        attachedFiles
        additionalComments

        finish()

        val intent = Intent(this, ApplicationViewerActivity::class.java)
        intent.putExtra("id", "any")
        startActivity(intent)
    }

    // Установить текущую неделю
    private fun updateWeek() {
        val startOfWeek = weekCalculator.getStartOfWeek()
        val endOfWeek = weekCalculator.getEndOfWeek()

        binding.week.text = "$startOfWeek - $endOfWeek"

        selectDay(currentDayOfWeek)
        updateSaveButton()
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
                        if (subject["selected"] as Boolean) R.color.blue else R.color.dark_grey
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

                subjectView.setOnClickListener {
                    val selected = subject["selected"] as Boolean

                    subject["selected"] = !selected
                    updateSaveButton()

                    subjectView.setCardBackgroundColor(
                        getColor(if (selected) R.color.dark_grey else R.color.blue)
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

    // Обновить кнопку для сохранения
    private fun updateSaveButton() {
        val countOfSelectedLessons = schedule.count { it["selected"] as Boolean }

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
