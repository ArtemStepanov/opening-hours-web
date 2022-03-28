package ru.stxima.openinghoursweb.service.openinghours.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.stxima.openinghoursweb.controller.openinghours.GetHumanReadableOpeningHoursFromRawDataRequest
import ru.stxima.openinghoursweb.controller.openinghours.OpenType
import ru.stxima.openinghoursweb.controller.openinghours.OpeningHoursRequest
import ru.stxima.openinghoursweb.service.openinghours.model.OpeningHours
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursService
import ru.stxima.openinghoursweb.util.DateTimeUtils
import java.time.DayOfWeek

@Service
class OpeningHoursServiceImpl : OpeningHoursService {

    @Value("\${openinghours.debug-mode}")
    private val debugMode: Boolean = false

    override fun convertOpeningHoursRawDataToReadableFormat(
        openingHoursFromRawDataRequest: GetHumanReadableOpeningHoursFromRawDataRequest
    ): List<OpeningHours> {
        val data = mutableListOf<OpeningHours>()
        val rawData = openingHoursFromRawDataRequest.data

        for (dayOfWeek in DayOfWeek.values()) {
            val hours = rawData[dayOfWeek] ?: emptyList()
            val hoursSortedMutable = hours
                .sortedBy { it.value }
                .toMutableList()

            if (hoursSortedMutable.firstOrNull()?.type == OpenType.CLOSE)
                hoursSortedMutable.removeFirst()

            if (hoursSortedMutable.isEmpty()) {
                data.add(
                    OpeningHours(
                        dayOfWeek = dayOfWeek,
                        openingHours = "Closed"
                    )
                )

                continue
            }

            // If the last entry is OPEN, then we add a CLOSE entry to the collection from the next day
            if (hoursSortedMutable.lastOrNull()?.type == OpenType.OPEN) {
                val closeTime = getCloseTimeFromTheNextDay(dayOfWeek, openingHoursFromRawDataRequest.data)
                hoursSortedMutable.add(closeTime)
            }

            val openingHoursConverted = convertRawOpeningHoursToString(hoursSortedMutable)

            data.add(
                OpeningHours(
                    dayOfWeek = dayOfWeek,
                    openingHours = openingHoursConverted
                )
            )
        }

        return data
    }

    private fun convertRawOpeningHoursToString(
        hours: MutableList<OpeningHoursRequest>
    ): String {
        val dataList = mutableListOf<String>()
        // We can use 'windowed' here because we can be sure, that all the data passed validation
        // and all the openings hours have paired closing hours
        val sortedAndWindowedHours = hours.windowed(2, 2)
        for ((current, next) in sortedAndWindowedHours) {
            if (debugMode) {
                dataList.add(
                    "${
                        current.value
                    } - ${
                        next.value
                    }"
                )

                continue
            }

            dataList.add(
                "${
                    DateTimeUtils.fromUnixTimeToString(
                        current.value.toLong()
                    )
                } - ${
                    DateTimeUtils.fromUnixTimeToString(
                        next.value.toLong()
                    )
                }"
            )
        }

        return dataList.joinToString(separator = ", ")
    }

    private fun getCloseTimeFromTheNextDay(
        currentDayOfWeek: DayOfWeek,
        data: Map<DayOfWeek, List<OpeningHoursRequest>>
    ): OpeningHoursRequest {
        // We can assume that data already has passed all the validation,
        // so that's why we can use !! here
        val nextDayOpenHours = data[currentDayOfWeek.plus(1)]!!
        return nextDayOpenHours.first()
    }
}