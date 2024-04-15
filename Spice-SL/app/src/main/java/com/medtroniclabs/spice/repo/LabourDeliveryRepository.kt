package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.Exception

class LabourDeliveryRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {

    suspend fun getStaticMetaData(
        labourDeliveryMetaLiveData: MutableLiveData<Resource<Boolean>>
    ) {
        try {
            labourDeliveryMetaLiveData.postLoading()
            withContext(Dispatchers.IO) {
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
                    labourDeliveryMetaLiveData.postSuccess(true)
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
            labourDeliveryMetaLiveData.postError()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_LABOUR_DELIVERY_LOADED.name,
                false
            )
        }
    }

    private fun generateChipItemByType(
        symptoms: List<LabourDeliveryMetaEntity>,
        deliveryAtDeliveryByPair: Pair<List<LabourDeliveryMetaEntity>,List<LabourDeliveryMetaEntity>>,
        deliveryTypeAndStatusPair: Pair<List<LabourDeliveryMetaEntity>,List<LabourDeliveryMetaEntity>>,
        neonateOutcome: List<LabourDeliveryMetaEntity>,
        riskFactors: List<LabourDeliveryMetaEntity>,
        conditionOfMother: List<LabourDeliveryMetaEntity>,
        motherDeliveryStatus: List<LabourDeliveryMetaEntity>
    ): List<LabourDeliveryMetaEntity> {
        val chipItemList = ArrayList<LabourDeliveryMetaEntity>()
        symptoms.forEach {it.category = MedicalReviewTypeEnums.LabourDelivery.name}
        deliveryAtDeliveryByPair.first.forEach { it.category = MedicalReviewTypeEnums.DeliveryAt.name }
        deliveryAtDeliveryByPair.second.forEach {it.category = MedicalReviewTypeEnums.DeliveryBy.name}
        deliveryTypeAndStatusPair.first.forEach {it.category = MedicalReviewTypeEnums.DeliveryType.name}
        deliveryTypeAndStatusPair.second.forEach {it.category = MedicalReviewTypeEnums.DeliveryStatus.name}
        neonateOutcome.forEach {it.category = MedicalReviewTypeEnums.NeonateOutcome.name}
        riskFactors.forEach {it.category = MedicalReviewTypeEnums.RiskFactors.name}
        conditionOfMother.forEach {it.category = MedicalReviewTypeEnums.ConditionOfMother.name}
        motherDeliveryStatus.forEach {it.category = MedicalReviewTypeEnums.MotherDeliveryStatus.name}
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

    suspend fun getLabourDeliveryList(
        labourDeliveryList: MutableLiveData<Resource<List<LabourDeliveryMetaEntity>>>
    ) {
        try {
            labourDeliveryList.postLoading()
            val response = roomHelper.getLabourDelivery()
            labourDeliveryList.postSuccess(response)
        } catch (e: Exception) {
            labourDeliveryList.postError()
        }
    }
}