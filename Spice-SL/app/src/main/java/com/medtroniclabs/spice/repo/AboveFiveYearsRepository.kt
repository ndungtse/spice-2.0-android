package com.medtroniclabs.spice.repo

import android.location.Location
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class AboveFiveYearsRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {
    suspend fun getStaticMetaData(
        menuType: String
    ): Resource<Boolean> {
        return try {
            val response = apiHelper.getAboveFiveYearsMetaData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.deleteExaminationsComplaints(menuType)
                    roomHelper.insertExaminationsComplaint(
                        generateChipItemByType(
                            presentingComplaints,
                            systemicExaminations,
                            medicalSupplies,
                            cost,
                            patientStatus
                        )
                    )
                    roomHelper.deleteDiagnosisList(MedicalReviewTypeEnums.AboveFiveYears.name)
                    //TODO: Type should be given from backend until we added type
                    diseaseCategories.onEach { it.type = MedicalReviewTypeEnums.AboveFiveYears.name }
                    roomHelper.saveDiagnosisList(diseaseCategories)
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name,
                    true
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }

    }

    private fun generateChipItemByType(
        presentingComplaints: List<MedicalReviewMetaItems>,
        systemicExaminations: List<MedicalReviewMetaItems>,
        medicalSupplies: List<MedicalReviewMetaItems>,
        cost: List<MedicalReviewMetaItems>,
        patientStatus: List<MedicalReviewMetaItems>
    ): List<MedicalReviewMetaItems> {
        val chipItemList = ArrayList<MedicalReviewMetaItems>()
        presentingComplaints.forEach {
            it.category = MedicalReviewTypeEnums.PresentingComplaints.name
        }
        systemicExaminations.forEach {
            it.category = MedicalReviewTypeEnums.SystemicExaminations.name
        }
        patientStatus.forEach { it.type = MedicalReviewTypeEnums.AboveFiveYears.name }
        chipItemList.addAll(presentingComplaints)
        chipItemList.addAll(systemicExaminations)
        chipItemList.addAll(medicalSupplies)
        chipItemList.addAll(cost)
        chipItemList.addAll(patientStatus)
        return chipItemList
    }

    suspend fun createAboveFiveYears(
        details: PatientListRespModel,
        selectedComplaintsExaminationsPair: Pair<List<String?>, List<String?>>,
        enteredComplaintsExaminationsClinicalNotes: Triple<String, String, String>,
        lastLocation: Location?
    ): Resource<AboveFiveYearsSummaryDetails> {
        return try {
            val request = createSubmitRequest(
                details,
                selectedComplaintsExaminationsPair,
                enteredComplaintsExaminationsClinicalNotes,
                lastLocation
            )
            val response = request?.let { apiHelper.createAboveFiveYearsResult(it) }
            if (response != null && response.isSuccessful) {
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
            Resource(state = ResourceState.ERROR)
        }
    }

    private fun createSubmitRequest(
        details: PatientListRespModel,
        selectedComplaintsExaminationsPair: Pair<List<String?>, List<String?>>,
        enteredComplaintsExaminationsClinicalNotes: Triple<String, String, String>,
        lastLocation: Location?
    ): AboveFiveYearsSubmitRequest? {
        return details.patientId?.let { id ->
            lastLocation?.let { location ->
                details.houseHoldId?.let { hhId ->
                    details.memberId?.let { memberId ->
                        AboveFiveYearsSubmitRequest(
                            assessmentType = MedicalReviewTypeEnums.AboveFiveYears.name,
                            presentingComplaints = selectedComplaintsExaminationsPair.first.filterNotNull(),
                            presentingComplaintsNotes = enteredComplaintsExaminationsClinicalNotes.first,
                            systemicExaminations = selectedComplaintsExaminationsPair.second.filterNotNull(),
                            systemicExaminationsNotes = enteredComplaintsExaminationsClinicalNotes.second,
                            clinicalNotes = enteredComplaintsExaminationsClinicalNotes.third,
                            encounter = MedicalReviewEncounter(
                                patientId = id,
                                provenance = ProvanceDto(
                                    createdDateTime = System.currentTimeMillis().convertToUtcDateTime()
                                ),
                                latitude = location.latitude,
                                longitude = lastLocation.longitude,
                                householdId = hhId,
                                memberId = memberId,
                                startTime = DateUtils.getCurrentDateAndTime(
                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                ),
                                endTime = DateUtils.getCurrentDateAndTime(
                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                ),
                                referred = true
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun getSummaryDetailMetaItems(
        type: String
    ): Resource<List<MedicalReviewMetaItems>> {
        return try {
            val response = roomHelper.getSummaryDetailMetaItems(type)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAboveFiveYearsSummaryDetails(
        request: AboveFiveYearsSummaryRequest
    ): Resource<AboveFiveYearsSummaryDetails> {
        return try {
            val response = apiHelper.getAboveFiveYearsSummaryDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun aboveFiveYearsSummaryCreate(
        details: PatientListRespModel,
        submitCreateId: String,
        selectedMedicalSupply: ArrayList<MultiSelectDropDownModel>?,
        selectedCostItem: String?,
        selectedPatientStatus: String?,
        nextFollowupDate: String?
    ): Resource<HashMap<String, Any>> {
        return try {
            val request = createSummarySubmitRequest(
                details,
                submitCreateId,
                selectedMedicalSupply,
                selectedCostItem,
                selectedPatientStatus,
                nextFollowupDate
            )
            val response = request?.let { apiHelper.aboveFiveYearsSummaryCreate(it) }
            if (response != null && response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    private fun createSummarySubmitRequest(
        details: PatientListRespModel,
        submitCreateId: String,
        selectedMedicalSupply: ArrayList<MultiSelectDropDownModel>?,
        selectedCostItem: String?,
        selectedPatientStatus: String?,
        nextFollowupDate: String?
    ): AboveFiveYearsSummarySubmitRequest? {
        val medicalSupplyList = ArrayList<String>()
        selectedMedicalSupply?.map { item -> item.value?.let { value -> medicalSupplyList.add(value) } }
        return details.patientId?.let { patientId ->
            details.memberId?.let { memberId ->
                details.houseHoldId?.let { houseHoldId ->
                    details.villageId?.let { villageId ->
                        AboveFiveYearsSummarySubmitRequest(
                            patientId = patientId,
                            memberId = memberId,
                            id = submitCreateId,
                            provenance = ProvanceDto(
                                createdDateTime = DateUtils.getCurrentDateAndTime(
                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                                )
                            ),
                            patientReference = details.id,
                            medicalSupplies = medicalSupplyList.ifEmpty { null },
                            cost = selectedCostItem,
                            patientStatus = selectedPatientStatus,
                            nextVisitDate = DateUtils.convertDateTimeToDate(
                                nextFollowupDate,
                                DateUtils.DATE_ddMMyyyy,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                            ),
                            householdId = houseHoldId,
                            villageId = villageId,
                            assessmentName = MedicalReviewTypeEnums.AboveFiveYears.name,
                            referralTicketType = MedicalReviewTypeEnums.ICCM.name
                        )
                    }
                }
            }
        }
    }
}