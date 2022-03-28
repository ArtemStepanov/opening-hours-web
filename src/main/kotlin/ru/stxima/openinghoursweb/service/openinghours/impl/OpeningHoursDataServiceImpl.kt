package ru.stxima.openinghoursweb.service.openinghours.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import ru.stxima.openinghoursweb.controller.openinghours.OpenType
import ru.stxima.openinghoursweb.controller.openinghours.OpeningHoursRequest
import ru.stxima.openinghoursweb.service.openinghours.OpeningHoursDataService
import ru.stxima.openinghoursweb.service.openinghours.model.GetHumanReadableOpeningHoursFromRawDataRequestValidationResult
import ru.stxima.openinghoursweb.util.DateTimeUtils
import java.time.DayOfWeek
import java.util.*

@Service
@Scope("singleton")
class OpeningHoursDataServiceImpl : OpeningHoursDataService {

    @Value("\${openinghours.debug-mode}")
    private val debugMode: Boolean = false

    companion object {
        private const val MAX_TIME = 86399
        private const val MIN_TIME = 0
    }

    override fun validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(
        request: Map<DayOfWeek, List<OpeningHoursRequest>>
    ): GetHumanReadableOpeningHoursFromRawDataRequestValidationResult {
        val errors = mutableListOf<String>()

        if (!validateRequestParameters(request, errors)) {
            return GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(errors)
        }

        val requestSorted = request.toSortedMap()
        for ((dayOfWeek, hours) in requestSorted) {
            val dayOfWeekFormatted = DateTimeUtils.getDayOfWeekDisplayName(dayOfWeek)
            val hoursSorted = hours.sortedBy { it.value }

            validateClosings(dayOfWeek, dayOfWeekFormatted, requestSorted, hoursSorted, errors)
            validateOpenings(dayOfWeek, dayOfWeekFormatted, requestSorted, hoursSorted, errors)
        }

        return GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(errorMessages = errors)
    }

    private fun validateRequestParameters(
        request: Map<DayOfWeek, List<OpeningHoursRequest>>,
        errors: MutableList<String>
    ): Boolean {
        request.forEach { (dayOfWeek, hours) ->
            val dayOfWeekFormatted = DateTimeUtils.getDayOfWeekDisplayName(dayOfWeek)
            hours.filter { it.value > MAX_TIME || it.value < MIN_TIME }.forEach {
                errors.add(
                    "Wrong input for the '${
                        it.type.toString().lowercase()
                    }' hour on $dayOfWeekFormatted. Passed value must be in a range between $MIN_TIME and $MAX_TIME"
                )
            }
        }

        return errors.isEmpty()
    }

    private fun validateOpenings(
        dayOfWeek: DayOfWeek,
        dayOfWeekFormatted: String,
        requestSorted: SortedMap<DayOfWeek, List<OpeningHoursRequest>>,
        hoursSorted: List<OpeningHoursRequest>,
        errors: MutableList<String>
    ) {
        val (close1, open1) = hoursSorted.partition { it.type == OpenType.CLOSE }
        for ((index, currentCloseHour) in close1.withIndex()) {
            val previousCloseHour = close1.getOrNull(index - 1)
            val openHourForTheCurrentDay = open1.lastOrNull {
                it.value < currentCloseHour.value
            }

            if (previousCloseHour != null && openHourForTheCurrentDay != null && openHourForTheCurrentDay.value < previousCloseHour.value) {
                errors.add(
                    getOpeningErrorMessage(dayOfWeekFormatted, currentCloseHour, OpenType.CLOSE)
                )

                continue
            }

            if (openHourForTheCurrentDay == null) {
                if (!checkIfOpenHourIsOnThePreviousDay(dayOfWeek, requestSorted))
                    errors.add(
                        getOpeningErrorMessage(dayOfWeekFormatted, currentCloseHour, OpenType.CLOSE)
                    )
            }
        }
    }

    private fun validateClosings(
        dayOfWeek: DayOfWeek,
        dayOfWeekFormatted: String,
        requestSorted: SortedMap<DayOfWeek, List<OpeningHoursRequest>>,
        hoursSorted: List<OpeningHoursRequest>,
        errors: MutableList<String>
    ) {
        val (open, close) = hoursSorted.partition { it.type == OpenType.OPEN }
        for ((index, currentOpenHour) in open.withIndex()) {
            val nextOpenHour = open.getOrNull(index + 1)
            val closeHourForTheCurrentDay = close.firstOrNull {
                it.value > currentOpenHour.value
            }

            if (!checkIfNextOpenHourLargeThanPossibleClosingHour(nextOpenHour, closeHourForTheCurrentDay)) {
                errors.add(
                    getOpeningErrorMessage(dayOfWeekFormatted, currentOpenHour, OpenType.OPEN)
                )

                continue
            }

            if (closeHourForTheCurrentDay == null) {
                if (!checkIfCloseHourIsOnTheNextDay(dayOfWeek, requestSorted))
                    errors.add(
                        getOpeningErrorMessage(dayOfWeekFormatted, currentOpenHour, OpenType.OPEN)
                    )
            }
        }
    }

    private fun checkIfNextOpenHourLargeThanPossibleClosingHour(
        nextOpenHour: OpeningHoursRequest?,
        closeHourForTheCurrentDay: OpeningHoursRequest?
    ): Boolean {
        return nextOpenHour == null || closeHourForTheCurrentDay == null || closeHourForTheCurrentDay.value < nextOpenHour.value
    }

    private fun checkIfCloseHourIsOnTheNextDay(
        dayOfWeek: DayOfWeek,
        requestSorted: SortedMap<DayOfWeek, List<OpeningHoursRequest>>
    ): Boolean {
        val firstHourForTheNextDay = requestSorted[dayOfWeek.plus(1)]?.minByOrNull { it.value }
        return firstHourForTheNextDay?.type == OpenType.CLOSE
    }

    private fun checkIfOpenHourIsOnThePreviousDay(
        dayOfWeek: DayOfWeek,
        requestSorted: SortedMap<DayOfWeek, List<OpeningHoursRequest>>
    ): Boolean {
        val lastHourForTheNextDay = requestSorted[dayOfWeek.minus(1)]?.maxByOrNull { it.value }
        return lastHourForTheNextDay?.type == OpenType.OPEN
    }

    private fun getOpeningErrorMessage(
        dayOfWeekFormatted: String,
        openingHoursRequest: OpeningHoursRequest,
        openType: OpenType
    ): String {
        val typeFormatted = if (openType == OpenType.OPEN) "closing" else "opening"

        val hourValue = if (debugMode) {
            "${openingHoursRequest.value}"
        } else {
            DateTimeUtils.fromUnixTimeToString(openingHoursRequest.value.toLong())
        }

        return "No $typeFormatted entry for the day '$dayOfWeekFormatted' and hour '$hourValue'"
    }
}