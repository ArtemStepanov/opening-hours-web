package ru.stxima.openinghoursweb.service.openinghours.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import ru.stxima.openinghoursweb.controller.openinghours.model.OpeningType
import ru.stxima.openinghoursweb.controller.openinghours.model.OpeningHoursRequest
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
        private val LOGGER = LoggerFactory.getLogger(OpeningHoursDataServiceImpl::class.java)

        private const val MAX_TIME_VALUE = 86399
        private const val MIN_TIME_VALUE = 0
    }

    override fun validateAndPrepareDataForGetHumanReadableOpeningHoursRequest(
        request: Map<DayOfWeek, List<OpeningHoursRequest>>
    ): GetHumanReadableOpeningHoursFromRawDataRequestValidationResult {
        val errors = mutableListOf<String>()

        LOGGER.trace("Validating request parameters")
        if (!validateRequestParameters(request, errors)) {
            LOGGER.trace("Some of request parameters are invalid")
            return GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(errors)
        }

        LOGGER.trace("Request parameters were validated successfully")

        val requestSorted = request.toSortedMap()
        for ((dayOfWeek, hours) in requestSorted) {
            val hoursSorted = hours.sortedBy { it.value }

            // Here we validate if each OPEN has paired CLOSE and vice versa.
            LOGGER.trace("Validating that for each OPEN entry there are CLOSE entry exists")
            validateClosingsOpenings(dayOfWeek, requestSorted, hoursSorted, errors, OpeningType.OPEN)

            LOGGER.trace("Validating that for each CLOSE entry there are OPEN entry exists")
            validateClosingsOpenings(dayOfWeek, requestSorted, hoursSorted, errors, OpeningType.CLOSE)
        }

        return GetHumanReadableOpeningHoursFromRawDataRequestValidationResult(errorMessages = errors)
    }

    /**
     * Validate if all the [openingType] has it opposite.
     * E.g. all the openings has the closings and vice versa.
     * The method will the [errors] collection with [String] representation of an error.
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
                        getOpeningErrorMessage(dayOfWeek, currentHour, openingType)
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
        val hour = if (openingType == OpeningType.OPEN) {
            requestSorted[dayOfWeek.plus(1)]?.minByOrNull { it.value }
        } else {
            requestSorted[dayOfWeek.minus(1)]?.maxByOrNull { it.value }
        }

        return hour?.type == if (openingType == OpeningType.OPEN) {
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
            hours.filter { it.value > MAX_TIME_VALUE || it.value < MIN_TIME_VALUE }.forEach {
                errors.add(
                    "Wrong input for the '${
                        it.type.toString().lowercase()
                    }' hour on $dayOfWeekFormatted. Passed value must be in a range between $MIN_TIME_VALUE and $MAX_TIME_VALUE"
                )
            }
        }

        return errors.isEmpty()
    }

    private fun getOpeningErrorMessage(
        dayOfWeek: DayOfWeek,
        openingHoursRequest: OpeningHoursRequest,
        openingType: OpeningType
    ): String {
        val typeFormatted = if (openingType == OpeningType.OPEN) "closing" else "opening"

        val hourValue = if (debugMode) {
            "${openingHoursRequest.value}"
        } else {
            DateTimeUtils.fromUnixTimeToString(openingHoursRequest.value.toLong())
        }

        return "No $typeFormatted entry for the day '${DateTimeUtils.getDayOfWeekDisplayName(dayOfWeek)}' and hour '$hourValue'"
    }
}