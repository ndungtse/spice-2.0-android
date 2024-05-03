package com.medtroniclabs.spice.ui.mypatients.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class MotherNeonateANCRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    suspend fun getMotherNeoNateAncStaticData(motherNeonateMetaResponse: MutableLiveData<Resource<Boolean>>) {
        try {
            motherNeonateMetaResponse.postLoading()
            val response = apiHelper.getMotherNeoNateAncStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.let { data ->
                    roomHelper.apply {
                        deleteExaminationsComplaintsForAnc(ANC.uppercase())
                        insertExaminationsComplaint(
                            generateChipItemByType(
                                data.presentingComplaints,
                                data.obstetricExaminations,
                                data.pregnancyHistories,
                                data.bloodGroup
                            )
                        )
                        SecuredPreference.putBoolean(
                            SecuredPreference.EnvironmentKey.IS_MOTHER_NEONATE_LOADEDANC.name,
                            true
                        )
                    }
                    motherNeonateMetaResponse.postSuccess()
                } ?: run {
                    motherNeonateMetaResponse.postError()
                }
            } else {
                motherNeonateMetaResponse.postError()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            motherNeonateMetaResponse.postError()
        }
    }

    private fun generateChipItemByType(
        presentingComplaints: List<MedicalReviewMetaItems>,
        obstetricExaminations: List<MedicalReviewMetaItems>,
        pregnancyHistories: List<MedicalReviewMetaItems>,
        bloodGroup: List<MedicalReviewMetaItems>
    ): List<MedicalReviewMetaItems> {
        val chipItemList = mutableListOf<MedicalReviewMetaItems>()
        chipItemList.addAll(presentingComplaints.map {
            it.apply {
                category = MedicalReviewTypeEnums.PresentingComplaints.name
            }
        })
        chipItemList.addAll(obstetricExaminations.map {
            it.apply {
                category = MedicalReviewTypeEnums.ObstetricExaminations.name
            }
        })
        chipItemList.addAll(pregnancyHistories.map {
            it.apply {
                category = MedicalReviewTypeEnums.PregnancyHistories.name
            }
        })
        chipItemList.addAll(bloodGroup.map {
            it.apply {
                type = ANC.uppercase()
                category = MedicalReviewTypeEnums.BloodGroup.name
            }
        })
        return chipItemList
    }

    fun getExaminationsComplaintsForAnc(
        category: String
    ): LiveData<List<MedicalReviewMetaItems>> {
        return roomHelper.getExaminationsComplaintsForAnc(category)
    }
}
