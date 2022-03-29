package ru.stxima.openinghoursweb.controller.openinghours

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpStatus
import ru.stxima.openinghoursweb.ObjectMapperFactory
import ru.stxima.openinghoursweb.TestResources
import ru.stxima.openinghoursweb.controller.openinghours.model.GetHumanReadableOpeningHoursFromRawDataRequest

/**
 * Integration tests for the [OpeningHoursController].
 * Actually, we can mock all the related services, but
 * there is no need in it, since they are parametereless
 * and initializing just correctly without any additional intervention.
 * And this way we can check all the logic works correctly better.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    // Disabling debug-mode to get actual responses
    properties = ["openinghours.debug-mode=false"]
)
internal class OpeningHoursControllerTest(
    @Autowired private val restTemplate: TestRestTemplate
) {
    private val objectMapper = ObjectMapperFactory.newJsonMapper()

    @ParameterizedTest
    @CsvSource(
        "request-correct-1.json, response-correct-1.html",
        "request-correct-2.json, response-correct-2.html",
        "request-correct-4.json, response-correct-3.html",
    )
    fun `Test convert works correctly with valid data input`(
        requestResourcePath: String,
        expectedResponseResourcePath: String
    ) {
        val requestResource = this.javaClass.getResource(
            "${TestResources.TEST_REQUESTS_BASE_PATH}/$requestResourcePath"
        )

        val responseResource = this.javaClass.getResource(
            "${TestResources.TEST_RESPONSES_BASE_PATH}/$expectedResponseResourcePath"
        )

        assertNotNull(requestResource, "Request resource was not found")
        assertNotNull(responseResource, "Response resource was not found")

        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(requestResource!!)

        val response = restTemplate.postForEntity<String>("/openinghours", requestModel)
        assertEquals(HttpStatus.OK, response.statusCode, "Status code is not OK")
        assertNotNull(response.body, "Response body was null")
        assertEquals(responseResource!!.readText(), response.body!!, "Expected response body is not equal to actual")
    }

    @ParameterizedTest
    @CsvSource(
        "request-invalid-1.json, response-invalid-1.html",
        "request-invalid-2.json, response-invalid-2.html",
        "request-invalid-3.json, response-invalid-3.html",
        "request-invalid-4.json, response-invalid-4.html",
        "request-invalid-4.json, response-invalid-5.html"
    )
    fun `Test request validation - return errors list for each data`(
        requestResourcePath: String,
        expectedResponseResourcePath: String
    ) {
        val requestResource = this.javaClass.getResource(
            "${TestResources.TEST_REQUESTS_BASE_PATH}/$requestResourcePath"
        )

        val responseResource = this.javaClass.getResource(
            "${TestResources.TEST_RESPONSES_BASE_PATH}/$expectedResponseResourcePath"
        )

        assertNotNull(requestResource, "Request resource was not found")
        assertNotNull(responseResource, "Response resource was not found")

        val requestModel =
            objectMapper.readValue<GetHumanReadableOpeningHoursFromRawDataRequest>(requestResource!!)

        val response = restTemplate.postForEntity<String>("/openinghours", requestModel)
        assertEquals(HttpStatus.OK, response.statusCode, "Status code is not OK")
        assertNotNull(response.body, "Response body was null")
        assertEquals(responseResource!!.readText(), response.body!!, "Expected response body is not equal to actual")
    }

    @Test
    fun `Test requesting no-data return no data page`() {
        val response = restTemplate.postForEntity<String>(
            "/openinghours",
            GetHumanReadableOpeningHoursFromRawDataRequest(
                emptyMap()
            )
        )

        assertEquals(HttpStatus.OK, response.statusCode, "Status code is not OK")
        assertNotNull(response.body, "Response body was null")

        val expectedResponse = this.javaClass.getResource(
            "${TestResources.TEST_RESPONSES_BASE_PATH}/response-no-data.html"
        )

        assertEquals(expectedResponse!!.readText(), response.body!!, "Expected response body is not equal to actual")
    }
}