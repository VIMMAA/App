package com.hits.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeekCalculator (
) {
    private val calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("d MMMM", Locale("ru"))

    private val currDay = calendar.get(Calendar.DAY_OF_WEEK)

    private var startDay: Int = 0
    private var endDay: Int = 0

    fun getStartDay () {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        startDay = calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun getMonth(): Int {
        return calendar.get(Calendar.MONTH)
    }

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
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        return formatDate(calendar.time)
    }

    // Возвращает начало указанного дня недели
    fun getStartDateOfWeek(): Date {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        return calendar.time
    }

    // Возвращает окончание указанного дня недели
    fun getEndDateOfWeek(): Date {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        return calendar.time
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

    fun getDayOfMonth(day: Int, month: Int): Int {
        calendar.set(Calendar.DAY_OF_MONTH, startDay)
        calendar.set(Calendar.MONTH, month)

        // Получаем текущий день недели для заданной начальной даты
        val currDayOfWeek = 2

        // Вычисляем разницу между текущим днем недели и целевым днем недели
        // Если целевой день недели раньше, чем текущий, корректируем на 7 дней назад
        val diff = if (day >= currDayOfWeek) {
            day - currDayOfWeek
        } else {
            day - currDayOfWeek + 7
        }

        // Добавляем разницу дней к началу недели
        calendar.add(Calendar.DAY_OF_MONTH, diff)

        val month = calendar.get(Calendar.MONTH)

        // Получаем число месяца для найденного дня недели
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun getYear(): Int {
        return calendar.get(Calendar.YEAR)
    }

    init {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
}
