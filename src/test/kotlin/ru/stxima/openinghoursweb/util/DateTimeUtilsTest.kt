package ru.stxima.openinghoursweb.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

internal class DateTimeUtilsTest {

    @ParameterizedTest
    @CsvSource("3600, 1 AM", "32400, 9 AM", "39600, 11 AM", "57600, 4 PM", "37800, 10:30 AM")
    fun `Test converting from unix time to plain understandable string - should format all the provided values`(
        input: Long,
        expected: String
    ) {
        val formatted = DateTimeUtils.fromUnixTimeToString(input)
        assertEquals(expected, formatted)
    }

    @ParameterizedTest
    @EnumSource(DayOfWeek::class)
    fun getDayOfWeekDisplayName(dayOfWeek: DayOfWeek) {
        val formatted = DateTimeUtils.getDayOfWeekDisplayName(dayOfWeek)
        assertEquals(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH), formatted)
    }
}