package org.medtroniclabs.uhis.ui.assessment.utils

import org.medtroniclabs.uhis.common.DateUtils
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

    fun addDateAndTimeForGlucose(resultMap: HashMap<String, Any>): Triple<String, String, Double> {
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
}
