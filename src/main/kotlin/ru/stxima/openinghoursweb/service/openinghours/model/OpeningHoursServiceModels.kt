package ru.stxima.openinghoursweb.service.openinghours.model

import java.time.DayOfWeek

/**
 * Model representing converted and human-readable data
 */
data class OpeningHours(
    val dayOfWeek: DayOfWeek,
    val openingHoursRange: String
)

data class ProcessingResult(
    var success: Boolean,
    var data: List<OpeningHours> = emptyList(),
    var validationErrors: List<String> = emptyList(),
)
