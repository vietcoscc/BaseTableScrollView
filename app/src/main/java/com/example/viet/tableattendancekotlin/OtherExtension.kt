package com.example.viet.tableattendancekotlin

import android.content.Context
import java.util.*
import kotlin.collections.HashMap

fun Int.getDayTitle(context: Context,mDayOfWeekInMonth: HashMap<Int,Int>, template: String = "%d %s"): String {
    val week:String = when (mDayOfWeekInMonth[this]) {
        Calendar.SUNDAY -> {
            context.getString(R.string.COMMON_SUN)
        }
        Calendar.MONDAY -> {
            context.getString(R.string.COMMON_MON)
        }
        Calendar.TUESDAY -> {
            context.getString(R.string.COMMON_TUE)
        }
        Calendar.WEDNESDAY -> {
            context.getString(R.string.COMMON_WED)
        }
        Calendar.THURSDAY -> {
            context.getString(R.string.COMMON_THU)
        }
        Calendar.FRIDAY -> {
            context.getString(R.string.COMMON_FRI)
        }
        Calendar.SATURDAY -> {
            context.getString(R.string.COMMON_SAT)
        }
        else -> {
            ""
        }

    }
    return String.format(template,this, week)
}

/**
 * Checks if two times are on the same day.
 *
 * @param calendar The second day.
 * @return Whether the times are on the same day.
 */
fun Calendar.isTheSameDay(calendar: Calendar): Boolean {
    return this.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            && this.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
}