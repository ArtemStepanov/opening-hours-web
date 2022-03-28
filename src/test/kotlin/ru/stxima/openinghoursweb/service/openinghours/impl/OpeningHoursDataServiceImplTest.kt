package ru.stxima.openinghoursweb.service.openinghours.impl

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import ru.stxima.openinghoursweb.ObjectMapperFactory
import ru.stxima.openinghoursweb.TestResources
import ru.stxima.openinghoursweb.controller.openinghours.GetHumanReadableOpeningHoursFromRawDataRequest
import ru.stxima.openinghoursweb.service.openinghours.model.GetHumanReadableOpeningHoursFromRawDataRequestValidationResult

internal class OpeningHoursDataServiceImplTest {

    private val service = OpeningHoursDataServiceImpl()
    private val objectMapper = ObjectMapperFactory.newJsonMapper()

    @Test
    fun `Validation - Passed for the correct request`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctRequest1)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(errorMessages = emptyList()),
            validationResult
        )
    }

    @Test
    fun `Validation - Does not pass because of unclosed day`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.invalidRequest1)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = listOf(
                    "No closing entry for the day 'Monday' and hour '2:46 AM'",
                    "No closing entry for the day 'Friday' and hour '6 PM'"
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Does not pass because of not opened day`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.invalidRequest2)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = listOf(
                    "No opening entry for the day 'Monday' and hour '1 AM'",
                    "No opening entry for the day 'Friday' and hour '6 PM'"
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Does not pass because of both unclosed and not opened days`() {

        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.invalidRequest3)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = listOf(
                    "No closing entry for the day 'Monday' and hour '2:46 AM'",
                    "No opening entry for the day 'Monday' and hour '1 AM'",
                    "No closing entry for the day 'Friday' and hour '6 PM'"
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Does not pass because of incorrect request`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.invalidRequest4)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)
        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = listOf(
                    "Wrong input for the 'open' hour on Tuesday. Passed value must be in a range between 0 and 86399",
                    "Wrong input for the 'close' hour on Tuesday. Passed value must be in a range between 0 and 86399",
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test open on Monday and close on Sunday validates correctly`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctRequest2)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = emptyList()
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test open on Sunday and close on Monday validates correctly`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctRequest3)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = emptyList()
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test day opened and not closed`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.invalidRequest5)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = listOf(
                    "No closing entry for the day 'Tuesday' and hour '10 AM'"
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test day closed and was not opened`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.invalidRequest6)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = listOf(
                    "No opening entry for the day 'Tuesday' and hour '10 AM'"
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test day opened and closed and then opened and closed the next day`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctRequest4)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = emptyList()
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test day opened and closed and then opened and closed the day after next day`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.invalidRequest7)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = listOf(
                    "No closing entry for the day 'Tuesday' and hour '9 PM'",
                    "No opening entry for the day 'Thursday' and hour '1 AM'"
                )
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test day closed on Monday then opened and closed and then opened on Sunday`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctRequest5)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = emptyList()
            ),
            validationResult
        )
    }

    @Test
    fun `Validation - Test day closed and opened repeatedly during the day`() {
        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(TestResources.correctRequest6)

        val validationResult = service.validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(requestModel.data)

        assertEquals(
            GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
                errorMessages = emptyList()
            ),
            validationResult
        )
    }
}