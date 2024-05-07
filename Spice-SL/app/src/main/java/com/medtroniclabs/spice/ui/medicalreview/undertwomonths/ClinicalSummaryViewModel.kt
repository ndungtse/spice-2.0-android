package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import ClinicalSummaryAndSigns
import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.MeasurementDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClinicalSummaryViewModel @Inject constructor() : ViewModel() {

    val resultBreastFeedingHashMap = HashMap<String, Any>()
    val resultMotherVitaminHashMap = HashMap<String, Any>()
    val exclusiveBreastFeedHashMap = HashMap<String, Any>()
    var selectedImmunisationStatus: String? = null
    var clinicalSummaryAndSigns = ClinicalSummaryAndSigns()

    fun updateWeight(weight: String) {
        val isEmpty = weight.isEmpty()
        val weightDouble = if (isEmpty) null else weight.toDouble()
        val weightUnit = if (isEmpty) null else MeasurementDefinedParams.Kilogram
        clinicalSummaryAndSigns =
            clinicalSummaryAndSigns.copy(weight = weightDouble, weightUnit = weightUnit)
    }

    fun updateVitaminAForMother() {
        if (resultMotherVitaminHashMap.containsKey(MedicalReviewDefinedParams.MOTHER_VITAMIN_TAG)) {
            val vitaminA =
                resultMotherVitaminHashMap[MedicalReviewDefinedParams.MOTHER_VITAMIN_TAG] as String
            clinicalSummaryAndSigns = if (vitaminA == DefinedParams.Yes) {
                clinicalSummaryAndSigns.copy(vitAForMother = true)
            } else {
                clinicalSummaryAndSigns.copy(vitAForMother = false)
            }
        }
    }

    fun updateBreastFeeding() {
        if (resultMotherVitaminHashMap.containsKey(MedicalReviewDefinedParams.BREAST_FEEDING_TAG)) {
            val breastFeeding =
                resultBreastFeedingHashMap[MedicalReviewDefinedParams.BREAST_FEEDING_TAG] as String
            clinicalSummaryAndSigns = if (breastFeeding == DefinedParams.Yes) {
                clinicalSummaryAndSigns.copy(breastFeeding = true)
            } else if (breastFeeding == DefinedParams.No) {
                clinicalSummaryAndSigns.copy(breastFeeding = false)
            } else {
                clinicalSummaryAndSigns.copy(breastFeeding = null)
            }
        }
    }

    fun updateExclusiveBreastFeeding() {
        if (resultMotherVitaminHashMap.containsKey(MedicalReviewDefinedParams.EXCLUSIVE_BREAST_FEED_TAG)) {
            val exclusiveBreastFeeding =
                resultBreastFeedingHashMap[MedicalReviewDefinedParams.EXCLUSIVE_BREAST_FEED_TAG] as String
            clinicalSummaryAndSigns = if (exclusiveBreastFeeding == DefinedParams.Yes) {
                clinicalSummaryAndSigns.copy(exclusiveBreastFeeding = true)
            } else if (exclusiveBreastFeeding == DefinedParams.No) {
                clinicalSummaryAndSigns.copy(exclusiveBreastFeeding = false)
            } else {
                clinicalSummaryAndSigns.copy(exclusiveBreastFeeding = null)
            }
        }
    }

    fun updateHeight(height: String) {
        val isEmpty = height.isEmpty()
        val heightDouble = if (isEmpty) null else height.toDouble()
        val heightUnit = if (isEmpty) null else MeasurementDefinedParams.Centimeter
        clinicalSummaryAndSigns =
            clinicalSummaryAndSigns.copy(height = heightDouble, heightUnit = heightUnit)
    }

    fun updateWaz(waz: String) {
        val wazDouble = if (waz.isEmpty()) null else waz.toDouble()
        clinicalSummaryAndSigns = clinicalSummaryAndSigns.copy(waz = wazDouble)
    }

    fun updateWhz(whz: String) {
        val whzDouble = if (whz.isEmpty()) null else whz.toDouble()
        clinicalSummaryAndSigns = clinicalSummaryAndSigns.copy(whz = whzDouble)
    }

    fun updateTemperature(temperature: String) {
        val isEmpty = temperature.isEmpty()
        val temperatureInt = if (isEmpty) null else {
            temperature.toInt()
        }
        val temperatureUnit = if (isEmpty) null else MeasurementDefinedParams.Celsius
        clinicalSummaryAndSigns = clinicalSummaryAndSigns.copy(
            temperature = temperatureInt,
            temperatureUnit = temperatureUnit
        )
    }

    fun updateImmunisationStatus() {
        clinicalSummaryAndSigns =
            clinicalSummaryAndSigns.copy(immunisationStatus = selectedImmunisationStatus)
    }

    fun updateRespiratoryRate(rate: String, repeatRate: String) {
        val rateInt = if (rate.isEmpty()) null else rate.toInt()
        val repeatInt = if (repeatRate.isEmpty()) null else repeatRate.toInt()
        clinicalSummaryAndSigns =
            clinicalSummaryAndSigns.copy(respirationRate = listOf(rateInt, repeatInt))
    }
}