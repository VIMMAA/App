package com.hits.app.application

import com.hits.app.data.remote.dto.LessonDto
import com.hits.app.utils.CalendarDay
import com.hits.app.utils.WeekLesson

//data class ApplicationGetResponseDto (
//    val id: String,
//    val studentId: String,
//    val submissionDate: String,
//    val status: String,
//    val lessons: List<LessonDto>,
//    val attachedFiles: List<AttachedFileWithIdDto>,
//    val comment: String
//)
//data class LessonDto(
//    val id: String,
//    val name: String,
//    val startTime: String,
//    val endTime: String
//)
//class WeekLesson (
//    val id: String,
//    val name: String,
//    val year: Int,
//    val month: Int,
//    val day: Int,
//    val timeSlot: Int,
//    var selected: Boolean
//)
//class CalendarDay (
//    val year: Int,
//    val month: Int,
//    val day: Int,
//    val buttonName: String
//)

public fun getApplicationInfo (lessons: List<LessonDto>?) : MutableList<WeekLesson> {
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