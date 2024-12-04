package com.medtroniclabs.spice.ui.medicalreview.investigation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.CodeDetailsObject
import com.medtroniclabs.spice.data.EncounterDetails
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
import com.medtroniclabs.spice.model.RemoveLabTestRequest
import com.medtroniclabs.spice.model.medicalreview.InvestigationModel
import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse
import com.medtroniclabs.spice.model.medicalreview.SearchRequestLabTest
import com.medtroniclabs.spice.ncd.data.LabTestPredictionResponse
import com.medtroniclabs.spice.ncd.data.PredictionRequest
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.InvestigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestigationViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val investigationRepository: InvestigationRepository
) : ViewModel() {

    var patientId: String? = null
    var encounterId: String? = null
    var visitId: String? = null
    var origin: String? = null

    val investigationSearchResponseListLiveData =
        MutableLiveData<Resource<ArrayList<SearchLabTestResponse>>>()

    val investigationListLiveData = MutableLiveData<ArrayList<InvestigationModel>>()

    val createLabTestLiveData = MutableLiveData<Resource<Map<String, Any>>>()

    val labTestListLiveData = MutableLiveData<Resource<ArrayList<LabTestListResponse>>>()

    val removeLabTestLiveData = MutableLiveData<Resource<Map<String, Any>>>()

    val labTestPredictionLivdata = MutableLiveData<Resource<LabTestPredictionResponse?>>()

    val markAsReviewedLiveData = MutableLiveData<Resource<APIResponse<HashMap<String, Any>>>>()

    fun searchInvestigationByName(searchTerm: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                investigationSearchResponseListLiveData.postLoading()
                val request = SearchRequestLabTest(searchTerm)
                val response = investigationRepository.searchInvestigationByName(request)
                response.data?.let {
                    investigationSearchResponseListLiveData.postSuccess(it)
                }
            } catch (e: Exception) {
                investigationSearchResponseListLiveData.postError()
            }
        }
    }

    fun addInvestigationModelToUI(investigationResponse: SearchLabTestResponse) {
        viewModelScope.launch(dispatcherIO) {
            val investigationList = investigationListLiveData.value ?: ArrayList()
            if (investigationResponse.formInput != null) {
                investigationList.forEach { it.dropdownState = false }
                try {
                    investigationList.add(
                        InvestigationModel(
                            investigationResponse.testName,
                            getInvestigationModelWithResult(investigationResponse),
                            SecuredPreference.getUserDetails()?.firstName ?: "",
                            SecuredPreference.getUserFhirId(),
                            DateUtils.getTodayDateDDMMYYYY(),
                            null,
                            investigationResponse.codeDetails,
                            dropdownState = true
                        )
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    investigationListLiveData.postValue(investigationList)
                }
            } else {
                investigationListLiveData.postValue(investigationList)
            }
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

    fun removeInvestigationModel(investigation: InvestigationModel) {
        viewModelScope.launch(dispatcherIO) {
            if (investigation.id != null) {
                removeLabTestLiveData.postLoading()
                try {
                    val resourceState =
                        investigationRepository.removeLabTest(RemoveLabTestRequest(investigation.id))
                    resourceState.data?.let {
                        removeLabTestLiveData.postSuccess(it)
                    } ?: kotlin.run {
                        removeLabTestLiveData.postError()
                    }
                } catch (e: Exception) {
                    removeLabTestLiveData.postError()
                }
            } else {
                val list = investigationListLiveData.value
                list?.let {
                    it.remove(investigation)
                    investigationListLiveData.postValue(it)
                }
            }
        }
    }

    fun removeInvestigationByID(id: String) {
        val list = investigationListLiveData.value
        list?.let { list ->
            list.removeIf { it.id == id }
            investigationListLiveData.postValue(list)
        }
    }


    fun createLabTest(
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
                        visitId = visitId
                    ),
                    labTestList
                )
                createLabTestLiveData.postValue(investigationRepository.createLabTest(request))
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


    fun getLabTestList(data: PatientListRespModel) {
        viewModelScope.launch(dispatcherIO) {
            val patientId = if (CommonUtils.isNonCommunity() ) data.patientId else data.id
            patientId?.let { id ->
                labTestListLiveData.postLoading()
                val response = investigationRepository.getLabTestList(LabTestListRequest(id))
                response.data?.let {
                    labTestListLiveData.postSuccess(it)
                } ?: kotlin.run {
                    labTestListLiveData.postError()
                }
            }
        }
    }


    fun addExistingLabTestListToUI(list: ArrayList<LabTestListResponse>) {
        viewModelScope.launch(dispatcherIO) {
            val investigationList = investigationListLiveData.value ?: ArrayList()
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

    fun getLabTestNudgeList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                val response = investigationRepository.getLabTestNudgeList(
                    predictionRequest = PredictionRequest(patientReference = patientId)
                )
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true) {
                        labTestPredictionLivdata.postSuccess(res.entity)
                    }
                }
            } catch (e: Exception) {
                //error Block
            }
        }
    }

    fun markAsReviewed(request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            markAsReviewedLiveData.postLoading()
            markAsReviewedLiveData.postValue(investigationRepository.markAsReviewed(request))
        }
    }
}