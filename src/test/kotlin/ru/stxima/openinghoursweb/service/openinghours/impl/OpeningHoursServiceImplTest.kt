package ru.stxima.openinghoursweb.service.openinghours.impl

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.stxima.openinghoursweb.ObjectMapperFactory
import ru.stxima.openinghoursweb.TestResources
import ru.stxima.openinghoursweb.controller.openinghours.GetHumanReadableOpeningHoursFromRawDataRequest
import ru.stxima.openinghoursweb.service.openinghours.model.OpeningHours
import java.time.DayOfWeek

internal class OpeningHoursServiceImplTest {

    private val service = OpeningHoursServiceImpl()
    private val objectMapper = ObjectMapperFactory.newJsonMapper()

    @Test
    fun `Test opened on Monday and closed on Tuesday converted correctly`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctConvertRequest1)
        val expected = listOf(
            OpeningHours(
                dayOfWeek = DayOfWeek.MONDAY,
                openingHoursRange = "6 PM - 1 AM"
            ),
            *DayOfWeek.values()
                // Skip Monday
                .drop(1)
                .map {
                    OpeningHours(
                        dayOfWeek = it,
                        openingHoursRange = "Closed"
                    )
                }
                .toTypedArray()
        )

        val converted = service.convertOpeningHoursRawDataToReadableFormat(
            requestModel
        )

        assertEquals(expected, converted)
    }

    @Test
    fun `Test opened on Sunday and closed on Monday converted correctly`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctConvertRequest2)
        val expected = listOf(
            *DayOfWeek.values()
                // Skip Sunday
                .dropLast(1)
                .map {
                    OpeningHours(
                        dayOfWeek = it,
                        openingHoursRange = "Closed"
                    )
                }
                .toTypedArray(),
            OpeningHours(
                dayOfWeek = DayOfWeek.SUNDAY,
                openingHoursRange = "6 PM - 1 AM"
            )
        )

        val converted = service.convertOpeningHoursRawDataToReadableFormat(
            requestModel
        )

        assertEquals(expected, converted)
    }

    @Test
    fun `Test convert works correctly for the whole week`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctConvertRequest3)
        val expected = listOf(
            OpeningHours(
                dayOfWeek = DayOfWeek.MONDAY,
                openingHoursRange = "Closed"
            ),
            OpeningHours(
                dayOfWeek = DayOfWeek.TUESDAY,
                openingHoursRange = "10 AM - 6 PM"
            ),
            OpeningHours(
                dayOfWeek = DayOfWeek.WEDNESDAY,
                openingHoursRange = "Closed"
            ),
            OpeningHours(
                dayOfWeek = DayOfWeek.THURSDAY,
                openingHoursRange = "10:30 AM - 6 PM"
            ),
            OpeningHours(
                dayOfWeek = DayOfWeek.FRIDAY,
                openingHoursRange = "10 AM - 1 AM"
            ),
            OpeningHours(
                dayOfWeek = DayOfWeek.SATURDAY,
                openingHoursRange = "10 AM - 1 AM"
            ),
            OpeningHours(
                dayOfWeek = DayOfWeek.SUNDAY,
                openingHoursRange = "12 PM - 9 PM, 11 PM - 1 AM"
            )
        )

        val converted = service.convertOpeningHoursRawDataToReadableFormat(
            requestModel
        )

        assertEquals(expected, converted)
    }
}