package ru.stxima.openinghoursweb.controller.openinghours.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.DayOfWeek

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConvertOpeningHoursRequest(
    val data: Map<DayOfWeek, List<OpeningHoursRequest>>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpeningHoursRequest(
    val type: OpeningType,
    val value: Int
)

data class OpeningHoursResponse(
    val dayOfWeek: String,
    val openingHoursRange: String
)

enum class OpeningType {
    OPEN,
    CLOSE
}
