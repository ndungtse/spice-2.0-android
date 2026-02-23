package com.medtroniclabs.spice.ui.mypatients.repo

import com.google.gson.Gson
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.NCDPatientRemoveRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class PatientRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    suspend fun getUserVillages(): List<VillageEntity> = roomHelper.getUserVillages()

    suspend fun getMenuForClinicalWorkflows(): List<ClinicalWorkflowEntity> = roomHelper.getMenuForClinicalWorkflows()

    suspend fun getPatients(request: PatientDetailRequest): Resource<PatientListRespModel> =
        try {
            val response = apiHelper.getPatient(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getPatientBasedOnId(id: String): Resource<PatientListRespModel> =
        try {
            val response = roomHelper.getPatientBasedOnId(id)
            Gson()
                .fromJson(response.patientDetails, PatientListRespModel::class.java)
                ?.let { data ->
                    Resource(state = ResourceState.SUCCESS, data = data)
                } ?: Resource(state = ResourceState.ERROR)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createPatientVisit(request: PatientVisitRequest): Resource<PatientVisitResponse> =
        try {
            val response = apiHelper.createPatientVisit(request)
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    Resource(state = ResourceState.SUCCESS, it)
                } ?: Resource(state = ResourceState.ERROR)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun ncdPatientRemove(request: NCDPatientRemoveRequest): Resource<Boolean> =
        try {
            val response = apiHelper.ncdPatientRemove(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
}
