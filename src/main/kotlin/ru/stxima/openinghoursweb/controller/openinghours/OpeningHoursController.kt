package ru.stxima.openinghoursweb.controller.openinghours

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.stxima.openinghoursweb.controller.openinghours.model.GetHumanReadableOpeningHoursFromRawDataRequest
import ru.stxima.openinghoursweb.controller.openinghours.model.OpeningHoursResponse
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursDataService
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursService
import ru.stxima.openinghoursweb.util.DateTimeUtils

@Controller
@RequestMapping("/openinghours")
class OpeningHoursController(
    private val openingHoursService: OpeningHoursService,
    private val openingHoursDataService: OpeningHoursDataService
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OpeningHoursController::class.java)
    }

    /**
     * Get human-readable restaurant opening hours from the raw data.
     * If no data is defined, method will return 'no-data' view.
     *
     * @param openingHoursFromRawDataRequest raw restaurant data.
     * @return View that is related to the response data.
     */
    @PostMapping
    fun getRestaurantOpeningHoursFromRawData(
        @RequestBody openingHoursFromRawDataRequest: GetHumanReadableOpeningHoursFromRawDataRequest,
        model: Model
    ): String {
        // If request contains no related data for the restaurant.
        if (openingHoursFromRawDataRequest.data.isEmpty()) {
            LOGGER.trace("There is no data in request")

            model["title"] = "No data for the specified restaurant"

            return "no-data"
        }

        LOGGER.trace("Validating request data")
        val validationResult = openingHoursDataService
            .validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(openingHoursFromRawDataRequest.data)
        LOGGER.trace("Validation result: {}", validationResult.hasErrors)

        if (validationResult.hasErrors) {
            LOGGER.trace("Validation contains errors")

            model["title"] = "Validation error"
            model["data"] = validationResult.errorMessages

            return "opening-hours-validation-error"
        }

        LOGGER.trace("Converting raw data to understandable data")
        model["title"] = "Opening hours"
        val openingHours = openingHoursService.convertOpeningHoursRawDataToReadableFormat(
            openingHoursFromRawDataRequest
        )

        model["data"] = openingHours.map {
            OpeningHoursResponse(
                dayOfWeek = DateTimeUtils.getDayOfWeekDisplayName(it.dayOfWeek),
                openingHoursRange = it.openingHoursRange
            )
        }

        return "opening-hours"
    }
}