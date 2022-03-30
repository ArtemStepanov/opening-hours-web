package ru.stxima.openinghoursweb.controller.openinghours

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.stxima.openinghoursweb.controller.openinghours.model.ConvertOpeningHoursRequest
import ru.stxima.openinghoursweb.controller.openinghours.model.OpeningHoursResponse
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursService
import ru.stxima.openinghoursweb.util.DateTimeUtils

@Controller
@RequestMapping("/openinghours")
class OpeningHoursController(
    private val openingHoursService: OpeningHoursService
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OpeningHoursController::class.java)
    }

    /**
     * Get human-readable restaurant opening hours from the raw data.
     * If no data is defined, method will return 'no-data' view.
     *
     * @param request raw restaurant data.
     * @return View that is related to the response data.
     */
    @PostMapping
    fun convertOpeningHours(
        @RequestBody request: ConvertOpeningHoursRequest,
        model: Model
    ): String {
        // If request contains no related data for the restaurant.
        if (request.data.isEmpty()) {
            LOGGER.trace("There is no data in request")

            model["title"] = "No data for the specified restaurant"

            return "no-data"
        }

        LOGGER.trace("Validating and processing request data")
        val processingResult = openingHoursService.validateAndProcess(request)
        LOGGER.trace("Validation result: {}", processingResult.success)

        if (!processingResult.success) {
            LOGGER.trace("Processing contains errors")

            model["title"] = "Validation error"
            model["data"] = processingResult.validationErrors

            return "opening-hours-validation-error"
        }

        LOGGER.trace("Converting raw data to understandable data")
        model["title"] = "Opening hours"


        model["data"] = processingResult.data.map {
            OpeningHoursResponse(
                dayOfWeek = DateTimeUtils.getDayOfWeekDisplayName(it.dayOfWeek),
                openingHoursRange = it.openingHoursRange
            )
        }

        return "opening-hours"
    }
}