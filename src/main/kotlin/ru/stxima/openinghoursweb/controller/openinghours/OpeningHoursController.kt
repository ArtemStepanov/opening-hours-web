package ru.stxima.openinghoursweb.controller.openinghours

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursDataService
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursService
import ru.stxima.openinghoursweb.util.DateTimeUtils

@Controller
@RequestMapping("/openinghours")
class OpeningHoursController(
    private val openingHoursService: OpeningHoursService,
    private val openingHoursDataService: OpeningHoursDataService
) {

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
            model["title"] = "No data for the specified restaurant"

            return "no-data"
        }

        val validationResult = openingHoursDataService
            .validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(openingHoursFromRawDataRequest.data)

        if (validationResult.hasErrors) {
            model["title"] = "Validation error"
            model["data"] = validationResult.errorMessages

            return "opening-hours-validation-error"
        }

        model["title"] = "Opening hours"
        val openingHours = openingHoursService.convertOpeningHoursRawDataToReadableFormat(
            openingHoursFromRawDataRequest
        )

        model["data"] = openingHours.map {
            OpeningHoursResponse(
                dayOfWeek = DateTimeUtils.getDayOfWeekDisplayName(it.dayOfWeek),
                openingHours = it.openingHours
            )
        }

        return "opening-hours"
    }
}