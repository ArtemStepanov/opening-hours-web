package ru.stxima.openinghoursweb.service.openinghours

import ru.stxima.openinghoursweb.controller.openinghours.GetHumanReadableOpeningHoursFromRawDataRequest
import ru.stxima.openinghoursweb.service.openinghours.model.OpeningHours

interface OpeningHoursService {

    /**
     * Convert prepared request data to the human-readable format
     *
     * @param openingHoursFromRawDataRequest raw request data
     * @return [List] of [OpeningHours] - parsed format that will be displayed on UI
     */
    fun convertOpeningHoursRawDataToReadableFormat(
        openingHoursFromRawDataRequest: GetHumanReadableOpeningHoursFromRawDataRequest
    ): List<OpeningHours>
}