package ru.stxima.openinghoursweb.service.openinghours.model

import java.time.DayOfWeek

/**
 * Model representing converted and human-readable data
 */
data class OpeningHours(
    val dayOfWeek: DayOfWeek,
    val openingHours: String
)
