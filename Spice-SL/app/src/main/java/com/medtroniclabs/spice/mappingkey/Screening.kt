package com.medtroniclabs.spice.mappingkey

import com.medtroniclabs.spice.formgeneration.model.BPModel

object Screening {
    const val Height = "height"
    const val Weight = "weight"
    const val BMI = "bmi"
    const val firstName = "firstName"
    const val lastName = "lastName"
    const val nationalId = "nationalId"
    const val phoneNumber = "phoneNumber"
    const val BPAverageMinimumValue = 50.0
    const val BPAverageMaximumValue = 300.0
    const val PulseMinimumValue = 50.0
    const val PulseMaximumValue = 300.0
    const val Systolic = "systolic"
    const val Diastolic = "diastolic"
    const val Pulse = "pulse"
    const val Hour = "hour"
    const val Minute = "minute"
    const val AM_PM = "am/pm"
    const val Last_Meal_Date = "last_meal_date"
    const val Today = "Today"
    const val Yesterday = "Yesterday"
    const val AM = "AM"
    const val PM = "PM"
    const val PHQ4 = "PHQ4"
    const val Fetch_MH_Questions = "Fetch_MH_Questions"
    const val Questions = "questions"
    const val Answer = "answer"
    const val ModelAnswers = "modelAnswers"
    const val Display_Order = "displayOrder"
    const val select = "select"
    const val type = "type"
    const val Question_Id = "questionId"
    const val Answer_Id = "answerId"
    const val mentalHealthScore = "score"
    const val Value = "value"
    const val PHQ4_Mental_Health = "phq4MentalHealth"
    const val Mandatory = "mandatory"
    const val isMandatory = "isMandatory"

    fun getEmptyBPReading(size: Int): ArrayList<BPModel> {
        val list = ArrayList<BPModel>()
        for (i in 1..size) {
            list.add(BPModel())
        }
        return list
    }
}