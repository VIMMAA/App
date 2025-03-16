package com.hits.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeekCalculator {
    private val calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("d MMMM", Locale("ru"))

    // Форматирование даты в строку
    private fun formatDate(date: Date): String {
        return formatter.format(date)
    }

    // Возвращает начало текущей недели
    fun getStartOfWeek(): String {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        return formatDate(calendar.time)
    }

    // Возвращает конец текущей недели
    fun getEndOfWeek(): String {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.add(Calendar.DAY_OF_WEEK, 6)

        val date = formatDate(calendar.time)

        calendar.add(Calendar.DAY_OF_WEEK, -6)

        return date
    }

    // Возвращает начало указанного дня недели
    fun getStartDateOfWeek(index: Int): Date {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.add(Calendar.DAY_OF_WEEK, index)

        val time = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, -index)

        return time
    }

    // Возвращает окончание указанного дня недели
    fun getEndDateOfWeek(index: Int): Date {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.add(Calendar.DAY_OF_WEEK, index + 1)

        val time = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, -(index + 1))

        return time
    }

    // Переход к следующей неделе
    fun nextWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
    }

    // Переход к предыдущей неделе
    fun previousWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
    }

    // Установить конкретную дату
    fun setDate(date: Date) {
        calendar.timeInMillis = date.time

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    init {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
}
