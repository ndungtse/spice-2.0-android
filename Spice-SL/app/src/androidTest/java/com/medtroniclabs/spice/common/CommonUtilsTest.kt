package com.medtroniclabs.spice.common

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.data.CountryModel
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.OrganizationModel
import com.medtroniclabs.spice.data.UserRole
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import org.junit.Test
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CommonUtilsTest {

    private var context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun getIntegerOrNull_int_returnInt() {
        val expectedResult = 1
        val result = CommonUtils.getIntegerOrNull(1)
        assert(expectedResult == result)
    }

    @Test
    fun getIntegerOrNull_string_returnNull() {
        val expectedResult = null
        val result = CommonUtils.getIntegerOrNull("string")
        assert(expectedResult == result)
    }
    @Test
    fun getIntegerOrNull_string_returnInt() {
        val expectedResult = 1
        val result = CommonUtils.getIntegerOrNull("1")
        assert(expectedResult == result)
    }

    @Test
    fun getIntegerOrNull_float_returnNull() {
        val expectedResult = null
        val result = CommonUtils.getIntegerOrNull(1.0f)
        assert(expectedResult == result)
    }

    @Test
    fun getLongOrNull_long_returnLong() {
        val expectedResult = 1L
        val result = CommonUtils.getLongOrNull(1L)
        assert(expectedResult == result)
    }

    @Test
    fun getLongOrNull_string_returnNull() {
        val expectedResult = null
        val result = CommonUtils.getLongOrNull("1L")
        assert(expectedResult == result)
    }

    @Test
    fun getLongOrNull_string_returnLong() {
        val expectedResult = 1.toLong()
        val result = CommonUtils.getLongOrNull("1")
        assert(expectedResult == result)
    }

    @Test
    fun getLongOrNull_null_returnNull() {
        val expectedResult = null
        val result = CommonUtils.getLongOrNull(null)
        assert(expectedResult == result)
    }

    @Test
    fun getLongOrNull_double_returnNull() {
        val expectedResult = 100.50.toLong()
        val result = CommonUtils.getLongOrNull(100.50)
        assert(expectedResult == result)
    }

    @Test
    fun getStringOrEmptyString_string_returnString() {
        val expectedResult = "1L"
        val result = CommonUtils.getStringOrEmptyString("1L")
        assert(expectedResult == result)
    }

    @Test
    fun getStringOrEmptyString_string_returnNull() {
        val expectedResult = ""
        val result = CommonUtils.getStringOrEmptyString(1)
        assert(expectedResult == result)
    }

    @Test
    fun getAgeFromDob_dateOfBirth_returnAge() {
        val expectedResult = "34"
        val dateOfBirth = ZonedDateTime.now().minusYears(34)
        val dateOfBirthString =
            dateOfBirth.format(DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        val result = CommonUtils.getAgeFromDob(dateOfBirthString, "months")
        assert(expectedResult == result)
    }

    @Test
    fun getAgeFromDob_dateOfBirth_returnNull() {
        val expectedResult = ""
        val result = CommonUtils.getAgeFromDob(null, "months")
        assert(expectedResult == result)
    }

    @Test
    fun getAgeFromDob_dateOfBirth_returnAgeInMonths() {
        val dateOfBirth = ZonedDateTime.now().minusYears(1)
            .format(DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        val expectedResult = "12 months"
        val result = CommonUtils.getAgeFromDob(dateOfBirth, "months")
        assert(expectedResult == result)
    }

    @Test
    fun getGenderText_stringMale_returnMale() {
        val expectedResult = context.getString(R.string.male_prefix)
        val result = CommonUtils.getGenderText("male", context = context)
        assert(expectedResult == result)
    }

    @Test
    fun getGenderText_stringMale_returnFemale() {
        val expectedResult = context.getString(R.string.female_prefix)
        val result = CommonUtils.getGenderText("female", context = context)
        assert(expectedResult == result)
    }

    @Test
    fun displayAge_dob_returnUnderScore() {
        val expectedResult = "-"
        val resultHashMap =
            hashMapOf<String, Any>(Pair("Year", 0), Pair("Month", 0), Pair("Week", 0))
        val result = CommonUtils.displayAge(resultHashMap, context = context)
        assert(expectedResult == result)
    }

    @Test
    fun displayAge_years_returnYears() {
        val resultHashMap = hashMapOf<String, Any>(Pair("year", 7))
        val expectedResult = "7 Years "
        val result = CommonUtils.displayAge(resultHashMap, context = context)
        assert(expectedResult == result)
    }

    @Test
    fun displayAge_months_returnMonths() {
        val resultHashMap = hashMapOf<String, Any>(Pair("month", 2))
        val expectedResult = "2 Months "
        val result = CommonUtils.displayAge(resultHashMap, context = context)
        assert(expectedResult == result)
    }

    @Test
    fun displayAge_weeks_returnWeeks() {
        val resultHashMap = hashMapOf<String, Any>(Pair("week", 2))
        val expectedResult = "2 Weeks"
        val result = CommonUtils.displayAge(resultHashMap, context = context)
        assert(expectedResult == result)
    }

    @Test
    fun displayAge_weeks_returnYearOfMonths() {
        val resultHashMap = hashMapOf<String, Any>(Pair("year", 2), Pair("month", 6))
        val expectedResult = "30 Months "
        val result = CommonUtils.displayAge(resultHashMap, context = context)
        assert(expectedResult == result)
    }

    @Test
    fun getOptionMap_valueString_returnMap() {
        val testResult = "test"
        val expectedResult = mapOf<String, Any>(
            Pair(DefinedParams.ID, "test"), Pair(DefinedParams.NAME, "test")
        )
        val result = CommonUtils.getOptionMap(testResult)
        assert(expectedResult == result)
    }

    @Test
    fun getBooleanAsString_true_returnYes() {
        val expectedResult = HouseHoldRegistration.yes
        val result = CommonUtils.getBooleanAsString(true)
        assert(expectedResult == result)
    }

    @Test
    fun getBooleanAsString_true_returnNo() {
        val expectedResult = HouseHoldRegistration.no
        val result = CommonUtils.getBooleanAsString(false)
        assert(expectedResult == result)
    }

    @Test
    fun convertListToString_listOfString_returnStringWithSeparator() {
        val expectedResult = "test, test1"
        val testString = ArrayList<String>()
        testString.add("test")
        testString.add("test1")
        val result = CommonUtils.convertListToString(testString)
        assert(expectedResult == result)
    }

    @Test
    fun isChw_returnBoolean() {
        val mockLoginResponse = getMockLogInResponseDetails()
        SecuredPreference.putUserDetails(mockLoginResponse)
        val result: Boolean = CommonUtils.isChw()
        assert(result)
    }

    private fun getMockLogInResponseDetails(): LoginResponse {
        return LoginResponse(
            username = "chw@test.com",
            isActive = true,
            roles = listOf(UserRole(id = 2, name = "CHW", level = 2, authority = "CHW")),
            id = 2L,
            authorization = null,
            deviceInfoId = 1L,
            countryCode = "21",
            country = CountryModel(
                id = 1, name = "SL", phoneNumberCode = "21", unitMeasurement = null, tenantId = 1
            ),
            currentDate = 1712745342610,
            timezone = null,
            tenantId = 9L,
            cultureId = null,
            organizations = arrayListOf(
                OrganizationModel(id = 12, name = "Milton Camp"),
                OrganizationModel(id = 9, name = "Bomali"),
                OrganizationModel(id = 5, name = "CM Healthcare")
            ),
            isSuperUser = true,
            suiteAccess = listOf("mob", "admin"),
            client = "mob"
        )
    }

    @Test
    fun isProvider_returnBoolean() {
        val mockLoginResponse = getMockProviderLogInResponseDetails()
        SecuredPreference.putUserDetails(mockLoginResponse)
        val result: Boolean = CommonUtils.isProvider()
        assert(result)
    }

    private fun getMockProviderLogInResponseDetails(): LoginResponse {
        return LoginResponse(
            username = "chw@test.com",
            isActive = true,
            roles = listOf(UserRole(id = 2, name = "PROVIDER", level = 2, authority = "PROVIDER")),
            id = 2L,
            authorization = null,
            deviceInfoId = 1L,
            countryCode = "21",
            country = CountryModel(
                id = 1, name = "SL", phoneNumberCode = "21", unitMeasurement = null, tenantId = 1
            ),
            currentDate = 1712745342610,
            timezone = null,
            tenantId = 9L,
            cultureId = null,
            organizations = arrayListOf(
                OrganizationModel(id = 12, name = "Milton Camp"),
                OrganizationModel(id = 9, name = "Bomali"),
                OrganizationModel(id = 5, name = "CM Healthcare")
            ),
            isSuperUser = true,
            suiteAccess = listOf("mob", "admin"),
            client = "mob"
        )
    }

    @Test
    fun getIsBooleanFromString_string_returnTrue() {
        val result = CommonUtils.getIsBooleanFromString("yes")
        assert(result)
    }

    @Test
    fun getIsBooleanFromString_string_returnFalse() {
        val result = CommonUtils.getIsBooleanFromString("no")
        assert(!result)
    }

    @Test
    fun getIsBooleanFromString_int_returnFalse() {
        val result = CommonUtils.getIsBooleanFromString(1)
        assert(!result)
    }

}