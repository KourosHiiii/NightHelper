package com.nighthelper.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun Int.toFa(): String = toString().map { ch ->
    if (ch.isDigit()) faDigits[ch - '0'] else ch
}.joinToString("")

private val jalaliMonths = listOf(
    "فروردین", "اردیبهشت", "خرداد",
    "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر",
    "دی", "بهمن", "اسفند"
)

/**
 * Standard Gregorian -> Jalali conversion (integer math, well-tested algorithm).
 * Returns "۱۴ آبان ۱۴۰۳" style strings.
 */
fun String.toJalaliFa(): String {
    return runCatching {
        val date = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        val (jy, jm, jd) = gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
        "${jd.toFa()} ${jalaliMonths[jm - 1]} ${jy.toFa()}"
    }.getOrDefault(this)
}

private fun gregorianToJalali(gyIn: Int, gm: Int, gd: Int): IntArray {
    var gy = gyIn
    val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    var jy = if (gy <= 1600) 0 else 979
    gy -= if (gy <= 1600) 621 else 1600
    val gy2 = if (gm > 2) (gy + 1) else gy
    var days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 -
        80 + gd + gDaysInMonth[gm - 1]
    jy += 33 * (days / 12053)
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += (days - 1) / 365
        days = (days - 1) % 365
    }
    val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
    val jd = 1 + (if (days < 186) (days % 31) else ((days - 186) % 30))
    return intArrayOf(jy, jm, jd)
}
