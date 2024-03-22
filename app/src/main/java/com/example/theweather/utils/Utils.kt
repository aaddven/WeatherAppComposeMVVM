package com.example.theweather.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDecimals(item: Double): String {
    return " %.0f".format(item)
}

fun formatDateTime(timestamp: Int): String {
    val sdf = SimpleDateFormat("hh:mm:aa")
    val date = java.util.Date(timestamp.toLong() * 1000)

    return sdf.format(date)
}

fun formatDate(timestamp: Int): String {
    val sdf = SimpleDateFormat("EEE, MMM d")
    val date = java.util.Date(timestamp.toLong() * 1000)

    return sdf.format(date)
}

fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

fun isFirstRun(context: Context): Boolean {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("isFirstRun", true)
}

fun updateFirstRunFlag(context: Context) {
    val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("isFirstRun", false).apply()
}





