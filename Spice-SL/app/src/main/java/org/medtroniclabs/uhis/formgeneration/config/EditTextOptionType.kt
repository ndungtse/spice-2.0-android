package org.medtroniclabs.uhis.formgeneration.config

/**
 * Possible option types for [ViewType.VIEW_TYPE_FORM_EDITTEXT]
 */
object EditTextOptionType {
    /**
     * If the input field for phone number, then render it without country code
     */
    const val PHONE_NUMBER_WITHOUT_COUNTRY_CODE = "phoneNumberWithoutCountryCode"

    /**
     * If the input field for person name then allow ^[A-Za-z]+([ .'-][A-Za-z]+)*$
     */
    const val PERSON_NAME = "personName"
}
