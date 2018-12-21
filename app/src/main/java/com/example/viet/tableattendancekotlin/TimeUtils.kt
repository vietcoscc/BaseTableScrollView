package com.example.viet.tableattendancekotlin

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {
    companion object {
        fun parseStringToDate(source: String, format: String): Date {
            val formatter = SimpleDateFormat(format)
            formatter.isLenient = false
            try {
                return formatter.parse(source)
            } catch (ex: ParseException) {
                throw IllegalArgumentException(ex)
            }
        }

        fun toCalendar(date: Date): Calendar {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return calendar
        }
    }
}