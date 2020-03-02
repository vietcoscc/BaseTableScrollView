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