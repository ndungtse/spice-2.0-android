package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class MedicalReviewRequestResponse(
    var initialMedicalReview: InitialMedicalReview? = null,
    var continuousMedicalReview: ContinuousMedicalReview? = null,
    val isPregnant: Boolean? = null,
    val patientReference: String? = null,
    val encounterReference: String? = null,
    var provenance: ProvanceDto? = null,
    val memberReference: String? = null
)

data class InitialMedicalReview(
    var comorbidities: List<Chip>? = null,
    var complications: List<Chip>? = null,
    var lifestyle: List<Chip>? = null
)

data class ContinuousMedicalReview(
    val physicalExams: List<Chip>? = null,
    val complaints: List<Chip>? = null,
    val complaintComments: String? = null,
    val physicalExamComments: String? = null,
    var clinicalNote: String? = null
)

data class Chip(
    val id: Long? = null,
    val name: String? = null,
    val value: String? = null,
    val other: Boolean? = null,
    val comments: String? = null,
    val answer: Answer? = null
)

data class Answer(
    var name: String? = null,
    val value: String? = null
)

data class MedicalReviewResponse(val message: String)