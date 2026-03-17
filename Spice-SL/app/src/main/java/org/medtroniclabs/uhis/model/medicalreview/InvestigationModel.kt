package org.medtroniclabs.uhis.model.medicalreview

import org.medtroniclabs.uhis.data.CodeDetailsObject
import org.medtroniclabs.uhis.formgeneration.model.FormResponse
import org.medtroniclabs.uhis.model.LabTestResultObject

data class InvestigationModel(
    val testName: String,
    val resultList: FormResponse? = null,
    val recommendedByName: String? = null,
    val recommendedBy: String,
    val recommendedOn: String,
    var resultHashMap: HashMap<String, Any>? = null,
    val codeDetails: CodeDetailsObject? = null,
    var dataError: Boolean = true,
    var labTestResultList: ArrayList<LabTestResultObject>? = null,
    val id: String? = null,
    var dropdownState: Boolean = false,
    val isReview: Boolean? = null,
    val components: ArrayList<Map<String, Any?>>? = null,
    val comments: String? = null,
    val descriptiveResult: String? = null,
    val labTestId: Long? = null,
)
