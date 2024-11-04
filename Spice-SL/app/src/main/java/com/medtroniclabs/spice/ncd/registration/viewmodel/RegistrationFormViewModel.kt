package com.medtroniclabs.spice.ncd.registration.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.internal.LinkedTreeMap
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.model.RegistrationResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.registration.repo.RegistrationRepository
import com.medtroniclabs.spice.ncd.screening.utils.ReferredReason
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
class RegistrationFormViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var registrationRepository: RegistrationRepository
) : ViewModel() {
    val registrationFormLayoutsLiveData = MutableLiveData<Resource<FormResponse>>()
    var registrationResponseLiveData = MutableLiveData<Resource<RegistrationResponse>>()
    var validatePatientResponseLiveDate =
        MutableLiveData<Resource<Pair<HashMap<String, Any>, List<FormLayout?>?>>>()

    val countrySpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val districtSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val chiefdomSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val villageSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()
    val programsSpinnerLiveData = MutableLiveData<Resource<LocalSpinnerResponse>>()

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            registrationFormLayoutsLiveData.postLoading()
            registrationFormLayoutsLiveData.postValue(registrationRepository.getFormData(formType))
        }
    }

    fun loadDataCacheByType(type: String, tag: String, selectedParent: Long?) {
        viewModelScope.launch(dispatcherIO) {
            when (type) {
                DefinedParams.Country -> {
                    countrySpinnerLiveData.postLoading()
                    countrySpinnerLiveData.postValue(
                        registrationRepository.getCountries(tag)
                    )
                }

                DefinedParams.District -> {
                    selectedParent?.let { parent ->
                        districtSpinnerLiveData.postLoading()
                        districtSpinnerLiveData.postValue(
                            registrationRepository.getCounties(tag, parent)
                        )
                    }
                }

                DefinedParams.Chiefdom -> {
                    selectedParent?.let { parent ->
                        chiefdomSpinnerLiveData.postLoading()
                        chiefdomSpinnerLiveData.postValue(
                            registrationRepository.getSubCounties(tag, parent)
                        )
                    }
                }

                DefinedParams.Village -> {
                    selectedParent?.let { parent ->
                        villageSpinnerLiveData.postLoading()
                        villageSpinnerLiveData.postValue(
                            registrationRepository.getAllVillages(tag, parent)
                        )
                    }
                }

                DefinedParams.Program -> {
                    programsSpinnerLiveData.postLoading()
                    programsSpinnerLiveData.postValue(
                        registrationRepository.getAllPrograms(tag)
                    )
                }
            }
        }
    }

    fun registerPatient(
        context: Context,
        hashMap: HashMap<String, Any>,
        id: Long?,
        patientId: Long?,
        signature: ByteArray?
    ) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        hashMap.apply {
            id?.let { requestId ->
                put(DefinedParams.ID, requestId)
            }
            patientId?.let { requestPatientId ->
                put(DefinedParams.PATIENT_ID, requestPatientId)
            }
            put(DefinedParams.TenantId, SecuredPreference.getTenantId())
            put(DefinedParams.HealthFacilityId, SecuredPreference.getOrganizationId())
            put(DefinedParams.HealthFacilityFhirId, SecuredPreference.getOrganizationFhirId())
            put(DefinedParams.Provenance, ProvanceDto())
        }
        StringConverter.convertGivenMapToString(hashMap)?.let { req ->
            builder.addFormDataPart("enrollmentRequest", req)
        }

        signature?.let { sign ->
            val signMap = CommonUtils.convertByteArrayToBitmap(sign)

            val identityValue = CommonUtils.getIdentityValue(hashMap)
            val fileName = "${identityValue}${Screening.RegistrationSignSuffix}.jpeg"

            val filePath = CommonUtils.getFilePath(identityValue, context)
            filePath.mkdirs()

            val file = File(filePath, fileName)

            val clearedExistingFile: Boolean = if (file.exists()) file.delete() else true

            if (clearedExistingFile && signMap != null) {
                val out = FileOutputStream(file)
                signMap.compress(Bitmap.CompressFormat.JPEG, 20, out)
                out.flush()
                out.close()
                file.let {
                    builder.addFormDataPart(
                        "signatureFile",
                        file.name,
                        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                }
            } else
                registrationResponseLiveData.postError()
        }

        viewModelScope.launch(dispatcherIO) {
            registrationResponseLiveData.postLoading()
            registrationResponseLiveData.postValue(registrationRepository.registerPatient(builder.build()))
        }
    }

    fun validatePatient(resp: HashMap<String, Any>, serverData: List<FormLayout?>?) {
        viewModelScope.launch(dispatcherIO) {
            validatePatientResponseLiveDate.postLoading()
            validatePatientResponseLiveDate.postValue(
                registrationRepository.validatePatient(
                    ReferredReason.validateRequest(resp),
                    Pair(resp, serverData)
                )
            )
        }
    }
}