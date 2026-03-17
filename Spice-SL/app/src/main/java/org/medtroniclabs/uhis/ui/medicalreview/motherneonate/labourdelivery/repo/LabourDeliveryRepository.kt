package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.labourdelivery.repo

import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.data.LabourDeliveryMetaEntity
import org.medtroniclabs.uhis.data.MedicalReviewSummarySubmitRequest
import org.medtroniclabs.uhis.data.model.CreateLabourDeliveryRequest
import org.medtroniclabs.uhis.data.model.CreateLabourDeliveryResponse
import org.medtroniclabs.uhis.data.model.LabourDeliverySummaryDetails
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class LabourDeliveryRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper,
) {
    suspend fun getStaticMetaData(): Resource<Boolean> =
        try {
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
                            pncNeonateOutcome,
                            riskFactors,
                            conditionOfMother,
                            motherDeliveryStatus,
                            stateOfPerineum,
                        ),
                    )
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_LABOUR_DELIVERY_LOADED.name,
                    true,
                )
                Resource(ResourceState.SUCCESS, true)
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_LABOUR_DELIVERY_LOADED.name,
                false,
            )
            Resource(ResourceState.ERROR)
        }

    private fun generateChipItemByType(
        symptoms: List<LabourDeliveryMetaEntity>,
        deliveryAtDeliveryByPair: Pair<List<LabourDeliveryMetaEntity>, List<LabourDeliveryMetaEntity>>,
        deliveryTypeAndStatusPair: Pair<List<LabourDeliveryMetaEntity>, List<LabourDeliveryMetaEntity>>,
        neonateOutcome: List<LabourDeliveryMetaEntity>,
        pncNeonateOutcome: List<LabourDeliveryMetaEntity>,
        riskFactors: List<LabourDeliveryMetaEntity>,
        conditionOfMother: List<LabourDeliveryMetaEntity>,
        motherDeliveryStatus: List<LabourDeliveryMetaEntity>,
        stateOfPerineum: List<LabourDeliveryMetaEntity>,
    ): List<LabourDeliveryMetaEntity> {
        val chipItemList = ArrayList<LabourDeliveryMetaEntity>()
        symptoms.forEach { it.category = MedicalReviewTypeEnums.MOTHER_DELIVERY_REVIEW.name }
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
        pncNeonateOutcome.forEach { it.category = MedicalReviewTypeEnums.PNCNeonateOutcome.name }
        riskFactors.forEach { it.category = MedicalReviewTypeEnums.RiskFactors.name }
        conditionOfMother.forEach { it.category = MedicalReviewTypeEnums.ConditionOfMother.name }
        motherDeliveryStatus.forEach {
            it.category = MedicalReviewTypeEnums.MotherDeliveryStatus.name
        }
        stateOfPerineum.forEach { it.category = MedicalReviewTypeEnums.StateOfPerineum.name }
        chipItemList.addAll(symptoms)
        chipItemList.addAll(deliveryAtDeliveryByPair.first)
        chipItemList.addAll(deliveryAtDeliveryByPair.second)
        chipItemList.addAll(deliveryTypeAndStatusPair.first)
        chipItemList.addAll(deliveryTypeAndStatusPair.second)
        chipItemList.addAll(neonateOutcome)
        chipItemList.addAll(pncNeonateOutcome)
        chipItemList.addAll(riskFactors)
        chipItemList.addAll(conditionOfMother)
        chipItemList.addAll(motherDeliveryStatus)
        chipItemList.addAll(stateOfPerineum)
        return chipItemList
    }

    suspend fun getLabourDeliveryList(): Resource<List<LabourDeliveryMetaEntity>> =
        try {
            val response = roomHelper.getLabourDelivery()
            Resource(ResourceState.SUCCESS, response)
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }

    suspend fun createLabourDeliveryMedicalReview(request: CreateLabourDeliveryRequest): Resource<CreateLabourDeliveryResponse> =
        try {
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

    suspend fun getLabourDeliverySummaryDetails(request: LabourDeliverySummaryDetails): Resource<CreateLabourDeliveryRequest> =
        try {
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

    suspend fun labourDeliverySummaryCreate(request: MedicalReviewSummarySubmitRequest): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.createSummarySubmit(request)
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
