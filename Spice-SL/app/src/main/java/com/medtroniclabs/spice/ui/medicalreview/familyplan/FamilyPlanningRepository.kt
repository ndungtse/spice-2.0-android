package com.medtroniclabs.spice.ui.medicalreview.familyplan

import android.location.Location
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams.DefaultID
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.EncounterDetails
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.Contraceptive
import com.medtroniclabs.spice.data.model.FamilyPlanningContraceptivesRequest
import com.medtroniclabs.spice.data.model.FamilyPlanningCreateResponse
import com.medtroniclabs.spice.data.model.FamilyPlanningSummaryResponse
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ClientType
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CombineOralContraceptive
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.CombinedOralContraceptiveComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Condoms
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.EmergencyContraceptive
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Implants
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Injectables
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.Microlut
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.MicrolutQuantity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherImplantComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherInjectableComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherPermanentMethodComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.OtherProgestinOnlyOralsComments
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PermanentMethod
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.PostPartum
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.ProgestinOnlyOrals
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class FamilyPlanningRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {

    suspend fun getStaticMetaData(
        menuType: String
    ): Resource<Boolean> {
        return try {
            val response = apiHelper.getFamilyPlanningStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.deleteExaminationsComplaints(menuType)
                    roomHelper.insertExaminationsComplaint(
                        generateChipItemByType(
                            familyPlanning
                        )
                    )
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_FAMILY_PLANNING_LOADED.name,
                    true
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_FAMILY_PLANNING_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }

    }

    private fun generateChipItemByType(familyPlanning: List<MedicalReviewMetaItems>): List<MedicalReviewMetaItems> {
        val chipItemList = ArrayList<MedicalReviewMetaItems>()
        familyPlanning.forEach { item ->
            val chipItem = MedicalReviewMetaItems(
                id = item.id,
                name = item.name,
                type = item.type,
                category = item.category,
                displayOrder = item.displayOrder,
                value = item.value
            )
            chipItemList.add(chipItem)
        }
        return chipItemList
    }

    suspend fun createFamilyPlanningMR(
        details: PatientListRespModel,
        resultPair: Pair<HashMap<String, Any>, List<ChipViewItemModel>>,
        occupation: String?,
        maritalStatus: String?,
        encounterId: String?,
        lastLocation: Location?,
        notes: String?,
    ): Resource<FamilyPlanningCreateResponse> {
        return try {
            val response = apiHelper.createFamilyPlanningMR(
                composeSubmitRequest(
                    details,
                    resultPair,
                    occupation,
                    maritalStatus,
                    encounterId,
                    lastLocation,
                    notes
                )
            )
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }

    private fun composeSubmitRequest(
        details: PatientListRespModel,
        resultPair: Pair<HashMap<String, Any>, List<ChipViewItemModel>>,
        occupation: String?,
        maritalStatus: String?,
        encounterId: String?,
        lastLocation: Location?,
        notes: String?,
    ): FamilyPlanningContraceptivesRequest {
        val resultMap = resultPair.first
        return FamilyPlanningContraceptivesRequest(
            id = encounterId,
            patientId = details.id,
            assessmentName = MedicalReviewTypeEnums.FAMILY_PLANNING_REVIEW.name,
            clinicalNotes = notes,
            encounter = MedicalReviewEncounter(
                id = encounterId,
                patientId = details.patientId,
                provenance = ProvanceDto(),
                latitude = lastLocation?.latitude ?: 0.0,
                longitude = lastLocation?.longitude ?: 0.0,
                householdId = details.houseHoldId,
                memberId = details.memberId,
                startTime = DateUtils.getCurrentDateAndTime(
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                ),
                endTime = DateUtils.getCurrentDateAndTime(
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                ),
                referred = true,
                villageId = details.villageId
            ),
            contraceptive = Contraceptive(
                occupation = occupation?.takeIf { it.isNotBlank() },
                maritalStatus = maritalStatus.takeIf { !(it.equals(DefaultID)) },
                clientType = (resultMap[ClientType] as? String)?.takeIf { it.isNotEmpty() },
                postPartum = (resultMap[PostPartum] as? String)?.takeIf { it.isNotEmpty() },
                combinedOralContraceptive = (resultMap[CombineOralContraceptive] as? String)?.takeIf { it.isNotEmpty() },
                otherCombinedOralContraceptive = (resultMap[CombinedOralContraceptiveComments] as? String)?.takeIf { it.isNotEmpty() },
                progestinOnlyOrals = (resultMap[ProgestinOnlyOrals] as? String)?.takeIf { it.isNotEmpty() },
                otherProgestinOnlyOrals = (resultMap[OtherProgestinOnlyOralsComments] as? String)?.takeIf { it.isNotEmpty() },
                microlutQuantity = (resultMap[MicrolutQuantity]as? Long)?.takeIf { it!=-1L },
                injectables = (resultMap[Injectables] as? String)?.takeIf { it.isNotEmpty() },
                otherInjectables = (resultMap[OtherInjectableComments] as? String)?.takeIf { it.isNotEmpty() },
                iucd = resultPair.second.mapNotNull { it.value },
                implants = (resultMap[Implants] as? String)?.takeIf { it.isNotEmpty() },
                otherImplants = (resultMap[OtherImplantComments] as? String)?.takeIf { it.isNotEmpty() },
                condoms = (resultMap[Condoms] as? String)?.takeIf { it.isNotEmpty() },
                emergencyContraceptive = (resultMap[EmergencyContraceptive] as? String)?.takeIf { it.isNotEmpty() },
                permanentMethod = (resultMap[PermanentMethod] as? String)?.takeIf { it.isNotEmpty() },
                otherPermanentMethod = (resultMap[OtherPermanentMethodComments] as? String)?.takeIf { it.isNotEmpty() }
            )
        )
    }

    suspend fun getMetaListByType(type: String): Resource<List<MedicalReviewMetaItems>> {
        return try {
            val response = roomHelper.getExaminationsComplaintByType(type)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getFamilyPlanningSummaryDetails(
        request: AboveFiveYearsSummaryRequest
    ): Resource<FamilyPlanningSummaryResponse> {
        return try {
            val response = apiHelper.getFamilyPlanningMRSummaryDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}