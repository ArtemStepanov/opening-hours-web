package ru.stxima.openinghoursweb.service.openinghours.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.stxima.openinghoursweb.controller.openinghours.GetHumanReadableOpeningHoursFromRawDataRequest
import ru.stxima.openinghoursweb.controller.openinghours.OpeningType
import ru.stxima.openinghoursweb.controller.openinghours.OpeningHoursRequest
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursService
import ru.stxima.openinghoursweb.service.openinghours.model.OpeningHours
import ru.stxima.openinghoursweb.util.DateTimeUtils
import java.time.DayOfWeek

@Service
class OpeningHoursServiceImpl : OpeningHoursService {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OpeningHoursServiceImpl::class.java)
    }

    @Value("\${openinghours.debug-mode}")
    private val debugMode: Boolean = false

    override fun convertOpeningHoursRawDataToReadableFormat(
        openingHoursFromRawDataRequest: GetHumanReadableOpeningHoursFromRawDataRequest
    ): List<OpeningHours> {
        LOGGER.trace("Debug mode is {}", (if (debugMode) "enabled" else "disabled"))

        val data = mutableListOf<OpeningHours>()
        val rawData = openingHoursFromRawDataRequest.data

        for (dayOfWeek in DayOfWeek.values()) {
            LOGGER.trace("Processing day '{}'", dayOfWeek)

            val hours = rawData[dayOfWeek] ?: emptyList()
            val hoursSortedMutable = hours
                .sortedBy { it.value }
                .toMutableList()

            LOGGER.trace("Day '{}' contains {} entries", dayOfWeek, hoursSortedMutable.size)

            if (hoursSortedMutable.firstOrNull()?.type == OpeningType.CLOSE)
                hoursSortedMutable.removeFirst()

            if (hoursSortedMutable.isEmpty()) {
                data.add(
                    OpeningHours(
                        dayOfWeek = dayOfWeek,
                        openingHoursRange = "Closed"
                    )
                )

                continue
            }

            // If the last entry is OPEN, then we add a CLOSE entry to the collection from the next day
            if (hoursSortedMutable.lastOrNull()?.type == OpeningType.OPEN) {
                LOGGER.trace("Moving closing time from {} to {}", dayOfWeek.plus(1), dayOfWeek)
                val closeTime = getCloseTimeFromTheNextDay(dayOfWeek, openingHoursFromRawDataRequest.data)
                hoursSortedMutable.add(closeTime)
            }

            val openingHoursConverted = convertRawOpeningHoursToString(hoursSortedMutable)

            data.add(
                OpeningHours(
                    dayOfWeek = dayOfWeek,
                    openingHoursRange = openingHoursConverted
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