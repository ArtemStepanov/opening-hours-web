package ru.stxima.openinghoursweb

/**
 * Class to store all the test resources
 */
class TestResources {

    companion object {
        const val TEST_REQUESTS_BASE_PATH = "/service/openinghours/requests"
        const val TEST_RESPONSES_BASE_PATH = "/service/openinghours/responses"

        val correctRequest1 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-1.json"
        )!!

        val correctRequest2 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-2.json"
        )!!

        val correctRequest3 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-3.json"
        )!!

        val correctRequest4 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-4.json"
        )!!

        val correctRequest5 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-5.json"
        )!!

        val correctRequest6 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-6.json"
        )!!

        val invalidRequest1 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-invalid-1.json"
        )!!

        val invalidRequest2 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-invalid-2.json"
        )!!

        val invalidRequest3 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-invalid-3.json"
        )!!

        val invalidRequest4 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-invalid-4.json"
        )!!

        val invalidRequest5 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-invalid-5.json"
        )!!

        val invalidRequest6 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-invalid-6.json"
        )!!

        val invalidRequest7 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-invalid-7.json"
        )!!

        val correctConvertRequest1 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-convert-1.json"
        )!!

        val correctConvertRequest2 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-convert-2.json"
        )!!

        val correctConvertRequest3 = TestResources::class.java.getResource(
            "$TEST_REQUESTS_BASE_PATH/request-correct-convert-3.json"
        )!!
    }
}