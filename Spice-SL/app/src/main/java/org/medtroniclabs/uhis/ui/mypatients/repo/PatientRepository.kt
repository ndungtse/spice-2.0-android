package org.medtroniclabs.uhis.ui.mypatients.repo

import com.google.gson.Gson
import org.medtroniclabs.uhis.db.entity.ClinicalWorkflowEntity
import org.medtroniclabs.uhis.db.entity.VillageEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.model.PatientDetailRequest
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.ncd.data.NCDPatientRemoveRequest
import org.medtroniclabs.uhis.ncd.data.PatientVisitRequest
import org.medtroniclabs.uhis.ncd.data.PatientVisitResponse
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
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
