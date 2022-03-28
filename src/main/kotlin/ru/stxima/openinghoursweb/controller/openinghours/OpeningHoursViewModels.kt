package ru.stxima.openinghoursweb.controller.openinghours

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.DayOfWeek

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetHumanReadableOpeningHoursFromRawDataRequest(
    val data: Map<DayOfWeek, List<OpeningHoursRequest>>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpeningHoursRequest(
    val type: OpenType,
    val value: Int
)

enum class OpenType {
    OPEN,
    CLOSE
}

data class OpeningHoursResponse(
    val dayOfWeek: String,
    val openingHours: String
)
