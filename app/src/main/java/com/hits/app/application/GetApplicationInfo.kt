package com.hits.app.application

import com.hits.app.data.remote.dto.LessonDto
import com.hits.app.utils.WeekLesson

fun getApplicationInfo(lessons: List<LessonDto>?): MutableList<WeekLesson> {
    val selectedLessonsList: MutableList<WeekLesson> = arrayListOf()

    if (lessons != null) {
        for(i in lessons.indices) {
            val year = getYear(lessons[i].startTime)
            val month = getMonth(lessons[i].startTime)
            val day = getDay(lessons[i].startTime)
            val timeSlot = getTimeSlot(lessons[i].startTime)

            selectedLessonsList.add(
                WeekLesson(lessons[i].id, lessons[i].name, year, month, day, timeSlot, true)
            )
        }
    }

    return selectedLessonsList
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