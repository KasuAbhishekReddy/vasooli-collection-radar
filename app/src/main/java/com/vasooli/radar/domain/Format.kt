package com.vasooli.radar.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/** Indian-style grouping, e.g. ₹1,23,456 */
fun money(value: Double): String {
    val n = abs(value).roundToLong().toString()
    val grouped = if (n.length <= 3) n else {
        val last3 = n.takeLast(3)
        val rest = n.dropLast(3)
        rest.reversed().chunked(2).joinToString(",").reversed() + "," + last3
    }
    return (if (value < 0) "-₹" else "₹") + grouped
}

/** Compact form for charts/cards, e.g. ₹1.2L, ₹45k */
fun shortMoney(value: Double): String {
    val v = abs(value)
    val s = when {
        v >= 1_00_00_000 -> String.format(Locale.US, "%.1fCr", v / 1_00_00_000)
        v >= 1_00_000 -> String.format(Locale.US, "%.1fL", v / 1_00_000)
        v >= 1_000 -> String.format(Locale.US, "%.0fk", v / 1_000)
        else -> v.roundToLong().toString()
    }
    return (if (value < 0) "-₹" else "₹") + s
}

private val dayMonthFmt = SimpleDateFormat("d MMM", Locale.getDefault())
fun dayMonth(ts: Long): String = dayMonthFmt.format(Date(ts))
