package com.medtroniclabs.spice.ui.medicalreview.prescription

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.MedicationRequestObject
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.PrescriptionRequest
import com.medtroniclabs.spice.db.entity.MedicationFrequencyEntity
import com.medtroniclabs.spice.di.IoDispatcher
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

    val medicationListLiveData =
        MutableLiveData<Resource<APIResponse<ArrayList<MedicationResponse>>>>()

    val selectedMedicationLiveDate = MutableLiveData<ArrayList<MedicationRequestObject>>()

    var patientId: String? = null

    fun searchMedicationByName(name: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationListLiveData.postLoading()
                val request = MedicationSearchRequest(name)
                val response = medicationRepository.searchMedicationByName(request)
                medicationListLiveData.postSuccess(response.body())
            } catch (e: Exception) {
                medicationListLiveData.postError()
            }
        }
    }

    fun updateMedicationList(
        medicationResponse: MedicationResponse
    ) {
        val medicationList = selectedMedicationLiveDate.value ?: ArrayList()
        medicationList.add(MedicationRequestObject(medicationResponse))
        selectedMedicationLiveDate.value = medicationList
    }

    private fun getFrequencyList(): ArrayList<MedicationFrequencyEntity> {
        val list = ArrayList<MedicationFrequencyEntity>()
        list.add(MedicationFrequencyEntity(10, "Daily", 100, "OD", 1))
        list.add(MedicationFrequencyEntity(4, "Twice a day", 101, "BD", 2))
        list.add(MedicationFrequencyEntity(1, "Three times a day", 102, "TDS", 3))
        list.add(MedicationFrequencyEntity(7, "Four times a day", 103, "QDS", 4))
        return list
    }

    fun getFrequencyMap(): ArrayList<Map<String, Any>> {
        val mapList = ArrayList<Map<String, Any>>()
        getFrequencyList().forEach { data ->
            val map = HashMap<String, Any>()
            map[DefinedParams.NAME] = data.name
            map[DefinedParams.ID] = data.id
            map[DefinedParams.Frequency] = data.frequency
            map[DefinedParams.Description] = data.description
            map[DefinedParams.DisplayOrder] = data.displayOrder
            mapList.add(map)
        }
        return mapList
    }

    fun createPrescription(
        signature: Bitmap,
        filePath: File,
        list: ArrayList<MedicationRequestObject>
    ) {
        viewModelScope.launch(dispatcherIO) {
            try {
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
                    "signatureFile",
                    file.name,
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
                list.forEach {
                    /*if (it.medicationResponse.prescribedDays != null){
                        val presciption = Prescription(
                            prescribedDays = it.medicationResponse.prescribedDays!!,
                            medicationName = it.medicationResponse.name,
                            medicationId = it.medicationResponse.id,
                            form = it.medicationResponse.dosageFormName,
                            frequency = )
                    }*/
                }
                val dataRequest = Gson().toJson(list)
                builder.addFormDataPart("prescriptionRequest", dataRequest)
                val requestBody = builder.build()
                medicationRepository.createPrescriptionRequest(requestBody)
            }catch (e:Exception){

            }
        }
    }

}