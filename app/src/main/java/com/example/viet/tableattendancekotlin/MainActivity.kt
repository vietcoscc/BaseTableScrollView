package com.example.viet.tableattendancekotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    lateinit var tableAttendanceView:TableAttendanceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tableAttendanceView  = findViewById(R.id.tableAttendanceView)
    }
}
