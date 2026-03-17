package org.medtroniclabs.uhis.ui.referralhistory.repo

import org.medtroniclabs.uhis.data.PncChildMedicalReview
import org.medtroniclabs.uhis.data.history.BirthDetails
import org.medtroniclabs.uhis.data.history.HistoryEntity
import org.medtroniclabs.uhis.data.history.MedicalReviewHistory
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.model.ReferralData
import org.medtroniclabs.uhis.model.ReferralDetailRequest
import org.medtroniclabs.uhis.model.medicalreview.RequestBirthDetails
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
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
