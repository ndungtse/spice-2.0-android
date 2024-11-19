package com.medtroniclabs.spice.ncd.medicalreview.prescription.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.DosageFrequency
import com.medtroniclabs.spice.data.EncounterDetails
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.data.PatientPrescriptionModel
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.PrescriptionCreateRequest
import com.medtroniclabs.spice.data.PrescriptionListRequest
import com.medtroniclabs.spice.data.RemovePrescriptionRequest
import com.medtroniclabs.spice.data.ResponseDataModel
import com.medtroniclabs.spice.data.UnitMetricEntity
import com.medtroniclabs.spice.data.UpdatePrescriptionModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.PredictionRequest
import com.medtroniclabs.spice.ncd.data.PrescriptionNudgeResponse
import com.medtroniclabs.spice.ncd.medicalreview.prescription.repo.NCDPrescriptionRepo
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
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
class NCDPrescriptionViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val prescriptionRepository: NCDPrescriptionRepo
) : ViewModel() {

    val medicationListLiveData = MutableLiveData<Resource<ArrayList<MedicationResponse>>>()
    val prescriptionListLiveData = MutableLiveData<Resource<ArrayList<Prescription>>>()
    val unitList = MutableLiveData<List<UnitMetricEntity>>()
    val createPrescriptionLiveData = MutableLiveData<Resource<Map<String, Any>>>()
    var patient_visit_id: String? = null
    var patientId: String? = null
    var selectedMedication: MedicationResponse? = null
    var prescriptionUIModel: ArrayList<MedicationResponse>? = null
    val reloadInstruction = MutableLiveData<Boolean>()
    val frequencyList = MutableLiveData<List<DosageFrequency>>()
    val discontinuedPrescriptionListLiveData = MutableLiveData<Resource<ArrayList<Prescription>>>()
    var savePrescriptionList: ArrayList<UpdatePrescriptionModel>? = null
    val updatePrescriptionLiveDate = MutableLiveData<Resource<ResponseDataModel>>()
    val removePrescriptionLiveData = MutableLiveData<Resource<Map<String, Any>>>()
    val medicationHistoryLiveData = MutableLiveData<Resource<ArrayList<Prescription>>>()
    val prescriptionPredictionResponseLiveDate =
        SingleLiveEvent<Resource<PrescriptionNudgeResponse>>()

    fun searchMedication(request: MedicationSearchRequest? = null) {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationListLiveData.postLoading()
                val response = request?.let { prescriptionRepository.searchMedication(request) }
                response?.data?.let {
                    medicationListLiveData.postSuccess(it)
                } ?: kotlin.run {
                    medicationListLiveData.postError()
                }
            } catch (e: Exception) {
                medicationListLiveData.postError()
            }
        }
    }

    fun getDosageFrequencyList() {
        viewModelScope.launch(dispatcherIO) {
            frequencyList.postValue(prescriptionRepository.getDosageFrequencyList())
        }
    }

    fun removePrescription(prescriptionId: String, reason: String?) {
        viewModelScope.launch(dispatcherIO) {
            removePrescriptionLiveData.postLoading()
            val response = prescriptionRepository.removePrescription(
                RemovePrescriptionRequest(
                    prescriptionId, ProvanceDto(),
                    reason,
                    requestFrom = DefinedParams.Africa
                )
            )
            removePrescriptionLiveData.postSuccess(response.data)
        }
    }

    fun getDosageUnitList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                unitList.postValue(prescriptionRepository.getUnitList(DefinedParams.PRESCRIPTION))
            } catch (_: Exception) {
                //Exception - Catch block
            }
        }
    }

    fun getPrescriptionsList(data: PatientListRespModel, isDeleted: Boolean = true) {
        data.id?.let { id ->
            getPrescriptionList(PrescriptionListRequest(id, isDeleted, DefinedParams.Africa))
        }
    }

    private fun getPrescriptionList(request: PrescriptionListRequest) {
        viewModelScope.launch(dispatcherIO) {
            if (request.isActive) {
                prescriptionListLiveData.postLoading()
            } else {
                discontinuedPrescriptionListLiveData.postLoading()
            }
            val response = prescriptionRepository.getPrescriptionList(request)
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


    fun createOrUpdatePrescription(
        signatureBitmap: Bitmap,
        filePath: File,
        request: PatientPrescriptionModel
    ) {
        updatePrescriptionLiveDate.postLoading()
        viewModelScope.launch(dispatcherIO) {
            try {
                filePath.mkdirs()
                val signature = "${request.patientVisitId}${DefinedParams.SIGN_SUFFIX}.jpeg"
                val file = File(filePath, signature)
                if (file.exists()) {
                    val result = file.delete()
                    if (result) {
                        uploadPrescription(
                            file = file,
                            signatureBitmap = signatureBitmap,
                            request = request,
                        )
                    } else {
                        updatePrescriptionLiveDate.postError()
                    }
                } else {
                    uploadPrescription(
                        file = file,
                        signatureBitmap = signatureBitmap,
                        request = request,
                    )
                }
            } catch (e: Exception) {
                updatePrescriptionLiveDate.postError()
            }
        }
    }

    fun getMedicationHistory(prescriptionId: String?) {
        medicationHistoryLiveData.postLoading()
        viewModelScope.launch(dispatcherIO) {
            try {
                val response = prescriptionRepository.getPatientPrescriptionHistoryList(
                    RemovePrescriptionRequest(
                        prescriptionId = prescriptionId,
                        requestFrom = DefinedParams.Africa
                    )
                )
                response.data.let {
                    medicationHistoryLiveData.postSuccess(it)
                }
            } catch (e: Exception) {
                medicationHistoryLiveData.postError()
            }
        }
    }

    private suspend fun uploadPrescription(
        file: File,
        signatureBitmap: Bitmap,
        request: PatientPrescriptionModel,
    ) {
        val out = FileOutputStream(file)
        signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 20, out)
        out.flush()
        out.close()
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart(
            "signature",
            file.name,
            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        )

        val prescriptionRequest = request.prescriptions?.let {
            PrescriptionCreateRequest(
                requestFrom = DefinedParams.Africa,
                encounter = EncounterDetails(
                    patientVisitId = patient_visit_id,
                    memberId = patientId,
                    provenance = ProvanceDto()
                ), prescriptions = it
            )
        }

        val dataRequest = Gson().toJson(prescriptionRequest)
        builder.addFormDataPart("prescriptionRequest", dataRequest)
        val requestBody = builder.build()
        val response = prescriptionRepository.createPrescriptionRequest(requestBody)
        response.data?.let {
            createPrescriptionLiveData.postSuccess(it)
        } ?: kotlin.run {
            createPrescriptionLiveData.postError()
        }
    }

     fun getPrescriptionPrediction() {
        viewModelScope.launch(dispatcherIO) {
            try {
                val response = prescriptionRepository.getNudgesList(PredictionRequest(memberId = patientId))
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true) {
                        prescriptionPredictionResponseLiveDate.postSuccess(res.entity)
                    }
                }
            } catch (e: Exception) {
                //error Block
            }
        }
    }
}