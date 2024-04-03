package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import javax.inject.Inject

class MedicalReviewBaseViewModel @Inject constructor() : ViewModel() {

    val timeOfDeliveryMap= HashMap<String, Any>()
    val timeOfLabourOnsetMap= HashMap<String, Any>()

    fun getComplaintsList(): ArrayList<ChipViewItemModel> {
        val listItem =  listOf("Headache", "Abdominal Pain", "Difficulty in breathing", "Chest Pain", "Body Swelling", "Joint/Back Ache", "Injury/cut/bruise", "Eye pain/discharge/itchiness/difficulty seeing","Skin rash/itchiness", "Burns", "Genital pain/swelling/discharge/bleeding", "Pain on passing urine", "Toothache/gum swelling", "None", "Other")
        var complaintList = ArrayList<ChipViewItemModel>()
        for (i in listItem.indices){
            complaintList.add(
                ChipViewItemModel(
                    name = listItem[i],
                    cultureValue = listItem[i],
                    type = "Complaints"
                )
            )
        }
        return complaintList
    }

    fun getExamsList(): List<ChipViewItemModel> {
        val listItem = listOf("Eye Exam", "Oral Exam", "Cardiovascular", "Respiratory system", "Abdominal Pelvic", "Foot Exam", "Neurological Exam")
        var examList = ArrayList<ChipViewItemModel>()
        for (i in listItem.indices){
            examList.add(
                ChipViewItemModel(
                    name = listItem[i],
                    cultureValue = listItem[i],
                    type = "Examinations"
                )
            )
        }
        return examList
    }
}