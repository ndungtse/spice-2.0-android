package com.medtroniclabs.spice.ui.mypatients.repo

import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDMedicalReviewRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    suspend fun getNcdMedicalReviewStaticData(): Resource<Boolean> {
        return try {
            val response = apiHelper.getNcdMRStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    roomHelper.deleteComorbidities()
                    roomHelper.insertComorbidities(it.comorbidity)

                    roomHelper.deleteComplications()
                    roomHelper.insertComplications(it.complications)

                    roomHelper.deleteLifestyle()
                    roomHelper.insertLifestyle(it.lifestyle)

                    roomHelper.deleteComplaints()
                    roomHelper.insertComplaints(it.complaints)

                    roomHelper.deletePhysicalExamination()
                    roomHelper.insertPhysicalExamination(it.physicalExamination)

                    roomHelper.deleteCurrentMedications()
                    roomHelper.insertCurrentMedications(it.currentMedication)

                    roomHelper.deleteTreatmentPlan()
                    roomHelper.insertTreatmentPlan(it.frequencyTypes)

                    roomHelper.deleteTreatmentPlanFrequencies()
                    roomHelper.insertTreatmentPlanFrequencies(it.frequencies)
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name,
                    true
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_NCD_MEDICAL_REVIEW_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }
    }
}