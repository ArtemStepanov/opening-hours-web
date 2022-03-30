package ru.stxima.openinghoursweb.service.openinghours

import ru.stxima.openinghoursweb.controller.openinghours.model.ConvertOpeningHoursRequest
import ru.stxima.openinghoursweb.service.openinghours.model.OpeningHours
import ru.stxima.openinghoursweb.service.openinghours.model.ProcessingResult

interface OpeningHoursService {

    /**
     * Convert prepared request data to the human-readable format.
     *
     * @param request raw request data.
     * @return [List] of [OpeningHours] - parsed format that will be displayed on UI.
     */
    fun validateAndProcess(
        request: ConvertOpeningHoursRequest
    ): ProcessingResult
}