package com.medtroniclabs.spice.ncd.medicalreview

import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel

object NCDMRUtil {
    const val MENU_ID = "MENU_ID"
    const val Complaints = "Complaints"
    const val Comorbidity = "Comorbidity"
    const val Complications = "Complications"
    const val LIFE_STYLE_SMOKE = "SMOKE"
    const val LIFE_STYLE_ALCOHOL = "ALCOHOL"
    const val LIFE_STYLE_NUT = "NUT"
    const val Yes_Currently = "Yes, currently"
    const val Yes_Past = "Yes, In the past"
    const val EncounterReference = "EncounterReference"
    const val PhysicalExamination = "PhysicalExamination"
    const val NCD = "NCD"
    const val MATERNAL_HEALTH = "MATERNALHEALTH"
    const val MENTAL_HEALTH = "MENTALHEALTH"
    const val IS_INITIAL_MR = "ISINITIALMR"
    const val IS_FEMALE = "ISFEMALE"
    const val CONFIRM_DIAGNOSIS_TYPE = "CONFIRM_DIAGNOSIS_TYPE"
    const val SUBSTANCE_DISORDER = "Substance Disorder"
    const val MENTALHEALTH = "Mental Health"
    const val HYPERTENSION = "Hypertension"
    const val DIABETES = "Diabetes"
    const val HIV = "HIV"
    const val PREGNANCY = "Pregnancy"
    const val PATIENT_REFERENCE = "PatientReference"
    const val MEMBER_REFERENCE = "MemberReference"
    const val CurrentMedication = "CurrentMedication"
    const val FrequencyTypes = "FrequencyTypes"
    const val DEFAULT = "default"


    fun validateInput(
        isMandatory: Boolean = false,
        chips: ArrayList<ChipViewItemModel>,
        etPhysicalExaminationComments: AppCompatEditText,
        tvErrorMessage: TextView
    ): Boolean {
        val hasChips = chips.isNotEmpty() // Check if there are any chips selected
        val hasOtherChip = chips.any {
            it.name.equals(
                DefinedParams.Other,
                ignoreCase = true
            )
        } // Check if 'Other' chip is present
        val commentsNotBlank =
            etPhysicalExaminationComments.text?.isNotBlank() == true // Check if the comments are not blank

        // If input is mandatory, additional validation is required
        if (isMandatory) {
            // If there are chips, we need to check further
            if (hasChips) {
                // If 'Other' chip is selected, check for non-blank comments
                if (hasOtherChip) {
                    return if (commentsNotBlank) {
                        tvErrorMessage.invisible() // Hide error if comments are valid
                        true
                    } else {
                        tvErrorMessage.visible() // Show error if comments are empty
                        false
                    }
                }
                // If no 'Other' chip is selected, input is valid
                tvErrorMessage.gone()
                return true
            } else {
                // If no chips are selected and mandatory, show error
                tvErrorMessage.visible()
                return false
            }
        }

        // If chips are empty and comments are blank, input is valid
        if (!hasChips && etPhysicalExaminationComments.text?.isBlank() == true) {
            tvErrorMessage.gone()
            return true
        }

        // If chips are not empty, check for 'Other' chip and non-blank comments
        if (hasChips) {
            if (hasOtherChip) {
                return if (commentsNotBlank) {
                    tvErrorMessage.invisible() // Hide error if comments are valid
                    true
                } else {
                    tvErrorMessage.visible() // Show error if comments are empty
                    false
                }
            }
            // If no 'Other' chip is selected, input is valid
            tvErrorMessage.gone()
            return true
        }

        return true // If no other conditions matched, input is considered valid
    }

    fun validateInputForCommentOption(
        isMandatory: Boolean = false,
        chips: ArrayList<ChipViewItemModel>,
        etPhysicalExaminationComments: AppCompatEditText,
        tvErrorMessage: TextView
    ): Boolean {
        val hasChips = chips.isNotEmpty() // Check if there are any chips selected
        val commentsNotBlank =
            etPhysicalExaminationComments.text?.isNotBlank() == true // Check if the comments are not blank

        // If input is mandatory, additional validation is required
        if (isMandatory) {
            // If there are chips, we need to check further
            if (hasChips || commentsNotBlank) {
                // If no 'Other' chip is selected, input is valid
                tvErrorMessage.gone()
                return true
            } else {
                // If no chips are selected and mandatory, show error
                tvErrorMessage.visible()
                return false
            }
        }

        // If chips are empty and comments are blank, input is valid
        if (!hasChips && etPhysicalExaminationComments.text?.isBlank() == true) {
            tvErrorMessage.gone()
            return true
        }

        // If chips are not empty, check for 'Other' chip and non-blank comments
        if (hasChips || commentsNotBlank) {
            tvErrorMessage.gone()
            return true
        }

        return true // If no other conditions matched, input is considered valid
    }
}