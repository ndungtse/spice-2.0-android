package com.medtroniclabs.spice.ui.referralhistory.repo

import com.medtroniclabs.spice.data.PncChildMedicalReview
import com.medtroniclabs.spice.data.history.BirthDetails
import com.medtroniclabs.spice.data.history.HistoryEntity
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.medicalreview.RequestBirthDetails
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class ReferralHistoryRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    suspend fun getReferralTicket(request: ReferralDetailRequest): Resource<ReferralData> =
        try {
            val response = apiHelper.getReferralsDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getPrescription(request: ReferralDetailRequest): Resource<HistoryEntity> =
        try {
            val response = apiHelper.getPrescription(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getInvestigation(request: ReferralDetailRequest): Resource<HistoryEntity> =
        try {
            val response = apiHelper.getInvestigation(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getMedicalReviewHistory(request: ReferralDetailRequest): Resource<MedicalReviewHistory> =
        try {
            val response = apiHelper.getMedicalReviewHistory(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getBirthDetails(request: RequestBirthDetails): Resource<BirthDetails> =
        try {
            val response = apiHelper.getBirthDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getMedicalReviewHistoryPNC(request: ReferralDetailRequest): Resource<PncChildMedicalReview> =
        try {
            val response = apiHelper.getMedicalReviewHistoryPNC(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
