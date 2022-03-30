package ru.stxima.openinghoursweb.service.openinghours.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.stxima.openinghoursweb.controller.openinghours.model.ConvertOpeningHoursRequest
import ru.stxima.openinghoursweb.controller.openinghours.model.OpeningHoursRequest
import ru.stxima.openinghoursweb.controller.openinghours.model.OpeningType
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursService
import ru.stxima.openinghoursweb.service.openinghours.model.OpeningHours
import ru.stxima.openinghoursweb.service.openinghours.model.ProcessingResult
import ru.stxima.openinghoursweb.util.DateTimeUtils
import java.time.DayOfWeek
import java.util.*

@Service
class OpeningHoursServiceImpl : OpeningHoursService {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OpeningHoursServiceImpl::class.java)

        private const val MAX_TIME_VALUE = 86399
        private const val MIN_TIME_VALUE = 0
    }

    override fun validateAndProcess(
        request: ConvertOpeningHoursRequest
    ): ProcessingResult {
        LOGGER.trace("Validating data")

        val rawData = request.data
        // val validationErrors = test(request)
        val validationErrors = validate(rawData)
        if (validationErrors.any()) {
            return ProcessingResult(
                success = false,
                validationErrors = validationErrors
            )
        }

        val data = mutableListOf<OpeningHours>()

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
                val closeTime = getCloseTimeFromTheNextDay(dayOfWeek, request.data)
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

        return ProcessingResult(
            success = true,
            data = data
        )
    }

    private fun validate(
        request: Map<DayOfWeek, List<OpeningHoursRequest>>
    ): List<String> {
        val errors = mutableListOf<String>()

        LOGGER.trace("Validating request parameters")
        if (!validateRequestParameters(request, errors)) {
            LOGGER.trace("Some of request parameters are invalid")
            return errors
        }

        LOGGER.trace("Request parameters were validated successfully")

        val requestSorted = request.toSortedMap()
        for ((dayOfWeek, openingHours) in requestSorted) {
            val openingHoursSorted = openingHours.sortedBy { it.value }

            // Here we validate if each OPEN has paired CLOSE and vice versa.
            LOGGER.trace("Validating that for each OPEN entry there are CLOSE entry exists")
            validateClosingsOpenings(dayOfWeek, requestSorted, openingHoursSorted, errors, OpeningType.OPEN)

            LOGGER.trace("Validating that for each CLOSE entry there are OPEN entry exists")
            validateClosingsOpenings(dayOfWeek, requestSorted, openingHoursSorted, errors, OpeningType.CLOSE)
        }

        return errors
    }

    private fun convertRawOpeningHoursToString(
        hours: MutableList<OpeningHoursRequest>
    ): String {
        val dataList = mutableListOf<String>()
        // We can use 'windowed' here because we can be sure, that all the data passed validation
        // and all the openings hours have paired closing hours
        val sortedAndWindowedHours = hours.windowed(2, 2)
        for ((current, next) in sortedAndWindowedHours) {
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

    /**
     * Validate if all the [openingType] has it opposite.
     * E.g. all the openings has the closings and vice versa.
     * The method fills the [errors] collection with a [String] representation of an error.
     *
     * @param dayOfWeek day of week to check the next or previous day for a possible containing of opening/closing.
     * @param requestSorted sorted request collection. Will be used to retrieve the next or previous day to check for a possible containing of opening/closing.
     * @param hoursSorted sorted collection of opening/closing hours for the [dayOfWeek].
     * @param errors errors collection.
     * @param openingType type to check opposites for. E.g. for [OpeningType.OPEN] we will check all the paired [OpeningType.CLOSE].
     */
    private fun validateClosingsOpenings(
        dayOfWeek: DayOfWeek,
        requestSorted: SortedMap<DayOfWeek, List<OpeningHoursRequest>>,
        hoursSorted: List<OpeningHoursRequest>,
        errors: MutableList<String>,
        openingType: OpeningType
    ) {
        val (left, right) = hoursSorted.partition { it.type == openingType }
        for (currentHour in left) {
            val oppositeHourForTheCurrentDay = if (openingType == OpeningType.OPEN) {
                right.firstOrNull {
                    it.value > currentHour.value
                }
            } else {
                right.lastOrNull {
                    it.value < currentHour.value
                }
            }

            if (oppositeHourForTheCurrentDay == null) {
                if (!checkIfOppositeHourIsOnTheNextOrPreviousDay(dayOfWeek, requestSorted, openingType))
                    errors.add(
                        getOpeningErrorMessage(dayOfWeek, currentHour.value, openingType)
                    )
            }
        }
    }

    /**
     * Function to check if opposite to [openingType] is on the next or the previous day exists.
     *
     * @param dayOfWeek day of week to check the next or previous day for a possible containing of opening/closing.
     * @param requestSorted request data to retrieve information from, using [dayOfWeek].
     * @param openingType type to check opposites for. E.g. for [OpeningType.OPEN] we will check that previous day contain [OpeningType.CLOSE] as a last entry.
     *
     * @return true if [openingType] has it pair, otherwise false.
     */
    private fun checkIfOppositeHourIsOnTheNextOrPreviousDay(
        dayOfWeek: DayOfWeek,
        requestSorted: SortedMap<DayOfWeek, List<OpeningHoursRequest>>,
        openingType: OpeningType
    ): Boolean {
        val openingTypeIsOpen = openingType == OpeningType.OPEN
        val hour = if (openingTypeIsOpen) {
            requestSorted[dayOfWeek.plus(1)]?.minByOrNull { it.value }
        } else {
            requestSorted[dayOfWeek.minus(1)]?.maxByOrNull { it.value }
        }

        return hour?.type == if (openingTypeIsOpen) {
            OpeningType.CLOSE
        } else {
            OpeningType.OPEN
        }
    }

    /**
     * Validate request entries.
     *
     * @param request request to validate.
     * @param errors errors collection.
     *
     * @return true if validation succeed, otherwise false.
     */
    private fun validateRequestParameters(
        request: Map<DayOfWeek, List<OpeningHoursRequest>>,
        errors: MutableList<String>
    ): Boolean {
        request.forEach { (dayOfWeek, hours) ->
            val dayOfWeekFormatted = DateTimeUtils.getDayOfWeekDisplayName(dayOfWeek)
            hours.forEach {
                if (it.value > MAX_TIME_VALUE || it.value < MIN_TIME_VALUE) {
                    errors.add(
                        "Wrong input for the '${
                            it.type.toString().lowercase()
                        }' hour on $dayOfWeekFormatted. Passed value must be in a range between $MIN_TIME_VALUE and $MAX_TIME_VALUE"
                    )
                }
            }
        }

        return errors.isEmpty()
    }

    private fun getOpeningErrorMessage(
        dayOfWeek: DayOfWeek,
        openingHoursValue: Int,
        openingType: OpeningType
    ): String {
        val typeFormatted = if (openingType == OpeningType.OPEN) "closing" else "opening"
        return "No $typeFormatted entry for the day '${
            DateTimeUtils.getDayOfWeekDisplayName(dayOfWeek)
        }' and hour '${
            DateTimeUtils.fromUnixTimeToString(openingHoursValue.toLong())
        }'"
    }
}