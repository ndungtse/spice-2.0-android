package com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.repo

import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.MedicalReviewSummarySubmitRequest
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.data.resource.LabourDeliverySummaryRequest
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.medicalreview.CreateLabourDeliveryRequest
import com.medtroniclabs.spice.model.medicalreview.CreateLabourDeliveryResponse
import com.medtroniclabs.spice.model.medicalreview.LabourDeliverySummaryDetails
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class LabourDeliveryRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {

    suspend fun getStaticMetaData(): Resource<Boolean> {
        return try {
            val response = apiHelper.getLabourDeliveryMetaData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.deleteLabourDelivery()
                    roomHelper.insertLabourDelivery(
                        generateChipItemByType(
                            symptoms,
                            Pair(deliveryAt, deliveryBy),
                            Pair(deliveryType, deliveryStatus),
                            neonateOutcome,
                            riskFactors,
                            conditionOfMother,
                            motherDeliveryStatus
                        )
                    )
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_LABOUR_DELIVERY_LOADED.name,
                    true
                )
                Resource(ResourceState.SUCCESS, true)
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_LABOUR_DELIVERY_LOADED.name,
                false
            )
            Resource(ResourceState.ERROR)
        }
    }

    private fun generateChipItemByType(
        symptoms: List<LabourDeliveryMetaEntity>,
        deliveryAtDeliveryByPair: Pair<List<LabourDeliveryMetaEntity>, List<LabourDeliveryMetaEntity>>,
        deliveryTypeAndStatusPair: Pair<List<LabourDeliveryMetaEntity>, List<LabourDeliveryMetaEntity>>,
        neonateOutcome: List<LabourDeliveryMetaEntity>,
        riskFactors: List<LabourDeliveryMetaEntity>,
        conditionOfMother: List<LabourDeliveryMetaEntity>,
        motherDeliveryStatus: List<LabourDeliveryMetaEntity>
    ): List<LabourDeliveryMetaEntity> {
        val chipItemList = ArrayList<LabourDeliveryMetaEntity>()
        symptoms.forEach { it.category = MedicalReviewTypeEnums.LabourDelivery.name }
        deliveryAtDeliveryByPair.first.forEach {
            it.category = MedicalReviewTypeEnums.DeliveryAt.name
        }
        deliveryAtDeliveryByPair.second.forEach {
            it.category = MedicalReviewTypeEnums.DeliveryBy.name
        }
        deliveryTypeAndStatusPair.first.forEach {
            it.category = MedicalReviewTypeEnums.DeliveryType.name
        }
        deliveryTypeAndStatusPair.second.forEach {
            it.category = MedicalReviewTypeEnums.DeliveryStatus.name
        }
        neonateOutcome.forEach { it.category = MedicalReviewTypeEnums.NeonateOutcome.name }
        riskFactors.forEach { it.category = MedicalReviewTypeEnums.RiskFactors.name }
        conditionOfMother.forEach { it.category = MedicalReviewTypeEnums.ConditionOfMother.name }
        motherDeliveryStatus.forEach {
            it.category = MedicalReviewTypeEnums.MotherDeliveryStatus.name
        }
        chipItemList.addAll(symptoms)
        chipItemList.addAll(deliveryAtDeliveryByPair.first)
        chipItemList.addAll(deliveryAtDeliveryByPair.second)
        chipItemList.addAll(deliveryTypeAndStatusPair.first)
        chipItemList.addAll(deliveryTypeAndStatusPair.second)
        chipItemList.addAll(neonateOutcome)
        chipItemList.addAll(riskFactors)
        chipItemList.addAll(conditionOfMother)
        chipItemList.addAll(motherDeliveryStatus)
        return chipItemList
    }

    suspend fun getLabourDeliveryList(): Resource<List<LabourDeliveryMetaEntity>> {
        return try {
            val response = roomHelper.getLabourDelivery()
            Resource(ResourceState.SUCCESS, response)
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

    suspend fun createLabourDeliveryMedicalReview(request: CreateLabourDeliveryRequest): Resource<CreateLabourDeliveryResponse> {
        return try {
            val response = apiHelper.createMedicalReviewLabourDelivery(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR, message = e.localizedMessage)
        }
    }

    suspend fun getLabourDeliverySummaryDetails(request: LabourDeliverySummaryDetails): Resource<CreateLabourDeliveryRequest> {
        return try {
            val response = apiHelper.getLabourDeliverySummaryDetails(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR, message = e.localizedMessage)
        }
    }

    suspend fun labourDeliverySummaryCreate(request: LabourDeliverySummaryRequest): Resource<HashMap<String, Any>> {
        return try{
            val response = apiHelper.createSummaryMotherNeonate(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR, message = e.localizedMessage)
        }
    }
}