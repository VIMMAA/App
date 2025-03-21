package com.hits.app.application

import android.content.res.Resources
import com.hits.app.data.remote.Network
import com.hits.app.data.remote.dto.LessonDto
import com.hits.app.utils.CalendarDay
import com.hits.app.utils.WeekLesson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


data class MyDate(
    val year: Int,
    val month: Int,
    val day: Int,
)

fun getCalendarCell(date: LocalDate): Pair<Int, Int> {
    // Получаем первый день месяца
    val firstDayOfMonth = date.withDayOfMonth(1)

    // Определяем день недели для первого дня месяца (согласно календарю, неделя начинается с воскресенья)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    // Номер дня в месяце
    val dayOfMonth = date.dayOfMonth

    // Строка в календаре (номер строки)
    val row = (dayOfMonth + firstDayOfWeek - 1) / 7

    // Столбец в календаре (номер столбца)
    val column = (dayOfMonth + firstDayOfWeek - 1) % 7

    return Pair(row, column)
}

fun updateLessonsSelections(
    resources: Resources,
    packageName: String,
    selectedLessons: List<LessonDto>,
    highlightedLessonsInWeek: MutableList<WeekLesson>,
    highlightedDaysInMonth: MutableList<CalendarDay>,
) {
    val dateFormatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd",
        Locale("ru")
    ).withZone(ZoneOffset.UTC)

    val dateFrom = selectedLessons.minByOrNull { it.startTime }?.startTime ?: return
    val dateTo = selectedLessons.maxByOrNull { it.endTime }?.endTime ?: return

    CoroutineScope(Dispatchers.IO).launch {
        val api = Network.scheduleApi
        val response = api.getSchedule(
            dateFrom = dateFormatter.format(Instant.parse(dateFrom)) + "T00:00:00Z",
            dateTo = dateFormatter.format(Instant.parse(dateTo)) + "T00:00:00Z",
        )

        response.body()?.let { lessons ->
            val schedule = lessons.groupBy(
                {
                    MyDate(
                        year = getYear(it.startTime),
                        month = getMonth(it.startTime),
                        day = getDay(it.startTime),
                    )
                },
                { it.id },
            )

            withContext(Dispatchers.Main) {
                selectedLessons.groupBy(
                    {
                        MyDate(
                            year = getYear(it.startTime),
                            month = getMonth(it.startTime),
                            day = getDay(it.startTime),
                        )
                    },
                    { it.id },
                ).forEach { (date, lessons) ->
                    if (schedule[date]?.toSet() == lessons.toSet()) {
                        val day = getCalendarCell(LocalDate.of(date.year, date.month, date.day))
                        val buttonId = resources.getIdentifier(
                            "day_${7 * day.first + day.second}",
                            "id",
                            packageName
                        )
                        val buttonName = resources.getResourceName(buttonId).substringAfterLast("/")
                        highlightedDaysInMonth.add(
                            CalendarDay(
                                year = date.year,
                                month = date.month,
                                day = date.day,
                                buttonName = buttonName,
                            )
                        )
                    }
                }
            }
        }
    }

    for (i in selectedLessons.indices) {
        val year = getYear(selectedLessons[i].startTime)
        val month = getMonth(selectedLessons[i].startTime)
        val day = getDay(selectedLessons[i].startTime)
        val timeSlot = getTimeSlot(selectedLessons[i].startTime)

        highlightedLessonsInWeek.add(
            WeekLesson(
                selectedLessons[i].id,
                selectedLessons[i].name,
                year,
                month,
                day,
                timeSlot,
                true
            )
        )
    }
}

fun getDay(date: String): Int {
    return Integer.parseInt(date.substringAfterLast('-').substringBefore('T'))
}

fun getMonth(date: String): Int {
    return Integer.parseInt(date.substringBeforeLast('-').substringAfter('-'))
}

fun getYear(date: String): Int {
    return Integer.parseInt(date.substringBefore('-'))
}

fun getTimeSlot(date: String): Int {
    return when (date.substringAfter("T")) {
        "08:45:00Z" -> 1
        "10:35:00Z" -> 2
        "12:25:00Z" -> 3
        "14:45:00Z" -> 4
        "16:35:00Z" -> 5
        "18:25:00Z" -> 6
        else -> 7
    }
}