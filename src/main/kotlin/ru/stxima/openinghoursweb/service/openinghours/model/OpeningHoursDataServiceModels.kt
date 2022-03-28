package ru.stxima.openinghoursweb.service.openinghours.model

data class GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(
    val errorMessages: List<String>
) {
    val hasErrors = errorMessages.any()
}
