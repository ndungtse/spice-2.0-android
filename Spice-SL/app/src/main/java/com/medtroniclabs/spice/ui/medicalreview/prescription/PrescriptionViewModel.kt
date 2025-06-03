package com.medtroniclabs.spice.ui.medicalreview.prescription

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.appextensions.takeIfNotNull
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.HIV
import com.medtroniclabs.spice.data.Category
import com.medtroniclabs.spice.data.EncounterDetails
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.MedicationGroupSearchRequest
import com.medtroniclabs.spice.data.MedicationRequestObject
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.PrescriptionListRequest
import com.medtroniclabs.spice.data.PrescriptionRequest
import com.medtroniclabs.spice.data.RemovePrescriptionRequest
import com.medtroniclabs.spice.data.model.ReasonForChangeParams
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.db.entity.FrequencyEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    val medicationListLiveData = MutableLiveData<Resource<java.util.ArrayList<MedicationResponse>>>()

    val selectedMedicationLiveData = MutableLiveData<ArrayList<MedicationRequestObject>>()

    val selectedMedicationGroupLiveData = MutableLiveData<ArrayList<MedicationRequestObject>>()

    val createPrescriptionLiveData = MutableLiveData<Resource<Map<String, Any>>>()

    val prescriptionListLiveData = MutableLiveData<Resource<ArrayList<Prescription>>>()

    val discontinuedPrescriptionListLiveData = MutableLiveData<Resource<ArrayList<Prescription>>>()

    val removePrescriptionLiveData = MutableLiveData<Resource<Map<String, Any>>>()

    var patientId: String? = null
    var isDiscontinuedMedicationView = false
    var discontinuedMedicationRegimen: Int? = null

    val frequencyListLiveDate = MutableLiveData<Resource<List<FrequencyEntity>>>()
    val instructionListLiveDate = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()

    val medicationGroupListLiveData = MutableLiveData<Resource<ArrayList<MedicationResponse>>>()

    private val _showReasonForChangeDialog = MutableLiveData<ReasonForChangeParams?>()
    val showReasonForChangeDialog: LiveData<ReasonForChangeParams?> = _showReasonForChangeDialog

    fun triggerReasonForChangeDialog(params: ReasonForChangeParams) {
        _showReasonForChangeDialog.value = params
    }


    fun searchMedicationByName(name: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationListLiveData.postLoading()
                val request = MedicationSearchRequest(name)
                val response = medicationRepository.searchMedicationByName(request)
                response.data?.let {
                    medicationListLiveData.postSuccess(it)
                } ?: kotlin.run {
                    medicationListLiveData.postError()
                }
            } catch (e: Exception) {
                medicationListLiveData.postError()
            }
        }
    }

    fun updateMedicationList(
        medicationResponse: ArrayList<MedicationRequestObject>,
        reset: Boolean,
    ) {
        if (reset) {
            selectedMedicationLiveData.value?.clear()
        }
        val medicationList: ArrayList<MedicationRequestObject> =
            selectedMedicationLiveData.value ?: ArrayList()
        medicationList.addAll(medicationResponse)
        selectedMedicationLiveData.value = medicationList
    }

    fun updateMedicationGroupList(
        medicationResponse: ArrayList<MedicationRequestObject>,
        reset: Boolean,
    ) {
        if (reset) {
            selectedMedicationGroupLiveData.value?.clear()
        }
        val medicationList: ArrayList<MedicationRequestObject> =
            selectedMedicationGroupLiveData.value ?: ArrayList()
        val existingGroupNames = selectedMedicationGroupLiveData.value?.mapNotNull { it.medicationResponse.groupName }?.toHashSet()

        val filteredListItems = existingGroupNames?.let {existingGroups ->
            medicationResponse.filter { it.medicationResponse.groupName !in existingGroups }
        } ?: medicationResponse

        medicationList.addAll(filteredListItems)
        selectedMedicationGroupLiveData.value = medicationList
    }

    fun getFrequencyList() {
        viewModelScope.launch(dispatcherIO) {
            frequencyListLiveDate.postValue(medicationRepository.getFrequencyList())
        }
    }

    fun getInstructionList() {
        viewModelScope.launch(dispatcherIO) {
            instructionListLiveDate.postValue(medicationRepository.getInstructionList())
        }
    }

    fun getFrequencyMap(): ArrayList<Map<String, Any>> {
        val mapList = ArrayList<Map<String, Any>>()
        frequencyListLiveDate.value?.data?.forEach { data ->
            val map = HashMap<String, Any>()
            map[DefinedParams.NAME] = data.name
            map[DefinedParams.ID] = data.id
            map[DefinedParams.Frequency] = data.frequency ?: 1
            map[DefinedParams.Description] = data.description ?: ""
            map[DefinedParams.DisplayOrder] = data.displayOrder
            mapList.add(map)
        }

        return mapList
    }

    fun getInstructionMap(): ArrayList<Map<String, Any>> {
        val mapList = ArrayList<Map<String, Any>>()
        val defaultMap = hashMapOf<String, Any>(
            DefinedParams.ID to DefinedParams.DefaultSelectID,
            DefinedParams.NAME to DefinedParams.DefaultIDLabel,
            DefinedParams.Value to DefinedParams.DefaultIDLabel,
            DefinedParams.DisplayOrder to 0
        )
        mapList.add(defaultMap)
        instructionListLiveDate.value?.data?.forEach { data ->
            data.value?.let { fhirValue ->
                val map = HashMap<String, Any>()
                map[DefinedParams.NAME] = data.name
                map[DefinedParams.ID] = data.id
                map[DefinedParams.Value] = fhirValue
                map[DefinedParams.DisplayOrder] = data.displayOrder
                mapList.add(map)
            }
        }
        return mapList
    }

    fun createPrescription(
        signature: Bitmap,
        filePath: File,
        list: ArrayList<MedicationRequestObject>,
        data: PatientListRespModel,
        encounterId: String?,
        reason: String?,
        regimenNumber: Int? = null
    ) {
        viewModelScope.launch(dispatcherIO) {
            try {
                createPrescriptionLiveData.postLoading()
                filePath.mkdirs()
                val file = File(filePath, "${DefinedParams.SIGN_SUFFIX}.jpeg")
                if (file.exists()) file.delete()
                val out = FileOutputStream(file)
                signature.compress(Bitmap.CompressFormat.JPEG, 20, out)
                out.flush()
                out.close()
                val builder = MultipartBody.Builder()
                builder.setType(MultipartBody.FORM)
                builder.addFormDataPart(
                    "signature",
                    file.name,
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
                val prescriptionList = ArrayList<Prescription>()
                list.forEach {
                    if (it.medicationResponse.prescribedDays != null) {
                        it.medicationResponse.name?.let { it1 ->
                            Prescription(
                                prescribedDays = it.medicationResponse.prescribedDays,
                                dosageDurationName = it.medicationResponse.dosageDurationName,
                                medicationName = it1,
                                medicationId = it.medicationResponse.id.toString(),
                                frequency = getMedicationFrequency(it),
                                prescribedSince = System.currentTimeMillis().convertToUtcDateTime(),
                                prescriptionId = it.medicationResponse.prescriptionId,
                                codeDetails = it.medicationResponse.codeDetails,
                                frequencyName = getMedicationFrequencyName(it),
                                groupName = it.medicationResponse.groupName,
                                categoryName = it.medicationResponse.category?.name,
                                groupUniqueId = it.medicationResponse.groupUniqueId,
                                instruction = it.medicationResponse.instruction ?: ""
                            )
                        }?.let { it2 ->
                            prescriptionList.add(
                                it2
                            )
                        }
                    }
                }

                val isHiv =
                    list.any { it.medicationResponse.category?.name?.equals(HIV, true) == true }
                val prescriptionRequest = PrescriptionRequest(
                    encounter = EncounterDetails(
                        id = encounterId,
                        patientReference = data.id,
                        patientId = data.patientId ?: "",
                        memberId = data.memberId ?: "", provenance = ProvanceDto()
                    ),
                    prescriptions = prescriptionList,
                    reasonsForChange = if (isHiv) reason else null,
                    regimenLine = if (isHiv) regimenNumber?.plus(1) else null
                )
                val dataRequest = Gson().toJson(prescriptionRequest)
                builder.addFormDataPart("prescriptionRequest", dataRequest)
                val requestBody = builder.build()
                val response = medicationRepository.createPrescriptionRequest(requestBody)
                response.data?.let {
                    createPrescriptionLiveData.postSuccess(it)
                } ?: kotlin.run {
                    createPrescriptionLiveData.postError()
                }
            } catch (e: Exception) {
                createPrescriptionLiveData.postError()
            }
        }
    }

    fun constructMedicationRequestObjectList(
        list: java.util.ArrayList<Prescription>
    ): ArrayList<MedicationRequestObject> {
        val medicationRequestObjectList = ArrayList<MedicationRequestObject>()
        list.forEach { prescription ->
            val medicationRequestObject =
                MedicationRequestObject(constructMedicationRequestObject(prescription))
            medicationRequestObjectList.add(medicationRequestObject)
        }
        return medicationRequestObjectList
    }

    fun constructMedicationRequestObject(prescription: Prescription): MedicationResponse {
        val selectedFrequencyMap = getSelectedFrequencyMap(prescription.frequencyName)

        return MedicationResponse(
            id = prescription.medicationId.toLongOrNull(),
            name = prescription.medicationName,
            selectedMap = selectedFrequencyMap,
            prescribedDays = prescription.prescribedDays,
            prescriptionId = prescription.prescriptionId,
            prescribedSince = prescription.prescribedSince,
            groupName = prescription.groupName,
            category = Category(name = prescription.categoryName ?: "", id = null),
            groupUniqueId = prescription.groupUniqueId,
            instruction = prescription.instruction,
            regimenLine = prescription.regimenLine,
        )
    }

    private fun getSelectedFrequencyMap(frequency: String): HashMap<String, Any>? {
        val list = getFrequencyMap().filter { it[DefinedParams.NAME] == frequency }
        return if (list.isNotEmpty())
            HashMap(list[0])
        else
            null
    }

    private fun getMedicationFrequency(data: MedicationRequestObject): Int {
        return data.medicationResponse.selectedMap?.get(DefinedParams.Frequency) as? Int? ?: 0
    }

    private fun getMedicationFrequencyName(data: MedicationRequestObject): String {
        return data.medicationResponse.selectedMap?.get(DefinedParams.NAME) as? String? ?: ""
    }

    private fun getPrescriptionList(request: PrescriptionListRequest) {
        viewModelScope.launch(dispatcherIO) {
            if (request.isActive) {
                prescriptionListLiveData.postLoading()
            } else {
                discontinuedPrescriptionListLiveData.postLoading()
            }
            val response = medicationRepository.getPrescriptionList(request)
            response.data?.let {
                if (request.isActive) {
                    prescriptionListLiveData.postSuccess(it)
                } else {
                    discontinuedPrescriptionListLiveData.postSuccess(it)
                }
            } ?: kotlin.run {
                if (request.isActive) {
                    prescriptionListLiveData.postError()
                } else {
                    discontinuedPrescriptionListLiveData.postError()
                }
            }
        }
    }

    fun removePrescription(prescriptionId: String, reason: String?) {
        viewModelScope.launch(dispatcherIO) {
            removePrescriptionLiveData.postLoading()
            val response = medicationRepository.removePrescription(
                RemovePrescriptionRequest(
                    prescriptionId, ProvanceDto(),
                    reason
                )
            )
            removePrescriptionLiveData.postSuccess(response.data)
        }
    }

    fun removeCommunityPrescription(request: List<RemovePrescriptionRequest>) {
        viewModelScope.launch(dispatcherIO) {
            removePrescriptionLiveData.postLoading()
            val response = medicationRepository.removeCommunityPrescription(request)
            removePrescriptionLiveData.postSuccess(response.data)
        }
    }

    fun getPrescriptionList(data: PatientListRespModel, isDeleted: Boolean = true) {
        getPrescriptionList(
            if (CommonUtils.isNonCommunity()) {
                PrescriptionListRequest(
                    patientReference = data.patientReference,
                    memberReference = data.id,
                    isDeleted
                )
            } else {
                PrescriptionListRequest(
                    data.id,
                    isActive = isDeleted
                )
            }
        )
    }

    fun getMedicationGroupByName(name: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationGroupListLiveData.postLoading()
                val response = medicationRepository.searchMedicationGroupByName(
                    MedicationGroupSearchRequest(name)
                )
                response.data?.let {
                    medicationGroupListLiveData.postSuccess(it)
                } ?: kotlin.run {
                    medicationGroupListLiveData.postError()
                }
            } catch (e: Exception) {
                medicationGroupListLiveData.postError()
            }
        }
    }

}