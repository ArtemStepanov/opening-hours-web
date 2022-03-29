package ru.stxima.openinghoursweb.service.openinghours

import ru.stxima.openinghoursweb.controller.openinghours.model.OpeningHoursRequest
import ru.stxima.openinghoursweb.service.openinghours.model.GetHumanReadableOpeningHoursFromRawDataRequestValidationResult
import java.time.DayOfWeek

interface OpeningHoursDataService {

    /**
     * Validate request data and prepare service-ready data that will be parsed by [OpeningHoursService].
     *
     * @param request raw request data.
     * @return [GetHumanReadableOpeningHoursFromRawDataRequestValidationResult] representing validation status.
     */
    fun validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(
        request: Map<DayOfWeek, List<OpeningHoursRequest>>
    ): GetHumanReadableOpeningHoursFromRawDataRequestValidationResult
}