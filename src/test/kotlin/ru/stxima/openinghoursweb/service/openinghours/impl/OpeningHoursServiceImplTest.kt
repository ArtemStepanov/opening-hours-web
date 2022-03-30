package ru.stxima.openinghoursweb.service.openinghours.impl

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.stxima.openinghoursweb.ObjectMapperFactory
import ru.stxima.openinghoursweb.TestResources
import ru.stxima.openinghoursweb.controller.openinghours.model.ConvertOpeningHoursRequest
import ru.stxima.openinghoursweb.service.openinghours.model.OpeningHours
import ru.stxima.openinghoursweb.service.openinghours.model.ProcessingResult
import java.time.DayOfWeek

internal class OpeningHoursServiceImplTest {

    private val service = OpeningHoursServiceImpl()
    private val objectMapper = ObjectMapperFactory.newJsonMapper()

    @Test
    fun `Validation - Pass for the correct request`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctRequest1)

        val validationResult = service.validateAndProcess(requestModel)

        assertTrue(validationResult.success)
    }

    @Test
    fun `Validation - Does not pass because of unclosed day`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.invalidRequest1)

        val validationResult = service.validateAndProcess(requestModel)

        assertEquals(
            listOf(
                "No closing entry for the day 'Monday' and hour '2:46 AM'",
                "No closing entry for the day 'Friday' and hour '6 PM'"
            ),
            validationResult.validationErrors
        )
    }

    @Test
    fun `Validation - Does not pass because of no opening day`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.invalidRequest2)

        val validationResult = service.validateAndProcess(requestModel)

        assertEquals(
            listOf(
                "No opening entry for the day 'Monday' and hour '1 AM'",
                "No opening entry for the day 'Friday' and hour '6 PM'"
            ),
            validationResult.validationErrors
        )
    }

    @Test
    fun `Validation - Does not pass because of no opening day 2`() {

        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.invalidRequest3)

        val validationResult = service.validateAndProcess(requestModel)

        assertEquals(
            listOf(
                "No closing entry for the day 'Monday' and hour '2:46 AM'",
                "No opening entry for the day 'Monday' and hour '1 AM'",
                "No closing entry for the day 'Friday' and hour '6 PM'"
            ),
            validationResult.validationErrors
        )
    }

    @Test
    fun `Validation - Does not pass because of incorrect request`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.invalidRequest4)

        val validationResult = service.validateAndProcess(requestModel)
        assertEquals(
            ProcessingResult(
                success = false,
                validationErrors = listOf(
                    "Wrong input for the 'open' hour on Tuesday. Passed value must be in a range between 0 and 86399",
                    "Wrong input for the 'close' hour on Tuesday. Passed value must be in a range between 0 and 86399",
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Pass open on Monday and close on Sunday validation`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctRequest2)

        val validationResult = service.validateAndProcess(requestModel)

        assertTrue(validationResult.success)
    }

    @Test
    fun `Validation - Pass open on Sunday and close on Monday validation`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctRequest3)

        val validationResult = service.validateAndProcess(requestModel)

        assertTrue(validationResult.success)
    }

    @Test
    fun `Validation - Does not pass day opened and not closed`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.invalidRequest5)

        val validationResult = service.validateAndProcess(requestModel)

        assertEquals(
            listOf(
                "No closing entry for the day 'Tuesday' and hour '10 AM'"
            ),
            validationResult.validationErrors
        )
    }

    @Test
    fun `Validation - Does not pass day closed and was not opened`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.invalidRequest6)

        val validationResult = service.validateAndProcess(requestModel)

        assertEquals(
            listOf(
                "No opening entry for the day 'Tuesday' and hour '10 AM'"
            ),
            validationResult.validationErrors
        )
    }

    @Test
    fun `Validation - Pass day opened and closed and then opened and closed the next day`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctRequest4)

        val validationResult = service.validateAndProcess(requestModel)

        assertTrue(validationResult.success)
    }

    @Test
    fun `Validation - Does no pass day opened and closed and then opened and closed the day after next day`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.invalidRequest7)

        val validationResult = service.validateAndProcess(requestModel)

        assertEquals(
            listOf(
                "No closing entry for the day 'Tuesday' and hour '9 PM'",
                "No opening entry for the day 'Thursday' and hour '1 AM'"
            ),
            validationResult.validationErrors
        )
    }

    @Test
    fun `Validation - Pass day closed on Monday then opened and closed and then opened on Sunday`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctRequest5)

        val validationResult = service.validateAndProcess(requestModel)

        assertTrue(validationResult.success)
    }

    @Test
    fun `Validation - Pass day closed and opened repeatedly during the day`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctRequest6)

        val validationResult = service.validateAndProcess(requestModel)

        assertTrue(validationResult.success)
    }

    @Test
    fun `Conversion - Pass day opened on Monday and closed on Tuesday`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctConvertRequest1)
        val expected = ProcessingResult(
            success = true,
            data = listOf(
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
        )

        val converted = service.validateAndProcess(
            requestModel
        )

        assertEquals(expected, converted)
    }

    @Test
    fun `Conversion - Pass day opened on Sunday and closed on Monday`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctConvertRequest2)
        val expected = ProcessingResult(
            success = true,
            data = listOf(
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
        )

        val converted = service.validateAndProcess(
            requestModel
        )

        assertEquals(expected, converted)
    }

    @Test
    fun `Conversion - Pass convert works correctly for the whole week`() {
        val requestModel =
            objectMapper.readValue<ConvertOpeningHoursRequest>(TestResources.correctConvertRequest3)
        val expected = ProcessingResult(
            success = true,
            data = listOf(
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
        )

        val converted = service.validateAndProcess(
            requestModel
        )

        assertEquals(expected, converted)
    }
}