package com.example.viet.tableattendancekotlin

import java.util.*

class TimeSheet {
    val currentMonth: Calendar = Calendar.getInstance()
    var timeSheetItems = ArrayList<TimeSheetItem>()
    var sumOfTimeInCell: SumOfTimeInCell? = SumOfTimeInCell()
}