package com.medtroniclabs.spice.ui.medicalreview.labTechnician

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.RoleConstant
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.CodeDetailsObject
import com.medtroniclabs.spice.data.EncounterDetails
import com.medtroniclabs.spice.data.Role
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.model.LabTestCreateRequest
import com.medtroniclabs.spice.model.LabTestDetails
import com.medtroniclabs.spice.model.LabTestListRequest
import com.medtroniclabs.spice.model.LabTestListResponse
import com.medtroniclabs.spice.model.LabTestResultObject
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.medicalreview.InvestigationModel
import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDLabTestViewModel @Inject constructor(
    private val labTestRepository: NCDLabTestRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    val labTestListLiveData = MutableLiveData<Resource<ArrayList<LabTestListResponse>>>()
    val investigationListLiveData = MutableLiveData<ArrayList<InvestigationModel>>()
    val createLabTestLiveData = MutableLiveData<Resource<APIResponse<Map<String, Any>>>>()
    var encounterId: String? = null

    fun getLabTestList(data: PatientListRespModel) {
        viewModelScope.launch(dispatcherIO) {
            val patientId = if (CommonUtils.isNonCommunity() ) data.patientId else data.id
            patientId?.let { id ->
                labTestListLiveData.postLoading()
                val response = labTestRepository.getLabTestList(LabTestListRequest(id, roleName = roleName()))
                response.data?.let {
                    labTestListLiveData.postSuccess(it)
                } ?: kotlin.run {
                    labTestListLiveData.postError()
                }
            }
        }
    }

    private fun roleName(): String? {
        return when {
            CommonUtils.isNCDProvider() -> RoleConstant.PROVIDER
            CommonUtils.isLabTechnician() -> RoleConstant.LAB_TECHNICIAN
            else -> null
        }
    }

    fun addExistingLabTestListToUI(list: ArrayList<LabTestListResponse>) {
        viewModelScope.launch(dispatcherIO) {
            val investigationList = ArrayList<InvestigationModel>()
            list.forEach { investigationExisting ->
                investigationList.add(
                    InvestigationModel(
                        testName = investigationExisting.testName,
                        recommendedBy = investigationExisting.recommendedBy,
                        recommendedOn = investigationExisting.recommendedOn,
                        recommendedByName = investigationExisting.recommendedName,
                        labTestResultList = investigationExisting.labTestResults,
                        id = investigationExisting.id,
                        resultList = getInvestigationModelWithResult(investigationExisting.labTestCustomization),
                        dropdownState = false
                    )
                )
            }
            investigationListLiveData.postValue(investigationList)
        }
    }

    private fun getInvestigationModelWithResult(investigationResponse: SearchLabTestResponse?): FormResponse? {
        if (investigationResponse != null) {
            try {
                val formFieldsType = object : TypeToken<FormResponse>() {}.type
                val formResponse: FormResponse =
                    Gson().fromJson(investigationResponse.formInput, formFieldsType)
                return formResponse
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun updateLabTest(
        resultFromInvestigation: List<InvestigationModel>?,
        patientDetail: PatientListRespModel
    ) {
        viewModelScope.launch(dispatcherIO) {
            try {
                createLabTestLiveData.postLoading()
                val labTestList = ArrayList<LabTestDetails>()
                resultFromInvestigation?.forEach { data ->
                    val detail = LabTestDetails(
                        testName = data.testName,
                        recommendedOn = data.recommendedOn,
                        recommendedBy = data.recommendedBy,
                        recommendedName = data.recommendedByName,
                        codeDetails = data.codeDetails,
                        labTestResults = getResultListObject(data),
                        id = data.id
                    )
                    labTestList.add(detail)
                }

                val request = LabTestCreateRequest(
                    EncounterDetails(
                        id = encounterId,
                        patientReference = if (CommonUtils.isNonCommunity() ) patientDetail.patientId.orEmpty() else patientDetail.id.orEmpty(),
                        patientId = if (CommonUtils.isNonCommunity() ) null else patientDetail.patientId.orEmpty(),
                        memberId = if (!CommonUtils.isNonCommunity() ) patientDetail.memberId.orEmpty() else patientDetail.id.orEmpty(),
                        provenance = ProvanceDto(),
                    ),
                    labTestList
                )
                createLabTestLiveData.postLoading()
                createLabTestLiveData.postValue(labTestRepository.updateLabTest(request))
            } catch (e: Exception) {
                createLabTestLiveData.postError()
            }
        }
    }

    private fun getResultListObject(data: InvestigationModel): ArrayList<LabTestResultObject>? {
        val labTestResultObjectList = ArrayList<LabTestResultObject>()
        var testedOn: String? = null
        data.resultHashMap?.let { map ->
            if (map.containsKey(DefinedParams.TestedOn) && map[DefinedParams.TestedOn] is String) {
                testedOn = map[DefinedParams.TestedOn] as String
            }
            data.resultList?.formLayout?.filter { it.viewType != ViewType.VIEW_TYPE_FORM_CARD_FAMILY && it.id != DefinedParams.TestedOn }
                ?.let { formLayoutList ->
                    getValidResultObject(formLayoutList, map, testedOn)?.let {
                        labTestResultObjectList.addAll(it)
                    }
                }
        }

        return if (labTestResultObjectList.size > 0) {
            labTestResultObjectList
        } else {
            null
        }
    }

    private fun getValidResultObject(
        formLayoutList: List<FormLayout>,
        resultMap: HashMap<String, Any>,
        testedOn: String?
    ): ArrayList<LabTestResultObject>? {
        val list = ArrayList<LabTestResultObject>()
        var validResultList = true
        formLayoutList.forEach { formData ->
            if (resultMap.containsKey(formData.id)) {
                val actualValue = resultMap[formData.id]
                var unitValue: String? = null
                if (resultMap.containsKey(formData.id + DefinedParams.Unit)) {
                    val unitValueAny = resultMap[formData.id + DefinedParams.Unit]
                    if (unitValueAny is String) {
                        unitValue = unitValueAny
                    }
                }
                actualValue?.let { value ->
                    val resultObject = LabTestResultObject(
                        name = formData.id,
                        value = value,
                        SecuredPreference.getUserFhirId(),
                        getCodeDetailsObject(formData),
                        testedOn = testedOn,
                        resource = formData.resource,
                        unitValue
                    )
                    list.add(resultObject)
                }
            } else {
                validResultList = false
            }
        }
        return if (validResultList) {
            list
        } else {
            null
        }
    }

    private fun getCodeDetailsObject(formData: FormLayout): CodeDetailsObject? {
        if (formData.code != null && formData.url != null) {
            return CodeDetailsObject(formData.code!!, formData.url!!)
        }
        return null
    }

}