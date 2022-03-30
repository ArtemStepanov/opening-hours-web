package ru.stxima.openinghoursweb.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

object DateTimeUtils {

    private val dateFormat = DateTimeFormatter.ofPattern("h a")
    private val dateFormatWithMinutes = DateTimeFormatter.ofPattern("h:mm a")

    /**
     * Convert unix time to the 'hh a' or 'hh:mm a' time format.
     *
     * @param unixTime unix time.
     * @return Formatted time string.
     */
    fun fromUnixTimeToString(unixTime: Long): String {
        val instant = Instant.ofEpochSecond(unixTime).atZone(ZoneOffset.UTC)
        return if (instant.minute > 0) dateFormatWithMinutes.format(instant) else dateFormat.format(instant)
    }

    /**
     * Get display name of the specified [DayOfWeek].
     *
     * @param dayOfWeek day of week to get display name for.
     * @return Display name.
     */
    fun getDayOfWeekDisplayName(dayOfWeek: DayOfWeek): String = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
}