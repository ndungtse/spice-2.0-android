package com.medtroniclabs.spice.ncd.screening.utils

interface DuplicationNudgeInterface {
    fun proceedEnrollment(patientTrackerId: Long?)

    fun proceedAssessment(patientTrackerId: Long?)
}