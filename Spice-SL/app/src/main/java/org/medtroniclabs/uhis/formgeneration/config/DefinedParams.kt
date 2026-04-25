package org.medtroniclabs.uhis.formgeneration.config

import org.medtroniclabs.uhis.common.DefinedParams

object DefinedParams {
    const val NAME = "name"
    const val ID = "id"
    const val VALUE = "value"
    const val CULTURE_VALUE = DefinedParams.CULTURE_VALUE
    const val DISPLAY_VALUE = "displayValue"
    const val DefaultIDLabel = "--Select--"

    // visibility related values
    const val VISIBLE = "visible"
    const val INVISIBLE = "invisible"
    const val GONE = "gone"
    const val VISIBILITY = "visibility"
    const val IS_ENABLED = "isEnabled"

    const val NationalId = "nationalId"
    const val Year = "year"
    const val Month = "month"
    const val Week = "week"
    const val Days = "days"
    const val DOBString = "T00:00:00+00:00"

    const val SSP16 = 16
    const val MONTHS = "Months"
    const val YEARS = "Years"
    const val WEEKS = "Weeks"
    const val MONTH = "Month"
    const val YEAR = "Year"
    const val Country = "country"
    const val WEEK = "Week"
    const val value = "value"
    const val Other = "Other"
    const val DAY = "Day"
    const val DAYS = "Days"
    const val NoSymptoms = "No symptoms"
    const val NONE = "None"
    const val NA = "na"
    const val Information = "information"
    const val HouseholdHeadRelationship = "household_head_relationship"
    const val Title = "Title"
    const val AccordionGroup = "accordionGroup"
    const val ChipGroup = "chipGroup"
    const val OtherMethodSpecify = "Other Method (specify)"
    const val NCD = "NCD"
    const val IMMUNISATION = "IMMUNISATION"

    const val SPINNER_VALUE = "_"

    const val DEFAULT_ID = "-1"

    const val OPTIONAL_DATA = "optionalData"

    const val BP_LOG = "bpLog"

    /**
     * Returns true if a given name starts with no symptoms or equals none
     */
    fun isNoSymptom(name: String) =
        name.startsWith(NoSymptoms, true) ||
            name.equals(NONE, true)
}
