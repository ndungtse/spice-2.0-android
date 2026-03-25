package org.medtroniclabs.uhis.ui.assessment.utils

import android.content.Context
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.formgeneration.model.BPModel
import org.medtroniclabs.uhis.ui.MenuConstants
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.AVG_BLOOD_PRESSURE
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.AVG_DIASTOLIC
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.AVG_SYSTOLIC
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.BP_LOG
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.BP_LOG_DETAILS
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.FBS
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.GLUCOSE
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.GLUCOSE_LOG
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.GLUCOSE_TYPE
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.GLUCOSE_UNIT
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.Glucose_Date_Time
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.HBA1C_DATE_TIME
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.MMOLL
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.NAME
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.NCD_SYMPTOMS
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.SYMPTOMS_LOG
import org.medtroniclabs.uhis.ui.assessment.rmnch.RMNCH
import org.medtroniclabs.uhis.ui.assessment.statuslogic.AssessmentStatus
import java.util.Locale
import kotlin.math.roundToInt

object AssessmentUtil {
    fun calculateAverageBloodPressure(resultMap: HashMap<String, Any>): Pair<Int, Int> {
        val bpLogs = resultMap[BP_LOG] as HashMap<String, Any>

        val bpLogDetail = bpLogs[BP_LOG_DETAILS] as? List<*> ?: return Pair(0, 0)

        val validReadings = bpLogDetail.mapNotNull { entry ->
            val sys = getSystolicValue(entry)
            val dia = getDiastolicValue(entry)

            if (sys > 0 && dia > 0) {
                sys to dia
            } else {
                null
            }
        }

        if (validReadings.isEmpty()) {
            bpLogs.remove(BP_LOG_DETAILS)
            return Pair(0, 0)
        }

        val avgSys = validReadings.map { it.first }.average()
        val avgDia = validReadings.map { it.second }.average()

        val finalSys = avgSys.roundToInt()
        val finalDia = avgDia.roundToInt()

        bpLogs[AVG_SYSTOLIC] = finalSys
        bpLogs[AVG_DIASTOLIC] = finalDia
        bpLogs[AVG_BLOOD_PRESSURE] = "$finalSys/$finalDia"
        bpLogs[BP_LOG_DETAILS] = bpLogDetail

        resultMap[BP_LOG] = bpLogs

        return Pair(finalSys, finalDia)
    }

    private fun getSystolicValue(map: Any?): Double = (map as? BPModel)?.systolic ?: 0.0

    private fun getDiastolicValue(map: Any?): Double = (map as? BPModel)?.diastolic ?: 0.0

    fun addDateAndTimeForGlucose(resultMap: HashMap<String, Any>): Triple<String?, String?, Double?> {
        if (resultMap.containsKey(GLUCOSE_LOG)) {
            val glucoseLogs = resultMap[GLUCOSE_LOG] as HashMap<String, Any>

            var unitType = MMOLL // Default unit type
            glucoseLogs[GLUCOSE_UNIT]?.let {
                unitType = it as String
            }

            var bgType = FBS // Default bg type
            glucoseLogs[GLUCOSE_TYPE]?.let {
                bgType = it as String
            }

            var bgValue = 0.0 // Default bg value
            glucoseLogs[GLUCOSE]?.let {
                bgValue = it as Double
            }

            val dateTime = DateUtils.getTodayDateDDMMYYYY()
            glucoseLogs[Glucose_Date_Time] = dateTime
            glucoseLogs[HBA1C_DATE_TIME] = dateTime

            resultMap[GLUCOSE_LOG] = glucoseLogs

            return Triple(unitType, bgType, bgValue)
        }
        return Triple(null, null, null)
    }

    fun getSymptomsList(resultMap: HashMap<String, Any>): List<String> {
        val list = mutableListOf<String>()

        val symptomsLogs = resultMap[SYMPTOMS_LOG] as HashMap<String, Any>
        if (symptomsLogs.containsKey(NCD_SYMPTOMS)) {
            val ncdSymptoms = symptomsLogs[NCD_SYMPTOMS] as List<*>

            ncdSymptoms.forEach {
                val symptom = it as HashMap<String, Any>
                if (symptom.containsKey(NAME)) {
                    list.add(symptom[NAME] as String)
                }
            }
        }

        return list
    }

    /**
     * Maps given service to respective service name, if no mapping found then returns upper case of service
     *
     * e.g, pwProfile -> Pregnant Women Registration
     */
    fun mapServiceToServiceName(service: String): String =
        when (service.lowercase()) {
            MenuConstants.PREGNANT_WOMEN_PROFILE.lowercase() -> "Pregnant Women Registration"
            MenuConstants.FP_MENU_ID.lowercase() -> "Family Planning"
            MenuConstants.EYE_CARE_MENU_ID.lowercase() -> "Eye Care"
            MenuConstants.CATARACT_MENU_ID.lowercase() -> "Cataract"
            MenuConstants.PREGNANCY_OUTCOME.lowercase() -> "Pregnancy Outcome"
            RMNCH.ANC.lowercase() -> "ANC"
            RMNCH.pnc_mother_key.lowercase() -> "PNC"
            RMNCH.ChildHoodVisit.lowercase() -> "Child Visit"
            else -> service.uppercase(Locale.ENGLISH)
        }

    /**
     * Returns referral status based on service
     */
    fun getReferralStatus(
        context: Context,
        service: String,
        referralStatus: String?,
    ): String =
        when (service.lowercase()) {
            RMNCH.ANC.lowercase(),
            RMNCH.pnc_mother_key.lowercase(),
            -> {
                referralStatus?.uppercase(Locale.ENGLISH)
            }

            else -> context.getString(R.string.na)
        } ?: run { context.getString(R.string.separator_double_hyphen) }

    /**
     * Returns display follow-up date based on service
     */
    fun getNextFollowUpDate(
        context: Context,
        service: String,
        followUpDate: String?,
    ): String =
        when (service.lowercase()) {
            RMNCH.ANC.lowercase(),
            RMNCH.pnc_mother_key.lowercase(),
            -> {
                followUpDate?.let {
                    DateUtils.convertDateFormat(
                        it,
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DATE_ddMMyyyy,
                    )
                }
            }

            else -> context.getString(R.string.na)
        } ?: run { context.getString(R.string.separator_double_hyphen) }

    /**
     * Maps corresponding status to display value
     */
    fun mapAssessmentStatus(status: String): String {
        val assessmentStatus = try {
            AssessmentStatus.valueOf(status)
        } catch (_: Exception) {
            AssessmentStatus.DEFAULT
        }
        return when (assessmentStatus) {
            AssessmentStatus.NORMAL_PREGNANCY -> {
                "Normal Pregnancy"
            }

            AssessmentStatus.HIGH_RISK_PW -> {
                "High Risk PW"
            }

            AssessmentStatus.USING_MODERN_FP -> {
                "Using modern family planning methods"
            }

            AssessmentStatus.NOT_USING_MODERN_FP -> {
                "Not using modern family planning methods"
            }

            AssessmentStatus.GAPS_IN_ANC -> {
                "Gaps IN ANC"
            }

            AssessmentStatus.C_SECTION -> {
                "C-Section"
            }

            AssessmentStatus.ASSISTED_DELIVERY -> {
                "Assisted Delivery"
            }

            AssessmentStatus.NORMAL_DELIVERY -> {
                "Normal Delivery"
            }

            AssessmentStatus.NEONATAL_DEATH -> {
                "Neonatal Death"
            }

            AssessmentStatus.ABORTION -> {
                "Abortion"
            }

            AssessmentStatus.STILL_BIRTH -> {
                "Still Birth"
            }

            AssessmentStatus.LIVE_BIRTH -> {
                "Live Birth"
            }

            AssessmentStatus.HIGH_RISK_PNC -> {
                "High Risk PNC"
            }

            AssessmentStatus.NORMAL_PNC -> {
                "Normal PNC"
            }

            AssessmentStatus.GAPS_IN_PNC -> {
                "Gaps IN PNC"
            }

            AssessmentStatus.DEFAULT -> {
                status.uppercase(Locale.ENGLISH)
            }
        }
    }

    /**
     * Returns newborn details from the map as list
     */
    fun findNewbornDetailsFromMap(map: Map<String, Any?>): List<*>? {
        val directList = map["newbornDetails"]
        if (directList is List<*>) return directList

        for (entry in map.entries) {
            if (entry.value is Map<*, *>) {
                val nestedMap = entry.value as Map<*, *>
                val nestedList = nestedMap["newbornDetails"]
                if (nestedList is List<*>) return nestedList
            }
        }
        return null
    }
}
